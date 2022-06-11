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
public class ItemSearchCriteria extends SearchCriteria {
    public static final String ITEMS_KEY = "Items";

    @Override
    public Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
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
                predicates.add(stack -> Registry.ITEM.getKey(stack.getItem()).equals(id));
            }
        }
        return Predicate.any(predicates);
    }

    public CompoundTag parseString(String input) throws InvalidSearchCriteriaException {
        var id = ResourceLocation.tryParse(input);
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + input);
        }
        var item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            throw new InvalidSearchCriteriaException("No item found with ID: " + input);
        }
        return fromItem(item);
    }

    @Override
    public ArgumentType<?> getArgumentType(CommandBuildContext context) {
        return ItemArgument.item(context);
    }

    public CompoundTag fromItem(Item item) {
        var tag = new CompoundTag();
        var list  = new ListTag();
        list.add(StringTag.valueOf(Registry.ITEM.getKey(item).toString()));
        tag.put(ITEMS_KEY, list);
        return tag;
    }

    public CompoundTag fromItems(List<Item> items) {
        var tag = new CompoundTag();
        var list  = new ListTag();
        for (Item item : items) {
            list.add(StringTag.valueOf(Registry.ITEM.getKey(item).toString()));
        }
        tag.put(ITEMS_KEY, list);
        return tag;
    }
}
