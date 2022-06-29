package red.jackf.whereisit.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(GuiComponent.class)
public interface AccessorGuiComponent {
    @Invoker("fillGradient")
    void whereisit$fillGradient(PoseStack poseStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int blitOffset);
}
