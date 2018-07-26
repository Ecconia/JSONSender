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
	
	private ReflectSender reflect;
	private TellrawSender tellraw;
	
	@Override
	public void onEnable()
	{
		inst = this;
		
		reflect = new ReflectSender();
		tellraw = new TellrawSender();
		
		getServer().getServicesManager().register(JSONPluginAPI.class, this, this, ServicePriority.Normal);
	}
	
	@Override
	public void onDisable()
	{
		inst = null;
		tellraw = null;
		reflect = null;
	}
	
	public static JSONPlugin getInstance()
	{
		return inst;
	}

	@Override
	public boolean json(Player player, String json)
	{
		return reflect.send(player, json) || tellraw.send(player, json);
	}
	
	@Override
	public boolean json(Player player, String json, String fallback)
	{
		boolean worked = json(player, json);
		
		if(!worked)
		{
			player.sendMessage(fallback);
		}
		
		return worked;
	}
}
