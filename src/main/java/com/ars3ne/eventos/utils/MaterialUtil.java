package com.ars3ne.eventos.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/*
    Code extracted from https://github.com/henrysaantos/inventory-api/blob/main/src/main/java/com/henryfabio/minecraft/inventoryapi/item/util/MaterialUtil.java
*/

public final class MaterialUtil {

    public static ItemStack convertFromLegacy(String materialName, int damage) {
        try {
            return new ItemStack(Material.getMaterial(materialName), 1, (short) damage);
        } catch (Exception error) {
            final Material material = Material.valueOf("LEGACY_" + materialName);

            return new ItemStack(material, 1, (short) damage);
        }
    }

}
