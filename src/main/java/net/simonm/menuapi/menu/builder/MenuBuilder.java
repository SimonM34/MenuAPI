package net.simonm.menuapi.menu.builder;

import net.simonm.menuapi.item.Item;
import net.simonm.menuapi.item.listener.ClickListener;
import net.simonm.menuapi.item.listener.builder.ClickListenerBuilder;
import net.simonm.menuapi.item.loader.ItemLoader;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class MenuBuilder {
    private final String title;
    private final int slots;
    private final List<Integer> specialSlots;
    private final Set<ClickListenerBuilder> items;

    public MenuBuilder(String title, int slots) {
        this(title, slots, new ArrayList<>());
    }
    public MenuBuilder(String title, int slots, List<Integer> specialSlots) {
        this(title, slots, specialSlots, new HashSet<>());
    }
    public MenuBuilder(String title, int slots, List<Integer> specialSlots, Set<ClickListenerBuilder> items) {
        this.title = title;
        this.slots = slots;
        this.specialSlots = specialSlots;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }
    public int getSlots() {
        return slots;
    }
    public List<Integer> getSpecialSlots() {
        return specialSlots;
    }
    public Set<ClickListenerBuilder> getItems() {
        return items;
    }

    public void loadItems(ConfigurationSection config, String path, ItemLoader itemLoader) {
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
                        items.add(ClickListenerBuilder.of(item, slot, listener));
                        return;
                    }
                }
                items.add(ClickListenerBuilder.of(item, slot));
            });
        }
    }
}
