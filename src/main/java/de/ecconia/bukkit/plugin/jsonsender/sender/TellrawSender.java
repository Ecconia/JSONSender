package de.ecconia.bukkit.plugin.jsonsender.sender;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import de.ecconia.bukkit.plugin.jsonsender.JSONPlugin;

public class TellrawSender
{
	private static Boolean tellrawExists;
	
	private static void setup(Player player)
	{
		//Test if command /tellraw exists:
		try {
			tellrawExists = player.getServer().dispatchCommand(new CommandSender() {
				public void setOp(boolean value) {}
				public boolean isOp() {return false;}
				public void removeAttachment(PermissionAttachment attachment) {}
				public void recalculatePermissions() {}
				public boolean isPermissionSet(Permission perm) {return false;}
				public boolean isPermissionSet(String name) {return false;}
				public boolean hasPermission(Permission perm) {return false;}
				public boolean hasPermission(String name) {return false;}
				public Set<PermissionAttachmentInfo> getEffectivePermissions() {return null;}
				public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {return null;}
				public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {return null;}
				public PermissionAttachment addAttachment(Plugin plugin, int ticks) {return null;}
				public PermissionAttachment addAttachment(Plugin plugin) {return null;}
				public void sendMessage(String[] messages) {}
				public void sendMessage(String message) {}
				public Server getServer() {return null;}
				public String getName() {return null;}
			}, "tellraw derps");
		} catch (CommandException e) {
			//lets assume it was mad, since the CommandSender was custom...
			//Well but at least /tellraw is installed.
			tellrawExists = true;
			
			player.getServer().getConsoleSender().sendMessage(JSONPlugin.prefix + ChatColor.RED + "Sending JSON with tellraw is not possible, tellraw is disabled. No way to send JSON to client left.");
		}
	}
	
	public static boolean send(Player player, String json)
	{
		if(tellrawExists == null)
		{
			setup(player);
		}
		
		if(tellrawExists)
		{
			player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "tellraw " + player.getName() + " " + json);
			return true;
		}
		
		return false;
	}
}
