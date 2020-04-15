package io.github.agenttroll.setblock;

import io.agenttroll.github.setblock.PreparedBulkSetEntry;
import io.agenttroll.github.setblock.SetBlockNms;
import org.bukkit.World;

public class SetBlockNms115 implements SetBlockNms {
    @Override
    public PreparedBulkSetEntry prepare(World world, BulkSetEntry entry) {
        return new PreparedBulkSetEntry115(world, entry);
    }
}
