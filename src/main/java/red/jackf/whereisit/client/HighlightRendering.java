package red.jackf.whereisit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.search.SearchExecutor;

public class HighlightRendering {
    @Nullable
    private static SearchExecutor.Result results = null;
    private static long resultsGameTime = -1;

    public static void setResults(SearchExecutor.Result results, long resultsGameTime) {
        HighlightRendering.results = results;
        HighlightRendering.resultsGameTime = resultsGameTime;
    }

    public static void clearHighlights() {
        results = null;
        resultsGameTime = -1;
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

    public static void setupEvents() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getGameTime() - resultsGameTime >= WhereIsIt.CONFIG.client.highlightFadeTime) {
                clearHighlights();
            }
        });

        WorldRenderEvents.LAST.register(context -> {
            if (results == null) return;

            var renderTime = context.world().getGameTime() + context.tickDelta();

            var red = 1f;
            var green = 0.5f;
            var blue = 1f;
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
                drawCube(bufferBuilder, result.pos(), camPos, 0.75f, red, green, blue, alpha);
            }

            tesselator.end();

            RenderSystem.enableDepthTest();
        });
    }
}
