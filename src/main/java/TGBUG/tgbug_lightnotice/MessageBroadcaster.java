package TGBUG.tgbug_lightnotice;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MessageBroadcaster {
    private final TGBUG_LightNotice plugin;
    private ConfigManager configManager;
    private final Random random = new Random();
    private List<Map<?, ?>> messagesList;
    private List<Map<?, ?>> randomMessagesList;
    private List<Map<?, ?>> timedMessagesList;

    private BukkitTask broadcastTask;
    private BukkitTask messagesBroadcastTask;
    private BukkitTask randomMessagesBroadcastTask;
    private List<BukkitTask> timedMessagesBroadcastTasks = new ArrayList<>();

    public MessageBroadcaster(TGBUG_LightNotice plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void startBroadcasting() {
        configManager = configManager.loadconfig();
        messagesList = configManager.getMessagesList();
        randomMessagesList = configManager.getRandomMessagesList();
        timedMessagesList = configManager.getTimedMessagesList();
        if (configManager.isMerge_random_notice()) {
            startMergedBroadcast();
        } else {
            startSeparateBroadcasts();
        }
        startTimedBroadcasts();
    }

    private void sendMessageToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        plugin.getLogger().info(message);
    }

    //合并广播部分
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

    //单独任务广播部分
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

    //定时广播部分
    private void startTimedBroadcasts() {
        for (Map<?, ?> entry : timedMessagesList) {
            for (Object key : entry.keySet()) {
                Map<String, Object> details = (Map<String, Object>) entry.get(key);
                if (details.get("delay") instanceof Number) {
                    long delay = ((Number) details.get("delay")).longValue();

                    BukkitTask timedMessagesBroadcastTask = new BukkitRunnable() {
                        @Override
                        public void run() {
                            List<String> timed_messages = configManager.translateMessage((List<String>) details.get("messages"), null);
                            if (timed_messages != null) {
                                for (String line : timed_messages) {
                                    sendMessageToAll(line);
                                }
                            }
                        }
                    }.runTaskTimer(plugin, delay * 20, delay * 20);
                    timedMessagesBroadcastTasks.add(timedMessagesBroadcastTask);
                } else {
                    plugin.getLogger().warning("timed_messages中有delay的值无效");
                    plugin.getLogger().warning("已取消注册此广播任务");
                }
            }
        }
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
        if (timedMessagesBroadcastTasks != null) {
            for (BukkitTask timedMessagesBroadcastTask : timedMessagesBroadcastTasks) {
                timedMessagesBroadcastTask.cancel();
            }
            timedMessagesBroadcastTasks = new ArrayList<>();
        }
    }

}
