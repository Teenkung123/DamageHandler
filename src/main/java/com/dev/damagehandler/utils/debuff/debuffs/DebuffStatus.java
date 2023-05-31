package com.dev.damagehandler.utils.debuff.debuffs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public abstract class DebuffStatus {
    public long duration;
    private final UUID uuid;
    private final String id;

    protected DebuffStatus(String id, long duration) {
        this.duration = duration;
        this.uuid = UUID.randomUUID();
        this.id = id;
    }

    public abstract List<DebuffStatus> getCurrentDebuff(List<DebuffStatus> allDebuff);

    public long getDuration() {
        return duration;
    }
    public UUID getUniqueId() {
        return uuid;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(this);
                sb.append(fieldName).append("=").append(fieldValue).append(",");
            }
            sb.append("duration=").append(duration);
        } catch (IllegalAccessException ignored) {}
        return this.getClass().getSimpleName()+"{"+sb+"}";
    }
}
