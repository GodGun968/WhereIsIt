package red.jackf.whereisit.search;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;

import java.util.ArrayList;
import java.util.List;

public class SearchRequest {
    public static final String CRITERIA_TYPE_KEY = "CriteriaType";
    private final List<CompoundTag> criteria;

    private SearchRequest(List<CompoundTag> criteria) {
        this.criteria = criteria;
    }

    /**
     * Creates a search request for a given itemstack. By default, this just searches for the item ID.
     * @param itemStack - Stack to search.
     * @param precise - Be more precise in searching; this means looking for enchantments and custom names as well.
     * @return Built search request
     */
    public static SearchRequest fromItemStack(ItemStack itemStack, boolean precise) {
        var builder = new Builder()
            .withCriteria(SearchCriteriaRegistry.ITEM_ID_KEY, SearchCriteriaRegistry.ITEM_ID.fromItem(itemStack.getItem()));

        if (precise) {
            if (itemStack.hasCustomHoverName()) {
                builder.withCriteria(SearchCriteriaRegistry.NAME_KEY, SearchCriteriaRegistry.NAME.fromStack(itemStack));
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
