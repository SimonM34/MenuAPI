package net.simonm.menuapi;

import net.simonm.menuapi.hooks.HeadDatabaseHook;
import net.simonm.menuapi.item.listener.ClickListener;
import net.simonm.menuapi.menu.Menu;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MenuAPI {
    private static final MenuAPI instance = new MenuAPI();
    private final Logger logger = Logger.getLogger("MenuAPI");
    private final Map<ItemStack, ClickListener> listeners = new HashMap<>();
    private boolean enabled;
    private HeadDatabaseHook headDatabaseHook;

    private MenuAPI() {
    }

    public void load(Plugin plugin) {
        if (headDatabaseHook != null && Bukkit.getPluginManager().isPluginEnabled("HeadDatabaseAPI")) {
            logger.info("Found HeadDatabaseAPI. Adding support...");
            headDatabaseHook = new HeadDatabaseHook();
        }
        if (!enabled) {
            registerListeners(plugin);
            logger.info("Registering listeners...");
            enabled = true;
        }
    }

    public static MenuAPI getInstance() {
        return instance;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public boolean isUsingHeadDatabase() {
        return headDatabaseHook != null;
    }
    public HeadDatabaseHook getHeadDatabaseHook() {
        return headDatabaseHook;
    }

    public ClickListener getListener(ItemStack item) {
        return listeners.get(item);
    }
    public void addListener(ItemStack item, ClickListener listener) {
        Validate.notNull(item, "Item cannot be null");
        Validate.notNull(listener, "Click Listener cannot be null");
        listeners.put(item, listener);
    }
    public void clearListeners() {
        listeners.clear();
    }

    public Menu createMenu(String title, int slots) {
        return new Menu() {
            private final Inventory inventory = Bukkit.createInventory(this, slots, title);

            @Override
            public Inventory getInventory() {
                return inventory;
            }
        };
    }

    private void registerListeners(Plugin plugin) {
        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvent(InventoryClickEvent.class, new Listener(){}, EventPriority.NORMAL, ($, rawEvent) -> {
            InventoryClickEvent event = (InventoryClickEvent) rawEvent;
            Inventory inventory = event.getClickedInventory();
            if (inventory == null)
                return;
            if (!(inventory.getHolder() instanceof Menu))
                return;

            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if (item == null)
                return;
            if (item.getType() == Material.AIR)
                return;

            ClickListener clickListener = MenuAPI.getInstance().getListener(item);
            if (clickListener != null) {
                Menu menu = (Menu) inventory.getHolder();
                if (item.hasItemMeta())
                    clickListener.onClick(menu, player, item, item.getItemMeta());
                clickListener.onClick(menu, player, item);
            }
        }, plugin);
    }
}
