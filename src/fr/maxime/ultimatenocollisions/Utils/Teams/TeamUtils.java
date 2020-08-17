package fr.maxime.ultimatenocollisions.Utils.Teams;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.maxime.ultimatenocollisions.Utils.ClassType;
import fr.maxime.ultimatenocollisions.Utils.Reflector;

public class TeamUtils
{
    private static final VersionData VERSION_DATA;
    
    static {
        VERSION_DATA = new VersionData();
    }
    
    public static void setCollisionRule(final Object packet, final CollisionRule collisionRule) {
        try {
            TeamUtils.VERSION_DATA.getFieldCollisionRule().set(packet, collisionRule.getName());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting collision field", e);
        }
    }
    
    public static TeamAction getPacketAction(final Object packet) {
        try {
            return TeamAction.fromId((int)TeamUtils.VERSION_DATA.getFieldAction().get(packet));
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error getting action field", e);
        }
    }
    
    public static void setPacketAction(final Object packet, final TeamAction action) {
        try {
            TeamUtils.VERSION_DATA.getFieldAction().set(packet, action.getMinecraftId());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting action field", e);
        }
    }
    
    public static boolean targetsPlayer(final Object packet, final Player player) {
        final Collection<String> teamMembers = getTeamMembers(packet);
        return teamMembers != null && teamMembers.contains(player.getName());
    }
    
    public static TeamPacket craftTeamCreatePacket(final Player player, final CollisionRule collisionRule) {
        final String teamName = UUID.randomUUID().toString().substring(0, 15);
        final Object teamPacket = TeamUtils.VERSION_DATA.createTeamPacket(TeamAction.CREATE, collisionRule, teamName, Collections.singletonList(player));
        return new TeamPacket(teamPacket);
    }
    
    static Collection<String> getTeamMembers(final Object packet) {
        try {
            @SuppressWarnings("unchecked")
			final Collection<String> identifiers = (Collection<String>) TeamUtils.VERSION_DATA.getFieldPlayerNames().get(packet);
            return identifiers;
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error getting players", e);
        }
    }
    
    static void setTeamMembers(final Object packet, final Collection<?> newMembers) {
        try {
            TeamUtils.VERSION_DATA.getFieldPlayerNames().set(packet, newMembers);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Error setting members", e);
        }
    }
    
    static Object createTeamNMSPacket(final TeamAction action, final CollisionRule collisionRule, final String name, final Collection<Player> players) {
        return TeamUtils.VERSION_DATA.createTeamPacket(action, collisionRule, name, players);
    }
    
    private static class VersionData
    {
        private final Field fieldCollisionRule;
        private final Field fieldPlayerNames;
        private final Field fieldAction;
        private final Field fieldName;
        private final Constructor<?> constructorTeamPacket;
        
        VersionData() {
            final Class<?> packetClass = Reflector.getClass(ClassType.NMS, "PacketPlayOutScoreboardTeam");
            this.fieldCollisionRule = Reflector.getInaccessibleField(packetClass, "f");
            this.fieldPlayerNames = Reflector.getInaccessibleField(packetClass, "h");
            this.fieldAction = Reflector.getInaccessibleField(packetClass, "i");
            this.fieldName = Reflector.getInaccessibleField(packetClass, "a");
            this.constructorTeamPacket = Reflector.getConstructor(packetClass, 0);
        }
        
        Field getFieldCollisionRule() {
            return this.fieldCollisionRule;
        }
        
        Field getFieldPlayerNames() {
            return this.fieldPlayerNames;
        }
        
        Field getFieldAction() {
            return this.fieldAction;
        }
        
        Object createTeamPacket(final TeamAction action, final CollisionRule collisionRule, final String name, final Collection<Player> players) {
            try {
                final Object packet = this.constructorTeamPacket.newInstance(new Object[0]);
                this.getFieldPlayerNames().set(packet, players.stream().map((Function<? super Player, ?>)OfflinePlayer::getName).collect(Collectors.toList()));
                this.getFieldCollisionRule().set(packet, collisionRule.getName());
                this.getFieldAction().set(packet, action.getMinecraftId());
                this.fieldName.set(packet, name);
                return packet;
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException("Error creating team packet", e);
            }
        }
    }
}
