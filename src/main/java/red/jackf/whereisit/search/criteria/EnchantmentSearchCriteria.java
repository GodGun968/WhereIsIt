package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

public class EnchantmentSearchCriteria extends SearchCriteria {
    @Override
    public Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        return null;
    }

    /**
     * In for form `ench_id [level]`. e.g.: `minecraft:efficiency 4` or `minecraft:unbreaking`
     */
    @Override
    public CompoundTag parseString(String input) throws InvalidSearchCriteriaException {
        var reader = new StringReader(input);
        try {
            var enchantmentId = ResourceLocation.read(reader);

        } catch (CommandSyntaxException e) {
            throw new InvalidSearchCriteriaException(e);
        }
        return null;
    }

    @Override
    public ArgumentType<?> getArgumentType(CommandBuildContext context) {
        return null;
    }
}
