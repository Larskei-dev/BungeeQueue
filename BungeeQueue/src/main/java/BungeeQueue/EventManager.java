package BungeeQueue;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventManager implements Listener {
    private QueueMain main;
    public EventManager(QueueMain main){
        this.main = main;
    }


    @EventHandler
    public void leaveEvent(PlayerDisconnectEvent event){
        main.removeFromQueues(event.getPlayer());
    }
}
