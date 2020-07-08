package net.simonm.menuapi.item;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.arcaniax.hdb.object.head.Head;
import net.simonm.menuapi.MenuAPI;
import net.simonm.menuapi.item.listener.ClickListener;
import net.simonm.menuapi.item.listener.builder.ClickListenerBuilder;
import net.simonm.menuapi.item.loader.ItemLoader;
import net.simonm.menuapi.menu.Menu;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class Item implements Cloneable {
    private final ItemStack item;
    private final ItemMeta meta;

    private Item(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }
    private Item(ItemStack item, ItemMeta meta) {
        this.item = item;
        this.meta = meta;
    }

    public String name() {
        return meta.getDisplayName();
    }
    public Item name(String name) {
        if (name != null) {
            meta.setDisplayName(name);
        }
        return this;
    }
    public boolean hasName() {
        return meta.hasDisplayName();
    }

    public List<String> lore() {
        return meta.getLore();
    }
    public Item lore(String line) {
        if (line != null) {
            List<String> lore = lore();
            if (lore == null)
                meta.setLore(lore = new ArrayList<>());
            lore.add(line);
        }
        return this;
    }
    public Item lore(List<String> lore) {
        if (lore != null)
            meta.setLore(lore);
        return this;
    }
    public boolean hasLore() {
        return meta.hasLore();
    }

    public Map<Enchantment, Integer> enchants() {
        return item.getEnchantments();
    }
    public Item enchant(Enchantment enchantment, int level) {
        item.addEnchantment(enchantment, level);
        return this;
    }
    public Item enchantUnsafe(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }
    public boolean hasEnchant(Enchantment enchantment) {
        return item.getEnchantments().containsKey(enchantment);
    }
    public boolean hasEnchants() {
        return !item.getEnchantments().isEmpty();
    }

    public Set<ItemFlag> flags() {
        return meta.getItemFlags();
    }
    public Item flag(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return null;
    }
    public boolean hasFlag(ItemFlag itemFlag) {
        return meta.hasItemFlag(itemFlag);
    }

    public OfflinePlayer skull() {
        Validate.isTrue(isSkull(), "Item is not a skull");
        return ((SkullMeta) meta).getOwningPlayer();
    }
    public Item skull(OfflinePlayer player) {
        Validate.isTrue(isSkull(), "Item is not a skull");
        ((SkullMeta) meta).setOwningPlayer(player);
        return this;
    }
    public boolean isSkull() {
        return meta instanceof SkullMeta;
    }

    public Item glow() {
        enchantUnsafe(Enchantment.CHANNELING, 1); // who tf uses this
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }
    public boolean hasGlow() {
        return hasEnchant(Enchantment.CHANNELING) && hasFlag(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemStack create() {
        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Object clone() {
        return new Item(item.clone(), meta.clone());
    }

    public static Item of(ItemStack item) {
        Validate.notNull(item, "Item cannot be null");
        return new Item(item);
    }
    public static Item of(Material material) {
        Validate.notNull(material, "Material cannot be null");
        return new Item(new ItemStack(material));
    }
    public static Item fromConfig(ConfigurationSection config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null)
            return null;

        return fromConfig(config);
    }
    public static Item fromConfig(ConfigurationSection section) {
        Item item = fromMaterial(section);
        if (section.isString("name")) {
            item.name(section.getString("name"));
        }
        if (section.isString("lore")) {
            item.lore(section.getString("lore"));
        }
        if (section.isList("lore")) {
            item.lore(section.getStringList("lore"));
        }
        if (section.isList("flags")) {
            item.flag(section.getStringList("flags").stream()
                    .map(Item::parseItemFlag)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toArray(ItemFlag[]::new));
        }
        if (section.isConfigurationSection("enchantments")) {
            section.getConfigurationSection("enchantments").getKeys(false).forEach(key -> {
                Optional<Enchantment> enchantment = parseEnchantment(key);
                if (!enchantment.isPresent())
                    return;

                if (!section.isInt(key))
                    return;
                item.enchant(enchantment.get(), section.getInt("unsafe_enchantments." + key));
            });
        }
        if (section.isConfigurationSection("unsafe_enchantments")) {
            section.getConfigurationSection("unsafe_enchantments").getKeys(false).forEach(key -> {
                Optional<Enchantment> enchantment = parseEnchantment("unsafe_enchantments." + key);
                if (!enchantment.isPresent())
                    return;

                if (!section.isInt("unsafe_enchantments." + key))
                    return;
                item.enchantUnsafe(enchantment.get(), section.getInt("unsafe_enchantments." + key));
            });
        }
        if (section.isBoolean("glow")) {
            if (section.getBoolean("glow")) {
                item.glow();
            }
        }
        if (section.isString("head_owner")) {
            UUID uuid = UUID.fromString(section.getString("head_owner"));
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            item.skull(player);
        }
        return item;
    }
    public static OptionalInt parseInteger(String string) {
        Validate.notNull(string, "Integer cannot be null");
        try {
            return OptionalInt.of(Integer.parseInt(string));
        } catch (IllegalArgumentException e) {
            return OptionalInt.empty();
        }
    }
    public static Optional<Material> parseMaterial(String string) {
        Validate.notNull(string, "Material name cannot be null");
        try {
            return Optional.of(Material.valueOf(string.toUpperCase().replace("_", "")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    public static Optional<ItemFlag> parseItemFlag(String string) {
        Validate.notNull(string, "Item flag name cannot be null");
        try {
            return Optional.of(ItemFlag.valueOf(string.toUpperCase().replace("_", "")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    @Deprecated
    public static Optional<Enchantment> parseEnchantment(String string) {
        Validate.notNull(string, "Enchantment name cannot be null");
        try {
            return Optional.of(Enchantment.getByName(string.toUpperCase().replace("_", "")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    private static Item fromMaterial(ConfigurationSection section) {
        if (MenuAPI.getInstance().isUsingHeadDatabase() && section.isString("head_id")) {
            return MenuAPI.getInstance().getHeadDatabaseHook().getHead(section.getString("head_id"));
        }
        Optional<Material> optionalMaterial = parseMaterial(section.getString("material", null));
        return optionalMaterial.map(Item::of).orElse(null);
    }
}
