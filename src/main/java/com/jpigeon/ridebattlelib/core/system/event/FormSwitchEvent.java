package com.jpigeon.ridebattlelib.core.system.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class FormSwitchEvent extends Event {
    private final Player player;
    private final ResourceLocation oldFormId;
    private final ResourceLocation newFormId;

    public FormSwitchEvent(Player player, ResourceLocation oldFormId, ResourceLocation newFormId) {
        this.player = player;
        this.oldFormId = oldFormId;
        this.newFormId = newFormId;
    }

    public static class Pre extends FormSwitchEvent {
        private boolean canceled = false;

        public Pre(Player player, ResourceLocation oldFormId, ResourceLocation newFormId) {
            super(player, oldFormId, newFormId);
        }

        public boolean isCancelable() {
            return true;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    public static class Post extends FormSwitchEvent {
        public Post(Player player, ResourceLocation oldFormId, ResourceLocation newFormId) {
            super(player, oldFormId, newFormId);
        }
    }


    public Player getPlayer() {
        return player;
    }

    public ResourceLocation getOldFormId() {
        return oldFormId;
    }

    public ResourceLocation getNewFormId() {
        return newFormId;
    }
}
