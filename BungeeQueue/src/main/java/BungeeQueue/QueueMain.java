package BungeeQueue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
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
    public File fileLists;
    public Configuration listConfig;
    public Configuration config;
    ArrayList<ProxiedPlayer> waitingPlayers = new ArrayList<>();
    ArrayList<String> waitingPlayersTargets = new ArrayList<>();
    boolean isCheck = false;
    CommandSender sender;
    ArrayList<ProxiedPlayer> pausedPlayers = new ArrayList<>();
    ArrayList<String> pausedTargets = new ArrayList<>();
    ArrayList<String> pausedServers = new ArrayList<>();
    ArrayList<String> blockedServers = new ArrayList<>();


    @Override
    public void onEnable() {
        // Plugin startup logic
        getProxy().getPluginManager().registerCommand(this, new Move(this));
        getProxy().getPluginManager().registerCommand(this, new QueueTime(this));
        getProxy().getPluginManager().registerCommand(this, new QTime(this));
        getProxy().getPluginManager().registerCommand(this, new Pause(this));
        getProxy().getPluginManager().registerListener(this, new EventManager(this));
        file = new File(getDataFolder(), "Config.yml");
        fileLists = new File(getDataFolder(), "Lists.yml");
        try {
            if (!file.exists() || !fileLists.exists()) {
                file.getParentFile().mkdirs();
                fileLists.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                    fileLists.createNewFile();
                    listConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileLists);
                    List<String> blockedservers = new ArrayList<>();
                    blockedservers.add("ExampleServer");
                    blockedservers.add("ExampleServer2");
                    listConfig.set("BlockedServers", blockedservers);
                    List<String> pausedServers = new ArrayList<>();
                    pausedServers.add("AnotherExampleServer");
                    pausedServers.add("AnotherExampleAsWell");
                    listConfig.set("PausedServers", pausedServers);
                    List<String> maximumSizing = new ArrayList<>();
                    maximumSizing.add("ExampleServer1");
                    maximumSizing.add("ExampleServer2");
                    listConfig.set("MaximumServerSizeNames", maximumSizing);
                    List<Integer> maximumNumbers = new ArrayList<>();
                    maximumNumbers.add(100);
                    maximumNumbers.add(200);
                    listConfig.set("MaximumServerSizes", maximumNumbers);
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(listConfig, fileLists);
                    config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                    config.set("0a", "▀█████████▄  ███    █▄  ███▄▄▄▄      ▄██████▄     ▄████████    ▄████████");
                    config.set("1a", "  ███    ███ ███    ███ ███▀▀▀██▄   ███    ███   ███    ███   ███    ███");
                    config.set("2a", "  ███    ███ ███    ███ ███   ███   ███    █▀    ███    █▀    ███    █▀ ");
                    config.set("3a", " ▄███▄▄▄██▀  ███    ███ ███   ███  ▄███         ▄███▄▄▄      ▄███▄▄▄    ");
                    config.set("4a", "▀▀███▀▀▀██▄  ███    ███ ███   ███ ▀▀███ ████▄  ▀▀███▀▀▀     ▀▀███▀▀▀    ");
                    config.set("5a", "  ███    ██▄ ███    ███ ███   ███   ███    ███   ███    █▄    ███    █▄ ");
                    config.set("6a", "  ███    ███ ███    ███ ███   ███   ███    ███   ███    ███   ███    ███");
                    config.set("7a", "▄█████████▀  ████████▀   ▀█   █▀    ████████▀    ██████████   ██████████");
                    config.set("8a", "--------------------------------------------------------------------------");
                    config.set("9a", "████████▄   ███    █▄     ▄████████ ███    █▄     ▄████████");
                    config.set("1b", "███    ███  ███    ███   ███    ███ ███    ███   ███    ███");
                    config.set("2b", "███    ███  ███    ███   ███    █▀  ███    ███   ███    █▀ ");
                    config.set("3b", "███    ███  ███    ███  ▄███▄▄▄     ███    ███  ▄███▄▄▄    ");
                    config.set("4b", "███    ███  ███    ███ ▀▀███▀▀▀     ███    ███ ▀▀███▀▀▀    ");
                    config.set("5b", "███    ███  ███    ███   ███    █▄  ███    ███   ███    █▄ ");
                    config.set("6b", "███  ▀ ███  ███    ███   ███    ███ ███    ███   ███    ███");
                    config.set("7b", " ▀██████▀▄█ ████████▀    ██████████ ████████▀    ██████████");
                    config.set("8b", "--------------------------------------------------------------------------");
                    config.set("|", "The multiplier below is what determines wait time, the larger the multiplier the longer the queue gets exponentially per player in queue");
                    config.set("||", "if 2 players are in queue 2nd player has Multiplier * 2 to wait before they enter the server, if your server is crashing because of players joining to fast try setting the multiplier larger");
                    config.set("-", "--------------------------------------------------------------------------");
                    config.set("ConnectingMessage", "&aConnecting... Please wait.");
                    config.set("Multiplier", 1.0);
                    config.set("WaitingQueueCheckTimer", 1.0);
                    config.set("|||", "This decides whether or not to factor server size in (default is false)");
                    config.set("||||", "If true new equation is (AmountOfPlayersInQueue * Multiplier) + AmountOfPlayersOnServer");
                    config.set("--", "--------------------------------------------------------------------------");
                    config.set("IsServerSizeCalculated", false);
                    config.set("DonorWaitMultiplier", 1.0);
                    config.set("---", "--------------------------------------------------------------------------");
                    config.set("|||||", "The message sent to non-donors, available placeholders %queuetime%  %queueposition% and %queuetotal% (QueueTime shows the estimated time till connection, queueposition shows their queueposition, and queuetotal shows total players in queue");
                    config.set("----", "--------------------------------------------------------------------------");
                    config.set("EnteringQueueMessage", "&aThank you for queueing you will be connected in %queuetime% seconds, you are %queueposition% player in queue");
                    config.set("EnteringDonorQueueMessage", "You are in donor queue, you are in %queueposition% and will join in %queuetime% seconds, there are %donorqueuetotal% players in queue");
                    config.set("SkippingQueueMessage", "You have skipped the queue");
                    config.set("FallBackServer", "lobby");
                    config.set("-----", "--------------------------------------------------------------------------");
                    config.set("||||||", "if a server is over this many players this will pause the queue for those specific people until that server has a empty spot available, it measures it based off of this integer and not the maximum of the server (in case you want admins or something to be able to login, anyone with bungeequeue.skip will override this, and will always attempt to connect regardless if the playercount exceeds maximumsize) available placeholder %waitposition%");
                    config.set("------", "--------------------------------------------------------------------------");
                    config.set("MaximumSize", 100);
                    config.set("FullServerCannotConnectMessage", "&cThe server you are trying to connect to is full you will be connected once there is a free slot available");
                    config.set("-------", "--------------------------------------------------------------------------");
                    config.set("|||||||", "This handles when a server is paused & not accessible and someone attempts to queue for it, it will put them in the waiting queue, and have them wait for the server to be unpaused, similar to the function of when a server is full");
                    config.set("--------", "--------------------------------------------------------------------------");
                    config.set("PausedServerMessage", "&cThe server you are trying to connect to is offline or unavailable you will be connected when it comes online. You are %waitingposition% waiting to connect.");
                    config.set("-------", "--------------------------------------------------------------------------");
                    config.set("||||||||", "This message is for players waiting to connect when a server is offline, or when it is full, everytime a player connects it will send this to everyone else waiting in queue, available placeholders %playerWhoConnected% and %waitingposition%");
                    config.set("--------", "--------------------------------------------------------------------------");
                    config.set("UpdateMessageForWaitingPlayers", "&a%playerWhoConnected% connected to the server, you are now %waitingposition% in queue");
                    config.set("-------", "--------------------------------------------------------------------------");
                    config.set("BlockedServerMessage", "&cThe Server you have tried to queue for is blocked and your queue has been cancelled");
                    config.set("--------", "--------------------------------------------------------------------------");
                    config.set("|||||||||", "The message sent when a player joins a new queue, this happens if they are in a waiting queue, or a paused queue only, it will automatically remove them from their previous queue");
                    config.set("---------", "--------------------------------------------------------------------------");
                    config.set("QueueResetMessage", "&aYou were &cremoved &afrom your previous queue because you joined a new queue.");
                    config.set("LeftQueueMessage", "&cYou left the %servername% queue.");
                    ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            listConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileLists);
            for(int i = 0; listConfig.getList("PausedServers").size() > i; i++){
                pausedServers.add(listConfig.getList("PausedServers").get(i).toString());
            }
            for(int i = 0; listConfig.getList("BlockedServers").size() > i; i++){
                blockedServers.add(listConfig.getList("BlockedServers").get(i).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("------------------------------");
        System.out.println(ChatColor.GREEN + "BungeeQueue Enabled");
        System.out.println(ChatColor.GREEN + "Version 3.0.5");
        System.out.println(ChatColor.GREEN + "By Larskei @Larskei#0001");
        System.out.println(ChatColor.GREEN + "Designed for use in Waterfall/Bungeecord 1.14.4");
        System.out.println("------------------------------");
    }
    //---------------------------------------------------------------------------------------------------------------------------------
    //method called when a player does /join
    //---------------------------------------------------------------------------------------------------------------------------------
    public void initiateMove(ProxiedPlayer player, String serverTarget) {
        if(blockedServers.contains(serverTarget)){
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("DenialToQueueMessage"))));
            return;
        }
        if(isPaused(serverTarget)){
            addPlayerToPauseList(player, serverTarget);
            return;
        }
        else {
            if (player.hasPermission("bungeequeue.skip")) {
                player.connect(getProxy().getServerInfo(serverTarget));
            } else {
                determineType(player, serverTarget);
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("ConnectingMessage"))));
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    //handles calling the right methods to determine which queue to use
    //---------------------------------------------------------------------------------------------------------------------------------
    public void determineType(ProxiedPlayer player, String target){
        if(isPaused(target)){
            addPlayerToPauseList(player, target);
            sendPauseMessage(player);
            return;
        }
        if(isFreeSlotAvailable(target)) {

            if (player.hasPermission("bungeequeue.donor")) {
                inDonorQueue = inDonorQueue + 1;
                queueDonorClean(player, target);
            } else {
                inQueue = inQueue + 1;
                queueRegularClean(player, target);
            }
        }
        else{
            addToWaitlist(player, target);
            String message = config.getString("FullServerCannotConnectMessage");
            message = message.replace("%waitposition%", String.valueOf(waitingPlayers.size()));
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }

    public void removeFromQueues(ProxiedPlayer player){
        if(pausedPlayers.contains(player)){
            for(int i = 0; pausedPlayers.size() > i; i++){
                if(pausedPlayers.get(i).equals(player)){
                    pausedPlayers.remove(i);
                    pausedTargets.remove(i);
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("QueueResetMessage"))));
                }
            }
        }
        if(waitingPlayers.contains(player)){
            for(int i = 0; waitingPlayers.size() > i; i++){
                if(waitingPlayers.get(i).equals(player)){
                    waitingPlayers.remove(i);
                    waitingPlayersTargets.remove(i);
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("QueueResetMessage"))));
                }
            }
        }
    }

    public void sendPauseMessage(ProxiedPlayer player){
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("PausedServerMessage"))));
    }


    //---------------------------------------------------------------------------------------------------------------------------------
    // This handles the messages sent to the players
    //---------------------------------------------------------------------------------------------------------------------------------
    public String getMessageToSend(ProxiedPlayer player, Boolean donor, String target){
        if(donor){
            //Handles replacing the placeholders inside of the configuration messages if the player is a donor
           String messageToBeTranslated = config.getString("EnteringDonorQueueMessage");
           String newMessage = messageToBeTranslated.replace("%queueposition%", String.valueOf(inDonorQueue));
           newMessage = newMessage.replace("%queuetime%", String.valueOf(getQueueTime(target, true)));
           newMessage = newMessage.replace("%donorqueuetotal%", String.valueOf(inDonorQueue));
           return newMessage;
        }
        else if(!donor){
            //handles replacing the message if the player is not a donor
            String messageToBeTranslated = config.getString("EnteringQueueMessage");
            String newMessage = messageToBeTranslated.replace("%queueposition%", String.valueOf(inQueue));
            newMessage = newMessage.replace("%queuetime%", String.valueOf(getQueueTime(target, false)));
            newMessage = newMessage.replace("%queuetotal%", String.valueOf(inQueue));
            return newMessage;
        }
        return null;
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    //handles clearing out the queue, as well as the waitlist operations
    //---------------------------------------------------------------------------------------------------------------------------------

    public void queueRegularClean(ProxiedPlayer player, String target) {
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', getMessageToSend(player, false, target))));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                movePlayer(player, target);
                inQueue = inQueue - 1;
            }
        };
        getProxy().getScheduler().schedule(this, runnable, getQueueTime(target, false), TimeUnit.MILLISECONDS);
    }


    public void queueDonorClean(ProxiedPlayer player, String target) {
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', getMessageToSend(player, true, target))));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                movePlayer(player, target);
                inDonorQueue = inDonorQueue - 1;
            }
        };
        getProxy().getScheduler().schedule(this, runnable, getQueueTime(target, true), TimeUnit.MILLISECONDS);
    }



    public void addToWaitlist(ProxiedPlayer player, String target){
        waitingPlayers.add(player);
        waitingPlayersTargets.add(target);
        if(!isCheck){
            queueChecker();
        }
    }

    public void queueChecker() {
        isCheck = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < getFirstInWait().size(); i++){
                        if(isFreeSlotAvailable(getFirstInWait().get(i).toString())){
                            for(int index = 0; index < waitingPlayers.size(); index++){
                                if(waitingPlayersTargets.get(i).equalsIgnoreCase(getFirstInWait().get(i).toString())){
                                   waitingPlayers.get(i).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', config.getString("ConnectingMessage"))));
                                   waitingPlayers.get(i).connect(getProxy().getServerInfo(waitingPlayersTargets.get(i)));
                                   waitingPlayers.remove(i);
                                   waitingPlayersTargets.remove(i);
                                   for(int indexer = 0; indexer < waitingPlayers.size(); indexer++){
                                       sendUpdateMessage(waitingPlayers.get(indexer));
                                   }
                                }
                            }
                        }
                    }
                }
            };
            getProxy().getScheduler().schedule(this, runnable, (int) config.getDouble("WaitingQueueCheckTimer") * 1000, TimeUnit.MILLISECONDS);
    }

    public List getFirstInWait(){
        List<String> firstTargets = new ArrayList<>();
        for(int i = 0; waitingPlayersTargets.size() - 1 >= i; i++){
            if(getProxy().getServers().containsKey(waitingPlayersTargets.get(i))){
                if(!firstTargets.contains(waitingPlayersTargets.get(i))){
                    firstTargets.add(waitingPlayersTargets.get(i));
                }
            }

        }
        return firstTargets;
    }

    public void sendUpdateMessage(ProxiedPlayer player){
        for(int i = 0; waitingPlayers.size() - 1 > i; i++){
            String message = config.getString("UpdateMessageForWaitingPlayers");
            message = message.replace("%waitingposition%", String.valueOf(i));
            message = message.replace("%playerWhoConnected%", player.getDisplayName());
            waitingPlayers.get(i).sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------
    //This handles the calculation of the queue time when a player joins the queue
    //---------------------------------------------------------------------------------------------------------------------------------

    public int getQueueTime(String serverName, boolean donator) {
        if (checkIfServerNameValid(serverName)) {
            if(config.getBoolean("isServerSizeCalculated")){
                if(donator){
                    return (inDonorQueue * (int) (config.getDouble("DonorWaitMultiplier") * 1000)) + getProxy().getServerInfo(serverName).getPlayers().size();
                }
                else{
                    return (inQueue * (int) (config.getDouble("Multiplier") * 1000)) + getProxy().getServerInfo(serverName).getPlayers().size();
                }
            }
            else{
                if(donator){
                    return inDonorQueue * (int) (config.getDouble("DonorWaitMultiplier") * 1000);
                }
                else{
                    return inQueue * (int) (config.getDouble("Multiplier") * 1000);
                }
            }
        }

        return 20;
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    //This is called to see if a free slot is available, and if not it returns false, used to determine whether or not the player should be in waiting queue or normal queue
    //---------------------------------------------------------------------------------------------------------------------------------

    public boolean isFreeSlotAvailable(String server){
        if(listConfig.getStringList("MaximumServerSizeNames").contains(server)){
            ArrayList<String> servers = new ArrayList<>();
            servers.addAll(listConfig.getStringList("MaximumServerSizeNames"));
            ArrayList<Integer> sizes = new ArrayList<>();
            sizes.addAll(listConfig.getIntList("MaximumServerSizes"));
            for(int i = 0; servers.size() > i; i++){
                if(servers.get(i).equalsIgnoreCase(server)){
                    if(sizes.get(i) >= getProxy().getServerInfo(server).getPlayers().size()){
                        return true;
                    } else{
                        return false;
                    }
                }
            }
        }
        if(getProxy().getServerInfo(server).getPlayers().size() <= config.getInt("MaximumSize")){
            return true;
        } else{
            return false;
        }
    }



    public void movePlayer(ProxiedPlayer player, String target) {

        player.connect(ProxyServer.getInstance().getServerInfo(target));
        System.out.println(ChatColor.GREEN + "[BungeeQueue] " + player.getDisplayName() + " moved to " + target);
    }


    public ProxiedPlayer getProxiedPlayer(String name) {
        return getProxy().getPlayer(name);
    }
    //---------------------------------------------------------------------------------------------------------------------------------
    //This is called when a player does the command initially so that it can verify if the server exists
    //---------------------------------------------------------------------------------------------------------------------------------

    public boolean checkIfServerNameValid(String serverName) {
        if(getProxy().getServers().containsKey(serverName)){
            return true;
        } else{
            return false;
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------
    //Pause methods, used to handle the pause and unpausing of servers as well as their players
    //---------------------------------------------------------------------------------------------------------------------------------

    public void pauseToggler(String target){
        if(pausedServers.contains(target)){
            clearPlayersInPause(target);
            pausedServers.remove(target);
            listConfig.set("PausedServers", pausedServers);
            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(listConfig, fileLists);
            }
            catch(IOException e){
             e.printStackTrace();
            }
        }
        else{
            pausedServers.add(target);
            listConfig.set("PausedServers", pausedServers);
            try {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(listConfig, fileLists);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void clearPlayersInPause(String target){
        for(int i = 0; pausedPlayers.size() > i; i++){
            if(pausedTargets.get(i).equalsIgnoreCase(target)){
                initiateMove(pausedPlayers.get(i), pausedTargets.get(i));
            }
        }
    }

    public void addPlayerToPauseList(ProxiedPlayer player, String target){
        String message = config.getString("PausedServerMessage");
        message = message.replace("%waitingpos", Integer.toString(pausedPlayers.size()));
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        pausedPlayers.add(player);
        pausedTargets.add(target);
    }

    public boolean isPaused(String target){
        if(pausedServers.contains(target)){
            return true;
        }
        else{
            return false;
        }
    }

    public void leaveQueue(ProxiedPlayer player){
        if(pausedPlayers.contains(player)){
            for(int i = 0; pausedPlayers.size() > i; i++){
                if(pausedPlayers.get(i).equals(player)){
                    String message = config.getString("LeftQueueMessage");
                    message = message.replace("%servername%", pausedTargets.get(i));
                    pausedPlayers.remove(i);
                    pausedTargets.remove(i);
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                }
            }
        }
        if(waitingPlayers.contains(player)){
            for(int i = 0; waitingPlayers.size() > i; i++){
                if(waitingPlayers.get(i).equals(player)){
                    String message = config.getString("LeftQueueMessage");
                    message = message.replace("%servername%", pausedTargets.get(i));
                    waitingPlayers.remove(i);
                    waitingPlayersTargets.remove(i);
                    player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
                }
            }
        }
    }










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
