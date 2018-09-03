package io.github.densyakun.bukkit.arcadetosochu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;

import io.github.densyakun.bukkit.minigamemanager.Game;

public class GameTosochu extends Game {
	
	public static final String name = "ArcadeTosochu";
	
	public MapTosochu map;
	
	public List<UUID> runners = new ArrayList<UUID>();
	public double prize = Main.main.startprize;
	
	public GameTosochu(MapTosochu map) {
		super(name);
		this.map = map;
		setEntrytime(Main.main.entrytime);
		setStarttime(Main.main.starttime);
		setEndtime(Main.main.endtime);
		setStoptime(Main.main.stoptime);
		//setMinplayers(3);
		setMinplayers(1);
		setMaxplayers(0);
	}
	
	@Override
	public void stop() {
		List<Player> players = getPlayers();
		for (int a = 0; a < players.size(); a++) {
			players.get(a).getInventory().clear();
			players.get(a).getInventory().setArmorContents(null);
			players.get(a).setGameMode(GameMode.ADVENTURE);
		}
		if (Main.main.lobby != null) {
			for (int a = 0; a < players.size(); a++) {
				players.get(a).teleport(Main.main.lobby.getSpawnLocation());
			}
		}
		super.stop();
	}
	
	@Override
	public void removePlayer(UUID uuid) {
		Player player = Main.main.getServer().getPlayer(uuid);
		if (player != null) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setGameMode(GameMode.ADVENTURE);
			if (Main.main.lobby != null) {
				player.teleport(Main.main.lobby.getSpawnLocation());
			}
			
			runners.remove(player.getUniqueId());
			if (runners.size() == 0) {
				stop();
			}
		}
		super.removePlayer(uuid);
	}
	
	@Override
	protected boolean addPlayer(Player player) {
		if (super.addPlayer(player)) {
			player.getInventory().clear();
			player.leaveVehicle();
			player.resetMaxHealth();
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getMaxHealth());
			
			if (isEntered()) {
				if (map.hunterspawn.size() != 0) {
					player.teleport(map.hunterspawn.get(new Random().nextInt(map.hunterspawn.size())));
				}
				
				player.sendMessage(ChatColor.GOLD + "逃走中へようこそ！");
				
				player.sendMessage(ChatColor.GOLD + "マップ: " + map.mapname);
				
				player.sendMessage(ChatColor.AQUA + "あなたはハンターです");
				
				player.sendMessage(ChatColor.GREEN + "プレイヤーを左クリック（または右）して捕まえよう！");
			} else {
				runners.add(player.getUniqueId());
				
				player.getInventory().setHelmet(new ItemStack(Material.GOLD_CHESTPLATE));
				
				if (map.spawn != null) {
					player.teleport(map.spawn);
				}
				
				player.sendMessage(ChatColor.GOLD + "逃走中へようこそ！");
				
				player.sendMessage(ChatColor.GOLD + "マップ: " + map.mapname);
				
				player.sendMessage(ChatColor.RED + "あなたはプレイヤーです");
				
				player.sendMessage(ChatColor.GREEN + "賞金を確認するには... /prize");
				player.sendMessage(ChatColor.GREEN + "自首はスポンジの上で... /jishu");
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isJoinable() {
		return !isEnded();
	}
	
	@Override
	public void countsec() {
		super.countsec();
		if (isStarted() && !isEnded() && runners.size() < players.size()) {
			prize = (double) Math.round((prize + Main.main.prizepersecond) * 10) / 10;
		}
	}
	
	public boolean isRunner(UUID uuid) {
		for (int a = 0; a < runners.size(); a++) {
			if (runners.get(a).equals(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	public void jishu(Player player) {
		if (isStarted() && !isEnded()) {
			Block ground = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if(ground.getType() == Material.SPONGE) {
				PlayerData data = Main.main.getPlayerData(player.getUniqueId());
				data.getprize += prize;
				Main.main.UpdatePlayerData(data);
				
				if (Bukkit.getServer().getPluginManager().getPlugin("iConomy") != null && 0.1 <= prize) {
					new Account(player.getName()).getHoldings().add(prize);
					iConomy.Template.set(Template.Node.PLAYER_CREDIT);
					iConomy.Template.add("name", player.getName());
					iConomy.Template.add("amount", iConomy.format(prize));
					Messaging.send(player, iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
				}
				
				for (int a = 0; a < players.size(); a++) {
					players.get(a).sendMessage(player.getDisplayName() + ChatColor.RED + "が自首しました");
					players.get(a).getWorld().playSound(players.get(a).getLocation(), Sound.ANVIL_USE, 1.0f, 1.0f);
				}
				
				removePlayer(player.getUniqueId());
			}
		}
	}
	
	public void Catch(Player hunter, Player target) {
		PlayerData data = Main.main.getPlayerData(target.getUniqueId());
		data.caughtBy += 1;
		Main.main.UpdatePlayerData(data);
		
		double prize_c = prize / 3;
		
		if (Bukkit.getServer().getPluginManager().getPlugin("iConomy") != null && 0.1 <= prize_c) {
			new Account(hunter.getName()).getHoldings().add(prize_c);
			iConomy.Template.set(Template.Node.PLAYER_CREDIT);
			iConomy.Template.add("name", hunter.getName());
			iConomy.Template.add("amount", iConomy.format(prize_c));
			Messaging.send(hunter, iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
		}
		
		PlayerData hunterdata = Main.main.getPlayerData(hunter.getUniqueId());
		data.caught += 1;
		Main.main.UpdatePlayerData(hunterdata);
		hunter.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
		
		for (int a = 0; a < players.size(); a++) {
			players.get(a).sendMessage(target.getDisplayName() + ChatColor.RED + "が捕獲されました");
			players.get(a).getWorld().playSound(players.get(a).getLocation(), Sound.ANVIL_USE, 1.0f, 1.0f);
		}
		
		removePlayer(target.getUniqueId());
	}
}
