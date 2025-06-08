package org.dimasik.antiesp;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class NMSUtil {
    public static boolean canSee(Player viewer, Player target) {
        if (viewer.hasPermission("antiesp.bypass")){
            return true;
        }

        if (isBehind(viewer, target) && viewer.getLocation().distance(target.getLocation()) >= 1.5) {
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

        List<Pair<EnumItemSlot, ItemStack>> items = List.of(
                Pair.of(EnumItemSlot.MAINHAND, nmsTarget.getItemInMainHand()),
                Pair.of(EnumItemSlot.OFFHAND, nmsTarget.getItemInOffHand()),
                Pair.of(EnumItemSlot.HEAD, nmsTarget.getEquipment(EnumItemSlot.HEAD)),
                Pair.of(EnumItemSlot.CHEST, nmsTarget.getEquipment(EnumItemSlot.CHEST)),
                Pair.of(EnumItemSlot.LEGS, nmsTarget.getEquipment(EnumItemSlot.LEGS)),
                Pair.of(EnumItemSlot.FEET, nmsTarget.getEquipment(EnumItemSlot.FEET))
        );

        nmsViewer.playerConnection.sendPacket(new PacketPlayOutEntityEquipment(
                nmsTarget.getId(),
                items
        ));
    }
}