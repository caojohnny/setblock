# `setblock`

`setblock` is a library for setting block(s) more quickly.

The Bukkit `Block` class is notoriously slow for setting
large quantities of blocks, useful for pasting schematics
and doing a reset of worlds that have been modified in a
minigame, for example. Luckily, it is possible to do a
number of optimizations and cut out unnecessary steps to
improve the runtime performance of setting blocks,
especially if a large number of blocks need to be modified.

# Implementation

Setting block data is a rather involved process. Simply
changing the data at a particular location is only part of
it. It also involves chunk loading, lighting updates, tile
entity updates, physics updates and notifying players of
the changes. Not all of these features are needed every
time a block is set. By cutting out the Minecraft server's
internal implementation of setting block data, it is
possible to customize which features are needed.

# Usage

This plugin is designed to be utilized as an API by other
plugins. You should depend on the `setblock-plugin`
module.

It can be used in the following way:

``` java
SetBlockPlugin plugin = Bukkit.getPluginManager().getPlugin("SetBlock");

List<BulkSetEntry> entries = new ArrayList<>();
entries.add(new BulkSetEntry(new Location(...), Material.STONE.createBlockData());
...
BulkSetEntry[] bseArray = entries.toArray(new BulkSetEntry[0]);

EnumSet<SetBlockOption> options = EnumSet.of(BUKKIT);
BulkSetBlock bsb = new BulkSetBlock(plugin, world, bseArray, options);

bsb.commit();
```

There are a number of additional optimizations that can be
made by pre-preparing entries and pre-loading chunks in the
`BulkSetBlock` class. In addition, the aforementioned
feature modifications can be tuned by changing the options
in the `options` set. While the safest (and slowest) option
is to use the `BUKKIT` default, it is possible to speed up
the commit operation by using `LOCK_FREE_WRITE` only. Again,
these can be tuned to your liking, and the documentation is
available as code comments in the `SetBlockOption` class.

# Building

``` shell
git clone https://github.com/caojohnny/setblock.git
cd setblock
./gradlew shadowJar
```

The jar file can be found in `setblock-plugin/build/libs`

# Caveats

- Designed for 1.15. No plans to change this as of yet.
- This is an API. It does nothing on its own.
- Should not be used for chunk generation, captured block
states are ignored
- Should be carefully tested before being deployed to
production

# Credits

Built with [IntelliJ IDEA](https://www.jetbrains.com/idea/)

Uses [PaperLib](https://github.com/PaperMC/PaperLib)

Inspired by [this thread](https://www.spigotmc.org/threads/methods-for-changing-massive-amount-of-blocks-up-to-14m-blocks-s.395868/)
