package fr.maxime.ultimatenocollisions.Utils.Packets;

import java.lang.reflect.*;
import java.util.*;
import org.bukkit.entity.*;

import fr.maxime.ultimatenocollisions.Utils.*;

class PacketSender
{
    private static final Class<?> CRAFT_PLAYER;
    private static final Class<?> PLAYER_CONNECTION;
    private static final Class<?> ENTITY_PLAYER;
    private static final Method GET_HANDLE;
    private static final Method SEND_PACKET;
    private static final Field PLAYER_CONNECTION_FIELD;
    private static final PacketSender instance;
    
    static {
        CRAFT_PLAYER = Objects.requireNonNull(Reflector.getClass(ClassType.CRAFTBUKKIT, "entity.CraftPlayer"));
        PLAYER_CONNECTION = Objects.requireNonNull(Reflector.getClass(ClassType.NMS, "PlayerConnection"));
        ENTITY_PLAYER = Objects.requireNonNull(Reflector.getClass(ClassType.NMS, "EntityPlayer"));
        GET_HANDLE = Reflector.getMethod(PacketSender.CRAFT_PLAYER, "getHandle");
        SEND_PACKET = Reflector.getMethod(PacketSender.PLAYER_CONNECTION, "sendPacket");
        PLAYER_CONNECTION_FIELD = Reflector.getField(PacketSender.ENTITY_PLAYER, "playerConnection");
        instance = new PacketSender();
    }
    
    private PacketSender() {
    }
    
    static PacketSender getInstance() {
        return PacketSender.instance;
    }
    
    void sendPacket(final Packet packet, final Player player) {
        this.sendPacket(packet.getNMSPacket(), this.getConnection(player));
    }
    
    private void sendPacket(final Object nmsPacket, final Object playerConnection) {
        Reflector.invokeMethod(PacketSender.SEND_PACKET, playerConnection, nmsPacket);
    }
    
    Object getConnection(final Player player) {
        final Object handle = Reflector.invokeMethod(PacketSender.GET_HANDLE, player, new Object[0]);
        return Reflector.getFieldValue(PacketSender.PLAYER_CONNECTION_FIELD, handle);
    }
}
