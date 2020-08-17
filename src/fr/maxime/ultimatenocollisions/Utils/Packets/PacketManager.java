package fr.maxime.ultimatenocollisions.Utils.Packets;

import org.bukkit.event.*;
import org.bukkit.plugin.*;

import fr.maxime.ultimatenocollisions.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

public class PacketManager implements Listener
{
    private static PacketManager instance;
    private final Map<UUID, PacketInjector> injectorMap;
    
    private PacketManager(final Plugin plugin) {
        this.injectorMap = new HashMap<UUID, PacketInjector>();
        Bukkit.getPluginManager().registerEvents((Listener)this, plugin);
    }
    
    public static synchronized PacketManager getInstance() {
        if (PacketManager.instance == null) {
            PacketManager.instance = new PacketManager((Plugin)Main.getInstance());
        }
        return PacketManager.instance;
    }
    
    public void addListener(final PacketListener listener, final Player player) {
        Objects.requireNonNull(listener, "listener can not be null");
        Objects.requireNonNull(player, "player can not be null");
        synchronized (this.injectorMap) {
            if (this.injectorMap.containsKey(player.getUniqueId())) {
                this.injectorMap.get(player.getUniqueId()).addPacketListener(listener);
            }
            else {
                try {
                    final PacketInjector injector = new PacketInjector(player);
                    injector.addPacketListener(listener);
                    this.injectorMap.put(player.getUniqueId(), injector);
                }
                catch (Exception ex) {}
            }
        }
        // monitorexit(this.injectorMap)
    }
    
    public void removeListener(final PacketListener listener, final Player player) {
        Objects.requireNonNull(listener, "listener can not be null");
        Objects.requireNonNull(player, "player can not be null");
        synchronized (this.injectorMap) {
            if (!this.injectorMap.containsKey(player.getUniqueId())) {
                // monitorexit(this.injectorMap)
                return;
            }
            final PacketInjector injector = this.injectorMap.get(player.getUniqueId());
            injector.removePacketListener(listener);
            if (injector.getListenerAmount() < 1) {
                injector.detach();
                this.injectorMap.remove(player.getUniqueId());
            }
        }
        // monitorexit(this.injectorMap)
    }
    
    public void removeAllListeners(final UUID uuid) {
        Objects.requireNonNull(uuid, "uuid can not be null");
        synchronized (this.injectorMap) {
            if (this.injectorMap.containsKey(uuid)) {
                this.injectorMap.get(uuid).detach();
                this.injectorMap.remove(uuid);
            }
        }
        // monitorexit(this.injectorMap)
    }
}
