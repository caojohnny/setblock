package io.github.caojohnny.setblock;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin main class. Do not use any methods.
 */
public class SetBlockPlugin extends JavaPlugin {
    private SetBlockNms nms;

    @Override
    public void onEnable() {
        // Plug Paper
        PaperLib.suggestPaper(this);

        String version = Bukkit.getVersion();
        if (version.contains("1.15")) {
            this.nms = new SetBlockNms115();
        } else {
            this.getLogger().severe(version + " is not supported");
            Bukkit.getPluginManager().disablePlugin(this);

            return;
        }
    }

    public SetBlockNms getNms() {
        return this.nms;
    }
}
