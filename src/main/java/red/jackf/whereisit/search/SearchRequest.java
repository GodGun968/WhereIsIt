package red.jackf.whereisit.search;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.client.HighlightRendering;
import red.jackf.whereisit.networking.WhereIsItNetworking;
import red.jackf.whereisit.search.criteria.SearchCriteria;
import red.jackf.whereisit.search.criteria.SearchCriteriaRegistry;
import red.jackf.whereisit.util.EnchantmentWithOptionalLevel;

import java.util.*;

/**
 * A request to search for items. Consists of a list of individual requests, each with criteria.
 *
 * Example: this will search for either a diamond, or a diamond pickaxe with efficiency 4.
 * <pre>
 * [
 *     [
 *         {
 *             CriteriaType: whereisit:item,
 *             Items: [minecraft:diamond]
 *         }
 *     ],
 *     [
 *         {
 *             CriteriaType: whereisit:item,
 *             Items: [minecraft:diamond_pickaxe]
 *         },
 *         {
 *             CriteriaType: whereisit:enchantment,
 *             Id: [minecraft:efficiency],
 *             Level: 4
 *         }
 *     ],
 * ]
 *
 * Packet Structure consists of an integer representing the number of individual requests, followed by said requests.
 *
 * Each individual request consists of an integer representing the number of criteria, followed by said criteria, each as CompoundTags.
 * </pre>
 */
public class SearchRequest {
    private static final List<Item> MOB_EFFECT_HOLDERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);

    public static final String CRITERIA_TYPE_KEY = "CriteriaType";
    // Reason for set: don't send search requests with identical criteria e.g. REI brewing stand potion slots
    private final Set<Individual> individualRequests = new HashSet<>();

    private static boolean shownNotInstalled = false;

    private SearchRequest() {}

    public SearchRequest withIndividual(Individual individualRequest) {
        individualRequests.add(individualRequest);
        return this;
    }

    public void trigger() {
        if (ClientPlayNetworking.canSend(WhereIsItNetworking.SEARCH_FOR_ITEM_C2S)) {
            ClientPlayNetworking.send(WhereIsItNetworking.SEARCH_FOR_ITEM_C2S, toByteBuf());
            HighlightRendering.clear();
            HighlightRendering.setLastRequest(fromByteBuf(toByteBuf()));
        } else {
            if (Minecraft.getInstance().player != null && !shownNotInstalled) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("whereisit.chat.notInstalledOnServer"));
                shownNotInstalled = true;
            }
        }
    }

    public static void resetShownNotInstalledMessage() {
        shownNotInstalled = false;
    }

    /**
     * Creates a FriendlyByteBuf able to be sent over the network.
     */
    @Environment(EnvType.CLIENT)
    public FriendlyByteBuf toByteBuf() {
        var buf = PacketByteBufs.create();
        if (individualRequests.size() == 0) {
            WhereIsIt.LOGGER.error("Zero individual requests for search request", new InvalidSearchCriteriaException());
        }
        buf.writeInt(individualRequests.size());
        for (Individual individual : individualRequests) {
            buf.writeInt(individual.criteria.size());
            for (CompoundTag criteria : individual.criteria) {
                buf.writeNbt(criteria);
            }
        }
        return buf;
    }

    /**
     * Creates a predicate representing the request.
     */
    public static SearchCriteria.Predicate fromByteBuf(FriendlyByteBuf byteBuf) {
        var requestSize = byteBuf.readInt();
        List<SearchCriteria.Predicate> predicates = new ArrayList<>(requestSize);
        for (int i = 0; i < requestSize; i++) {
            var criteriaSize = byteBuf.readInt();
            List<SearchCriteria.Predicate> criteriaList = new ArrayList<>(criteriaSize);
            for (int j = 0; j < criteriaSize; j++) {
                var nbt = byteBuf.readNbt();
                if (nbt == null) {
                    WhereIsIt.LOGGER.error("No NBT for criteria found", new InvalidSearchCriteriaException());
                    continue;
                }
                var id = ResourceLocation.tryParse(nbt.getString(CRITERIA_TYPE_KEY));
                if (id == null) {
                    WhereIsIt.LOGGER.error("Unknown criteria ID: " + nbt.getString(CRITERIA_TYPE_KEY), new InvalidSearchCriteriaException());
                    continue;
                }
                var criteria = SearchCriteriaRegistry.CRITERIA.get(id);
                try {
                    criteriaList.add(criteria.predicateFromTag(nbt));
                } catch (InvalidSearchCriteriaException e) {
                    WhereIsIt.LOGGER.error("Error creating criteria predicate for " + id, e);
                }
            }
            predicates.add(SearchCriteria.Predicate.all(criteriaList));
        }
        return SearchCriteria.Predicate.any(predicates);
    }

    /**
     * Creates a search request for a given itemstack. By default, this just searches for the item ID.
     * @param itemStack - Stack to search.
     * @param alternateBehavior - Changes default search behaviour; this normally means being more precise.
     * @return Built search request
     */
    public static SearchRequest fromItemStack(ItemStack itemStack, boolean alternateBehavior) {
        return new SearchRequest().withIndividual(Individual.fromItemStack(itemStack, alternateBehavior));
    }

    public static SearchRequest fromItemStacks(List<ItemStack> itemStacks, boolean alternateBehaviour) {
        var request = new SearchRequest();

        for (ItemStack stack : itemStacks) {
            request.withIndividual(Individual.fromItemStack(stack,  alternateBehaviour));
        }

        return request;
    }

    public record Individual(List<CompoundTag> criteria) {

        /**
         * Creates an individual search request for a given itemstack. By default, this just searches for the item ID.
         *
         * @param itemStack         - Stack to search.
         * @param alternateBehavior - Changes default search behaviour; this normally means being more precise.
         * @return Individual Search Request
         */
        public static Individual fromItemStack(ItemStack itemStack, boolean alternateBehavior) {
            var builder = new Builder()
                .withItem(itemStack.getItem());

            // Add enchantments if alternate behaviour or an enchanted book.
            if (itemStack.getItem() == Items.ENCHANTED_BOOK || alternateBehavior) {
                var enchantments = EnchantmentHelper.getEnchantments(itemStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    builder.withEnchantment(entry.getKey(), entry.getValue());
                }
            }

            // Add potion effects
            if (MOB_EFFECT_HOLDERS.contains(itemStack.getItem())) {
                for (MobEffectInstance mobEffect : PotionUtils.getMobEffects(itemStack)) {
                    builder.withMobEffect(mobEffect);
                }
            }

            // Add custom names
            if (alternateBehavior && itemStack.hasCustomHoverName()) {
                builder.withName(itemStack.getHoverName().getString());
            }
            return builder.build();
        }

        @SuppressWarnings("UnusedReturnValue")
        public static class Builder {
            private final List<CompoundTag> criteria = new ArrayList<>();

            private Builder() {
            }

            public Builder withName(String name) {
                return withCriteria(SearchCriteriaRegistry.NAME_KEY, SearchCriteriaRegistry.NAME.tagFromType(name));
            }

            public Builder withItem(Item item) {
                return withCriteria(SearchCriteriaRegistry.ITEM_KEY, SearchCriteriaRegistry.ITEM.tagFromType(item));
            }

            public Builder withMobEffect(MobEffectInstance mobEffect) {
                return withCriteria(SearchCriteriaRegistry.MOB_EFFECT_KEY, SearchCriteriaRegistry.MOB_EFFECT.tagFromType(mobEffect));
            }

            public Builder withEnchantment(Enchantment enchantment) {
                return withEnchantment(enchantment, -1);
            }

            public Builder withEnchantment(Enchantment enchantment, int level) {
                var enchantmentWithLevel = new EnchantmentWithOptionalLevel(enchantment, level == -1 ? Optional.empty() : Optional.of(level));
                return withCriteria(SearchCriteriaRegistry.ENCHANTMENT_KEY, SearchCriteriaRegistry.ENCHANTMENT.tagFromType(enchantmentWithLevel));
            }

            public Builder withCriteria(ResourceLocation key, CompoundTag data) {
                if (SearchCriteriaRegistry.CRITERIA.containsKey(key)) {
                    var copy = data.copy();
                    copy.putString(CRITERIA_TYPE_KEY, key.toString());
                    criteria.add(copy);
                } else {
                    WhereIsIt.LOGGER.warn("Unknown criteria: " + key);
                }
                return this;
            }

            public Individual build() {
                return new Individual(this.criteria);
            }
        }

        @Override
        public String toString() {
            return "Individual{" + criteria + "}";
        }
    }

}
