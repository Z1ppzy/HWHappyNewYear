package ru.meloncode.xmas;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class ParticleContainer {

    final static DustOptions[] COLORS = new DustOptions[]{
            new DustOptions(Color.LIME, 1f),
            new DustOptions(Color.RED, 1f),
            new DustOptions(Color.AQUA, 1f),
            new DustOptions(Color.YELLOW, 1f),
            new DustOptions(Color.BLUE, 1f),
            new DustOptions(Color.FUCHSIA, 1f)
    };

    private final Particle type;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float speed;
    private final int count;

    public ParticleContainer(Particle type, float offsetX, float offsetY, float offsetZ, float speed, int count) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.count = count;
    }

    public void playEffect(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        // Проверяем загрузку чанка
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ)) {
            return;
        }

        location = location.clone();
        location.add(0.5, 0.5, 0.5);

        // ИСПРАВЛЕНО: getPlayers() вместо getNearbyEntities() для async
        // Проверяем расстояние вручную
        for (Player player : location.getWorld().getPlayers()) {
            // 256 = 16*16 (квадрат расстояния)
            if (player.getLocation().distanceSquared(location) > 256) {
                continue;
            }

            try {
                if (type == Particle.DUST) {
                    int colorIndex = ThreadLocalRandom.current().nextInt(COLORS.length);
                    player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, COLORS[colorIndex]);
                } else {
                    player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed);
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
        }
    }
}