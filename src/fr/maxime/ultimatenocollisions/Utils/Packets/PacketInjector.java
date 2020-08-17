package fr.maxime.ultimatenocollisions.Utils.Packets;

import java.lang.ref.*;
import org.bukkit.entity.*;

import fr.maxime.ultimatenocollisions.Utils.*;

import java.util.concurrent.*;

import io.netty.channel.*;
import java.util.*;

class PacketInjector extends ChannelDuplexHandler
{
    private volatile boolean isClosed;
    private Channel channel;
    private List<PacketListener> packetListeners;
    private WeakReference<Player> playerWeakReference;
    
    PacketInjector(final Player player) {
        this.packetListeners = new CopyOnWriteArrayList<PacketListener>();
        Objects.requireNonNull(player, "player can not be null!");
        this.playerWeakReference = new WeakReference<Player>(player);
        try {
            this.attach(player);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void attach(final Player player) throws Exception {
        final Object playerConnection = PacketSender.getInstance().getConnection(player);
        final Object manager = Reflector.getFieldValue(playerConnection, "networkManager");
        this.channel = (Channel)Reflector.getFieldValue(manager, "channel");
        if (this.channel.pipeline().get("ocm_handler") != null) {
            debug("Old listener lingered around", new Object[0]);
            final ChannelHandler old = this.channel.pipeline().get("ocm_handler");
            if (old instanceof PacketInjector) {
                debug("Detaching old listener", new Object[0]);
                ((PacketInjector)old).detach();
            }
            this.channel.pipeline().remove("ocm_handler");
        }
        try {
            this.channel.pipeline().addBefore("packet_handler", "ocm_handler", (ChannelHandler)this);
        }
        catch (NoSuchElementException e) {
            throw new NoSuchElementException("No base handler found. Was the player instantly disconnected?");
        }
    }
    
    void detach() {
        debug("Detaching injector... (%d)", this.hashCode());
        if (this.isClosed || !this.channel.isOpen()) {
            debug("Closed(%b) or channel closed(%b) already! (%d)", this.isClosed, !this.channel.isOpen(), this.hashCode());
            return;
        }
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove((ChannelHandler)this);
            this.channel = null;
            this.isClosed = true;
            this.playerWeakReference.clear();
            this.packetListeners.clear();
            debug("Injector successfully detached (%d)", this.hashCode());
        });
    }
    
    void addPacketListener(final PacketListener packetListener) {
        Objects.requireNonNull(packetListener, "packetListener can not be null");
        if (this.isClosed) {
            throw new IllegalStateException("Channel already closed. Adding of listener invalid");
        }
        this.packetListeners.add(packetListener);
    }
    
    void removePacketListener(final PacketListener packetListener) {
        this.packetListeners.remove(packetListener);
    }
    
    int getListenerAmount() {
        return this.packetListeners.size();
    }
    
    public void write(final ChannelHandlerContext channelHandlerContext, final Object packet, final ChannelPromise channelPromise) throws Exception {
        if (this.playerWeakReference == null || this.playerWeakReference.get() == null) {
            debug("playerWeakReference or its value is null. This should NOT happen at this stage. (write@%d)", this.hashCode());
            this.detach();
            super.write(channelHandlerContext, packet, channelPromise);
            return;
        }
        final PacketEvent event = new PacketEvent(packet, PacketEvent.ConnectionDirection.TO_CLIENT, this.playerWeakReference.get());
        for (final PacketListener packetListener : this.packetListeners) {
            try {
                if (this.isClosed) {
                    continue;
                }
                packetListener.onPacketSend(event);
            }
            catch (Exception ex) {}
        }
        if (!event.isCancelled()) {
            super.write(channelHandlerContext, packet, channelPromise);
        }
    }
    
    public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object packet) throws Exception {
        if (this.playerWeakReference == null || this.playerWeakReference.get() == null) {
            debug("playerWeakReference or its value is null. This should NOT happen at this stage. (read@%d)", this.hashCode());
            this.detach();
            super.channelRead(channelHandlerContext, packet);
            return;
        }
        final PacketEvent event = new PacketEvent(packet, PacketEvent.ConnectionDirection.TO_Server, this.playerWeakReference.get());
        for (final PacketListener packetListener : this.packetListeners) {
            try {
                if (this.isClosed) {
                    continue;
                }
                packetListener.onPacketReceived(event);
            }
            catch (Exception ex) {}
        }
        if (!event.isCancelled()) {
            super.channelRead(channelHandlerContext, packet);
        }
    }
    
    private static void debug(final String message, final Object... formatArgs) {
    }
}
