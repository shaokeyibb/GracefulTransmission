package io.hikarilan.gracefultransmission;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class GracefulTransmission extends JavaPlugin {

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private FileConfiguration configuration;

    @Getter
    private int upDuration;

    @Getter
    private int fadeInDuration;

    @Getter
    private int stayDuration;

    @Getter
    private int fadeOutDuration;

    @Getter
    private int downDuration;

    @Getter
    private int downStayDuration;

    @Getter
    private double upOffset;

    @Getter
    private boolean modifyWeather;

    @Getter
    private double dayCycle;

    @Override
    public void onLoad() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfiguration();
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(this);
    }

    private void reloadConfiguration() {
        this.configuration = getConfig();
        this.upDuration = configuration.getInt("general.up-duration", 40);
        this.fadeInDuration = configuration.getInt("general.fade-in-duration", 20);
        this.stayDuration = configuration.getInt("general.stay-duration", 20);
        this.fadeOutDuration = configuration.getInt("general.fade-out-duration", 40);
        this.downDuration = configuration.getInt("general.down-duration", 40);
        this.downStayDuration = configuration.getInt("general.down-stay-duration", 20);
        this.upOffset = configuration.getDouble("general.up-offset", 100);
        this.modifyWeather = configuration.getBoolean("general.modify-weather", true);
        this.dayCycle = configuration.getDouble("general.day-cycle", 1);
    }
}
