package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

public interface SearchCriteria<T> {

    SearchCriteria.Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException;

    CompoundTag tagFromType(T input);

    interface Predicate {
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
