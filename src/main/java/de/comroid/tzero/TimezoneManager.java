package de.comroid.tzero;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import de.comroid.util.files.FileProvider;

import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.Nullable;

public enum TimezoneManager implements Closeable {
    INSTANCE;

    private final File STORAGE_FILE = FileProvider.getFile("data/zones.properties");

    private final Map<Long, TimeZone> zoneMap;

    TimezoneManager() {
        try {
            zoneMap = new ConcurrentHashMap<>();

            Properties properties = new Properties();
            properties.load(new FileInputStream(STORAGE_FILE));

            properties.forEach((idStr, zoneStr) -> {
                long id = Long.parseLong(String.valueOf(idStr));
                TimeZone zone = TimeZone.getTimeZone(String.valueOf(zoneStr));

                zoneMap.put(id, zone);
            });
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Nullable
    public TimeZone setZone(User user, TimeZone zone) {
        TimeZone[] prev = new TimeZone[1];
        zoneMap.compute(user.getId(), (k, v) -> {
            if (v != null) prev[0] = v;
            return zone;
        });
        return prev[0];
    }

    public ZonedDateTime translate(ZonedDateTime time, User from, User to) {
        TimeZone origin = zoneMap.getOrDefault(from.getId(), TimeZone.getDefault());
        TimeZone target = zoneMap.getOrDefault(to.getId(), TimeZone.getDefault());

        return time.withZoneSameLocal(target.toZoneId());
    }

    public TimeZone getZone(User user) {
        return zoneMap.getOrDefault(user.getId(), TimeZone.getDefault());
    }

    @Override
    public void close() throws IOException {
        tick();
    }

    public void tick() throws IOException {
        Properties properties = new Properties();

        for (Map.Entry<Long, TimeZone> entry : zoneMap.entrySet()) {
            Long id = entry.getKey();
            TimeZone zone = entry.getValue();

            properties.put(String.valueOf(id), zone.getID());
        }

        properties.store(new FileOutputStream(STORAGE_FILE), "All Timezones stored per-user");
    }
}
