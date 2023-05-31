package com.dev.damagehandler.debuff;

import com.dev.damagehandler.debuff.debuffs.DebuffStatus;

import java.lang.reflect.Field;
import java.util.*;

import static com.dev.damagehandler.debuff.Debuff.mapDebuffData;

public class DebuffData {

    private final UUID uuid;
    private final List<DebuffStatus> totalDebuff = new ArrayList<>();

    public DebuffData(UUID uuid) {
        this.uuid = uuid;
    }

    public List<DebuffStatus> getTotalDebuffs() {
        return totalDebuff;
    }

    public List<DebuffStatus> getActivateDebuffs() {
        List<DebuffStatus> output = new ArrayList<>();
        Set<String> debuffClass = new HashSet<>();
        for (DebuffStatus debuff : totalDebuff) {
            if (debuffClass.contains(debuff.getClass().getName())) continue;

            List<DebuffStatus> allDebuff = new ArrayList<>();
            for (DebuffStatus d : totalDebuff) {
                if (debuff.getClass().getName().equals(d.getClass().getName())) {
                    allDebuff.add(d);
                }
            }

            debuffClass.add(debuff.getClass().getName());
            output.addAll(debuff.getCurrentDebuff(allDebuff));
        }
        return output;
    }

    public <T> T getActivateDebuff(Class<T> debuff, String[] key, Object[] value) {
        ActivatedDebuffLoop: for (DebuffStatus debuffStatus : getActivateDebuffs()) {
            if (debuffStatus.getClass().equals(debuff)) {
                Class<?> clazz = debuffStatus.getClass();
                Field[] fields = clazz.getDeclaredFields();
                HashMap<String, Object> data = new HashMap<>();
                try {
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        Object fieldValue = field.get(debuffStatus);
                        data.put(fieldName, fieldValue);
                    }
                    data.put("duration", debuffStatus.getDuration());
                } catch (IllegalAccessException ignored) {}

                if (key.length == 0) return debuff.cast(debuffStatus);
                for (int k = 0; k < key.length; k++) {
                    if (value[k] == null) continue ActivatedDebuffLoop;
                    if (!data.containsKey(key[k])) continue ActivatedDebuffLoop;
                    if (!data.get(key[k]).equals(value[k])) continue ActivatedDebuffLoop;
                }
                return debuff.cast(debuffStatus);
            }
        }
        return null;
    }

    public void addDebuff(DebuffStatus debuff) {
        totalDebuff.add(debuff);
        if (!mapDebuffData.containsKey(this.uuid)) mapDebuffData.put(this.uuid, this);
    }

    public void removeDebuff(UUID uuid) {
        for (DebuffStatus debuff : totalDebuff) {
            if (debuff.getUniqueId() == uuid) {
                totalDebuff.remove(debuff);
                if (totalDebuff.isEmpty()) mapDebuffData.remove(this.uuid);
                break;
            }
        }
    }

    public void reduceDuration(UUID uuid, long duration) {
        for (DebuffStatus debuff : totalDebuff) {
            if (debuff.getUniqueId() == uuid) {
                if (debuff.getDuration() <= duration) {
                    removeDebuff(uuid);
                } else {
                    debuff.setDuration(debuff.getDuration() - duration);
                }
                break;
            }
        }
    }
}
