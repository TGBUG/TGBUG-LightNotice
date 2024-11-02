package TGBUG.tgbug_lightnotice;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


import java.util.List;
import java.util.Map;
import java.util.Random;

public class MessageBroadcaster {
    private final TGBUG_LightNotice plugin;
    private ConfigManager configManager;
    private final Random random = new Random();
    private List<Map<?, ?>> messagesList;
    private List<Map<?, ?>> randomMessagesList;

    private BukkitTask broadcastTask;
    private BukkitTask messagesBroadcastTask;
    private BukkitTask randomMessagesBroadcastTask;

    public MessageBroadcaster(TGBUG_LightNotice plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void startBroadcasting() {
        configManager = configManager.loadconfig();
        messagesList = configManager.getMessagesList();
        randomMessagesList = configManager.getRandomMessagesList();
        if (configManager.isMerge_random_notice()) {
            startMergedBroadcast();
        } else {
            startSeparateBroadcasts();
        }
    }

    private void sendMessageToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        plugin.getLogger().info(message);
    }

    private void startMergedBroadcast() {
        broadcastTask = new BukkitRunnable() {
            int messageIndex = 0;
            int randomMessageCounter = 0; // 用于计数广播的消息数量

            @Override
            public void run() {
                // 检查是否达到随机消息广播的间隔
                if (randomMessageCounter >= configManager.getRandom_notice_interval()) {
                    // 随机广播 random_messages.yml 内容
                    List<String> randomMessage = getRandomMessage(randomMessagesList);
                    if (randomMessage != null) {
                        for (String line : randomMessage) {
                            sendMessageToAll(line);
                        }
                    }
                    randomMessageCounter = 0;
                }
                // 广播 messages.yml 内容
                else if (messageIndex < messagesList.size()) {
                    Map<?, ?> messageMap = messagesList.get(messageIndex);
                    for (Object key : messageMap.keySet()) {
                        List<String> messages = configManager.getMessage(messagesList, key.toString(), null);
                        if (messages != null) {
                            for (String line : messages) {
                                sendMessageToAll(line);
                            }
                        }
                    }
                    messageIndex++;
                    randomMessageCounter++;
                } else {
                    messageIndex = 0;
                }
            }
        }.runTaskTimer(plugin, configManager.getPeriod() * 20, configManager.getPeriod() * 20);
    }



    private void startSeparateBroadcasts() {
        // 启动 messages.yml 的广播
        messagesBroadcastTask = new BukkitRunnable() {
            int messageIndex = 0;

            @Override
            public void run() {
                if (messageIndex < messagesList.size()) {
                    Map<?, ?> messageMap = messagesList.get(messageIndex);
                    for (Object key : messageMap.keySet()) {
                        List<String> messages = configManager.getMessage(messagesList, key.toString(), null);
                        if (messages != null) {
                            for (String line : messages) {
                                sendMessageToAll(line);
                            }
                        }
                    }
                    messageIndex++;
                } else {
                    messageIndex = 0;
                }
            }
        }.runTaskTimer(plugin, configManager.getPeriod() * 20, configManager.getPeriod() * 20);

        // 启动 random_messages.yml 的广播
        randomMessagesBroadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<String> randomMessage = getRandomMessage(randomMessagesList);
                if (randomMessage != null) {
                    for (String line : randomMessage) {
                        sendMessageToAll(line);
                    }
                }
            }
        }.runTaskTimer(plugin, configManager.getRandom_period() * 20, configManager.getRandom_period() * 20);
    }


    private List<String> getRandomMessage(List<Map<?, ?>> randomMessagesList) {
        if (randomMessagesList.isEmpty()) return null;

        int randomIndex = random.nextInt(randomMessagesList.size());
        Map<?, ?> randomMessageMap = randomMessagesList.get(randomIndex);
        for (Object key : randomMessageMap.keySet()) {
            return configManager.getMessage(randomMessagesList, key.toString(), null);
        }
        return null;
    }

    public void stopBroadcasts() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        if (messagesBroadcastTask != null) {
            messagesBroadcastTask.cancel();
            messagesBroadcastTask = null;
        }
        if (randomMessagesBroadcastTask != null) {
            randomMessagesBroadcastTask.cancel();
            randomMessagesBroadcastTask = null;
        }
    }

}
