package gg.emopedia.allayfix;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public class AllayFix implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            ChunkPos playerChunkPos = player.getChunkPos();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + dx, playerChunkPos.z + dz);
                    Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                    if (chunk instanceof WorldChunk worldChunk) {
                        if (isLazyChunk(worldChunk)) {
                            Box chunkBox = new Box(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(),
                                    chunkPos.getEndX() + 1, world.getTopY(), chunkPos.getEndZ() + 1);
                            for (AllayEntity allay : world.getEntitiesByClass(AllayEntity.class, chunkBox, Entity::isAlive)) {
                                preventDamageInLazyChunks(allay);
                            }
                        } else {
                            Box chunkBox = new Box(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(),
                                    chunkPos.getEndX() + 1, world.getTopY(), chunkPos.getEndZ() + 1);
                            for (AllayEntity allay : world.getEntitiesByClass(AllayEntity.class, chunkBox, Entity::isAlive)) {
                                resetDamage(allay);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isLazyChunk(Chunk chunk) {
        return chunk.getStatus() != ChunkStatus.FULL;
    }

    private void preventDamageInLazyChunks(AllayEntity allay) {
        if (!allay.isInvulnerable()) {
            allay.setInvulnerable(true);
        }
    }

    private void resetDamage(AllayEntity allay) {
        if (allay.isInvulnerable()) {
            allay.setInvulnerable(false);
        }
    }
}
