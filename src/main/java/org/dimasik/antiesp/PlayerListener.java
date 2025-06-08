package org.dimasik.antiesp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerListener implements Listener {
    private final PlayerVisibilityManager visibilityManager;

    public PlayerListener(PlayerVisibilityManager visibilityManager) {
        this.visibilityManager = visibilityManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (AntiESP.getInstance().getPluginConfig().getBoolean("debug")) {
            String penis = "мне лень было делать дебаг, багов вроде нет, если что отпишите тг @Dimasik_v201O";
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info(penis);
            AntiESP.getInstance().getLogger().info("Игрок зашел: " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        visibilityManager.showAllPlayersFor(player);
    }
}