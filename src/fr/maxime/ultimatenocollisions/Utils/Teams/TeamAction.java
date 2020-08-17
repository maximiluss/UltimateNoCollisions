package fr.maxime.ultimatenocollisions.Utils.Teams;

public enum TeamAction
{
    CREATE("CREATE", 0, 0), 
    DISBAND("DISBAND", 1, 1), 
    UPDATE("UPDATE", 2, 2), 
    ADD_PLAYER("ADD_PLAYER", 3, 3), 
    REMOVE_PLAYER("REMOVE_PLAYER", 4, 4);
    
    private int minecraftId;
    
    private TeamAction(final String name, final int ordinal, final int minecraftId) {
        this.minecraftId = minecraftId;
    }
    
    public int getMinecraftId() {
        return this.minecraftId;
    }
    
    public static TeamAction fromId(final int id) {
        TeamAction[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final TeamAction rule = values[i];
            if (rule.getMinecraftId() == id) {
                return rule;
            }
        }
        return null;
    }
}
