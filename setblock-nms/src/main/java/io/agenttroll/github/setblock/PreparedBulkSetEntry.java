package io.agenttroll.github.setblock;

import io.github.agenttroll.setblock.BulkSetEntry;
import io.github.agenttroll.setblock.SetBlockOption;
import org.bukkit.Chunk;

import java.util.EnumSet;

public interface PreparedBulkSetEntry {
    boolean prepareChunk(Chunk chunk, EnumSet<SetBlockOption> options);

    void commit(EnumSet<SetBlockOption> options);

    int getChunkX();

    int getChunkZ();
}
