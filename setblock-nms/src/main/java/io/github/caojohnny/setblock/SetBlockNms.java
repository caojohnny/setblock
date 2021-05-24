package io.github.caojohnny.setblock;

import org.bukkit.World;

public interface SetBlockNms {
    PreparedBulkSetEntry prepare(World world, BulkSetEntry entry);
}
