package org.dimasik.antiesp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiESP extends JavaPlugin {
    private static AntiESP instance;
    private PlayerVisibilityManager visibilityManager;
    private PlayerCheckTask checkTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        visibilityManager = new PlayerVisibilityManager();
        checkTask = new PlayerCheckTask(visibilityManager);
        checkTask.runTaskTimerAsynchronously(this, 1L, 1L);
        getServer().getPluginManager().registerEvents(new PlayerListener(visibilityManager), this);
    }

    @Override
    public void onDisable() {
        if (checkTask != null) checkTask.cancel();
        visibilityManager.showAllPlayers();
    }

    public static AntiESP getInstance() { return instance; }
    public FileConfiguration getPluginConfig() { return getConfig(); }
}