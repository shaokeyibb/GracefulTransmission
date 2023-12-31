package io.hikarilan.gracefultransmission;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class TransmissionProcess {

    private final GracefulTransmission plugin;

    private final Player player;

    private final Location to;

    private final VirtualEntity entity;

    public TransmissionProcess(GracefulTransmission plugin, Player player, Location to) {
        this.plugin = plugin;
        this.player = player;
        this.to = to;
        this.entity = new VirtualEntity(plugin, player);

        run();
    }

    private void run() {
        val upDuration = plugin.getUpDuration();
        val fadeInDuration = plugin.getFadeInDuration();
        val stayDuration = plugin.getStayDuration();
        val fadeOutDuration = plugin.getFadeOutDuration();
        val downDuration = plugin.getDownDuration();
        val downStayDuration = plugin.getDownStayDuration();

        val upLocation = player.getLocation().clone().add(0, plugin.getUpOffset(), 0);
        upLocation.setPitch(90);

        val downLocation = to.clone();
        downLocation.setY(upLocation.getY());
        downLocation.setPitch(90);

        entity.attach();

        entity.move(upLocation, upDuration);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            timeCycle(Math.max(0, plugin.getDayCycle()), fadeInDuration + stayDuration + fadeOutDuration + downDuration);
            if (plugin.isModifyWeather()) {
                player.setPlayerWeather(player.getWorld().isClearWeather() ? WeatherType.DOWNFALL : WeatherType.CLEAR);
            }
        }, upDuration);
        if (upLocation.distanceSquared(downLocation) >= Math.pow(player.getWorld().getViewDistance() * 16, 2)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                entity.move(upLocation.clone().add(downLocation.clone().subtract(upLocation.clone()).toVector().normalize().multiply(Math.pow(player.getWorld().getViewDistance(), 2))), stayDuration / 2);
            }, upDuration + fadeInDuration);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(to, PlayerTeleportEvent.TeleportCause.SPECTATE);
                entity.teleport(downLocation.clone().subtract(downLocation.clone().subtract(upLocation.clone()).toVector().normalize().multiply(Math.pow(player.getWorld().getViewDistance(), 2))));
                entity.move(downLocation, stayDuration / 2);
            }, upDuration + fadeInDuration + stayDuration / 2);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                entity.move(to, downDuration);
            }, upDuration + fadeInDuration + stayDuration + fadeOutDuration);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                entity.move(downLocation, stayDuration);
            }, upDuration + fadeInDuration);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(to, PlayerTeleportEvent.TeleportCause.SPECTATE);
                entity.move(to, downDuration);
            }, upDuration + fadeInDuration + stayDuration + fadeOutDuration);
        }
        Bukkit.getScheduler().runTaskLater(plugin, player::resetPlayerWeather, upDuration + fadeInDuration + stayDuration + fadeOutDuration);
        Bukkit.getScheduler().runTaskLater(plugin, player::resetPlayerTime, upDuration + fadeInDuration + stayDuration + fadeOutDuration + downDuration);
        Bukkit.getScheduler().runTaskLater(plugin, entity::detach, upDuration + fadeInDuration + stayDuration + fadeOutDuration + downDuration + downStayDuration);
    }

    private void timeCycle(double cycle, int duration) {
        val durationCounter = new AtomicInteger(duration);
        val timer = new AtomicReference<BukkitTask>();

        long timeStep = (long) (cycle * 24000L / duration);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> timer.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            player.setPlayerTime(player.getPlayerTimeOffset() + timeStep, true);

            if (durationCounter.decrementAndGet() == 0) {
                timer.get().cancel();
            }
        }, 0, 1)));
    }
}
