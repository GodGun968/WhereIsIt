package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.mixins.AccessorAbstractContainerScreen;
import red.jackf.whereisit.mixins.AccessorGuiComponent;
import red.jackf.whereisit.search.SearchExecutor;
import red.jackf.whereisit.search.criteria.SearchCriteria;

public class HighlightRendering {
    @Nullable
    private static SearchExecutor.Result results = null;
    private static SearchCriteria.Predicate lastRequest = null;
    private static long resultsGameTime = -1;

    public static void setResults(SearchExecutor.Result results, long resultsGameTime) {
        HighlightRendering.results = results;
        HighlightRendering.resultsGameTime = resultsGameTime;
    }

    public static void setLastRequest(SearchCriteria.Predicate lastRequest) {
        HighlightRendering.lastRequest = lastRequest;
    }

    public static void clear() {
        results = null;
        resultsGameTime = -1;
        lastRequest = null;
    }

    public static void drawCube(BufferBuilder builder, BlockPos pos, Vec3 camPos, float sideLength, float red, float green, float blue, float alpha) {
        var sideRadius = sideLength / 2;

        var minX = pos.getX() + 0.5f - camPos.x - sideRadius;
        var minY = pos.getY() + 0.5f - camPos.y - sideRadius;
        var minZ = pos.getZ() + 0.5f - camPos.z - sideRadius;
        var maxX = pos.getX() + 0.5f - camPos.x + sideRadius;
        var maxY = pos.getY() + 0.5f - camPos.y + sideRadius;
        var maxZ = pos.getZ() + 0.5f - camPos.z + sideRadius;

        LevelRenderer.addChainedFilledBoxVertices(builder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void drawHighlights(WorldRenderContext context) {
        if (results == null) return;

        var renderTime = context.world().getGameTime() + context.tickDelta();

        var colour = getCurrentRainbowColour();
        var alpha = 0.8f * Math.max((1f - (renderTime - resultsGameTime) / WhereIsIt.CONFIG.client.highlightFadeTime), 0);

        var camPos = context.camera().getPosition();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (var result : results.positions()) {
            drawCube(bufferBuilder, result.pos(), camPos, 0.5f, colour.x(), colour.y(), colour.z(), alpha);
        }

        tesselator.end();

        RenderSystem.enableDepthTest();
    }

    public static void drawText(PoseStack poseStack, Camera camera, MultiBufferSource consumers, Vec3 pos, Component text) {
        poseStack.pushPose();
        poseStack.translate(pos.x - camera.getPosition().x, pos.y - camera.getPosition().y + 1, pos.z - camera.getPosition().z);
        poseStack.mulPose(camera.rotation());
        var factor = 0.025f;
        poseStack.scale(-factor, -factor, factor);
        var matrix4f = poseStack.last().pose();
        var backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        var backgroundColour = (int) (backgroundOpacity * 255F) << 24;
        var font = Minecraft.getInstance().font;
        var xOffset = (-font.width(text) / 2);

        var maxLight = 0xF000F0;

        font.drawInBatch(text, xOffset, 0, 0x20ffffff, false, matrix4f, consumers, true, backgroundColour, maxLight);
        font.drawInBatch(text, xOffset, 0, 0xffffffff, false, matrix4f, consumers, false, 0, maxLight);

        poseStack.popPose();
    }

    private static void drawNames(WorldRenderContext context) {
        if (results == null) return;

        for (var result : results.positions()) {
            if (result.name() != null) {
                drawText(context.matrixStack(), context.camera(), context.consumers(), Vec3.atCenterOf(result.pos()), result.name());
            }
        }
    }

    public static void drawSlots(AbstractContainerScreen<?> containerScreen, PoseStack poseStack) {
        if (lastRequest == null) return;

        var guiComponent = (AccessorGuiComponent) containerScreen;
        var menuAccess = (MenuAccess<?>) containerScreen;
        for (Slot slot : menuAccess.getMenu().slots) {
            if (slot.hasItem()) {
                var stack = slot.getItem();
                if (lastRequest.test(stack)) {
                    var x = slot.x + ((AccessorAbstractContainerScreen) containerScreen).whereisit$getLeftPos();
                    var y = slot.y + ((AccessorAbstractContainerScreen) containerScreen).whereisit$getTopPos();

                    var colour = packVec3Colour(getCurrentRainbowColour());

                    guiComponent.whereisit$fillGradient(poseStack, x - 1, y - 1, x + 17, y + 17, colour, colour, containerScreen.getBlitOffset());
                }
            }
        }
    }

    public static int packVec3Colour(Vector3f colour) {
        int r = (int) (colour.x() * 255);
        int g = (int) (colour.y() * 255);
        int b = (int) (colour.z() * 255);
        return ((32768 + r << 8) + g << 8) + b;
    }

    public static Vector3f getCurrentRainbowColour() {
        var hue = (System.currentTimeMillis() / 50) % 360;
        var factor = 1 - Math.abs(Mth.positiveModulo(hue / 60f, 2) - 1);

        int segment = (int) (hue / 60);
        switch (segment) {
            case 0:
                return new Vector3f(1, factor, 0);
            case 1:
                return new Vector3f(factor, 1, 0);
            case 2:
                return new Vector3f(0, 1, factor);
            case 3:
                return new Vector3f(0, factor, 1);
            case 4:
                return new Vector3f(factor, 0, 1);
            case 5:
                return new Vector3f(1, 0, factor);
        }

        throw new RuntimeException("Exhausted switch statement: H" + hue + ", F" + factor);
    }

    public static void setupEvents() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getGameTime() - resultsGameTime >= WhereIsIt.CONFIG.client.highlightFadeTime) {
                clear();
            }
        });

        WorldRenderEvents.LAST.register(HighlightRendering::drawHighlights);

        WorldRenderEvents.AFTER_ENTITIES.register(HighlightRendering::drawNames);

        // Slot highlights are rendered from MixinAbstractContainerScreen.class
    }
}
