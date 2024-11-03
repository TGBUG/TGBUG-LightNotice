package TGBUG.tgbug_lightnotice;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Commands implements CommandExecutor, TabCompleter {
    private ConfigManager configManager;
    private final MessageBroadcaster messageBroadcaster;

    public Commands(ConfigManager configManager, MessageBroadcaster messageBroadcaster) {
        this.configManager = configManager;
        this.messageBroadcaster = messageBroadcaster;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("使用方法: /ln [reload|view]");
        } else {
            switch (args[0].toLowerCase()) {
                case "reload":
                    messageBroadcaster.stopBroadcasts();
                    configManager = configManager.loadconfig();
                    messageBroadcaster.startBroadcasting();
                    sender.sendMessage("配置文件已重新加载！");
                    return true;

                case "view":
                    switch (args[1].toLowerCase()) {
                        case "messages":
                            if (args.length == 3) {
                                String specifiedKey = args[2];
                                List<Map<?, ?>> messagesList = configManager.getMessagesList();
                                if (command_messages(sender, specifiedKey, messagesList)) return true;
                            } else {
                                sender.sendMessage("使用方法: /ln view [广播类型] [广播名]");
                            }
                            return true;

                        case "random_messages":
                            if (args.length == 3) {
                                String specifiedKey = args[2];
                                List<Map<?, ?>> messagesList = configManager.getRandomMessagesList();
                                if (command_messages(sender, specifiedKey, messagesList)) return true;
                            } else {
                                sender.sendMessage("使用方法: /ln view [广播类型] [广播名]");
                            }
                            return true;

                        case "timed_messages":
                            if (args.length == 3) {
                                String specifiedKey = args[2];
                                List<Map<?, ?>> messagesList = configManager.getTimedMessagesList();
                                //此处不能再用command_messages方法，重写
                                if (messagesList == null) {
                                    sender.sendMessage("消息列表未加载，请检查配置。");
                                    return true;
                                }
                                OfflinePlayer player;
                                if (sender instanceof Player) {
                                    player = Bukkit.getOfflinePlayer(sender.getName());
                                } else {
                                    player = Bukkit.getOfflinePlayer("Console");
                                }
                                for (Map<?, ?> messagesMap : messagesList) {
                                    if (messagesMap.containsKey(specifiedKey)) {
                                        Map<String, Object> details = (Map<String, Object>) messagesMap.get(specifiedKey);
                                        for (String line : configManager.translateMessage((List<String>) details.get("messages"), player)) {
                                            sender.sendMessage(line);
                                        }
                                    }
                                }
                            } else {
                                sender.sendMessage("使用方法: /ln view [广播类型] [广播名]");
                            }
                            return true;
                    }

                default:
                    sender.sendMessage("未知命令");
                    return true;
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("view");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    break;
                case "view":
                    completions.add("messages");
                    completions.add("random_messages");
                    completions.add("timed_messages");
                    break;
            }
        } else if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "messages":
                    List<Map<?, ?>> messagesList = configManager.getMessagesList();
                    for (Map<?, ?> message : messagesList) {
                        for (Object key : message.keySet()) {
                            completions.add(key.toString());
                        }
                    }
                    break;
                case "random_messages":
                    List<Map<?, ?>> randomMessagesList = configManager.getRandomMessagesList();
                    for (Map<?, ?> message : randomMessagesList) {
                        for (Object key : message.keySet()) {
                            completions.add(key.toString());
                        }
                    }
                    break;
                case "timed_messages":
                    List<Map<?, ?>> timedMessagesList = configManager.getTimedMessagesList();
                    for (Map<?, ?> message : timedMessagesList) {
                        for (Object key : message.keySet()) {
                            completions.add(key.toString());
                        }
                    }
            }
        }
        return completions;
    }

    private boolean command_messages(CommandSender sender, String specifiedKey, List<Map<?, ?>> messagesList) {
        if (messagesList == null) {
            sender.sendMessage("消息列表未加载，请检查配置。");
            return true;
        }
        OfflinePlayer player;

        if (sender instanceof Player) {
            player = Bukkit.getOfflinePlayer(sender.getName());
        } else {
            player = Bukkit.getOfflinePlayer("Console");
        }

        List<String> message = configManager.getMessage(messagesList, specifiedKey, player);
        if (message != null && !message.isEmpty()) {
            for (String line : message) {
                sender.sendMessage(line);
            }
        } else {
            sender.sendMessage("未找到广播: " + specifiedKey);
        }
        return false;
    }
}
