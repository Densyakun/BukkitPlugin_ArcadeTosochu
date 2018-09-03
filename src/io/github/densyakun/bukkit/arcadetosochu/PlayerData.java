package io.github.densyakun.bukkit.arcadetosochu;
import java.io.Serializable;
import java.util.UUID;
public class PlayerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	UUID uuid;
	int getprize = 0;
	int getprizeonhunter = 0;
	int caught = 0;
	int caughtBy = 0;
	public PlayerData(UUID uuid) {
		this.uuid = uuid;
	}
	public UUID getUuid() {
		return uuid;
	}
	public int getGetPrize() {
		return getprize;
	}
	public int getGetprizeOnHunter() {
		return getprizeonhunter;
	}
	public int getCaught() {
		return caught;
	}
	public int getCaughtBy() {
		return caughtBy;
	}
	public void clear() {
		getprize = 0;
		getprizeonhunter = 0;
		caught = 0;
		caughtBy = 0;
	}
}
