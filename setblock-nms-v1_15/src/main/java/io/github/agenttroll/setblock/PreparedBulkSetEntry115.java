package io.github.agenttroll.setblock;

import io.agenttroll.github.setblock.PreparedBulkSetEntry;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

import java.util.EnumSet;

public class PreparedBulkSetEntry115 implements PreparedBulkSetEntry {
    private final World nmsWorld;
    private final BulkSetEntry entry;
    private final IBlockData data;
    private final int chunkX;
    private final int chunkZ;

    private final int chunkRelX;
    private final int csIdx;
    private final int chunkRelZ;

    private Chunk nmsChunk;
    private ChunkSection chunkSection;

    private final BlockPosition blockPos;

    public PreparedBulkSetEntry115(org.bukkit.World world, BulkSetEntry entry) {
        this.nmsWorld = ((CraftWorld) world).getHandle();
        this.entry = entry;
        this.data = ((CraftBlockData) entry.getData()).getState();
        this.chunkX = entry.getX() >> 4;
        this.chunkZ = entry.getZ() >> 4;

        this.chunkRelX = entry.getX() & 15;
        this.csIdx = entry.getY() >> 4;
        this.chunkRelZ = entry.getZ() & 15;

        this.blockPos = new BlockPosition(this.entry.getX(), this.entry.getY(), this.entry.getZ());
    }

    @Override
    public boolean prepareChunk(org.bukkit.Chunk chunk, EnumSet<SetBlockOption> options) {
        if (options.contains(SetBlockOption.BUKKIT)) {
            return true;
        }

        if (chunk != null) {
            CraftChunk craftChunk = (CraftChunk) chunk;
            this.nmsChunk = craftChunk.getHandle();
        } else {
            this.nmsChunk = (Chunk) this.nmsWorld.getChunkAt(this.chunkX, this.chunkZ, ChunkStatus.FULL);
        }

        // nms.Chunk#setType(BlockPosition, IBlockData, boolean, boolean)
        ChunkSection[] chunkSections = this.nmsChunk.getSections();
        this.chunkSection = chunkSections[this.csIdx];
        if (this.chunkSection == Chunk.a) {
            if (this.data.isAir()) {
                return false;
            }

            this.chunkSection = new ChunkSection(this.csIdx << 4);
            chunkSections[this.csIdx] = this.chunkSection;
        }

        this.nmsChunk.markDirty();

        return true;
    }

    @Override
    public void commit(EnumSet<SetBlockOption> options) {
        if (options.contains(SetBlockOption.BUKKIT)) {
            this.nmsWorld.getWorld()
                    .getBlockAt(this.entry.getX(), this.entry.getY(), this.entry.getZ())
                    .setBlockData(this.entry.getData());
            return;
        }

        if (this.nmsChunk != null && this.chunkSection == null) {
            return;
        }

        // obc.CraftBlock#setTypeAndData(IBlockData, boolean)
        if (options.contains(SetBlockOption.PRE_TILE_CHECK)) {
            if (!this.data.isAir() && this.data.getBlock() instanceof BlockTileEntity && this.data.getBlock() != this.nmsWorld.getType(this.blockPos).getBlock()) {
                if (this.nmsWorld instanceof net.minecraft.server.v1_15_R1.World) {
                    this.nmsWorld.removeTileEntity(this.blockPos);
                } else {
                    this.nmsWorld.setTypeAndData(this.blockPos, Blocks.AIR.getBlockData(), 0);
                }
            }
        }

        IBlockData prevBlockData;
        boolean isChunkEmptyPrior = this.chunkSection.c();

        int csRelY = this.entry.getY() & 15;
        boolean useLock = !options.contains(SetBlockOption.WRITE_LOCK_FREE);
        if (!options.contains(SetBlockOption.WRITE_UNSAFE)) {
            prevBlockData = this.chunkSection.setType(this.chunkRelX, csRelY, this.chunkRelZ, this.data, useLock);
        } else if (useLock) {
            DataPaletteBlock<IBlockData> palette = this.chunkSection.getBlocks();
            prevBlockData = palette.setBlock(this.chunkRelX, csRelY, this.chunkRelZ, this.data);
        } else {
            DataPaletteBlock<IBlockData> palette = this.chunkSection.getBlocks();
            prevBlockData = palette.b(this.chunkRelX, csRelY, this.chunkRelZ, this.data);
        }

        // nms.Chunk#setType(BlockPosition, IBlockData, boolean, boolean)
        boolean applyPhysics = options.contains(SetBlockOption.POST_APPLY_PHYSICS);
        if (prevBlockData == this.data) {
            prevBlockData = null;
        } else {
            if (options.contains(SetBlockOption.POST_UPDATE_HEIGHTMAP)) {
                this.nmsChunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING).a(this.chunkRelX, this.entry.getY(), this.chunkRelZ, this.data);
                this.nmsChunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES).a(this.chunkRelX, this.entry.getY(), this.chunkRelZ, this.data);
                this.nmsChunk.heightMap.get(HeightMap.Type.OCEAN_FLOOR).a(this.chunkRelX, this.entry.getY(), this.chunkRelZ, this.data);
                this.nmsChunk.heightMap.get(HeightMap.Type.WORLD_SURFACE).a(this.chunkRelX, this.entry.getY(), this.chunkRelZ, this.data);
            }

            if (options.contains(SetBlockOption.POST_UPDATE_LIGHT)) {
                boolean isChunkEmptyPost = this.chunkSection.c();
                if (isChunkEmptyPrior != isChunkEmptyPost) {
                    this.nmsChunk.world.getChunkProvider().getLightEngine().a(this.blockPos, isChunkEmptyPost);
                }
            }

            boolean updatePrevTile = options.contains(SetBlockOption.POST_UPDATE_PREV_TILE);
            boolean updateNewTile = options.contains(SetBlockOption.POST_UPDATE_TILE);

            if (updatePrevTile || updateNewTile) {
                Block newBlock = this.data.getBlock();
                Block prevBlock = prevBlockData.getBlock();

                if (updatePrevTile) {
                    if (!this.nmsChunk.world.isClientSide) {
                        // boolean is i & 64 != 0, i is only ever 3 and 1042 so this is always false
                        prevBlockData.remove(this.nmsChunk.world, this.blockPos, this.data, false);
                    } else if (prevBlock != newBlock && prevBlock instanceof ITileEntity) {
                        this.nmsChunk.world.removeTileEntity(this.blockPos);
                    }
                }

                if (this.chunkSection.getType(this.chunkRelX, csRelY, this.chunkRelZ).getBlock() != newBlock) {
                    prevBlockData = null;
                } else {
                    if (updatePrevTile) {
                        if (prevBlock instanceof ITileEntity) {
                            TileEntity tileentity = this.nmsChunk.a(this.blockPos, Chunk.EnumTileEntityState.CHECK);
                            if (tileentity != null) {
                                tileentity.invalidateBlockCache();
                            }
                        }
                    }

                    if (updateNewTile) {
                        if (!this.nmsChunk.world.isClientSide && applyPhysics && (!this.nmsChunk.world.captureBlockStates || newBlock instanceof BlockTileEntity)) {
                            // boolean is i & 64 != 0, i is only ever 3 and 1042 for CraftBlock so this is always false
                            this.data.onPlace(this.nmsChunk.world, this.blockPos, prevBlockData, false);
                        }

                        if (newBlock instanceof ITileEntity) {
                            TileEntity tileentity = this.nmsChunk.a(this.blockPos, Chunk.EnumTileEntityState.CHECK);
                            if (tileentity == null) {
                                tileentity = ((ITileEntity) newBlock).createTile(this.nmsChunk.world);
                                this.nmsChunk.world.setTileEntity(this.blockPos, tileentity);
                            } else {
                                tileentity.invalidateBlockCache();
                            }
                        }
                    }
                }
            }
        }

        // nms.World#setTypeAndData(BlockPosition, IBlockData, int)
        if (prevBlockData != null) {
            IBlockData iblockdata2 = this.nmsWorld.getType(this.blockPos);
            if (options.contains(SetBlockOption.POST_UPDATE_LIGHT)) {
                if (iblockdata2 != prevBlockData
                        && (iblockdata2.b(this.nmsWorld, this.blockPos) != prevBlockData.b(this.nmsWorld, this.blockPos)
                        || iblockdata2.h() != prevBlockData.h()
                        || iblockdata2.g()
                        || prevBlockData.g())) {
                    this.nmsWorld.getMethodProfiler().enter("queueCheckLight");
                    this.nmsWorld.getChunkProvider().getLightEngine().a(this.blockPos);
                    this.nmsWorld.getMethodProfiler().exit();
                }
            }

            if (applyPhysics) {
                try {
                    this.nmsWorld.notifyAndUpdatePhysics(this.blockPos, this.nmsChunk, prevBlockData, this.data, iblockdata2, 3);
                } catch (StackOverflowError var9) {
                    net.minecraft.server.v1_15_R1.World.lastPhysicsProblem = new BlockPosition(this.blockPos);
                }
            } else if (options.contains(SetBlockOption.POST_DO_NOTIFY)) {
                // obc.CraftBlock#setTypeAndData(IBlockData, boolean)
                this.nmsWorld.notify(this.blockPos, prevBlockData, this.data, 3);
            }
        }
    }

    @Override
    public int getChunkX() {
        return this.chunkX;
    }

    @Override
    public int getChunkZ() {
        return this.chunkZ;
    }
}
