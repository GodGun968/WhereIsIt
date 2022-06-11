package red.jackf.whereisit.search;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.util.EnchantmentWithOptionalLevel;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

public class SearchRequest {
    private static final List<Item> MOB_EFFECT_HOLDERS = List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);

    public static final String CRITERIA_TYPE_KEY = "CriteriaType";
    private final List<CompoundTag> criteria;

    private SearchRequest(List<CompoundTag> criteria) {
        this.criteria = criteria;
    }

    /**
     * Creates a search request for a given itemstack. By default, this just searches for the item ID.
     * @param itemStack - Stack to search.
     * @param alternateBehavior - Changes default search behaviour; this normally means being more precise.
     * @return Built search request
     */
    public static SearchRequest fromItemStack(ItemStack itemStack, boolean alternateBehavior) {
        var builder = new Builder()
            .withItems(Collections.singletonList(itemStack.getItem()));

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

    public static SearchRequest fromItemStacks(List<ItemStack> itemStacks, boolean alternateBehaviour) {
        var builder = new SearchRequest.Builder()
            .withItems(itemStacks.stream().map(ItemStack::getItem).collect(Collectors.toList()));

        // TODO: fork into multiple simultaenous requests instead of this hack
        if (MOB_EFFECT_HOLDERS.contains(itemStacks.get(0).getItem())) {
            for (MobEffectInstance mobEffect : PotionUtils.getMobEffects(itemStacks.get(0))) {
                builder.withMobEffect(mobEffect);
            }
        }

        return builder.build();
    }

    public void trigger() {
        // TODO: trigger request
        System.out.println("Search: " + criteria);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private final List<CompoundTag> criteria = new ArrayList<>();

        public Builder() {}

        public Builder withName(String name) {
            return withCriteria(SearchCriteriaRegistry.NAME_KEY, SearchCriteriaRegistry.NAME.tagFromType(name));
        }

        public Builder withItems(List<Item> items) {
            return withCriteria(SearchCriteriaRegistry.ITEMS_KEY, SearchCriteriaRegistry.ITEMS.tagFromType(items));
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

        public SearchRequest build() {
            return new SearchRequest(this.criteria);
        }
    }
}
