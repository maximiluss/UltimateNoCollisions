package fr.maxime.ultimatenocollisions.Utils;

public enum ClassType
{
    NMS("NMS", 0, "net.minecraft.server"), 
    CRAFTBUKKIT("CRAFTBUKKIT", 1, "org.bukkit.craftbukkit");
    
    private String pkg;
    
    private ClassType(final String name, final int ordinal, final String pkg) {
        this.pkg = pkg;
    }
    
    public String getPackage() {
        return this.pkg;
    }
}
