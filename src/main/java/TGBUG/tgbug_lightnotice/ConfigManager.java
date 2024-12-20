package TGBUG.tgbug_lightnotice;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final TGBUG_LightNotice plugin;
    private boolean isMerge_random_notice;
    private Long period;
    private Long random_period;
    private List<Map<?, ?>> messagesList;
    private List<Map<?, ?>> random_messagesList;
    private List<Map<?, ?>> timed_MessageList;
    private int random_notice_interval;
    private boolean bStats;

    public ConfigManager(TGBUG_LightNotice plugin) {
        this.plugin = plugin;
    }

    public ConfigManager loadconfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        File random_messagesFile = new File(plugin.getDataFolder(), "random_messages.yml");
        File timed_messagesFile = new File(plugin.getDataFolder(), "timed_messages.yml");

        checkConfigFile.checkConfigFile(configFile, plugin);
        checkConfigFile.checkConfigFile(messagesFile, plugin);
        checkConfigFile.checkConfigFile(random_messagesFile, plugin);
        checkConfigFile.checkConfigFile(timed_messagesFile, plugin);


        //加载配置文件完毕，获取变量部分

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        FileConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        FileConfiguration random_messages = YamlConfiguration.loadConfiguration(random_messagesFile);
        FileConfiguration timed_messages = YamlConfiguration.loadConfiguration(timed_messagesFile);

        isMerge_random_notice = config.getBoolean("merge_random_notice");
        period = config.getLong("period");
        random_period = config.getLong("random_period");
        random_notice_interval = config.getInt("random_notice_interval");
        bStats = config.getBoolean("bStats");
        messagesList = messages.getMapList("messages");
        random_messagesList = random_messages.getMapList("random_messages");
        timed_MessageList = timed_messages.getMapList("timed_messages");

        //回传实例
        return this;
    }

    //回传变量
    public boolean isMerge_random_notice() {
        return isMerge_random_notice;
    }

    public Long getPeriod() {
        return period;
    }

    public Long getRandom_period() {
        return random_period;
    }

    public int getRandom_notice_interval() {
        return random_notice_interval;
    }

    public boolean isBStats() {
        return bStats;
    }

    public List<Map<?, ?>> getMessagesList() {
        return messagesList;
    }

    public List<Map<?, ?>> getRandomMessagesList() {
        return random_messagesList;
    }

    public List<Map<?, ?>> getTimedMessagesList() {
        return timed_MessageList;
    }

    // 获取消息
    public List<String> getMessage(List<Map<?, ?>> messagesList, String specifiedKey, OfflinePlayer p) {

        for (Map<?, ?> messagesMap : messagesList) {
            if (messagesMap.containsKey(specifiedKey)) {
                // 获取指定键的值（List<String>）
                List<String> messages = (List<String>) messagesMap.get(specifiedKey);
                //转换消息
                return translateMessage(messages, p);
            }
        }
        return null;
    }

    public List<String> translateMessage(List<String> messages, OfflinePlayer p) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            // 替换颜色代码
            message = ChatColor.translateAlternateColorCodes('&', message);
            // 替换占位符
            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
                message = PlaceholderAPI.setPlaceholders(p, message);
            }
            messages.set(i, message);
        }
        return messages;
    }
}
