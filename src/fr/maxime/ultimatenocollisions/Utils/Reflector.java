package fr.maxime.ultimatenocollisions.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.maxime.ultimatenocollisions.Utils.Packets.Packet;
import fr.maxime.ultimatenocollisions.Utils.Packets.PacketType;

public class Reflector
{
    private static String version;
    
    static {
        Reflector.version = "";
        try {
            Reflector.version = Bukkit.getServer().getClass().getName().split("\\.")[3];
            final Class<?> CRAFT_PLAYER = getClass(ClassType.CRAFTBUKKIT, "entity.CraftPlayer");
            assert CRAFT_PLAYER != null;
        }
        catch (Exception e) {
            System.err.println("Failed to load Reflector");
            e.printStackTrace();
        }
    }
    
    public static String getVersion() {
        return Reflector.version;
    }
    
    public static boolean versionIsNewerOrEqualAs(final int major, final int minor, final int patch) {
        return getMajorVersion() >= major && getMinorVersion() >= minor && getPatchVersion() >= patch;
    }
    
    private static int getMajorVersion() {
        return Integer.parseInt(getVersionSanitized().split("_")[0]);
    }
    
    private static String getVersionSanitized() {
        return getVersion().replaceAll("[^\\d_]", "");
    }
    
    private static int getMinorVersion() {
        return Integer.parseInt(getVersionSanitized().split("_")[1]);
    }
    
    private static int getPatchVersion() {
        final String[] split = getVersionSanitized().split("_");
        if (split.length < 3) {
            return 0;
        }
        return Integer.parseInt(split[2]);
    }
    
    public static Class<?> getClass(final ClassType type, final String name) {
        try {
            return Class.forName(String.format("%s.%s.%s", type.getPackage(), Reflector.version, name));
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Method getMethod(final Class<?> clazz, final String name) {
        return Arrays.stream(clazz.getMethods()).filter(method -> method.getName().equals(name)).findFirst().orElse(null);
    }
    
    public static <T> T invokeMethod(final Method method, final Object handle, final Object... params) {
        try {
            @SuppressWarnings("unchecked")
			final T t = (T) method.invoke(handle, params);
            return t;
        }
        catch (IllegalAccessException | InvocationTargetException ex5) {
            final ReflectiveOperationException ex4 = null;
            @SuppressWarnings("unused")
			final ReflectiveOperationException ex2 = ex4;
            final ReflectiveOperationException ex3 = null;
            throw new RuntimeException(ex3);
        }
    }
    
    public static <T, R> Function<T, R> memoizeMethodAndInvoke(final Class<T> clazz, final String name, final Object... params) {
        final Method method = getMethod(clazz, name);
        return (Function<T, R>)(t -> invokeMethod(method, t, params));
    }
    
    public static Field getField(final Class<?> clazz, final String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Field getInaccessibleField(final Class<?> clazz, final String fieldName) {
        final Field field = getField(clazz, fieldName);
        field.setAccessible(true);
        return field;
    }
    
    public static Object getFieldValue(final Object object, final String fieldName) throws Exception {
        return findFieldWithinHierarchy(object, fieldName).get(object);
    }
    
    private static Field findFieldWithinHierarchy(final Object object, final String fieldName) throws NoSuchFieldException {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            Field[] declaredFields;
            for (int length = (declaredFields = clazz.getDeclaredFields()).length, i = 0; i < length; ++i) {
                final Field field = declaredFields[i];
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
    
    public static Object getFieldValue(final Field field, final Object handle) {
        field.setAccessible(true);
        try {
            return field.get(handle);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setFieldValue(final Object object, final String fieldName, final Object value) throws Exception {
        getInaccessibleField(object.getClass(), fieldName).set(object, value);
    }
    
    public static Constructor<?> getConstructor(final Class<?> clazz, final int numParams) {
        return Arrays.stream(clazz.getConstructors()).filter(constructor -> constructor.getParameterCount() == numParams).findFirst().orElse(null);
    }
    
    public static boolean inheritsFrom(final Class<?> toCheck, final Class<?> inheritedClass) {
        if (inheritedClass.isAssignableFrom(toCheck)) {
            return true;
        }
        Class<?>[] interfaces;
        for (int length = (interfaces = toCheck.getInterfaces()).length, i = 0; i < length; ++i) {
            final Class<?> implementedInterface = interfaces[i];
            if (inheritsFrom(implementedInterface, inheritedClass)) {
                return true;
            }
        }
        return false;
    }
    
    public static class Packets
    {
        public static Class<?> getPacket(final PacketType type, final String name) {
            return Reflector.getClass(ClassType.NMS, "Packet" + type.prefix + name);
        }
        
        public static void sendPacket(final Player player, final Object packet) {
            try {
                Packet.createFromNMSPacket(packet).send(player);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
