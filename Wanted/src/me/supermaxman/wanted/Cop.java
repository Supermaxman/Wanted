package me.supermaxman.wanted;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cop{
	public static ArrayList<Cop> cops = new ArrayList<Cop>();
	private Wanted plugin;
	private PigZombie entity;
	private boolean first = true;
	public Cop(Wanted plugin) {
		this.plugin = plugin;
	}


	public void spawn(Player wanted) {
		PigZombie cop = (PigZombie)wanted.getWorld().spawnEntity(findSpawn(wanted), EntityType.PIG_ZOMBIE);
		cop.setCustomName(this.plugin.configuration.COP_NAME);
		cop.setCustomNameVisible(!this.plugin.configuration.COP_NAME.equalsIgnoreCase("NONE"));
		cop.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, this.plugin.configuration.COP_SPEED, false));
		cop.getEquipment().setArmorContents(this.plugin.configuration.COP_ARMOR);
		cop.setAnger(2147483647);
		cop.setTarget(wanted);
		setEntity(cop);
		cops.add(this);
	}

	public Location findSpawn(Player p) {
		Random r = new Random();

		int x = r.nextInt(31)-15;
		int z = r.nextInt(31)-15;
		Location loc = p.getLocation().add(x, 0, z);
		loc.setY(heighestBlockAtIgnoreRoof(p.getLocation()));
		return loc;
	}

	public void despawn(PigZombie cop) {
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


	public PigZombie getEntity() {
		return entity;
	}


	public void setEntity(PigZombie entity) {
		this.entity = entity;
	}


	public boolean isFirst() {
		return first;
	}


	public void setFirst(boolean first) {
		this.first = first;
	}
}