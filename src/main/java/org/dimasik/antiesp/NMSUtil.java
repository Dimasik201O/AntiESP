package org.dimasik.antiesp;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class NMSUtil {
    public static boolean canSee(Player viewer, Player target) {
        if (viewer.hasPermission("antiesp.bypass")){
            return true;
        }

        if (isBehind(viewer, target) && viewer.getLocation().distance(target.getLocation()) >= 2.25) {
            return false;
        }

        CraftPlayer craftViewer = (CraftPlayer) viewer;
        CraftPlayer craftTarget = (CraftPlayer) target;
        EntityPlayer nmsViewer = craftViewer.getHandle();
        EntityPlayer nmsTarget = craftTarget.getHandle();

        AxisAlignedBB bb = nmsTarget.getBoundingBox();
        WorldServer world = nmsViewer.getWorldServer();

        int verticalPoints = 10;
        int horizontalPoints = 5;

        for (int v = 0; v < verticalPoints; v++) {
            double y = bb.minY + (bb.maxY - bb.minY) * v / (verticalPoints - 1);

            for (int h = 0; h < horizontalPoints; h++) {
                double x = bb.minX + (bb.maxX - bb.minX) * h / (horizontalPoints - 1);
                double z = bb.minZ + (bb.maxZ - bb.minZ) * h / (horizontalPoints - 1);

                Vec3D start = new Vec3D(nmsViewer.locX(), nmsViewer.locY() + nmsViewer.getHeadHeight(), nmsViewer.locZ());
                Vec3D end = new Vec3D(x, y, z);

                MovingObjectPosition result = world.rayTrace(
                        new RayTrace(start, end,
                                RayTrace.BlockCollisionOption.COLLIDER,
                                RayTrace.FluidCollisionOption.NONE,
                                nmsViewer)
                );

                if (result == null || result.getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isBehind(Player viewer, Player target) {
        Vector direction = viewer.getLocation().getDirection();
        Vector toTarget = target.getLocation().toVector().subtract(viewer.getLocation().toVector());
        return direction.angle(toTarget) > Math.PI/2;
    }

    public static void sendTeleportPacket(Player viewer, Player target, boolean hide) {
        EntityPlayer nmsTarget = ((CraftPlayer) target).getHandle();
        EntityPlayer nmsViewer = ((CraftPlayer) viewer).getHandle();

        if (hide) {
            nmsViewer.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(nmsTarget.getId()));
        } else {
            nmsViewer.playerConnection.sendPacket(new PacketPlayOutEntityTeleport(nmsTarget));
        }
    }

    public static void hidePlayerItems(Player viewer, Player target) {
        EntityPlayer nmsViewer = ((CraftPlayer) viewer).getHandle();
        EntityPlayer nmsTarget = ((CraftPlayer) target).getHandle();

        List<Pair<EnumItemSlot, ItemStack>> items = List.of(
                Pair.of(EnumItemSlot.MAINHAND, ItemStack.b),
                Pair.of(EnumItemSlot.OFFHAND, ItemStack.b),
                Pair.of(EnumItemSlot.HEAD, ItemStack.b),
                Pair.of(EnumItemSlot.CHEST, ItemStack.b),
                Pair.of(EnumItemSlot.LEGS, ItemStack.b),
                Pair.of(EnumItemSlot.FEET, ItemStack.b)
        );

        nmsViewer.playerConnection.sendPacket(new PacketPlayOutEntityEquipment(
                nmsTarget.getId(),
                items
        ));
    }

    public static void showPlayerItems(Player viewer, Player target) {
        EntityPlayer nmsViewer = ((CraftPlayer) viewer).getHandle();
        EntityPlayer nmsTarget = ((CraftPlayer) target).getHandle();

        ItemStack mainHand = nmsTarget.getItemInMainHand();
        ItemStack offHand = nmsTarget.getItemInOffHand();
        ItemStack head = nmsTarget.getEquipment(EnumItemSlot.HEAD);
        ItemStack chest = nmsTarget.getEquipment(EnumItemSlot.CHEST);
        ItemStack legs = nmsTarget.getEquipment(EnumItemSlot.LEGS);
        ItemStack feet = nmsTarget.getEquipment(EnumItemSlot.FEET);

        org.bukkit.inventory.ItemStack bukkitMainHand = CraftItemStack.asBukkitCopy(mainHand);
        org.bukkit.inventory.ItemStack bukkitOffHand = CraftItemStack.asBukkitCopy(offHand);
        org.bukkit.inventory.ItemStack bukkitHead = head != null ? CraftItemStack.asBukkitCopy(head) : new org.bukkit.inventory.ItemStack(Material.AIR);
        org.bukkit.inventory.ItemStack bukkitChest = chest != null ? CraftItemStack.asBukkitCopy(chest) : new org.bukkit.inventory.ItemStack(Material.AIR);
        org.bukkit.inventory.ItemStack bukkitLegs = legs != null ? CraftItemStack.asBukkitCopy(legs) : new org.bukkit.inventory.ItemStack(Material.AIR);
        org.bukkit.inventory.ItemStack bukkitFeet = feet != null ? CraftItemStack.asBukkitCopy(feet) : new org.bukkit.inventory.ItemStack(Material.AIR);

        NMSUtil nmsUtil = new NMSUtil();
        bukkitMainHand = nmsUtil.spoofMeta(bukkitMainHand);
        bukkitOffHand = nmsUtil.spoofMeta(bukkitOffHand);
        bukkitHead = nmsUtil.spoofMeta(bukkitHead);
        bukkitChest = nmsUtil.spoofMeta(bukkitChest);
        bukkitLegs = nmsUtil.spoofMeta(bukkitLegs);
        bukkitFeet = nmsUtil.spoofMeta(bukkitFeet);

        mainHand = CraftItemStack.asNMSCopy(bukkitMainHand);
        offHand = CraftItemStack.asNMSCopy(bukkitOffHand);
        head = CraftItemStack.asNMSCopy(bukkitHead);
        chest = CraftItemStack.asNMSCopy(bukkitChest);
        legs = CraftItemStack.asNMSCopy(bukkitLegs);
        feet = CraftItemStack.asNMSCopy(bukkitFeet);

        List<Pair<EnumItemSlot, ItemStack>> items = List.of(
                Pair.of(EnumItemSlot.MAINHAND, mainHand),
                Pair.of(EnumItemSlot.OFFHAND, offHand),
                Pair.of(EnumItemSlot.HEAD, head != null ? head : ItemStack.b),
                Pair.of(EnumItemSlot.CHEST, chest != null ? chest : ItemStack.b),
                Pair.of(EnumItemSlot.LEGS, legs != null ? legs : ItemStack.b),
                Pair.of(EnumItemSlot.FEET, feet != null ? feet : ItemStack.b)
        );

        nmsViewer.playerConnection.sendPacket(new PacketPlayOutEntityEquipment(
                nmsTarget.getId(),
                items
        ));
    }

    public org.bukkit.inventory.ItemStack spoofMeta(org.bukkit.inventory.ItemStack item){
        if(item == null){
            return new org.bukkit.inventory.ItemStack(Material.AIR, 64);
        }
        if(!item.hasItemMeta()){
            return new org.bukkit.inventory.ItemStack(item.getType(), 64);
        }
        org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(item.getType(), 64);
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            itemStack = HeadUtil.getSkull(itemStack, HeadUtil.getSkullTexture(item));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        ItemMeta original = item.getItemMeta();
        if(!original.getEnchants().isEmpty()){
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&x&1&E&1&E&1&E&lV&x&2&2&2&2&2&2&le&x&2&6&2&6&2&6&ln&x&2&6&2&6&2&6&lo&x&2&6&2&6&2&6&lm"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static void sendDestroyPacket(Player viewer, Player target) {
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(target.getEntityId()));
    }

    public static void sendSpawnPacket(Player viewer, Player target) {
        EntityPlayer entityPlayer = ((CraftPlayer) target).getHandle();
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendHeadRotationPacket(Player viewer, Player target) {
        PacketPlayOutEntityHeadRotation headPacket = new PacketPlayOutEntityHeadRotation(
                ((CraftPlayer) target).getHandle(),
                (byte) ((target.getLocation().getYaw() % 360.0F) * 256.0F / 360.0F)
        );
        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
                target.getEntityId(),
                (byte) ((target.getLocation().getYaw() % 360.0F) * 256.0F / 360.0F),
                (byte) ((target.getLocation().getPitch() % 360.0F) * 256.0F / 360.0F),
                true
        );
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(headPacket);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(lookPacket);
    }

    public static void sendInvisibilityPacket(Player viewer, Player target, boolean invis) {
        EntityPlayer entityPlayer = ((CraftPlayer) target).getHandle();
        DataWatcher watcher = entityPlayer.getDataWatcher();

        byte flags = watcher.get(DataWatcherRegistry.a.a(0));

        if (invis) {
            flags |= 0x20;
        } else {
            flags &= ~0x20;
        }

        watcher.set(DataWatcherRegistry.a.a(0), flags);

        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(
                target.getEntityId(),
                watcher,
                true
        );
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }
}