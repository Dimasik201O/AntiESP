package org.dimasik.antiesp;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getServer;

public class PlayerVisibilityManager {
    private final Map<UUID, Set<UUID>> hiddenFrom = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> hiddenItemsFrom = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> destroyFrom = new ConcurrentHashMap<>();

    public void hidePlayer(Player hider, Player target) {
        UUID hiderId = hider.getUniqueId();
        UUID targetId = target.getUniqueId();

        hiddenItemsFrom.computeIfAbsent(targetId, k -> new HashSet<>()).add(hiderId);
        if(target.hasPotionEffect(PotionEffectType.INVISIBILITY) || target.isSneaking()){
            destroyFrom.computeIfAbsent(targetId, k -> new HashSet<>()).add(hiderId);
            NMSUtil.sendDestroyPacket(hider, target);
        }
        NMSUtil.hidePlayerItems(hider, target);
    }

    public void showPlayer(Player viewer, Player target) {
        UUID viewerId = viewer.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (hiddenItemsFrom.getOrDefault(targetId, Collections.emptySet()).remove(viewerId)) {
            setPlayerVisible(viewer, target);
            NMSUtil.showPlayerItems(viewer, target);
        }
    }

    public void setPlayerVisible(Player viewer, Player target) {
        UUID viewerId = viewer.getUniqueId();
        UUID targetId = target.getUniqueId();

        if (destroyFrom.getOrDefault(targetId, Collections.emptySet()).remove(viewerId)) {
            NMSUtil.sendSpawnPacket(viewer, target);
            NMSUtil.sendHeadRotationPacket(viewer, target);
            NMSUtil.sendInvisibilityPacket(viewer, target, target.hasPotionEffect(PotionEffectType.INVISIBILITY));
        }
    }

    public void showAllPlayers() {
        hiddenFrom.forEach((targetId, viewers) -> {
            Player target = AntiESP.getInstance().getServer().getPlayer(targetId);
            if (target != null) {
                viewers.forEach(viewerId -> {
                    Player viewer = AntiESP.getInstance().getServer().getPlayer(viewerId);
                    if (viewer != null) {
                        NMSUtil.sendTeleportPacket(viewer, target, false);
                    }
                });
            }
        });

        hiddenItemsFrom.forEach((targetId, viewers) -> {
            Player target = AntiESP.getInstance().getServer().getPlayer(targetId);
            if (target != null) {
                viewers.forEach(viewerId -> {
                    Player viewer = AntiESP.getInstance().getServer().getPlayer(viewerId);
                    if (viewer != null) {
                        NMSUtil.showPlayerItems(viewer, target);
                    }
                });
            }
        });

        hiddenFrom.clear();
        hiddenItemsFrom.clear();
    }

    public void showAllPlayersFor(Player player) {
        UUID playerId = player.getUniqueId();

        hiddenFrom.entrySet().removeIf(entry -> {
            if (entry.getValue().remove(playerId)) {
                Player target = getServer().getPlayer(entry.getKey());
                if (target != null) {
                    NMSUtil.sendTeleportPacket(player, target, false);
                }
            }
            return entry.getValue().isEmpty();
        });

        hiddenItemsFrom.entrySet().removeIf(entry -> {
            if (entry.getValue().remove(playerId)) {
                Player target = getServer().getPlayer(entry.getKey());
                if (target != null) {
                    NMSUtil.showPlayerItems(player, target);
                }
            }
            return entry.getValue().isEmpty();
        });
    }

    public boolean isHiddenFrom(Player target, Player viewer) {
        return hiddenFrom.getOrDefault(target.getUniqueId(), Collections.emptySet()).contains(viewer.getUniqueId()) ||
                hiddenItemsFrom.getOrDefault(target.getUniqueId(), Collections.emptySet()).contains(viewer.getUniqueId());
    }
}