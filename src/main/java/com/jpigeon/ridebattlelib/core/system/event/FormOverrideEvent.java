package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Collections;
import java.util.Map;

/**
 * 在变身前匹配形态时强制修改匹配形态的覆盖(相当于形态琐)
 */
public class FormOverrideEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final Map<Identifier, ItemStack> driverItems;
    private final Identifier currentForm;
    private Identifier overrideForm;

    public FormOverrideEvent(Player player, Map<Identifier, ItemStack> driverItems, Identifier currentForm) {
        this.player = player;
        this.driverItems = Collections.unmodifiableMap(driverItems);
        this.currentForm = currentForm;
        this.overrideForm = null;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 强制覆盖形态
     * @param overrideForm 变身时强制更改至的形态
     */
    public void overrideForm(Identifier overrideForm) {
        this.overrideForm = overrideForm;
    }

    public Map<Identifier, ItemStack> getDriverItems() {
        return driverItems;
    }

    public Identifier getCurrentForm() {
        return currentForm;
    }

    public Identifier getOverrideForm() {
        return overrideForm;
    }
}