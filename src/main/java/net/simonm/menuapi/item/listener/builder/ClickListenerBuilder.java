package net.simonm.menuapi.item.listener.builder;

import net.simonm.menuapi.item.Item;
import net.simonm.menuapi.item.listener.ClickListener;
import org.apache.commons.lang.Validate;

public class ClickListenerBuilder {
    private final Item item;
    private final int slot;
    private final ClickListener listener;

    private ClickListenerBuilder(Item item, int slot, ClickListener listener) {
        this.item = item;
        this.slot = slot;
        this.listener = listener;
    }

    public Item getItem() {
        return item;
    }
    public int getSlot() {
        return slot;
    }
    public ClickListener getListener() {
        return listener;
    }

    public static ClickListenerBuilder of(Item item, int slot, ClickListener listener) {
        Validate.notNull(item, "Item cannot be null");
        return new ClickListenerBuilder(item, slot, listener);
    }
    public static ClickListenerBuilder of(Item item, ClickListener listener) {
        return of(item, -1, listener);
    }
    public static ClickListenerBuilder of(Item item, int slot) {
        return of(item, slot, null);
    }
}
