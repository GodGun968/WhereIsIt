package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

public class ItemIdSearchCriteria extends SearchCriteria {
    public static final String ITEM_ID_KEY = "Item";

    @Override
    public Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(ITEM_ID_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No item key passed in nbt: " + tag);
        }
        var id = ResourceLocation.tryParse(tag.getString(ITEM_ID_KEY));
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + tag.getString(ITEM_ID_KEY));
        }
        return stack -> Registry.ITEM.getKey(stack.getItem()).equals(id);
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
        tag.putString(ITEM_ID_KEY, Registry.ITEM.getKey(item).toString());
        return tag;
    }
}
