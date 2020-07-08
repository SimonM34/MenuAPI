package net.simonm.menuapi.hooks;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.simonm.menuapi.item.Item;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseHook {
    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();

    public Item getHead(String id) {
        ItemStack head = headDatabaseAPI.getItemHead(id);
        return head != null ? Item.of(head) : null;
    }
}
