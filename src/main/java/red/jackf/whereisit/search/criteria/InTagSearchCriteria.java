package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import red.jackf.whereisit.command.argument.ItemTagArgument;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

public class InTagSearchCriteria implements SearchCriteria<TagKey<Item>> {
    public static final String TAG_ID_KEY = "Tag";

    @Override
    public Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(TAG_ID_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No tag key passed in nbt: " + tag);
        }
        var id = ResourceLocation.tryParse(tag.getString(TAG_ID_KEY));
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + tag.getString(TAG_ID_KEY));
        }
        return stack -> stack.is(TagKey.create(Registry.ITEM_REGISTRY, id));
    }

    @Override
    public CompoundTag tagFromType(TagKey<Item> input) {
        var tag = new CompoundTag();
        tag.putString(TAG_ID_KEY, input.location().toString());
        return tag;
    }
}
