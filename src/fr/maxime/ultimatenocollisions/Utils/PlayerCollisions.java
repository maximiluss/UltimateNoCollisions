package fr.maxime.ultimatenocollisions.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.maxime.ultimatenocollisions.Utils.Packets.PacketAdapter;
import fr.maxime.ultimatenocollisions.Utils.Packets.PacketEvent;
import fr.maxime.ultimatenocollisions.Utils.Packets.PacketManager;
import fr.maxime.ultimatenocollisions.Utils.Teams.CollisionRule;
import fr.maxime.ultimatenocollisions.Utils.Teams.TeamAction;
import fr.maxime.ultimatenocollisions.Utils.Teams.TeamPacket;
import fr.maxime.ultimatenocollisions.Utils.Teams.TeamUtils;

public class PlayerCollisions implements Listener
{
    private CollisionPacketListener collisionPacketListener;
    private Map<Player, TeamPacket> playerTeamMap;
    
    public PlayerCollisions() {
        this.collisionPacketListener = new CollisionPacketListener();
        this.playerTeamMap = Collections.synchronizedMap(new WeakHashMap<Player, TeamPacket>());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(final PlayerJoinEvent e) {
        PacketManager.getInstance().addListener(this.collisionPacketListener, e.getPlayer());
        this.createOrUpdateTeam(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(final PlayerChangedWorldEvent e) {
        this.createOrUpdateTeam(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        PacketManager.getInstance().removeAllListeners(event.getPlayer().getUniqueId());
    }
    
    private void createOrUpdateTeam(final Player player) {
        final CollisionRule collisionRule = CollisionRule.NEVER;
        if (this.playerTeamMap.containsKey(player)) {
            final TeamPacket teamPacket = this.playerTeamMap.get(player);
            teamPacket.setTeamAction(TeamAction.UPDATE);
            teamPacket.setCollisionRule(collisionRule);
            teamPacket.send(player);
        }
        else {
            this.createAndSendNewTeam(player, collisionRule);
        }
    }
    
    private void createAndSendNewTeam(final Player player, final CollisionRule collisionRule) {
        final TeamPacket newTeamPacket = TeamUtils.craftTeamCreatePacket(player, collisionRule);
        this.playerTeamMap.put(player, newTeamPacket);
        newTeamPacket.send(player);
    }
    
    public void reload() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.createOrUpdateTeam(player);
        }
    }
    
    private class CollisionPacketListener extends PacketAdapter
    {
        private final Class<?> targetClass;
        
        private CollisionPacketListener() {
            this.targetClass = Reflector.getClass(ClassType.NMS, "PacketPlayOutScoreboardTeam");
        }
        
        @Override
        public void onPacketSend(final PacketEvent packetEvent) {
            if (packetEvent.getPacket().getPacketClass() != this.targetClass) {
                return;
            }
            final Object nmsPacket = packetEvent.getPacket().getNMSPacket();
            final CollisionRule collisionRule = CollisionRule.NEVER;
            this.updateToPacket(packetEvent.getPlayer(), PlayerCollisions.this.playerTeamMap.computeIfAbsent(packetEvent.getPlayer(), player -> new TeamPacket()), nmsPacket);
            if (TeamUtils.targetsPlayer(nmsPacket, packetEvent.getPlayer())) {
                this.updateToPacket(packetEvent.getPlayer(), PlayerCollisions.this.playerTeamMap.get(packetEvent.getPlayer()), nmsPacket);
            }
            TeamUtils.setCollisionRule(nmsPacket, collisionRule);
            final TeamPacket teamPacket = PlayerCollisions.this.playerTeamMap.get(packetEvent.getPlayer());
            if (teamPacket == null || !teamPacket.teamExists()) {
                PlayerCollisions.this.createAndSendNewTeam(packetEvent.getPlayer(), collisionRule);
            }
        }
        
        private void updateToPacket(final Player player, final TeamPacket teamPacket, final Object nmsPacket) {
            teamPacket.adjustToUpdate(nmsPacket);
            if (!teamPacket.teamExists()) {
                PlayerCollisions.this.playerTeamMap.remove(player);
            }
        }
    }
}
