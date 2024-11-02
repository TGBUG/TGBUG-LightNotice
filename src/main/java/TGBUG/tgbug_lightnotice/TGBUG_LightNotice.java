package TGBUG.tgbug_lightnotice;

import org.bukkit.plugin.java.JavaPlugin;

public final class TGBUG_LightNotice extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ConfigManager configManager = new ConfigManager(this);
        configManager.loadconfig();
        if (configManager.isBStats()) {
            Metrics metrics = new Metrics(this, 23788);
        }
        this.getCommand("lightnotice").setExecutor(new Commands(this));
        this.getCommand("lightnotice").setTabCompleter(new Commands(this));
        MessageBroadcaster messageBroadcaster = new MessageBroadcaster(this);
        messageBroadcaster.startBroadcasting();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
