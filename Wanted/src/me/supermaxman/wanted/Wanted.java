package me.supermaxman.wanted;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;



public class Wanted extends JavaPlugin {
	public static Wanted plugin;
	public Configuration configuration;
	public static final Logger log = Logger.getLogger("Minecraft");
	public static ScoreboardManager scoreboardManager;
	public static HashMap<UUID, Scoreboard> scoreboardMap = new HashMap<UUID, Scoreboard>();
	public static HashMap<UUID, BukkitTask> wantedTasks = new HashMap<UUID, BukkitTask>();
	public static HashMap<Stat, HashMap<UUID, Integer>> statMaps = new HashMap<Stat, HashMap<UUID, Integer>>();
	public static BukkitTask updateTask = null;
	public static Economy economy = null;

	public void onEnable() {
		plugin = this;
		if (!setupEconomy() ) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		configuration = new Configuration(this);
		scoreboardManager = getServer().getScoreboardManager();
		getServer().getPluginManager().registerEvents(new WantedListener(), plugin);
		log.info(getName() + " has been enabled.");
		loadStats();
		runUpdateTask();
	}

	public void onDisable() {

		log.info(getName() + " has been disabled.");
	}
	public void loadStats() {
		YamlConfiguration statC = this.configuration.getConfig("stats");
		String statGroup;
		Stat stat;
		for (Iterator<String> localIterator1 = statC.getKeys(false).iterator(); localIterator1.hasNext(); ) { 
			statGroup = (String)localIterator1.next();
			stat = Stat.valueOf(statGroup.toUpperCase());

			statMaps.put(stat, new HashMap<UUID, Integer>());

			for (String player : statC.getConfigurationSection(statGroup).getKeys(false))
				statMaps.get(stat).put(UUID.fromString(player), Integer.valueOf(statC.getInt(statGroup + "." + player)));  
		}

	}

	public void saveStats() {
		YamlConfiguration statC = this.configuration.getConfig("stats");

		for (Stat statGroup : statMaps.keySet()) {
			statC.createSection(statGroup.name(), (Map<?, ?>)statMaps.get(statGroup));
		}

		this.configuration.saveConfig("stats");
	}

	public static int getStat(Stat statGroup, Player player) {
		return getStat(statGroup, player.getUniqueId());
	}

	public static int getStat(Stat statGroup, UUID uuid) {
		if (!(statMaps.get(statGroup).containsKey(uuid))) {
			setStat(statGroup, uuid, 0);
		}
		return statMaps.get(statGroup).get(uuid).intValue();
	}

	public static void setStat(Stat statGroup, UUID uuid, int score) {
		statMaps.get(statGroup).put(uuid, Integer.valueOf(score));
	}

	public void resetWantedTimer(Player player){
		UUID uuid = player.getUniqueId();

		if (wantedTasks.containsKey(player.getUniqueId())) {
			((BukkitTask)wantedTasks.remove(player.getUniqueId())).cancel();
		}

		startWantedTask(player, getStat(Stat.WANTED, uuid));
	}

	public static void resetWanted(Player player) {
		if (wantedTasks.containsKey(player.getUniqueId())) {
			((BukkitTask)wantedTasks.remove(player.getUniqueId())).cancel();
		}

		statMaps.get(Stat.WANTED).put(player.getUniqueId(), Integer.valueOf(0));
	}

	public static void incrementWanted(Player player) {
		UUID uuid = player.getUniqueId();

		statMaps.get(Stat.WANTED).put(uuid, Integer.valueOf(Math.min(5, getStat(Stat.WANTED, uuid) + 1)));

		startWantedTask(player, getStat(Stat.WANTED, uuid));
	}

	public void decrementWanted(Player player) {
		UUID uuid = player.getUniqueId();
		statMaps.get(Stat.WANTED).put(uuid, Integer.valueOf(Math.max(0, getStat(Stat.WANTED, uuid) - 1)));

		if (getStat(Stat.WANTED, uuid) > 0)
			startWantedTask(player, getStat(Stat.WANTED, uuid));
	}

	private static void startWantedTask(final Player player, int score){

		if (wantedTasks.containsKey(player.getUniqueId())) {
			wantedTasks.remove(player.getUniqueId()).cancel();
		}
		wantedTasks.put(player.getUniqueId(), plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
			public void run() {
				plugin.decrementWanted(player);
				plugin.updateScoreboard(player);
			}
		}, plugin.configuration.WANTED_COOLDOWN * 20));
	}


	private boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}


	private void runUpdateTask(){
		if (updateTask != null) {
			updateTask.cancel();
		}

		updateTask = getServer().getScheduler().runTaskTimer(this, new Runnable(){
			public void run() {
				for (Player player : Wanted.this.getServer().getOnlinePlayers())
					Wanted.this.updateScoreboard(player);
			}
		}
		, 20L, this.configuration.UPDATE_INTERVAL * 20);
	}

	public void updateScoreboard(Player player){
		Scoreboard pBoard = scoreboardMap.containsKey(player.getUniqueId()) ? (Scoreboard)scoreboardMap.get(player.getUniqueId()) : scoreboardManager.getNewScoreboard();
		if (pBoard.getObjective("main") != null) {
			pBoard.getObjective("main").unregister();
		}
		Objective main = pBoard.registerNewObjective("main", "dummy");
		main.setDisplayName(getWantedString(player));
		main.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (String fLine : configuration.BOARD_FORMAT) {
			if (fLine.equalsIgnoreCase("%kills%")) {
				main.getScore(ChatColor.GREEN + "Kills").setScore(getStat(Stat.KILLS, player));
			}else if (fLine.equalsIgnoreCase("%deaths%")) {
				main.getScore(ChatColor.RED + "Deaths").setScore(getStat(Stat.DEATHS, player));
			}else if (fLine.equalsIgnoreCase("%killstreak%")) {
				main.getScore(ChatColor.DARK_PURPLE + "Kill Streak").setScore(getStat(Stat.KILLSTREAKS, player));
			}else if (fLine.equalsIgnoreCase("%wanted%")) {
				main.getScore(ChatColor.DARK_RED + "Wanted").setScore(getStat(Stat.WANTED, player));
			}else if (fLine.equalsIgnoreCase("%money%")) {
				main.getScore(ChatColor.YELLOW + "Balance").setScore(economy.hasAccount(player.getName()) ? (int)economy.getBalance(player.getName()) : 0);
			}else if (fLine.equalsIgnoreCase("%online%")) {
				main.getScore(ChatColor.GOLD + "Online").setScore(getServer().getOnlinePlayers().length);
			}else {
				main.getScore(ChatColor.translateAlternateColorCodes('&', fLine).replace("%player%", player.getName())).setScore(2147483647);
			}
		}
		player.setScoreboard(pBoard);
	}

	private String getWantedString(Player player) {
		StringBuilder string = new StringBuilder();

		int wanted = getStat(Stat.WANTED, player);

		for (int i = 0; i < wanted; i++) {
			string.append('★');
			string.append(' ');
		}

		string.append(ChatColor.DARK_GRAY);

		for (int i = 0; i < 5 - wanted; i++) {
			string.append('★');
			string.append(' ');
		}

		return string.toString().trim();
	}
}