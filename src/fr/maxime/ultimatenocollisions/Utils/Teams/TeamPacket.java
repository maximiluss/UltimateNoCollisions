package fr.maxime.ultimatenocollisions.Utils.Teams;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import fr.maxime.ultimatenocollisions.Utils.Reflector;

public class TeamPacket
{
    private Object packetShallowClone;
    
    TeamPacket(final Object packet) {
        this.packetShallowClone = packet;
    }
    
    public TeamPacket() {
        this(TeamUtils.createTeamNMSPacket(TeamAction.CREATE, CollisionRule.NEVER, "placeholder", Collections.emptyList()));
    }
    
    public void adjustToUpdate(final Object updatePacket) {
        switch (TeamUtils.getPacketAction(updatePacket)) {
            case CREATE: {
                this.cloneFrom(updatePacket);
                break;
            }
            case DISBAND: {
                this.packetShallowClone = null;
                break;
            }
            case UPDATE: {
                this.cloneFrom(updatePacket);
                break;
            }
            case ADD_PLAYER: {
                this.adjustToPlayerAdded(updatePacket);
                break;
            }
            case REMOVE_PLAYER: {
                this.adjustToPlayerRemoved(updatePacket);
                break;
            }
        }
    }
    
    public boolean teamExists() {
        return this.packetShallowClone != null;
    }
    
    public void setCollisionRule(final CollisionRule collisionRule) {
        TeamUtils.setCollisionRule(this.packetShallowClone, collisionRule);
    }
    
    public void setTeamAction(final TeamAction teamAction) {
        TeamUtils.setPacketAction(this.packetShallowClone, teamAction);
    }
    
    public void send(final Player player) {
        Reflector.Packets.sendPacket(player, this.packetShallowClone);
    }
    
    private void adjustToPlayerAdded(final Object updatePacket) {
        final Set<String> newMembers = new HashSet<String>(TeamUtils.getTeamMembers(this.packetShallowClone));
        newMembers.addAll(TeamUtils.getTeamMembers(updatePacket));
        TeamUtils.setTeamMembers(this.packetShallowClone, newMembers);
    }
    
    private void adjustToPlayerRemoved(final Object updatePacket) {
        final Set<String> newMembers = new HashSet<String>(TeamUtils.getTeamMembers(this.packetShallowClone));
        newMembers.removeAll(TeamUtils.getTeamMembers(updatePacket));
        TeamUtils.setTeamMembers(this.packetShallowClone, newMembers);
    }
    
    private void cloneFrom(final Object packet) {
        try {
            Field[] declaredFields;
            for (int length = (declaredFields = packet.getClass().getDeclaredFields()).length, i = 0; i < length; ++i) {
                final Field declaredField = declaredFields[i];
                declaredField.setAccessible(true);
                declaredField.set(this.packetShallowClone, declaredField.get(packet));
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error cloning packet", e);
        }
    }
}
