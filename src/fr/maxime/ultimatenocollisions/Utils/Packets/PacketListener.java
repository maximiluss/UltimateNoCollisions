package fr.maxime.ultimatenocollisions.Utils.Packets;

public interface PacketListener
{
    void onPacketReceived(final PacketEvent p0);
    
    void onPacketSend(final PacketEvent p0);
}
