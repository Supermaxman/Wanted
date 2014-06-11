package me.supermaxman.wanted;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Cop{
	private Wanted plugin;
	private Map<PigZombie, BukkitTask> copTaskMap = new HashMap<PigZombie, BukkitTask>();
	private Map<PigZombie, Integer> idleMap = new HashMap<PigZombie, Integer>();
	private Map<PigZombie, UUID> targetMap = new HashMap<PigZombie, UUID>();

	public Cop(Wanted plugin) {
		this.plugin = plugin;
	}

	public boolean is(PigZombie cop) {
		return (cop.getCustomName() != null) && (cop.getCustomName().equalsIgnoreCase(plugin.configuration.COP_NAME));
	}

	public boolean hasTarget(PigZombie cop) {
		return this.targetMap.containsKey(cop);
	}

	public void retarget(PigZombie cop) {
		cop.setAnger(2147483647);
		cop.setTarget(this.plugin.getServer().getPlayer((UUID)this.targetMap.get(cop)));
	}

	public void spawn(Player wanted) {
		PigZombie cop = (PigZombie)wanted.getWorld().spawnEntity(findSpawn(wanted), EntityType.PIG_ZOMBIE);
		cop.setCustomName(this.plugin.configuration.COP_NAME);
		cop.setCustomNameVisible(!this.plugin.configuration.COP_NAME.equalsIgnoreCase("NONE"));
		cop.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, this.plugin.configuration.COP_SPEED, false));
		cop.getEquipment().setArmorContents(this.plugin.configuration.COP_ARMOR);
		cop.setAnger(2147483647);
		cop.setTarget(wanted);

		this.targetMap.put(cop, wanted.getUniqueId());


	}

	public Location findSpawn(Player p) {
		Random r = new Random();

		int x = r.nextInt(41)-20;
		int z = r.nextInt(41)-20;
		Location loc = p.getLocation().add(x, 0, z);
		loc.setY(heighestBlockAtIgnoreRoof(p.getLocation()));
		return loc;
	}

	public void despawn(PigZombie cop) {
		if (this.copTaskMap.containsKey(cop)) {
			((BukkitTask)this.copTaskMap.remove(cop)).cancel();
		}

		this.idleMap.remove(cop);

		cop.remove();
	}

	@SuppressWarnings("deprecation")
	public int heighestBlockAtIgnoreRoof(Location loc) {
		int i = loc.getWorld().getSeaLevel();
		while (i < 250) {
			if (loc.getWorld().getBlockAt(loc.getBlockX(), i, loc.getBlockZ()).getTypeId() == 0) {
				break;
			}
			i++;
		}
		return i;
	}
}