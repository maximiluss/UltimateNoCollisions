package fr.maxime.ultimatenocollisions.Utils.Packets;

public enum PacketType
{
    PlayOut("PlayOut", 0, "PlayOut"), 
    PlayIn("PlayIn", 1, "PlayIn");
    
    public String prefix;
    
    private PacketType(final String name, final int ordinal, final String prefix) {
        this.prefix = prefix;
    }
}
