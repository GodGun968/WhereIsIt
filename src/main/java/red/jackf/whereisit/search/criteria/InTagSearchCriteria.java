package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import red.jackf.whereisit.command.argument.ItemTagArgument;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

// TODO: Untested
public class InTagSearchCriteria extends SearchCriteria {
    public static final String TAG_ID_KEY = "Tag";

    @Override
    public Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(TAG_ID_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No tag key passed in nbt: " + tag);
        }
        var id = ResourceLocation.tryParse(tag.getString(TAG_ID_KEY));
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + tag.getString(TAG_ID_KEY));
        }
        return stack -> stack.is(TagKey.create(Registry.ITEM_REGISTRY, id));
    }

    public CompoundTag parseString(String input) throws InvalidSearchCriteriaException {
        var id = ResourceLocation.tryParse(input);
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ResourceLocation: " + input);
        }
        if (!Registry.ITEM.isKnownTagName(TagKey.create(Registry.ITEM_REGISTRY, id))) {
            throw new InvalidSearchCriteriaException("Unknown item tag: " + input);
        }
        var tag = new CompoundTag();
        tag.putString(TAG_ID_KEY, id.toString());
        return tag;
    }

    @Override
    public ArgumentType<?> getArgumentType(CommandBuildContext context) {
        return ItemTagArgument.itemTag();
    }
}
