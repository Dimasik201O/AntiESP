package org.dimasik.antiesp;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadUtil {

    public static String getSkullTexture(ItemStack head) {
        if (head == null || !(head.getItemMeta() instanceof SkullMeta)) {
            return "";
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = null;

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profile = (GameProfile) profileField.get(headMeta);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (profile == null) {
            return "";
        }

        Property textureProperty = profile.getProperties().get("textures").iterator().next();
        if (textureProperty == null) {
            return "";
        }

        return textureProperty.getValue();
    }

    public static ItemStack getSkull(ItemStack head, String base64) {
        ItemStack itemStack = head.clone();
        if (base64.isEmpty()) {
            return itemStack;
        }

        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", base64));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        itemStack.setItemMeta(headMeta);
        return itemStack;
    }
}
