package TGBUG.tgbug_lightnotice;

import org.bukkit.plugin.java.JavaPlugin;

public final class TGBUG_LightNotice extends JavaPlugin {

    @Override
    public void onEnable() {
        // 初始化部分
        ConfigManager configManager = new ConfigManager(this).loadconfig();
        MessageBroadcaster messageBroadcaster = new MessageBroadcaster(this, configManager);
        this.getCommand("lightnotice").setExecutor(new Commands(configManager, messageBroadcaster));
        this.getCommand("lightnotice").setTabCompleter(new Commands(configManager, messageBroadcaster));
        // 启动bStats和广播任务
        configManager.loadconfig();
        if (configManager.isBStats()) {
            Metrics metrics = new Metrics(this, 23788);
        }
        messageBroadcaster.startBroadcasting();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
