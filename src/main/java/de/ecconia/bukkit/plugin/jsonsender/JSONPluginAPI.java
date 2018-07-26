package de.ecconia.bukkit.plugin.jsonsender;

import org.bukkit.entity.Player;

public interface JSONPluginAPI
{
	/**
	 * Attempts to send JSON-Text to a player by reflection or tellraw.
	 * 
	 * @param player Player which receives the JSON-Text
	 * @param json JSON-Text which will be sent to the player
	 * @return boolean - true if json has been sent to player
	 */
	boolean json(Player player, String json);
	
	/**
	 * Attempts to send JSON-Text to a player by reflection or tellraw, else it sends the provided fallback.
	 * 
	 * @param player Player which receives the JSON-Text
	 * @param json JSON-Text which will be sent to the player
	 * @param fallback message which will be sent in case that reflection or tellraw fails
	 * @return boolean - true if json has been sent to player by refelection or tellraw
	 */
	boolean json(Player player, String json, String fallback);
}
