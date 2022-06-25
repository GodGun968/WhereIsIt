package red.jackf.whereisit.search.criteria;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;
import red.jackf.whereisit.util.EnchantmentWithOptionalLevel;

public class EnchantmentSearchCriteria implements SearchCriteria<EnchantmentWithOptionalLevel> {
    public static final String ENCHANTMENT_ID_KEY = "Id";
    public static final String ENCHANTMENT_LEVEL_KEY = "Level";

    @Override
    public Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(ENCHANTMENT_ID_KEY, Tag.TAG_STRING)) {
            throw new InvalidSearchCriteriaException("No enchantment id key passed in nbt: " + tag);
        }
        var id = ResourceLocation.tryParse(tag.getString(ENCHANTMENT_ID_KEY));
        if (id == null) {
            throw new InvalidSearchCriteriaException("Not a valid ID: " + tag.getString(ENCHANTMENT_ID_KEY));
        }
        var enchantment = Registry.ENCHANTMENT.get(id);
        if (enchantment == null) {
            throw new InvalidSearchCriteriaException("Unknown enchantment: " + id);
        }

        var targetLevel = tag.contains(ENCHANTMENT_LEVEL_KEY, Tag.TAG_INT) ? tag.getInt(ENCHANTMENT_LEVEL_KEY) : -1;
        return stack -> {
            var stackLevel = EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0);
            if (stackLevel == 0) return false; // does not have enchantment
            if (targetLevel == -1) return true; // does have enchantment; any level
            return targetLevel == stackLevel;
        };
    }

    @Override
    public CompoundTag tagFromType(EnchantmentWithOptionalLevel input) {
        var tag = new CompoundTag();
        tag.putString(ENCHANTMENT_ID_KEY, String.valueOf(Registry.ENCHANTMENT.getKey(input.enchantment())));
        if (input.level().isPresent()) {
            tag.putInt(ENCHANTMENT_LEVEL_KEY, input.level().get());
        }
        return tag;
    }
}
