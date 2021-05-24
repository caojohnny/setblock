package io.github.caojohnny.setblock;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

/**
 * A data-wrapper class used to represent a queued block
 * change to be set into a location (world-agnostic).
 */
public class BulkSetEntry {
    private final int x;
    private final int y;
    private final int z;
    private final BlockData data;

    public BulkSetEntry(int x, int y, int z, BlockData data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    public BulkSetEntry(Location location, BlockData data) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.data = data;
    }

    public BulkSetEntry(Vector vector, BlockData data) {
        this.x = vector.getBlockX();
        this.y = vector.getBlockY();
        this.z = vector.getBlockZ();
        this.data = data;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public BlockData getData() {
        return this.data;
    }
}
