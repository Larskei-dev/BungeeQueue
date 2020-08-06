package BungeeQueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.Console;

public class Pause extends Command {
    private QueueMain main;

    public Pause(QueueMain main){
        super("pause");
        this.main = main;
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("bungeequeue.pause")) {

            if (args.length == 0) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "You must enter a server name to pause"));
                return;
            }
            if (args.length > 0) {
                if (main.checkIfServerNameValid(args[0])) {
                    main.pauseToggler(args[0]);
                    if(main.isPaused(args[0])){
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6Queue&7] &aYou have &2&lPaused &athe queue for " + args[0])));
                        return;
                    }
                    else {
                        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7[&6Queue&7] &aYou have &c&lUnPaused &athe queue for " + args[0])));
                        return;
                    }
                }
            }
        } else{
            sender.sendMessage(new TextComponent(ChatColor.RED + "You don't have permission to do this!"));
            return;
        }
    }

}
