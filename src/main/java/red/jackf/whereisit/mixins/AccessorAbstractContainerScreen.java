package red.jackf.whereisit.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public interface AccessorAbstractContainerScreen {
    @Accessor("hoveredSlot")
    @Nullable
    Slot whereisit$hoveredSlot();

    @Accessor("leftPos")
    int whereisit$getLeftPos();

    @Accessor("topPos")
    int whereisit$getTopPos();
}
