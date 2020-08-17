package fr.maxime.ultimatenocollisions.Utils.Packets;

import org.bukkit.entity.*;

public class PacketEvent
{
    private Packet packet;
    private Player player;
    private boolean cancelled;
    private ConnectionDirection direction;
    
    protected PacketEvent(final Object packet, final boolean cancelled, final ConnectionDirection direction, final Player player) {
        this.packet = Packet.createFromNMSPacket(packet);
        this.cancelled = cancelled;
        this.direction = direction;
        this.player = player;
    }
    
    protected PacketEvent(final Object packet, final ConnectionDirection direction, final Player player) {
        this(packet, false, direction, player);
    }
    
    public Packet getPacket() {
        return this.packet;
    }
    
    public void setPacket(final Packet packet) {
        this.packet = packet;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public ConnectionDirection getDirection() {
        return this.direction;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public enum ConnectionDirection
    {
        TO_CLIENT("TO_CLIENT", 0), 
        TO_Server("TO_Server", 1);
        
        private ConnectionDirection(final String name, final int ordinal) {
        }
    }
}
