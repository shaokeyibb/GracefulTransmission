package io.hikarilan.gracefultransmission;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VirtualEntity {

    private final GracefulTransmission plugin;

    private final ProtocolManager protocolManager;

    private final Player player;

    private final int entityID;

    private Location location;

    public VirtualEntity(GracefulTransmission plugin, Player player) {
        this.plugin = plugin;
        this.protocolManager = plugin.getProtocolManager();
        this.player = player;
        this.entityID = -player.getEntityId();
        this.location = player.getLocation();

        spawn();
        setInvisible();
    }


    @SneakyThrows
    private void spawn() {
        val packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, entityID);
        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getIntegers().write(1, 1 /*Armor Stand*/);

        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        packet.getBytes()
                .write(0, (byte) (location.getYaw() * 256.0F / 360.0F))
                .write(1, (byte) (location.getPitch() * 256.0F / 360.0F))
                .write(2, (byte) (location.getYaw() * 256.0F / 360.0F));

        protocolManager.sendServerPacket(player, packet);
    }

    @SneakyThrows
    private void setInvisible() {
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, entityID);

        val watcher = new WrappedDataWatcher();
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20);
        packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        protocolManager.sendServerPacket(player, packet);
    }

    @SneakyThrows
    private void setPlayerSpectator(boolean reset) {
        val packet = protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);

        packet.getGameStateIDs().write(0, 3);
        packet.getFloat().write(0, reset ? player.getGameMode().getValue() : 3F);

        protocolManager.sendServerPacket(player, packet);
    }

    @SneakyThrows
    public void attach() {
        setPlayerSpectator(false);

        val packet = protocolManager.createPacket(PacketType.Play.Server.CAMERA);
        packet.getIntegers().write(0, entityID);

        protocolManager.sendServerPacket(player, packet);
    }

    @SneakyThrows
    public void detach() {
        val packet = protocolManager.createPacket(PacketType.Play.Server.CAMERA);
        packet.getIntegers().write(0, player.getEntityId());

        protocolManager.sendServerPacket(player, packet);

        setPlayerSpectator(true);
    }

    @SneakyThrows
    public void teleport(Location location) {
        this.location = location;

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, entityID);
        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        packet.getBytes()
                .write(0, (byte) (location.getYaw() * 256.0F / 360.0F))
                .write(1, (byte) (location.getPitch() * 256.0F / 360.0F));

        packet.getBooleans()
                .write(0, false);

        protocolManager.sendServerPacket(player, packet);
    }

    @SneakyThrows
    private void lookAt(float headYaw) {
        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, entityID);
        packet.getBytes().write(0, (byte) (headYaw * 256.0F / 360.0F));

        protocolManager.sendServerPacket(player, packet);
    }

    public void move(Location to, int duration) {
        val durationCounter = new AtomicInteger(duration);
        val timer = new AtomicReference<BukkitTask>();

        double xStep = (to.getX() - location.getX()) / duration;
        double yStep = (to.getY() - location.getY()) / duration;
        double zStep = (to.getZ() - location.getZ()) / duration;
        float yawStep = (to.getYaw() - location.getYaw()) / duration;
        float pitchStep = (to.getPitch() - location.getPitch()) / duration;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> timer.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            location.add(xStep, yStep, zStep);
            location.setYaw(location.getYaw() + yawStep);
            location.setPitch(location.getPitch() + pitchStep);

            teleport(location);

            if (durationCounter.decrementAndGet() == 0) {
                timer.get().cancel();
            }
        }, 0, 1)));
    }

}
