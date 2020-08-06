package BungeeQueue;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;

public class Move extends Command {
    private QueueMain main;

    public Move(QueueMain main) {
        super("join");
        this.main = main;
    }


    public void execute(CommandSender sender, String[] args) {


        if (args.length == 0) {
                sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "You must enter a server name to move to"));
                return;
            }
            if (main.checkIfServerNameValid(args[0])) {
                if (sender instanceof ProxiedPlayer) {
                    if(main.getProxy().getServerInfo(args[0]).canAccess(sender)) {
                        if(main.isPaused(args[0])){
                            if(main.pausedPlayers.contains(main.getProxiedPlayer(sender.getName()))){
                                main.removeFromQueues(main.getProxiedPlayer(sender.getName()));
                                return;
                            }
                            main.addPlayerToPauseList(getPlayer(sender.getName()), args[0]);
                            return;
                        }
                        main.initiateMove(getPlayer(sender.getName()), args[0]);
                        return;
                    }
                }
            } else if (!main.checkIfServerNameValid(args[0])) {
                sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "Invalid Server Name"));
                return;


            } else if (sender instanceof Server) {
                sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "You have to be a player to use this command"));
                return;
            }

            if (!main.checkIfServerNameValid(args[0]) && sender instanceof ProxiedPlayer) {

                sender.sendMessage(new TextComponent(ChatColor.RED + "That server does not exist"));
                return;
            }
    }


    public ProxiedPlayer getPlayer(String name) {
        return main.getProxiedPlayer(name);
    }

}


