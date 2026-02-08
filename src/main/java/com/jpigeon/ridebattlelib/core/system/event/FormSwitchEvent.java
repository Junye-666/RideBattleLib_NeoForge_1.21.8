package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 形态切换事件
 */
public class FormSwitchEvent extends Event {
    private final Player player;
    private final Identifier oldFormId;
    private final Identifier newFormId;

    public FormSwitchEvent(Player player, Identifier oldFormId, Identifier newFormId) {
        this.player = player;
        this.oldFormId = oldFormId;
        this.newFormId = newFormId;
    }

    public static class Pre extends FormSwitchEvent implements ICancellableEvent {
        public Pre(Player player, Identifier oldFormId, Identifier newFormId) {
            super(player, oldFormId, newFormId);
        }
    }

    public static class Post extends FormSwitchEvent {
        public Post(Player player, Identifier oldFormId, Identifier newFormId) {
            super(player, oldFormId, newFormId);
        }
    }


    public Player getPlayer() {
        return player;
    }

    public Identifier getOldFormId() {
        return oldFormId;
    }

    public Identifier getNewFormId() {
        return newFormId;
    }
}
