package com.massivecraft.factions.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * This class implements an easy way to map
 * config values like: materials, lore, name, color
 * to a respective ItemStack, also has utility methods
 * to merge, clone, etc
 */
public class SimpleItem {
    private String name;
    private List<String> lore;
    private Material material;
    private DyeColor color;
    private boolean enchant;
    private short data;
    private int slot;

    SimpleItem(Builder builder) {
        this.name = builder.name;
        this.lore = builder.lore;
        this.material = builder.material;
        this.color = builder.color;
        this.enchant = builder.enchant;
        this.data = builder.data;
        this.slot = builder.slot;
    }

    public SimpleItem(SimpleItem item) {
        this.name = item.name;
        this.lore = item.lore;
        this.material = item.material;
        this.color = item.color;
        this.enchant = item.enchant;
        this.data = item.data;
        this.slot = item.slot;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ItemStack get() {
        if (isValid()) {
            ItemStack itemStack = new ItemStack(material, 1, data);
            ItemMeta meta = itemStack.getItemMeta();

            if (name != null) {
                meta.setDisplayName(name);
            }
            // Empty list if not specified
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            if (enchant) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
            }

            // If a user places a color, they should be expected to put a colorable object
            if (color != null) {
                // ItemStack.setData() does not work :(
                itemStack.setDurability(color.getWoolData());
            }

            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    // All non null values in 'from' will be merged into this ItemGUI
    public void merge(SimpleItem from) {
        if (from.material != null) {
            material = from.material;
        }
        if (from.name != null) {
            name = from.name;
        }
        if (from.color != null) {
            color = from.color;
        }
        if (!from.lore.isEmpty()) {
            lore = from.lore;
        }
        enchant = from.enchant;
        data = from.data;
        slot = from.slot;
    }

    public boolean isValid() {
        // For an ItemStack to be built this class needs the material, if more information is available then it will be used
        return material != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public boolean isEnchant() {
        return enchant;
    }

    public void setEnchant(boolean enchant) {
        this.enchant = enchant;
    }

    public short getData() {
        return data;
    }

    public void setData(short data) {
        this.data = data;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        if (slot < 0 || slot > 53) {
            throw new IllegalArgumentException("Slot must range between 0 and 53");
        }
        this.slot = slot;
    }

    public static class Builder {
        private Material material;
        private String name;
        private List<String> lore;
        private DyeColor color;
        private boolean enchant;
        private short data;
        private int slot = -1;

        private Builder() {
        }

        public Builder setColor(DyeColor color) {
            this.color = color;
            return this;
        }

        public Builder setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setLore(String... lore) {
            return setLore(Arrays.stream(lore).toList());
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public Builder setEnchant(boolean enchant) {
            this.enchant = enchant;
            return this;
        }

        public Builder setData(short data) {
            this.data = data;
            return this;
        }

        public Builder atSlot(int slot) {
            if (slot < 0 || slot > 53) {
                throw new IllegalArgumentException("Slot must range between 0 and 53");
            }
            this.slot = slot;
            return this;
        }

        public SimpleItem build() {
            return new SimpleItem(this);
        }
    }
}
