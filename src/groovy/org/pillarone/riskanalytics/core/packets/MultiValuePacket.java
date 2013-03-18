package org.pillarone.riskanalytics.core.packets;

import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.core.util.PacketUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
abstract public class MultiValuePacket extends Packet {

    private static final String PATH_SEPARATOR = " ";


    @Override
    public Map<String, Number> getValuesToSave() throws IllegalAccessException {
        return getValuesToSave(null);
    }

    public Map<String, Number> getValuesToSave(String prefix) throws IllegalAccessException {
        Map<String, Number> map = new HashMap<String, Number>();
        Class currentClass = getClass();
        do {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!(field.getName().startsWith("__timeStamp")) && ((Number.class.isAssignableFrom(field.getType()) ||
                    (field.getType().isPrimitive() && Number.class.isAssignableFrom((Class) PacketUtils.primitiveToWrapperClass.get(field.getType())))))) {
                    field.setAccessible(true);
                    String fieldName = prefix == null ? field.getName() : prefix + PATH_SEPARATOR + field.getName();
                    map.put(fieldName, (Number) field.get(this));
                }
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != MultiValuePacket.class);

        return map;
    }

    public List<String> getFieldNames() {
        return getFieldNames(null);
    }

    public List<String> getFieldNames(String prefix) {
        List<String> fieldNames = new ArrayList<String>();
        Class currentClass = getClass();
        do {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!(field.getName().startsWith("__timeStamp")) && ((Number.class.isAssignableFrom(field.getType()) ||
                    (field.getType().isPrimitive() && Number.class.isAssignableFrom((Class) PacketUtils.primitiveToWrapperClass.get(field.getType())))))) {
                    String fieldName = prefix == null ? field.getName() : prefix + PATH_SEPARATOR + field.getName();
                    fieldNames.add(fieldName);
                }
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != MultiValuePacket.class);
        return fieldNames;
    }
}