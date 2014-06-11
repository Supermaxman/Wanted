package me.supermaxman.wanted;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class Configuration
{
	private Wanted plugin;
	private TreeMap<String, YamlConfiguration> loadedConfigs = new TreeMap<String, YamlConfiguration>(String.CASE_INSENSITIVE_ORDER);
	public String BOARD_TITLE;
	public List<String> BOARD_FORMAT;
	public int UPDATE_INTERVAL;
	public String COP_NAME;
	public int COP_SPEED;
	public int COP_DAMAGE;
	public int WANTED_COOLDOWN;
	public ItemStack[] COP_ARMOR;

	public Configuration(Wanted plugin){
		this.plugin = plugin;
		loadConfig("config");
		loadConfig("stats");

		this.BOARD_TITLE = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("boardTitle"));
		this.BOARD_FORMAT = plugin.getConfig().getStringList("boardFormat");
		this.UPDATE_INTERVAL = plugin.getConfig().getInt("updateInterval");
		this.COP_NAME = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("copName"));
		this.COP_SPEED = plugin.getConfig().getInt("copSpeed");
		this.COP_DAMAGE = plugin.getConfig().getInt("copDamage");
		this.WANTED_COOLDOWN = plugin.getConfig().getInt("wantedCooldown", 60);
		ArrayList<ItemStack> copArmor = new ArrayList<ItemStack>();
		for (String material : plugin.getConfig().getStringList("copArmor"))
			copArmor.add(new ItemStack(Material.matchMaterial(material), 1));
		this.COP_ARMOR = ((ItemStack[])copArmor.toArray(new ItemStack[copArmor.size()]));
	}

	public boolean isConfigLoaded(String fileName) {
		return this.loadedConfigs.containsKey(fileName);
	}

	public YamlConfiguration getConfig(String fileName) {
		return (YamlConfiguration)this.loadedConfigs.get(fileName);
	}

	public void loadConfig(String fileName) {
		File configF = new File(this.plugin.getDataFolder(), fileName + ".yml");
		if (!configF.exists()) {
			if (this.plugin.getResource(fileName + ".yml") != null) this.plugin.saveResource(fileName + ".yml", false); else {
				try
				{
					configF.createNewFile();
				} catch (IOException e) {
					this.plugin.getLogger().severe("Error! Could not create custom configuration: " + fileName);
					e.printStackTrace();
				}
			}
		}
		if (!isConfigLoaded(fileName)) this.loadedConfigs.put(fileName, YamlConfiguration.loadConfiguration(configF)); 
	}

	public void reloadConfig(String fileName)
	{
		File configF = new File(this.plugin.getDataFolder(), fileName + ".yml");
		if (!isConfigLoaded(fileName))
			this.plugin.getLogger().severe("Error! Tried to reload non-existent config file: " + fileName);
		else try {
			((YamlConfiguration)this.loadedConfigs.get(fileName)).load(configF);
		} catch (IOException e) {
			this.plugin.getLogger().severe("Error! Could not reload custom configuration: " + fileName);
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			this.plugin.getLogger().severe("Error! Could not reload custom configuration: " + fileName);
			e.printStackTrace();
		} 
	}

	public void saveConfig(String fileName)
	{
		File configF = new File(this.plugin.getDataFolder(), fileName + ".yml");
		if (!isConfigLoaded(fileName))
			this.plugin.getLogger().severe("Error! Tried to save non-existent config file: " + fileName);
		else try {
			((YamlConfiguration)this.loadedConfigs.get(fileName)).save(configF);
		} catch (IOException e) {
			this.plugin.getLogger().severe("Error! Could not save custom configuration: " + fileName);
			e.printStackTrace();
		}
	}
}