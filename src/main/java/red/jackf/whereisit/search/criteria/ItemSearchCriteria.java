package red.jackf.whereisit.search.criteria;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches by any of a list of item IDs.
 */
public class ItemSearchCriteria implements SearchCriteria<Item> {
    public static final String ITEM_KEY = "Item";

    @Override
    public Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(ITEM_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No item string passed in nbt: " + tag);
        }
        var str = tag.getString(ITEM_KEY);
        var id = ResourceLocation.tryParse(str);
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + tag.getString(ITEM_KEY));
        }
        if (!Registry.ITEM.containsKey(id)) {
            throw new InvalidSearchCriteriaException("Unknown item: " + id);
        }
        var item = Registry.ITEM.get(id);
        return stack -> stack.getItem().equals(item);
    }

    @Override
    public CompoundTag tagFromType(Item input) {
        var tag = new CompoundTag();
        tag.put(ITEM_KEY, StringTag.valueOf(Registry.ITEM.getKey(input).toString()));
        return tag;
    }
}
