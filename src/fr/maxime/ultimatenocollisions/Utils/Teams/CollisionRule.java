package fr.maxime.ultimatenocollisions.Utils.Teams;

public enum CollisionRule
{
    NEVER("NEVER", 0, "never");
    
    private String name;
    
    private CollisionRule(final String name2, final int ordinal, final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
