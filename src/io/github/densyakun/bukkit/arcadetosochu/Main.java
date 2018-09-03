package io.github.densyakun.bukkit.arcadetosochu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.densyakun.bukkit.minigamemanager.Game;
import io.github.densyakun.bukkit.minigamemanager.MiniGameCommandListener;
import io.github.densyakun.bukkit.minigamemanager.MiniGameManager;

public class Main extends JavaPlugin implements Listener, MiniGameCommandListener {
	
	public static final String param_is_not_enough = "パラメータが足りません";
	public static final String param_wrong_cmd = "パラメータが間違っています";
	public static final String cmd_player_only = "このコマンドはプレイヤーのみ実行できます";
	
	public static Main main;
	String prefix;
	World lobby;
	int entrytime = 60;
	int starttime = 10;
	int endtime = 180;
	int stoptime = 5;
	double startprize = 1.0;
	double prizepersecond = 0.1;
	private File mapsfile;
	private File datafile;
	private List<MapTosochu> maps = new ArrayList<MapTosochu>();
	private List<PlayerData> pdata = new ArrayList<PlayerData>();
	
	@Override
	public void onEnable() {
		Main.main = this;
		prefix = ChatColor.GREEN + "[" + getName() + "]";
		mapsfile = new File(getDataFolder(), "maps.dat");
		datafile = new File(getDataFolder(), "data.dat");
		load();
		getServer().getPluginManager().registerEvents(this, this);
		MiniGameManager.minigamemanager.addMiniGameCommandListener(this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "]有効");
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		saveDefaultConfig();
		lobby = getServer().getWorld(getConfig().getString("lobby-world", "world"));
		if (lobby != null) {
			getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "] Lobby: " + lobby.toString());
		} else {
			getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "ロビーのワールドが見つかりません");
		}
		entrytime = getConfig().getInt("entry-time", entrytime);
		starttime = getConfig().getInt("start-time", starttime);
		endtime = getConfig().getInt("end-time", endtime);
		stoptime = getConfig().getInt("stop-time", stoptime);
		startprize = getConfig().getDouble("startprize", startprize);
		prizepersecond = getConfig().getDouble("prizepersecond", prizepersecond);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapsfile));
			maps = (ArrayList<MapTosochu>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			maps = new ArrayList<MapTosochu>();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datafile));
			pdata = (ArrayList<PlayerData>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			pdata = new ArrayList<PlayerData>();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (label.equalsIgnoreCase("prize")) {
				Game game = MiniGameManager.minigamemanager.getPlayingGame(((Player) sender).getUniqueId());
				if (game instanceof GameTosochu) {
					sender.sendMessage(ChatColor.GREEN + "現在の賞金: " + ChatColor.GOLD + ((GameTosochu) game).prize);
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "逃走中をプレイしていません");
				}
			} else if (label.equalsIgnoreCase("jishu")) {
				Game game = MiniGameManager.minigamemanager.getPlayingGame(((Player) sender).getUniqueId());
				if (game instanceof GameTosochu) {
					((GameTosochu) game).jishu((Player) sender);
				} else {
					sender.sendMessage(prefix + ChatColor.RED + "逃走中をプレイしていません");
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean MiniGameCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("tosochu")) {
			if (args.length == 1) {
				sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
				sender.sendMessage(ChatColor.GREEN + "/game tosochu (map|kit|join|stats)");
			} else if (args[1].equalsIgnoreCase("map")) {
				if (sender.isOp() || sender.hasPermission("arcadetosochu.admin")) {
					if (args.length == 2) {
						sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN + "/game tosochu map (create|delete|spawn|hunterspawn)");
					} else if (args[2].equalsIgnoreCase("create")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game tosochu map create (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "そのマップ名は使用されています");
									return true;
								}
							}
							if (args[3].indexOf(',') == -1 && args[3].indexOf('[') == -1 && args[3].indexOf(']') == -1) {
								maps.add(new MapTosochu(args[3], null));
								sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.BLUE + "新しいマップを作成しました マップ名: " + args[3]);
								mapsave();
							} else {
								sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "マップ名に\",\", \"[\", \"]\"は使用できません");
							}
						}
					} else if (args[2].equalsIgnoreCase("delete")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game tosochu map delete (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.BLUE + "マップを削除しました マップ名: " + args[3]);
									maps.remove(a);
									mapsave();
									return true;
								}
							}
							sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "マップが見つかりません");
						}
					} else if (args[2].equalsIgnoreCase("spawn")) {
						if (args.length <= 4) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map)");
						} else if (args[3].equalsIgnoreCase("set")) {
							for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									map.spawn = adjustLocation(((Entity) sender).getLocation());
									mapsave();
									sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点を設定しました");
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else if (args[3].equalsIgnoreCase("get")) {
							for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									if (map.spawn == null) {
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点は設定されていません");
									} else {
										((Entity) sender).teleport(map.spawn);
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点に移動しました");
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else {
							sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map)");
						}
					} else if (args[2].equalsIgnoreCase("hunterspawn")) {
						if (args.length <= 4) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map hunterspawn (add|set|get|clear) (map)");
						} else if (args[3].equalsIgnoreCase("add")) {
							for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									map.hunterspawn.add(adjustLocation(((Entity) sender).getLocation()));
									mapsave();
									sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のハンタースタート地点を追加しました");
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else if (args[3].equalsIgnoreCase("set")) {
							/*for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									map.spawn = adjustLocation(((Entity) sender).getLocation());
									mapsave();
									sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のハンタースタート地点を設定しました");
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");*/
							sender.sendMessage(prefix + ChatColor.RED + "準備中");
						} else if (args[3].equalsIgnoreCase("get")) {
							/*for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									if (map.spawn == null) {
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のハンタースタート地点は設定されていません");
									} else {
										((Entity) sender).teleport(map.spawn);
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のハンタースタート地点に移動しました");
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");*/
							sender.sendMessage(prefix + ChatColor.RED + "準備中");
						} else if (args[3].equalsIgnoreCase("clear")) {
							for (int b = 0; b < maps.size(); b++) {
								MapTosochu map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									map.hunterspawn.clear();
									mapsave();
									sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のハンタースタート地点を初期化しました");
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else {
							sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map hunterspawn (add|set|get|clear) (map)");
						}
					} else {
						sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN + "/game tosochu map (create|delete|spawn|hunterspawn)");
					}
				} else {
					sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "権限がありません");
				}
			} else if (args[1].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (MiniGameManager.minigamemanager
							.getPlayingGame(((OfflinePlayer) sender).getUniqueId()) == null) {
						List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
						for (int a = 0; a < games.size(); a++) {
							Game game = games.get(a);
							if (!(game instanceof GameTosochu && game.isJoinable())) {
								games.remove(a);
							}
						}
						if (args.length != 2) {
							for (int a = 0; a < maps.size(); a++) {
								MapTosochu map = maps.get(a);
								if (map.mapname.equals(args[2])) {
									for (int c = 0; c < games.size(); c++) {
										Game game = games.get(c);
										if (game instanceof GameTosochu
												&& map.mapname.equals(((GameTosochu) game).map.mapname)) {
											if (!MiniGameManager.minigamemanager.joinGame((Player) sender, game)) {
												sender.sendMessage(prefix
														+ ChatColor.RED + "このマップが使用しているゲームに入ることが出来ません");
											}
											return true;
										}
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりません");
						}
						if (0 < games.size()) {
							int a;
							while (0 < games.size() && !MiniGameManager.minigamemanager.joinGame((Player) sender,
									games.get(a = new Random().nextInt(games.size())))) {
								games.remove(a);
							}
						}
						if (0 == games.size()) {
							List<MapTosochu> b = maps;
							for (int c = 0; c < b.size();) {
								boolean d = true;
								for (int e = 0; e < games.size(); e++) {
									Game game = games.get(e);
									if (game instanceof GameTosochu && maps.get(c).equals(((GameTosochu) game).map)) {
										b.remove(c);
										d = false;
										break;
									}
								}
								if (d) {
									c++;
								}
							}
							if (0 < b.size()) {
								MiniGameManager.minigamemanager.joinGame((Player) sender,
										new GameTosochu(b.get(new Random().nextInt(b.size()))));
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "使用可能なマップがありません");
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "ゲーム中です");
					}
				}
			} else if (args[1].equalsIgnoreCase("stats")) {
				if (args.length <= 2) {
					if (sender instanceof Player) {
						PlayerData data = getPlayerData(((OfflinePlayer) sender).getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD + ((Player) sender).getDisplayName()
											+ "の情報: \nGetPrize: " + data.getGetPrize() + "\nCaught: " + data.getCaught());
						}
					}
				} else {
					@SuppressWarnings("deprecation")
					OfflinePlayer player = getServer().getOfflinePlayer(args[2]);
					if (player != null) {
						PlayerData data = getPlayerData(player.getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD
									+ (player.getPlayer() != null ? player.getPlayer().getDisplayName()
											: player.getName())
									+ "の情報: \nGetPrize: " + data.getGetPrize() + "\nCaught: " + data.getCaught());
						}
					}
				}
			} else {
				sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
				sender.sendMessage(ChatColor.GREEN + "/game tosochu (map|kit|join|stats)");
			}
			return true;
		}
		return false;
	}
	
	public void mapsave() {
		getDataFolder().mkdirs();
		try {
			mapsfile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mapsfile));
			oos.writeObject(maps);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pdatasave() {
		getDataFolder().mkdirs();
		try {
			datafile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datafile));
			oos.writeObject(pdata);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PlayerData getPlayerData(UUID uuid) {
		for (int a = 0; a < pdata.size(); a++) {
			if (pdata.get(a).getUuid().equals(uuid)) {
				return pdata.get(a);
			}
		}
		PlayerData data = new PlayerData(uuid);
		pdata.add(data);
		return data;
	}
	
	public void UpdatePlayerData(PlayerData data) {
		boolean a = true;
		for (int b = 0; b < pdata.size(); b++) {
			if (pdata.get(b).getUuid().equals(data.getUuid())) {
				a = false;
				pdata.set(b, data);
				break;
			}
		}
		if (a) {
			pdata.add(data);
		}
		pdatasave();
	}
	
	public List<MapTosochu> getMaps() {
		return maps;
	}
	
	public List<MapTosochu> getEnabledMaps() {
		List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
		List<MapTosochu> a = maps;
		for (int b = 0; b < games.size(); b++) {
			Game game = games.get(b);
			if (game instanceof GameTosochu) {
				for (int c = 0; c < a.size();) {
					if (((GameTosochu) game).map.mapname.equals(a.get(c))) {
						a.remove(c);
						break;
					} else {
						c++;
					}
				}
			}
		}
		return a;
	}
	
	public Location adjustLocation(Location location) {
		location.setX((double) (Math.round(location.getX() * 2)) / 2);
		location.setY((double) (Math.round(location.getY() * 2)) / 2);
		location.setZ((double) (Math.round(location.getZ() * 2)) / 2);
		location.setYaw((float) (Math.round(location.getYaw() / 15)) * 15);
		location.setPitch((float) (Math.round(location.getPitch() / 15)) * 15);
		return location;
	}
	
	@EventHandler
	public void PlayerMove(PlayerMoveEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu && !game.isStarted()) {
			e.setTo(new Location(e.getPlayer().getWorld(), e.getFrom().getX(), e.getTo().getY(), e.getFrom().getZ(),
					e.getTo().getYaw(), e.getTo().getPitch()));
		}
	}
	
	@EventHandler
	public void EntityDamageByEntity(EntityDamageByEntityEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getDamager().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			e.setCancelled(true);
			
			if (!((GameTosochu) game).isRunner(e.getDamager().getUniqueId()) && ((GameTosochu) game).isRunner(e.getEntity().getUniqueId())) {
				((GameTosochu) game).Catch((Player) e.getDamager(), (Player) e.getEntity()); 
			}
		}
	}

	@EventHandler
	public void PlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			if (!((GameTosochu) game).isRunner(e.getPlayer().getUniqueId()) && ((GameTosochu) game).isRunner(e.getRightClicked().getUniqueId())) {
				((GameTosochu) game).Catch(e.getPlayer(), (Player) e.getRightClicked()); 
			}
		}
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getEntity().getUniqueId());
		if (game != null && game instanceof GameTosochu && game.isStarted()) {
			e.setKeepInventory(true);
			game.removePlayer(e.getEntity().getUniqueId());
		}
	}
	
	@EventHandler
	public void PlayerTeleport(PlayerTeleportEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu && game.isStarted() && e.getCause() == TeleportCause.COMMAND) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void PlayerDropItem(PlayerDropItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void PlayerPickupItem(PlayerPickupItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void BlockBreak(BlockBreakEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void BlockPlace(BlockPlaceEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameTosochu) {
			e.setCancelled(true);
		}
	}
}
