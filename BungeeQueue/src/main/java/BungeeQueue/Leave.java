package BungeeQueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Leave extends Command {
    private QueueMain main;

    public Leave(QueueMain main){
        super("leavequeue");
        this.main = main;
    }

    public void execute(CommandSender sender, String[] args){
        if(sender instanceof ProxiedPlayer){
            main.leaveQueue(main.getProxiedPlayer(sender.getName()));
            main.removeFromQueues(main.getProxiedPlayer(sender.getName()));
        } else{
            sender.sendMessage(new TextComponent(ChatColor.RED + "Only a player can do this!"));
        }
    }


}
