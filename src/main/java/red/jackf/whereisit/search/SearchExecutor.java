package red.jackf.whereisit.search;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.search.criteria.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public abstract class SearchExecutor {
    public static Result search(ServerPlayer player, ServerLevel level, SearchCriteria.Predicate predicate) {
        var results = new ArrayList<Result.Position>();

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
                                results.add(new Result.Position(bePos, getTitle(bePos, be)));
                            }
                        }
                    }
                }
            }
        }

        return new Result(results);
    }

    @Nullable
    public static Component getTitle(BlockPos pos, BlockEntity entity) {
        if (entity instanceof Nameable nameable) {
            return nameable.getCustomName();
        }

        return null;
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

    public static record Result(List<Position> positions) {
        public FriendlyByteBuf toByteBuf() {
            var buf = PacketByteBufs.create();
            buf.writeInt(positions.size());
            for (Position position : positions) {
                buf.writeBlockPos(position.pos);
                buf.writeBoolean(position.name != null);
                if (position.name != null) {
                    buf.writeComponent(position.name);
                }
            }
            return buf;
        }

        public static Result fromByteBuf(FriendlyByteBuf buf) {
            var size = buf.readInt();
            var positions = new ArrayList<Position>();
            for (int i = 0; i < size; i++) {
                var pos = buf.readBlockPos();
                Component name = null;
                if (buf.readBoolean()) {
                    name = buf.readComponent();
                }
                positions.add(new Position(pos, name));
            }
            return new Result(positions);
        }

        public static record Position(BlockPos pos, @Nullable Component name) {}
    }
}
