package net.simonm.menuapi.item.loader;

import net.simonm.menuapi.item.Item;
import net.simonm.menuapi.item.listener.ClickListener;
import org.bukkit.configuration.ConfigurationSection;

public interface ItemLoader {
    ClickListener onLoad(ConfigurationSection section, Item item);
}
