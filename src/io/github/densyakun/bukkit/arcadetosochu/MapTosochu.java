package io.github.densyakun.bukkit.arcadetosochu;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
public class MapTosochu implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String mapname;
	public transient Location spawn;
	public transient ArrayList<Location> hunterspawn;
	public MapTosochu(String mapname, Location spawn) {
		this.mapname = mapname;
		this.spawn = spawn;
		hunterspawn = new ArrayList<Location>();
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		stream.writeObject(spawn == null ? null : spawn.serialize());
		ArrayList<Map<String, Object>> a = new ArrayList<Map<String, Object>>();
		for (int b = 0; b < hunterspawn.size(); b++) {
			a.add(hunterspawn.get(b).serialize());
		}
		stream.writeObject(a);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		
		Map<String, Object> spawn = (Map<String, Object>) stream.readObject();
		if (spawn != null) {
			this.spawn = Location.deserialize(spawn);
		}
		
		hunterspawn = new ArrayList<Location>();
		ArrayList<Map<String, Object>> a = (ArrayList<Map<String, Object>>) stream.readObject();
		for (int b = 0; b < a.size(); b++) {
			hunterspawn.add(Location.deserialize(a.get(b)));
		}
	}
}
