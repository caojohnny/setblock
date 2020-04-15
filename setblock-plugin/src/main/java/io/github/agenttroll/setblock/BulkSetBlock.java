package io.github.agenttroll.setblock;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.agenttroll.github.setblock.PreparedBulkSetEntry;
import io.agenttroll.github.setblock.SetBlockNms;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a queue of blocks that will be
 * committed to the world.
 */
public class BulkSetBlock {
    private final SetBlockPlugin plugin;
    private final World world;
    private final BulkSetEntry[] entries;
    private final EnumSet<SetBlockOption> options;

    private PreparedBulkSetEntry[] preparedEntries;
    private Collection<CompletableFuture<Chunk>> preparedChunks;
    private int preparedChunkCount;

    public BulkSetBlock(SetBlockPlugin plugin, World world, BulkSetEntry[] entries, EnumSet<SetBlockOption> options) {
        this.plugin = plugin;
        this.world = world;
        this.entries = entries;
        this.options = options;
    }

    /**
     * Pre-prepares the entries in this bulk operation to
     * be ready for the commit.
     *
     * <p>It is not necessary to explicitly call this
     * method, but it may have a minor performance
     * improvement if the preparation can be called earlier
     * than the commit operation in order to cache some
     * state variables.</p>
     *
     * <p>Calling this method twice has no effect.</p>
     */
    public void prepareEntries() {
        if (this.preparedEntries == null) {
            this.preparedEntries = new PreparedBulkSetEntry[this.entries.length];

            SetBlockNms nms = this.plugin.getNms();
            for (int i = 0, len = this.entries.length; i < len; i++) {
                this.preparedEntries[i] = nms.prepare(this.world, this.entries[i]);
            }
        }
    }

    /**
     * Pre-prepares the chunks that need to be loaded in
     * order for the commit operation to occur.
     *
     * <p>It is not necessary to explicitly call this
     * method, but it may have a minor performance
     * improvement if the prepareation can be called
     * earlier than the commit operation in order to cache
     * some state variables.</p>
     *
     * <p>Calling this method twice has no effect.</p>
     *
     * @return the collection of futures in the case the
     * server supports asynchronous chunk loading
     */
    public Collection<CompletableFuture<Chunk>> prepareChunks() {
        if (this.preparedChunks == null) {
            if (this.preparedEntries == null) {
                this.prepareEntries();
            }

            this.preparedChunks = new ArrayList<>();

            Table<Integer, Integer, CompletableFuture<Chunk>> chunkTable = HashBasedTable.create();
            for (int i = 0, len = this.preparedEntries.length; i < len; i++) {
                PreparedBulkSetEntry entry = this.preparedEntries[i];

                CompletableFuture<Chunk> f = chunkTable.get(entry.getChunkX(), entry.getChunkZ());
                if (f == null) {
                    f = PaperLib.getChunkAtAsync(this.world, entry.getChunkX(), entry.getChunkZ());
                    f.thenRun(() -> this.preparedChunkCount++);
                    if (this.options.contains(SetBlockOption.PRE_CHUNK_HOLD)) {
                        f.thenAccept(ch -> ch.addPluginChunkTicket(this.plugin));
                    }

                    chunkTable.put(entry.getChunkX(), entry.getChunkZ(), f);
                    this.preparedChunks.add(f);
                }

                f.thenAccept(chunk -> entry.prepareChunk(chunk, this.options));
            }
        }

        return this.preparedChunks;
    }

    /**
     * Commits the bulk-set operation into the world,
     * performing the necessary preparation steps not
     * explicitly called and writing the changes to the
     * chunk data palettes.
     */
    public void commit() {
        if (this.preparedEntries == null) {
            this.prepareEntries();
        }

        for (int i = 0, len = this.preparedEntries.length; i < len; i++) {
            PreparedBulkSetEntry entry = this.preparedEntries[i];
            if (this.preparedChunks == null || this.preparedChunks.size() != this.preparedChunkCount) {
                entry.prepareChunk(null, this.options);
            }

            entry.commit(this.options);
        }

        if (this.preparedChunks != null && this.options.contains(SetBlockOption.PRE_CHUNK_HOLD)) {
            for (CompletableFuture<Chunk> f : this.preparedChunks) {
                f.thenAccept(ch -> ch.removePluginChunkTicket(this.plugin));
            }
        }
    }

    /**
     * Obtains the prepared bulk-set entries.
     *
     * <p>Be careful to not modify this array, unless
     * through {@link #prepareEntries()}.</p>
     *
     * @return {@code null} if not prepared
     */
    public PreparedBulkSetEntry[] getPreparedEntries() {
        return this.preparedEntries;
    }

    /**
     * Obtains the chunks being queued for the operation.
     *
     * <p>Be careful to not modify this collection, unless
     * through {@link #prepareChunks()}.</p>
     *
     * @return {@code null} if not prepared
     */
    public Collection<CompletableFuture<Chunk>> getPreparedChunks() {
        return this.preparedChunks;
    }
}
