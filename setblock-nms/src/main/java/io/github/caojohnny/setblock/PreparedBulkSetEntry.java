package io.github.caojohnny.setblock;

import org.bukkit.Chunk;

import java.util.EnumSet;

public interface PreparedBulkSetEntry {
    boolean prepareChunk(Chunk chunk, EnumSet<SetBlockOption> options);

    void commit(EnumSet<SetBlockOption> options);

    int getChunkX();

    int getChunkZ();
}
