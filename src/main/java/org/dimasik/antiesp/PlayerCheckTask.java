package org.dimasik.antiesp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class PlayerCheckTask extends BukkitRunnable {
    private final PlayerVisibilityManager visibilityManager;

    public PlayerCheckTask(PlayerVisibilityManager visibilityManager) {
        this.visibilityManager = visibilityManager;
    }

    @Override
    public void run() {
        if (!AntiESP.getInstance().getPluginConfig().getBoolean("enable")) return;

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            Bukkit.getOnlinePlayers().forEach(target -> {
                if (viewer.equals(target)) return;
                if (viewer.getWorld() != target.getWorld()) return;
                if(!target.hasPotionEffect(PotionEffectType.INVISIBILITY) && !target.isSneaking()){
                    visibilityManager.setPlayerVisible(viewer, target);
                }
                else if(!viewer.hasPotionEffect(PotionEffectType.BLINDNESS) || viewer.getLocation().distance(target.getLocation()) < 5){
                    visibilityManager.setPlayerVisible(viewer, target);
                }
                double distance = viewer.getLocation().distance(target.getLocation());
                int maxRadius = AntiESP.getInstance().getPluginConfig().getInt("max-radius", 64);
                if (distance > maxRadius) {
                    visibilityManager.hidePlayer(viewer, target);
                    return;
                }

                boolean canSee = NMSUtil.canSee(viewer, target);
                if (canSee) {
                    visibilityManager.showPlayer(viewer, target);
                } else {
                    visibilityManager.hidePlayer(viewer, target);
                }
            });
        });
    }
}