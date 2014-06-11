package me.supermaxman.wanted;

import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;


public class WantedListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Wanted.plugin.updateScoreboard(e.getPlayer());
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCopDamage(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity target = e.getEntity();

		if ((damager instanceof PigZombie)) {
			if (((PigZombie) damager).isCustomNameVisible()) {
				if(((PigZombie) damager).getCustomName().equals(Wanted.plugin.configuration.COP_NAME))e.setDamage(Wanted.plugin.configuration.COP_DAMAGE);
			}
		}else if ((target instanceof PigZombie) && (damager instanceof Player) && isCop(target)){
			if (Wanted.getStat(Stat.WANTED, damager.getUniqueId()) == 0) {

				Wanted.incrementWanted((Player)damager);
			}else {
				Wanted.plugin.resetWantedTimer((Player)damager);
			}
		}
	}

	@EventHandler
	public void onCopDeath(EntityDeathEvent event)
	{
		if (((event.getEntity() instanceof PigZombie)) && (event.getEntity().getKiller() != null)) {
			PigZombie entity = (PigZombie)event.getEntity();

			if (isCop(entity)) {
				Player killer = entity.getKiller();
				Wanted.incrementWanted(killer);
				new Cop(Wanted.plugin).spawn(killer);
			}
		}
	}

	@EventHandler
	public void onCopTarget(EntityTargetEvent e) {
		if (((e.getEntity() instanceof PigZombie)) && (isCop(e.getEntity())))
			if ((e.getTarget() instanceof Player)) {
				if (Wanted.getStat(Stat.WANTED, (Player)e.getTarget()) == 0)
					e.setCancelled(true);
			}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e){
		Wanted.setStat(Stat.DEATHS, e.getEntity().getUniqueId(), Wanted.getStat(Stat.DEATHS, e.getEntity()) + 1);
		Wanted.setStat(Stat.KILLSTREAKS, e.getEntity().getUniqueId(), 0);
		Wanted.resetWanted(e.getEntity());

		if (e.getEntity().getKiller() == null) {
			return;
		}

		Player killer = e.getEntity().getKiller();

		Wanted.setStat(Stat.KILLS, killer.getUniqueId(), Wanted.getStat(Stat.KILLS, killer) + 1);
		Wanted.setStat(Stat.KILLSTREAKS, killer.getUniqueId(), Wanted.getStat(Stat.KILLSTREAKS, killer) + 1);
		Wanted.incrementWanted(killer);

		new Cop(Wanted.plugin).spawn(killer);
	}

	boolean isCop(Entity e){
		if(e instanceof PigZombie) {
			if (((PigZombie) e).isCustomNameVisible()) {
				if(((PigZombie) e).getCustomName().equals(Wanted.plugin.configuration.COP_NAME)) {
					return true;
				}
			}
		}
		return false;
	}
}
