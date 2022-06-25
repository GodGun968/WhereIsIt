package red.jackf.whereisit.search;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.search.criteria.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchExecutor {
    public static List<BlockPos> search(ServerPlayer player, ServerLevel level, SearchCriteria.Predicate predicate) {
        var results = new ArrayList<BlockPos>();

        var radius = WhereIsIt.CONFIG.server.searchRange;
        var playerPos = player.blockPosition();

        int minChunkX = (-radius + playerPos.getX()) >> 4;
        int maxChunkX = (radius + 1 + playerPos.getX()) >> 4;
        int minChunkZ = (-radius + playerPos.getZ()) >> 4;
        int maxChunkZ = (radius + 1 + playerPos.getZ()) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {

                var chunk = level.getChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                for (var entry : chunk.getBlockEntities().entrySet()) {
                    var be = entry.getValue();
                    var bePos = entry.getKey();
                    if (bePos.closerThan(playerPos, radius)) {

                        // Most standard inventories (chests, furnaces, etc)
                        if (be instanceof Container container) {
                            if (searchContainer(container, predicate)) {
                                results.add(bePos);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    private static boolean searchContainer(Container container, SearchCriteria.Predicate predicate) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            var item = container.getItem(i);
            if (!item.isEmpty() && searchItemStack(item, predicate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean searchItemStack(ItemStack item, SearchCriteria.Predicate predicate) {
        return predicate.test(item);
    }
}
