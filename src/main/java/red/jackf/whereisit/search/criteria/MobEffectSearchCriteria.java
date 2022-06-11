package red.jackf.whereisit.search.criteria;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.alchemy.PotionUtils;
import red.jackf.whereisit.search.InvalidSearchCriteriaException;

/**
 * Searches for a {@link MobEffectInstance} on an item (think tipped arrow, potions).
 */
public class MobEffectSearchCriteria implements SearchCriteria<MobEffectInstance> {
    public static final String EFFECT_ID_KEY = "Id";
    public static final String EFFECT_AMPLIFIER_KEY = "Amplifier";
    public static final String EFFECT_DURATION_KEY = "Duration";

    @Override
    public Predicate predicateFromTag(CompoundTag tag) throws InvalidSearchCriteriaException {
        if (!tag.contains(EFFECT_ID_KEY, Tag.TAG_STRING))
            throw new InvalidSearchCriteriaException("No effect ID key passed in nbt: " + tag);

        var id = ResourceLocation.tryParse(tag.getString(EFFECT_ID_KEY));
        if (id == null)
            throw new InvalidSearchCriteriaException("Invalid ID: " + tag.getString(EFFECT_ID_KEY));
        if (!Registry.MOB_EFFECT.containsKey(id))
            throw new InvalidSearchCriteriaException("Unknown mob effect: " + id);
        var mobEffect = Registry.MOB_EFFECT.get(id);
        if (!tag.contains(EFFECT_AMPLIFIER_KEY, Tag.TAG_INT))
            throw new InvalidSearchCriteriaException("No amplifier key passed in nbt: " + tag);
        var amplifier = tag.getInt(EFFECT_AMPLIFIER_KEY);
        if (!tag.contains(EFFECT_DURATION_KEY, Tag.TAG_INT))
            throw new InvalidSearchCriteriaException("No duration key passed in nbt: " + tag);
        var duration = tag.getInt(EFFECT_DURATION_KEY);
        return stack -> {
            var effects = PotionUtils.getMobEffects(stack);
            for (var effect : effects) {
                if (effect.getEffect() == mobEffect
                    && effect.getAmplifier() == amplifier
                    && effect.getDuration() == duration) return true;
            }
            return false;
        };
    }

    @Override
    public CompoundTag tagFromType(MobEffectInstance input) {
        var tag = new CompoundTag();
        tag.putString(EFFECT_ID_KEY, String.valueOf(Registry.MOB_EFFECT.getKey(input.getEffect())));
        tag.putInt(EFFECT_AMPLIFIER_KEY, input.getAmplifier());
        tag.putInt(EFFECT_DURATION_KEY, input.getDuration());
        return tag;
    }
}
