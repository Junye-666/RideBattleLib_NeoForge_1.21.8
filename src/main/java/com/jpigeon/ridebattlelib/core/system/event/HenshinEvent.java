package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 变身事件
 */
public class HenshinEvent extends Event {
    private final Player player;
    private final Identifier riderId;
    private final Identifier formId;

    public HenshinEvent(Player player, Identifier riderId, Identifier formId) {
        this.player = player;
        this.riderId = riderId;
        this.formId = formId;
    }

    public static class Pre extends HenshinEvent implements ICancellableEvent {
        public Pre(Player player, Identifier riderId, Identifier formId) {
            super(player, riderId, formId);
        }
    }

    public static class Post extends HenshinEvent {
        public Post(Player player, Identifier riderId, Identifier formId) {
            super(player, riderId, formId);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Identifier getRiderId() {
        return riderId;
    }

    public Identifier getFormId() {
        return formId;
    }
}
