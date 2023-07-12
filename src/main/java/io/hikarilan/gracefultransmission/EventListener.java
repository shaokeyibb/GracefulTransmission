package io.hikarilan.gracefultransmission;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public class EventListener implements Listener {

    private final GracefulTransmission plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN && e.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND)
            return;

        if (e.getFrom().getWorld() == null || e.getTo() == null || e.getTo().getWorld() == null) return;

        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) return;

        e.setCancelled(true);

        new TransmissionProcess(plugin, e.getPlayer(), e.getTo());
    }

}
