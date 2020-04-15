package io.agenttroll.github.setblock;

import io.github.agenttroll.setblock.BulkSetEntry;
import org.bukkit.World;

public interface SetBlockNms {
    PreparedBulkSetEntry prepare(World world, BulkSetEntry entry);
}
