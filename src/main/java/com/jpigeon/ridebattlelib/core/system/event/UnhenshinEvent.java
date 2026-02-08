package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 解除变身事件
 * <p>
 * 用于解除变身时播放音效等
 */
public class UnhenshinEvent extends Event {
    private final Player player;
    private final Identifier riderId;
    private final Identifier formId;
    private final boolean isPenalty;

    public UnhenshinEvent(Player player, Identifier riderId, Identifier formId, boolean isPenalty) {
        this.player = player;
        this.riderId = riderId;
        this.formId = formId;
        this.isPenalty = isPenalty;
    }

    public static class Pre extends UnhenshinEvent implements ICancellableEvent {
        public Pre(Player player, Identifier riderId, Identifier formId, boolean isPenalty) {
            super(player, riderId, formId, isPenalty);
        }
    }

    public static class Post extends UnhenshinEvent {
        public Post(Player player, Identifier riderId, Identifier formId, boolean isPenalty) {
            super(player, riderId, formId, isPenalty);
        }
    }

    public Player getPlayer() { return player; }
    public Identifier getRiderId() { return riderId; }
    public Identifier getFormId() { return formId; }
    public boolean isPenalty() { return isPenalty; }
}
