package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

public abstract class SearchCriteria {

    public abstract SearchCriteria.Predicate fromTag(CompoundTag tag) throws InvalidSearchCriteriaException;

    public abstract CompoundTag parseString(String input) throws InvalidSearchCriteriaException;

    public abstract ArgumentType<?> getArgumentType(CommandBuildContext context);

    public interface Predicate {
        boolean test(ItemStack in);

        static Predicate any(Iterable<Predicate> predicates) {
            return stack -> {
                for (var predicate : predicates) {
                    if (predicate.test(stack)) return true;
                }
                return false;
            };
        }

        static Predicate all(Iterable<Predicate> predicates) {
            return stack -> {
                for (var predicate : predicates) {
                    if (!predicate.test(stack)) return false;
                }
                return true;
            };
        }
    }
}
