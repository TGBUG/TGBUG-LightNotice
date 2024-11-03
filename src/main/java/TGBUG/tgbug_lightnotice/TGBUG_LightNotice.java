package TGBUG.tgbug_lightnotice;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TGBUG_LightNotice extends JavaPlugin {
    ConfigManager configManager = new ConfigManager(this).loadconfig();
    MessageBroadcaster messageBroadcaster = new MessageBroadcaster(this, configManager);

    @Override
    public void onEnable() {
        // 初始化部分
        this.getCommand("lightnotice").setExecutor(new Commands(configManager, messageBroadcaster));
        this.getCommand("lightnotice").setTabCompleter(new Commands(configManager, messageBroadcaster));
        // 启动bStats和广播任务
        configManager.loadconfig();
        if (configManager.isBStats()) {
            Metrics metrics = new Metrics(this, 23788);
        }
        messageBroadcaster.startBroadcasting();
        this.getLogger().info("LightNotice轻量广播启动完毕，开始检查更新");
        CheckUpdate checkUpdate = new CheckUpdate();
        String currentVersion = this.getDescription().getVersion();new BukkitRunnable() {
            public void run() {
                String UpdateMessages = checkUpdate.checkForUpdates(currentVersion, "TGBUG", "TGBUG-LightNotice", "master");
                Bukkit.getScheduler().runTask(TGBUG_LightNotice.this, () -> getLogger().info(UpdateMessages));
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        messageBroadcaster.stopBroadcasts();
        this.getLogger().info("LightNotice轻量广播已卸载");
    }
}
