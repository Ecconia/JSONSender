package de.ecconia.bukkit.plugin.jsonsender.sender;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.ecconia.bukkit.plugin.jsonsender.JSONException;
import de.ecconia.bukkit.plugin.jsonsender.JSONPlugin;

public class ReflectSender
{
	//If setup has been made
	private AtomicBoolean setup = new AtomicBoolean(false);
	
	private boolean sendMessage;
	private boolean sendPacket;
	
	//Reflect both:
	private Method _Handle;
	private Method _Serialize;
	
	//Reflect one:
	private Method _SendMessage;
	
	//Refect two:
	private Field _Connection;
	private Method _SendPacket;
	private Constructor<?> _Packet;
	
	private void setup0(Player player)
	{
		String doing = "starting";
		
		String nmsPackage;
		Object entityPlayer;
		Class<?> entityPlayerClass;
		
		try
		{
			//CLASS(player)
			doing = "getting player class";
			Class<?> craftPlayerClass = player.getClass();
			//Validation:
			if(!craftPlayerClass.getSimpleName().equals("CraftPlayer"))
			{
				throw new WrongClassNameException();
			}
			
			//METHOD(class-craftplayer, "getHandle")
			doing = "getting getHandle from craftPlayerClass";
			_Handle = craftPlayerClass.getDeclaredMethod("getHandle");
			
			//USE(HANDLE, player)
			doing = "getting entityPlayer from getHandle on player";
			entityPlayer = _Handle.invoke(player);
			
			//CLASS(obj-entityplayer)
			doing = "getting entityPlayerClass from entityPlayer";
			entityPlayerClass = entityPlayer.getClass();
			//Validation:
			if(!entityPlayerClass.getSimpleName().equals("EntityPlayer"))
			{
				throw new WrongClassNameException();
			}
			
			//GET_NMS_VERSION(class-entityplayer)
			doing = "getting nmsPackage from entityPlayerClass name";
			nmsPackage = entityPlayerClass.getName().substring(0, entityPlayerClass.getName().length() - "EntityPlayer".length());
			//Alternative: ~ Bukkit.getServer().getClass().getName().split(".")[3]
			
			//GETCLASS(nms + "IChatBaseComponent$ChatSerializer")
			doing = "getting chatSerializerClass";
			Class<?> chatSerializerClass = Class.forName(nmsPackage + "IChatBaseComponent$ChatSerializer");
			//Alternative: chatBaseClass.getClasses()[0]
			
			//METHOD(class-chatbase_serializer, "a", String.class)
			doing = "getting serialization method a with parameter String from chatSerializerClass";
			_Serialize = chatSerializerClass.getDeclaredMethod("a", String.class);
		}
		catch (Exception e)
		{
			handleException(player.getServer().getConsoleSender(), e, doing);
			player.getServer().getConsoleSender().sendMessage(JSONPlugin.prefix + ChatColor.RED + "Sending JSON via reflection is not possible, please report to the developer.");
			setup.set(true);
			return;
		}
		
		// REFLECTION ONE #################################################
		
		try
		{
			//GETCLASS(nms + IChatBaseComponent)
			doing = "getting chatBaseClass";
			Class<?> chatBaseClass = Class.forName(nmsPackage + "IChatBaseComponent");
			
			//METHOD(class-entity_player, "sendMessage", "class-chatbase")
			doing = "getting method sendMessage with parameter chatBaseClass in entityPlayerClass";
			_SendMessage = entityPlayerClass.getDeclaredMethod("sendMessage", chatBaseClass);
			
			//Successfully got everything for reflection one
			sendMessage = true;
		}
		catch (Exception e)
		{
			handleException(player.getServer().getConsoleSender(), e, doing);
			player.getServer().getConsoleSender().sendMessage(JSONPlugin.prefix + ChatColor.YELLOW + "Sending JSON via \"sendMessage\" is not possible, trying fallback.");
		}
		
		// REFLECTION TWO #################################################
		
		try
		{
			//FIELD(class-entityplayer, "playerConnection")
			doing = "getting field playerConnection from entityPlayerClass";
			_Connection = entityPlayerClass.getDeclaredField("playerConnection");
			
			//USE(_CONNECTION, obj-entityplayer)
			doing = "getting connection for entityPlayer";
			Object obj_connection = _Connection.get(entityPlayer);
			
			//METHOD(obj_connection, "sendPacket")
			doing = "getting method sendPacket in obj_connection";
			for(Method method : obj_connection.getClass().getDeclaredMethods())
			{
				if(method.getName().equals("sendPacket"))
				{
					_SendPacket = method;
					break;
				}
			}
			//Using this method, to skip parameter classes
			
			//GETCLASS(nms + PacketPlayOutChat)
			doing = "getting packetChatClass";
			Class<?> packetChatClass = Class.forName(nmsPackage + "PacketPlayOutChat");
			
			//CONSTRUCTOR(packetChatClass)
			doing = "getting constructor for packetChatClass";
			for(Constructor<?> constructor : packetChatClass.getDeclaredConstructors())
			{
				Class<?>[] parameters = constructor.getParameterTypes();
				if(parameters.length == 1 && parameters[0].getName().endsWith("IChatBaseComponent"))
				{
					_Packet = constructor;
					break;
				}
			}
			
			//Successfully got everything for reflection two
			sendPacket = true;
		}
		catch (Exception e)
		{
			handleException(player.getServer().getConsoleSender(), e, doing);
			if(sendMessage)
			{
				player.getServer().getConsoleSender().sendMessage(JSONPlugin.prefix + ChatColor.YELLOW + "Sending JSON via \"sendPacket\" is not possible.");
			}
			else
			{
				player.getServer().getConsoleSender().sendMessage(JSONPlugin.prefix + ChatColor.RED + "Sending JSON via \"sendPacket\" is not possible, cannot use reflection, fallback to /tellraw.");
			}
		}
		
		setup.set(true);
	}
	
	private synchronized void setup(Player player)
	{
		//Check if its still unloaded
		if(!setup.get())
		{
			setup0(player);
		}
	}
	
	@SuppressWarnings("serial")
	private static class WrongClassNameException extends Exception
	{
	}
	
	private void handleException(ConsoleCommandSender console, Exception e, String doing)
	{
		console.sendMessage(JSONPlugin.prefix + ChatColor.RED + e.getClass().getSimpleName() + " was thrown while " + doing + ".");
		console.sendMessage(JSONPlugin.prefix + ChatColor.RED + "Message: " + e.getMessage());
	}
	
	public boolean send(Player player, String json)
	{
		if(!setup.get())
		{
			setup(player);
		}
		
		if(sendMessage)
		{
			try
			{
				//USE(_HANDLE, player)
				Object entityPlayer = _Handle.invoke(player);
				
				//USE(_SERIALIZE, null, json))
				Object chatbase;
				try
				{
					chatbase = _Serialize.invoke(null, json);
				}
				catch (InvocationTargetException e)
				{
					throw new JSONException("Invalid JSON format: " + e.getCause().getMessage());
				}
				
				//USE(_SENDMESSAGE, obj-entityplayer, obj-message)
				_SendMessage.invoke(entityPlayer, chatbase);
				
				return true;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				handleException(player.getServer().getConsoleSender(), e, "sending message via reflection \"sendMessage\" dumping this method." + (sendPacket ? ChatColor.YELLOW + " Falling back to other reflection method." : " Reflection is not possible anymore!"));
			}
			sendMessage = false;
		}
		
		if(sendPacket)
		{
			try
			{
				Object entityPlayer = _Handle.invoke(player);
				Object connection = _Connection.get(entityPlayer);
				
				Object chatbase;
				try
				{
					chatbase = _Serialize.invoke(null, json);
				}
				catch (InvocationTargetException e)
				{
					throw new JSONException("Invalid JSON format: " + e.getCause().getMessage());
				}
				
				Object packet = _Packet.newInstance(chatbase);
				_SendPacket.invoke(connection, packet);
				
				return true;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e)
			{
				handleException(player.getServer().getConsoleSender(), e, "sending message via reflection \"sendPacket\" dumping this method. Reflection is not possible anymore!");
			}
			sendPacket = false;
		}
		
		return false;
	}
}
