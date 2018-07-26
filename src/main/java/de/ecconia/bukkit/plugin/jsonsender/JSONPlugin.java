package de.ecconia.bukkit.plugin.jsonsender;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import de.ecconia.bukkit.plugin.jsonsender.sender.ReflectSender;
import de.ecconia.bukkit.plugin.jsonsender.sender.TellrawSender;

public class JSONPlugin extends JavaPlugin implements JSONPluginAPI
{
	public static final String prefix = ChatColor.WHITE + "[" + ChatColor.GOLD + "JSONSender" + ChatColor.WHITE + "] ";
	
	private static JSONPlugin inst;
	
	@Override
	public void onEnable()
	{
		inst = this;
		
		getServer().getServicesManager().register(JSONPluginAPI.class, this, this, ServicePriority.Normal);
	}
	
	@Override
	public boolean json(Player player, String json)
	{
		if(ReflectSender.send(player, json))
		{
			return true;
		}
		
		if(TellrawSender.send(player, json))
		{
			return true;
		}
		
		return false;
	}
	
	public static JSONPlugin getInstance()
	{
		return inst;
	}
}
