package red.jackf.whereisit.util;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

public record EnchantmentWithOptionalLevel(Enchantment enchantment, Optional<Integer> level) {
}
