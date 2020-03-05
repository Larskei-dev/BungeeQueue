package BungeeQueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public final class QueueMain extends Plugin {

    int inQueue = 0;
    int inDonorQueue = 0;
    boolean isDonor = false;
    public File file;
    public Configuration config;
    ArrayList<List<String>> listOfGroups = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getProxy().getPluginManager().registerCommand(this, new Move(this));
        getProxy().getPluginManager().registerCommand(this, new QueueTime(this));
        getProxy().getPluginManager().registerCommand(this, new QTime(this));
        file = new File(getDataFolder(), "Config.yml");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                    config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                    config.set("#", "The multiplier below is what determines wait time, the larger the multiplier the longer the queue gets exponentially per player in queue");
                    config.set("##", "if 2 players are in queue 2nd player has Multiplier * 2 to wait before they enter the server, if your server is crashing because of players joining to fast try setting the multiplier larger");
                    config.set("Multiplier", 1);
                    config.set("###", "This decides whether or not to factor server size in (default it false)");
                    config.set("####", "If true new equation is (AmountOfPlayersInQueue * Multiplier) + AmountOfPlayersOnServer");
                    config.set("#####:", "^^^ Follows normal order of operations ^^^");
                    config.set("IsServerSizeCalculated", false);
                    config.set("DonorWaitMultiplier", 1);
                    config.set("JoinedQueueMessage", "Thank you for queueing");
                    config.set("######", "The message that tells you that you are in queue is split into 3 different messages in the config so it will do message1 + the queue time + message2 + howManyPlayersAreInQueue + message3");
                    config.set("EnteringNonDonorQueueMessage1", "You are in queue, you will join in ");
                    config.set("EnteringNonDonorQueueMessage2", " seconds there are ");
                    config.set("EnteringNonDonorQueueMessage3", " Players in queue.");
                    config.set("########", "This is the Donor queue message format message1 + queueTime in seconds + message2");
                    config.set("EnterningDonorQueueMessage1", "You are in donor queue, you will join in ");
                    config.set("EnteringDonorQueueMessage2", " seconds");
                    config.set("ThankYouMessage", "Thank you for queueing");
                    config.set("SkippingQueueMessage", "You have skipped the queue");
                    config.set("#########", "enabling this option means when someone with bungeequeue.kick joins a full server it will kick a random player without the permission bungeequeue.nokick to the specified fallbackserver");
                    config.set("kickNonDonorPlayers", false);
                    config.set("FallBackServer", "lobby");
                    config.set("##########", "if a server is over this many players it will begin to kick players for players who have bungeequeue.kick permission");
                    config.set("MaximumSize", 100);
                    config.set("FullServerCannotConnectMessage", "The server you are trying to connect to is full.");
                    config.set("###########", "Enables server groups, server groups are like fallback servers however they are relative to a group so if you have for example Group1 and it has Server1 and Server2 in it, when a player is kicked from server1 it will push them onto server2 or any server that has their player amount below what the set maximum is, if a server cannot be found it will connect them to the defined fallback server, regardless if the player has the kick permission or not, it will not try to kick other players when moving them after being kicked from another server this is to reduce the chance of players constantly being kicked and moved around possibly causing lag");
                    config.set("############", "This feature supports unlimted groups currently of unlimited size, you must SET how MANY groups there are in total, KEEP IN MIND COMPUTERS START COUNTING AT 0 SO if you have 5 groups you should put 4 below, EVERY server you have must be in a group EXCEPT for the fallback server if enabled, if it is not IT WILL throw an error");
                    config.set("AmountOfGroups", 0);
                    config.set("ServerGrouping", false);
                    List<String> list = new ArrayList<>();
                    list.add("exampleServer");
                    list.add("exampleServer2");
                    list.add("YourServerNameHere");
                    config.set("Group0", list);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(file.exists() && config.getBoolean("ServerGrouping")){
            for(int i = 0; i <= config.getInt("AmountOfGroups"); i++){
                listOfGroups.add(config.getStringList("Group" + i));
            }
        }
        System.out.println("------------------------------");
        System.out.println(ChatColor.GREEN + "BungeeQueue Enabled");
        System.out.println(ChatColor.GREEN + "Version 2.0.7");
        System.out.println(ChatColor.GREEN + "By Larskei @Larskei#0001");
        System.out.println(ChatColor.GREEN + "Designed for use in Waterfall/Bungeecord 1.14.4");
        System.out.println("------------------------------");
    }




    public void initiateMove(ProxiedPlayer player, String serverTarget) {

        if (player.hasPermission("bungeequeue.skip")) {

            if (player.hasPermission("bungeequeue.kick")) {

                if (getProxy().getServerInfo(serverTarget).getPlayers().size() >= config.getInt("MaximumSize")) {

                    if (serverKick(serverTarget)) {

                        player.connect(getProxy().getServerInfo(serverTarget));

                    } else {

                        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("FullServerCannotConnectMessage"))));
                    }
                }
            } else {

                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("SkippingQueueMessage"))));
                player.connect(getProxy().getServerInfo(serverTarget));
            }
        }

        if (player.hasPermission("bungeequeue.donor")) {

            if (player.hasPermission("bungeequeue.kick") && getProxy().getServerInfo(serverTarget).getPlayers().size() >= config.getInt("MaximumSize")) {

                if (serverKick(serverTarget)) {

                    isDonor = true;
                    inDonorQueue++;
                    int queueTime = getServerSize(serverTarget) / 1000;

                    player.sendMessage(new TextComponent(ChatColor.GREEN + config.getString("ThankYouMessage")));
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("EnteringDonorQueueMessage1")) + getServerSize(serverTarget) + ChatColor.translateAlternateColorCodes('&', config.getString("EnteringDonorQueueMessage2"))));
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            movePlayer(player, serverTarget);
                            inDonorQueue = inDonorQueue - 1;

                        }
                    };
                    getProxy().getScheduler().schedule(this, runnable, getServerSize(serverTarget), TimeUnit.SECONDS);
                }

                else if (!serverKick(serverTarget)) {

                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("FullServerCannotConnectMessage"))));
                }

            }

            else {

                isDonor = true;
                inDonorQueue++;
                int queueTime = getServerSize(serverTarget) / 1000;

                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("ThankYouMessage"))));
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("EnteringDonorQueueMessage1")) + getServerSize(serverTarget) + ChatColor.translateAlternateColorCodes('&', config.getString("EnteringDonorQueueMessage2"))));
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        movePlayer(player, serverTarget);
                        inDonorQueue = inDonorQueue - 1;

                    }
                };
                getProxy().getScheduler().schedule(this, runnable, getServerSize(serverTarget), TimeUnit.SECONDS);
            }

        }
        if (!player.hasPermission("bungeequeue.donor") && !player.hasPermission("bungeequeue.skip")) {
            if (player.hasPermission("bungeequeue.kick") && getProxy().getServerInfo(serverTarget).getPlayers().size() >= config.getInt("MaximumSize")) {
                if (serverKick(serverTarget)) {
                    player.connect(getProxy().getServerInfo(serverTarget));
                } else {
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("FullServerCannotConnectMessage"))));
                }
            } else {

                inQueue++;
                int queueTime = getServerSize(serverTarget) / 1000;
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("JoinedQueueMessage"))));
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("EnteringNonDonorQueueMessage1")) + queueTime + ChatColor.translateAlternateColorCodes('&', config.getString("EnteringNonDonorQueueMessage2")) + inQueue + ChatColor.translateAlternateColorCodes('&', config.getString("EnteringNonDonorQueueMessage3"))));
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        movePlayer(player, serverTarget);
                        inQueue = inQueue - 1;

                    }
                };
                getProxy().getScheduler().schedule(this, runnable, getServerSize(serverTarget), TimeUnit.SECONDS);
            }
        }
    }




    public boolean serverKick(String serverTarget) {
        for (ProxiedPlayer player : getProxy().getServerInfo(serverTarget).getPlayers()) {
            if (!player.hasPermission("bungeequeue.nokick")) {
                if(config.getBoolean("ServerGrouping")){
                    moveGroups(player);
                    return true;

                }else{
                    player.connect(getProxy().getServerInfo(config.getString("FallBackServer")));
                    return true;
                }
            }
        }
        return false;
    }

    public String getGroup(ProxiedPlayer player){
        for(int i = 0; i <= listOfGroups.size(); i++){
            for(int index = 0; index <= listOfGroups.get(i).size(); index++){
                if(player.getServer().toString().equalsIgnoreCase(listOfGroups.get(i).get(index).toString())){
                    String indexes = String.valueOf(i) + String.valueOf(index);
                    return indexes;
                }

            }



        }
        return null;

    }

    public void moveGroups(ProxiedPlayer player){
        for(int i = 0; i <= listOfGroups.get(i).size(); i++){
            if(!listOfGroups.get(Integer.parseInt(getGroup(player).substring(0, 1))).get(i).equalsIgnoreCase(player.getServer().toString()) && getProxy().getServerInfo(listOfGroups.get(Integer.parseInt(getGroup(player).substring(0, 1))).get(i)).getPlayers().size() < config.getInt("MaximumSize")){
                player.connect(getProxy().getServerInfo(listOfGroups.get(Integer.parseInt(getGroup(player).substring(0, 1))).get(i).toString()));
                return;
            }
        }
        player.connect(getProxy().getServerInfo(config.getString("FallBackServer")));
        return;
    }




    //---------------------------------------------------------------------------------------------------------------------------------


    public int getServerSize(String serverName) {
        if (checkIfServerNameValid(serverName)) {
            if (!config.getBoolean("IsServerSizeCalculated")) {
                if (isDonor) {
                    int donorConfiggedWait = config.getInt("DonorWaitMultiplier");
                    return (donorConfiggedWait * inDonorQueue) + getProxy().getServerInfo(serverName).getPlayers().size();
                } else {
                    int configgedWait = config.getInt("WaitMultiplier");
                    return configgedWait * inQueue;
                }
            }
            if (config.getBoolean("IsServerSizeCalculated")) {
                if (isDonor) {
                    int donorConfiggedWait = config.getInt("DonorWaitMultiplier");
                    return (donorConfiggedWait * inDonorQueue) + getProxy().getServerInfo(serverName).getPlayers().size();
                } else {
                    int configgedWait = config.getInt("WaitMuliplier");
                    return (inQueue * configgedWait) + getProxy().getServerInfo(serverName).getPlayers().size();
                }
            }


        }

        return 20;
    }


    public void movePlayer(ProxiedPlayer player, String target) {

        player.connect(ProxyServer.getInstance().getServerInfo(target));
        System.out.println(ChatColor.GREEN + "[BungeeQueue] " + player.getDisplayName() + " moved to " + target + " was in queue for " + getServerSize(target) + " seconds.");
    }


    public ProxiedPlayer getProxiedPlayer(String name) {
        return getProxy().getPlayer(name);
    }

    public boolean checkIfServerNameValid(String serverName) {
        if (getProxy().getServerInfo(serverName).getName().equalsIgnoreCase(serverName)) {
            return true;
        } else {
            return false;
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------
    //API
    //---------------------------------------------------------------------------------------------------------------------------------


    public int getQueueSize(){
        return inQueue;
    }
    public int getDonorQueueSize(){
        return inDonorQueue;
    }








    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("------------------------------");
        System.out.println("BungeeQueue Queue Shutting Down");
        System.out.println("------------------------------");
        System.out.println("[BungeeQueue] SHUTDOWN SUCCESSFUL");
    }
}
