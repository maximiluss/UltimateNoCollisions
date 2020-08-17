package fr.maxime.ultimatenocollisions;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.maxime.ultimatenocollisions.Utils.PlayerCollisions;

public class Main extends JavaPlugin
{
    private static Main instance;

	ConsoleCommandSender console = Bukkit.getConsoleSender();
	
    public void onEnable() {
        Main.instance = this;

		/** Loading Messages **/
        console.sendMessage("§f- §6====================================== §f-");
        console.sendMessage("§6      NoCollisions §ePlugin Is Enable");
        console.sendMessage("§e          Plugin by §6Maxime#3390");
        console.sendMessage("§e             Version: §61.0.0");
        console.sendMessage("§f- §6====================================== §f-");
        
        /** Loading Listeners **/
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerCollisions(), (Plugin)this);
    }
    
    public void onDisable() {
		/** Disabling Messages **/
        console.sendMessage("§f- §6====================================== §f-");
        console.sendMessage("§6      NoCollisions §ePlugin Is Disable");
        console.sendMessage("§e          Plugin by §6Maxime#3390");
        console.sendMessage("§e             Version: §61.0.0");
        console.sendMessage("§f- §6====================================== §f-");
    }
    
    public static Main getInstance() {
        return Main.instance;
    }
}
