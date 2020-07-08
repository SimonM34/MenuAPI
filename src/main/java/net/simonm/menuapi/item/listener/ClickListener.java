package net.simonm.menuapi.item.listener;

import net.simonm.menuapi.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface ClickListener {
    void onClick(Menu menu, Player player, ItemStack item);
    void onClick(Menu menu, Player player, ItemStack item, ItemMeta meta);
}
