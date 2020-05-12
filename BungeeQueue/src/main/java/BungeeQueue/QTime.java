package BungeeQueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class QTime extends Command {

    private QueueMain main;

    public QTime(QueueMain main) {
        super("queuetime");
        this.main = main;
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0){
            sender.sendMessage(new TextComponent(ChatColor.RED + "You must enter a server name to see the Queue time"));

        }
        if (!main.checkIfServerNameValid(args[0])){
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid server name"));
        }
        if (main.checkIfServerNameValid(args[0])) {
            sender.sendMessage(new TextComponent(ChatColor.GREEN + " Queue time for " + args[0] + " is currently " + main.getQueueTime(args[0], false) + " seconds"));
            return;
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + " That server does not exist or you have not specified a server"));
            return;
        }
    }

}
