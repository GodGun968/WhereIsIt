package red.jackf.whereisit.search;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.util.EnchantmentWithOptionalLevel;

import javax.swing.text.html.Option;
import java.util.*;

public class SearchRequest {
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

        if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
            var enchantments = EnchantmentHelper.getEnchantments(itemStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                builder.withEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (alternateBehavior) {
            if (itemStack.hasCustomHoverName()) {
                builder.withName(itemStack.getHoverName().getString());
            }

            if (itemStack.getItem() != Items.ENCHANTED_BOOK) {
                var enchantments = EnchantmentHelper.getEnchantments(itemStack);
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    builder.withEnchantment(entry.getKey(), entry.getValue());
                }
            }
        }
        return builder.build();
    }

    public void trigger() {
        // TODO: trigger request
        System.out.println("Search: " + criteria);
    }

    public static class Builder {
        private final List<CompoundTag> criteria = new ArrayList<>();

        public Builder() {}

        public Builder withName(String name) {
            return withCriteria(SearchCriteriaRegistry.NAME_KEY, SearchCriteriaRegistry.NAME.tagFromType(name));
        }

        public Builder withItems(List<Item> items) {
            return withCriteria(SearchCriteriaRegistry.ITEMS_KEY, SearchCriteriaRegistry.ITEMS.tagFromType(items));
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
