package red.jackf.whereisit.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.whereisit.client.HighlightRendering;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    /**
     * Draws the in-inventory slot highlights for items. Triggered from here instead of {@link net.fabricmc.fabric.api.client.screen.v1.ScreenEvents}
     * to draw behind the tooltip.
     */
    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void whereisit$drawSlotHighlights(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo ci) {
        HighlightRendering.drawSlots((AbstractContainerScreen<?>) (Object) this, poseStack);
    }
}
