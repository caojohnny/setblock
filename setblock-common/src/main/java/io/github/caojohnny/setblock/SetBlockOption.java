package io.github.caojohnny.setblock;

import org.bukkit.block.data.BlockData;

/**
 * Options used to decide what "features" to utilize in
 * order to set a block's type and data.
 */
public enum SetBlockOption {
    /**
     * Utilize the Bukkit implementation to set block.
     *
     * <p>This makes BulkSetBlock a wrapper over
     * {@link org.bukkit.block.Block#setBlockData(BlockData)}
     * and overrides every other option (i.e. other options
     * are not used).</p>
     */
    BUKKIT,

    /**
     * "Holds" a chunk using a plugin chunk ticket to
     * prevent it from unloaded. Use this if the chunks
     * are pre-loaded through BulkSetEntry and the
     * blocks are committed in a separate tick.
     */
    PRE_CHUNK_HOLD,

    /**
     * Performs a pre-check for setting a tile entity to
     * air, used by the CraftBlock implementation.
     */
    PRE_TILE_CHECK,

    /**
     * Turns on unsafe writing. The default implementation
     * for setting a block type in ChunkSection counts
     * non-empty blocks. Utilizing this option bypasses
     * block counting.
     */
    WRITE_UNSAFE,

    /**
     * Writes to a chunk section without utilizing a lock.
     *
     * <p>For most practical purposes, it doesn't seem like
     * this option really makes much of a difference. It is
     * left on by default, only use this option if you know
     * better than I what it even does in the first place.</p>
     */
    WRITE_LOCK_FREE,

    /**
     * Turns on height map updates after the block has been
     * written to the chunk.
     */
    POST_UPDATE_HEIGHTMAP,

    /**
     * Turns on lighting updates.
     *
     * <p>As far as I can tell, this probably requires
     * {@link #WRITE_UNSAFE} to be OFF (not included in
     * the option set).</p>
     */
    POST_UPDATE_LIGHT,

    /**
     * Turns on tile entity updates made to the previous
     * occupant of the block position.
     */
    POST_UPDATE_PREV_TILE,

    /**
     * Turns on tile updates for the block being set.
     */
    POST_UPDATE_TILE,

    /**
     * Turns on physics being applied to neighboring blocks
     * as a result of the block update.
     */
    POST_APPLY_PHYSICS,

    /**
     * Turns on block notification. This means that blocks
     * being updated will write to the dirty block array
     * and therefore will be sent to the player automatically
     * rather than requiring them to relog in order to see
     * the update to the data palette.
     */
    POST_DO_NOTIFY
}
