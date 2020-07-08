package net.simonm.menuapi.menu;

import net.simonm.menuapi.MenuAPI;
import net.simonm.menuapi.item.Item;
import net.simonm.menuapi.item.listener.ClickListener;
import net.simonm.menuapi.item.listener.builder.ClickListenerBuilder;
import net.simonm.menuapi.item.loader.ItemLoader;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.OptionalInt;

public interface Menu extends InventoryHolder {
    default Menu add(ClickListenerBuilder listenerBuilder) {
        return add(listenerBuilder, listenerBuilder.getSlot());
    }
    default Menu add(ClickListenerBuilder listenerBuilder, int slot) {
        return add(slot, listenerBuilder.getItem(), listenerBuilder.getListener());
    }
    default Menu add(int slot, Item item, ClickListener listener) {
        return add(slot, item.create(), listener);
    }
    default Menu add(int slot, ItemStack item, ClickListener listener) {
        if (listener != null)
            MenuAPI.getInstance().addListener(item, listener);
        return add(slot, item);
    }
    default Menu add(int slot, Item item) {
        return add(slot, item.create());
    }
    default Menu add(int slot, ItemStack item) {
        Validate.notNull(item, "Item cannot be null");
        Validate.isTrue(slot >= 0, "Slot cannot be below 0");
        getInventory().setItem(slot, item);
        return this;
    }

    default void loadItems(ConfigurationSection config, String path, ItemLoader itemLoader) {
        ConfigurationSection section = config.getConfigurationSection(path + ".items");
        if (section != null) {
            section.getKeys(false).forEach(key -> {
                OptionalInt optionalInt = Item.parseInteger(key);
                if (!optionalInt.isPresent())
                    return;

                Item item = Item.fromConfig(section, "." + key);
                if (item == null)
                    return;

                int slot = optionalInt.getAsInt();
                if (itemLoader != null) {
                    ClickListener listener = itemLoader.onLoad(section, item);
                    if (listener != null) {
                        add(slot, item, listener);
                        return;
                    }
                }
                add(slot, item);
            });
        }
    }
}
