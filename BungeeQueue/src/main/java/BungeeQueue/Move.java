package BungeeQueue;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class Move extends Command {
    private QueueMain main;
    boolean onPause = false;

    public Move(QueueMain main) {
        super("move");
        this.main = main;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "You must enter a server name to move to"));
            return;
        }
        if(args.length == 2){
            main.initiateMove(main.getProxiedPlayer(args[1]), args[0].toString());
            return;
        }


        if (main.checkIfServerNameValid(args[0])) {
            if (sender instanceof ProxiedPlayer) {
                getPlayer(sender.toString()).sendMessage(new TextComponent(ChatColor.GREEN + "Connecting... Please wait"));
                main.initiateMove(getPlayer(sender.getName()), args[0]);
                return;
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

    public void setPause(Boolean pauseStat) {
        onPause = pauseStat;
    }


}
