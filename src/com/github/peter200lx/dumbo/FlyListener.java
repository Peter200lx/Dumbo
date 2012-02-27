package com.github.peter200lx.dumbo;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class FlyListener implements Listener {
	protected static final Logger log = Logger.getLogger("Minecraft");

	private static HashSet<String> hovering = new HashSet<String>();

	@EventHandler
	public void catchinteract(PlayerInteractEvent event){
		Player subject = event.getPlayer();
		if((Dumbo.bind != Material.AIR)							&&
				(Dumbo.bind==subject.getItemInHand().getType())	&&
				Dumbo.flyEnabled								&&
				Dumbo.hasPerm(subject,"dumbo.fly")				)
			this.fly(event);
	}

	@EventHandler
	public void catchMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		if (hovering.contains(player.getName()))
			player.setVelocity(player.getVelocity().setY(0.165));
	}

	@EventHandler
	public void catchFall(EntityDamageEvent event) {
		Entity ent = event.getEntity();
		if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)		&&
				ent instanceof Player										&&
				((Player)ent).getItemInHand().getType().equals(Dumbo.bind)	&&
				Dumbo.hasPerm((Player)ent,"dumbo.fly")						){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void catchLogOff(PlayerQuitEvent event) {
		if(hovering.contains(event.getPlayer().getName())) {
			hovering.remove(event.getPlayer().getName());
			if(Dumbo.debug) log.info("[Dumbo][catchLogOff] "+event.getPlayer().getName()+
					"Has logged off. Removed from hovering set");
		}
	}

	private void fly(PlayerInteractEvent event) {
		Action act = event.getAction();
		Player subject = event.getPlayer();
		if(Dumbo.debug) log.info("[Dumbo][fly] "+subject.getName()+
				" " + act.toString()+" with the fly tool");
		if(act.equals(Action.LEFT_CLICK_AIR)||act.equals(Action.LEFT_CLICK_BLOCK)) {
			if(hovering.contains(subject.getName())) {
				subject.sendMessage("The magic feather has stopped working, and now you fall.");
				hovering.remove(subject.getName());
			} else {
				if (Dumbo.floatEnabled && Dumbo.hasPerm(subject, "dumbo.float")) {
					subject.sendMessage("The slow-fall command works, and now you drift gently.");
					hovering.add(subject.getName());
				}
			}
		} else if(act.equals(Action.RIGHT_CLICK_AIR)||act.equals(Action.RIGHT_CLICK_BLOCK)) {
			//This code block is copied from VoxelAir from FlyRidgeFly
			// Further modifications by peter200lx
			double cProt = subject.getLocation().getYaw() % 360.0F;
			if (cProt > 0.0D) {
				cProt -= 720.0D;
			}
			double pRot = Math.abs(cProt % 360.0D);
			double pX = 0.0D;
			double pZ = 0.0D;
			double pY = 0.0D;
			double pPit = subject.getLocation().getPitch();
			double pyY = 0.0D;
			if ((pPit < 21.0D) && (pPit > -21.0D)) {
				pX = Math.sin(Math.toRadians(pRot)) * 10.0D;
				pZ = Math.cos(Math.toRadians(pRot)) * 10.0D;
				if (subject.getLocation().getY() < Dumbo.cruise)
					pY = 2.5D;
				else if (subject.getLocation().getY() <= Dumbo.cruise + 5)
					pY = 1.0D;
				else
					pY = 0.0D;
			}
			else {
				if (pPit < 0.0D) {
					pY = Math.sin(Math.toRadians(Math.abs(pPit))) * 10.0D;
					pyY = Math.cos(Math.toRadians(Math.abs(pPit))) * 10.0D;
					pX = Math.sin(Math.toRadians(pRot)) * pyY;
					pZ = Math.cos(Math.toRadians(pRot)) * pyY;
				} else if (pPit < 30.0D) {
					pY = 4.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 6.0D;
					pZ = Math.cos(Math.toRadians(pRot)) * 6.0D;
				} else if (pPit < 60.0D) {
					pY = 5.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 3.0D;
					pZ = Math.cos(Math.toRadians(pRot)) * 3.0D;
				} else if (pPit < 75.0D) {
					pY = 6.0D;
					pX = Math.sin(Math.toRadians(pRot)) * 1.5D;
					pZ = Math.cos(Math.toRadians(pRot)) * 1.5D;
				} else {
					pY = Dumbo.thrust;
					pX = 0.0D;
					pZ = 0.0D;
				}
			}
			if (subject.isSneaking() && Dumbo.tpEnabled && (Dumbo.hasPerm(subject,"dumbo.teleport")))
				subject.teleport(new Location(subject.getWorld(), subject.getLocation().getX() + pX,
						subject.getLocation().getY() + pY, subject.getLocation().getZ() + pZ,
						subject.getLocation().getYaw(), subject.getLocation().getPitch()));
			else
				subject.setVelocity(new Vector(pX, pY / 2.5D, pZ));
		}
	}
}
