package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches by any of a list of item IDs.
 */
public class ItemSearchCriteria implements SearchCriteria<List<Item>> {
    public static final String ITEMS_KEY = "Items";

    @Override
    public Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            throw new InvalidSearchCriteriaException("No item list passed in nbt: " + tag);
        }
        var list = tag.getList(ITEMS_KEY, Tag.TAG_STRING);
        List<Predicate> predicates = new ArrayList<>();
        for (Tag element : list) {
            if (element.getId() == Tag.TAG_STRING) {
                var id = ResourceLocation.tryParse(tag.getAsString());
                if (id == null) {
                    throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + tag.getString(ITEMS_KEY));
                }
                if (!Registry.ITEM.containsKey(id)) {
                    throw new InvalidSearchCriteriaException("Unknown item: " + id);
                }
                predicates.add(stack -> Registry.ITEM.getKey(stack.getItem()).equals(id));
            }
        }
        return Predicate.any(predicates);
    }

    @Override
    public CompoundTag tagFromType(List<Item> input) {
        var tag = new CompoundTag();
        var list  = new ListTag();
        for (Item item : input) {
            list.add(StringTag.valueOf(Registry.ITEM.getKey(item).toString()));
        }
        tag.put(ITEMS_KEY, list);
        return tag;
    }
}
