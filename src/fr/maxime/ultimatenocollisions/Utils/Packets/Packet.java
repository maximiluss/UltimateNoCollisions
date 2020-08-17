package fr.maxime.ultimatenocollisions.Utils.Packets;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.bukkit.entity.Player;

import fr.maxime.ultimatenocollisions.Utils.ClassType;
import fr.maxime.ultimatenocollisions.Utils.Reflector;

public class Packet
{
    private static final Class<?> NMS_PACKET_CLASS;
    private Class<?> packetClass;
    private Object rawPacket;
    
    static {
        NMS_PACKET_CLASS = Reflector.getClass(ClassType.NMS, "Packet");
    }
    
    private Packet(final Class<?> packetClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this(packetClass.getConstructor((Class<?>[])new Class[0]).newInstance(new Object[0]));
    }
    
    private Packet(final Object packet) {
        this.rawPacket = packet;
        this.packetClass = packet.getClass();
    }
    
    public static Packet create(final String name) {
        String packetName = name;
        if (!packetName.startsWith("Packet")) {
            packetName = "Packet" + packetName;
        }
        final Class<?> packetClass = Reflector.getClass(ClassType.NMS, packetName);
        Objects.requireNonNull(packetClass, "packetClass can not be null!");
        try {
            return new Packet(packetClass);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex5) {
            final ReflectiveOperationException ex4 = null;
            @SuppressWarnings("unused")
			final ReflectiveOperationException ex2 = ex4;
            final ReflectiveOperationException ex3 = null;
            final String message = "Failed to create packet!" + String.format("Name: %s, Replaced, %s Class: %s", name, packetName, packetClass);
            throw new RuntimeException(message, ex3);
        }
    }
    
    public static Packet createFromNMSPacket(final Object nmsPacket) {
        Objects.requireNonNull(nmsPacket, "nmsPacket can not be null");
        if (Packet.NMS_PACKET_CLASS == null) {
            throw new IllegalStateException("Could not find packet class! Therefore this class is broken.");
        }
        if (!Reflector.inheritsFrom(nmsPacket.getClass(), Packet.NMS_PACKET_CLASS)) {
            throw new IllegalArgumentException("You must pass a 'Packet' object!");
        }
        try {
            return new Packet(nmsPacket);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create packet!", e);
        }
    }
    
    public Object getNMSPacket() {
        return this.rawPacket;
    }
    
    public void send(final Player... players) {
        for (final Player player : players) {
            PacketSender.getInstance().sendPacket(this, player);
        }
    }
    
    public Class<?> getPacketClass() {
        return this.packetClass;
    }
}
