package TGBUG.tgbug_lightnotice;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;
import java.util.Map;

public class Commands implements CommandExecutor{
    private final ConfigManager configManager;
    private final MessageBroadcaster messageBroadcaster;

    public Commands(TGBUG_LightNotice plugin) {
        this.configManager = new ConfigManager(plugin);
        this.configManager.loadconfig();
        this.messageBroadcaster = new MessageBroadcaster(plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("使用方法: /ln [reload|view]");
        } else {
            switch (args[0].toLowerCase()) {
                case "reload":
                    messageBroadcaster.stopBroadcasts();
                    configManager.loadconfig();
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
                    }

                default:
                    sender.sendMessage("未知命令");
                    return true;
            }
        }
        return true;
    }

    private boolean command_messages(CommandSender sender, String specifiedKey, List<Map<?, ?>> messagesList) {
        if (messagesList == null) {
            sender.sendMessage("消息列表未加载，请检查配置。");
            return true;
        }
        OfflinePlayer player;

        if (sender instanceof ConsoleCommandSender) {
            player = Bukkit.getOfflinePlayer("Console");
        } else {
            player = Bukkit.getOfflinePlayer(sender.getName());
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
