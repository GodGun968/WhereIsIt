package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

/**
 * Search for an itemstack via it's name, minus formatting.
 */
public class NameSearchCriteria extends SearchCriteria {
    public static final String NAME_KEY = "Name";

    @Override
    public Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(NAME_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No name key passed in nbt: " + tag);
        }
        var name = tag.getString(NAME_KEY);
        return stack -> {
            if (stack.hasCustomHoverName()) {
                return stack.getHoverName().getString().contains(name);
            } else {
                return false;
            }
        };
    }

    @Override
    public CompoundTag parseString(String input) throws InvalidSearchCriteriaException {
        var tag = new CompoundTag();
        tag.putString(NAME_KEY, input);
        return tag;
    }

    @Override
    public ArgumentType<?> getArgumentType(CommandBuildContext context) {
        return StringArgumentType.string();
    }

    public CompoundTag fromStack(ItemStack stack) {
        var tag = new CompoundTag();
        tag.putString(NAME_KEY, stack.getHoverName().getString());
        return tag;
    }
}
