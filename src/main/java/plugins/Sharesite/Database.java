package plugins.Sharesite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import plugins.Sharesite.common.FileStorage;
import plugins.Sharesite.common.SmartMap;

/**
 * The database knows both the configuration and which
 * freesites exists. It also remember recently deleted freesites
 * at least 3 hours after the latest one was deleted.
 * The database is for now stored unencrypted in freenets folder.
 */
public class Database {
	private ArrayList<Freesite> freesites;
	private ArrayList<Freesite> recentlyDeleted;
	private int nextUniqueKey;
	private long lastDeletedTime;

	public Database() {
		freesites = new ArrayList<Freesite>();
		recentlyDeleted = new ArrayList<Freesite>();
		nextUniqueKey = 0;
		lastDeletedTime = 0;

		addFromMap(FileStorage.load("Sharesite.db"));
	}

	public synchronized int numFreesites() {
		return freesites.size();
	}

	public synchronized ArrayList<Freesite> getFreesites() {
		return new ArrayList<Freesite>(freesites);
	}

	public synchronized Freesite getFreesiteWithUniqueKey(int uniqueKey) {
		for (int i = 0; i < freesites.size(); i++) {
			if (freesites.get(i).getUniqueKey() == uniqueKey) {
				return freesites.get(i);
			}
		}

		return null;
	}

	public synchronized int numDeleted() {
		return recentlyDeleted.size();
	}

	public synchronized void createFreesite() {
		Freesite c = new Freesite(nextUniqueKey);
		nextUniqueKey++;
		freesites.add(c);
	}

	public synchronized void delete(Freesite freesite) {
		freesites.remove(freesite);
		recentlyDeleted.add(freesite);
		lastDeletedTime = System.currentTimeMillis();
	}

	public synchronized void restoreDeleted() {
		freesites.addAll(recentlyDeleted);
		recentlyDeleted = new ArrayList<Freesite>();
		Collections.sort(freesites);
	}

	public synchronized void save() {
		// Clean up old recently deleted; has to be done somewhere
		if (recentlyDeleted.size() > 0 &&
		        System.currentTimeMillis() > lastDeletedTime + 3*60*60*1000) { // 3h
			recentlyDeleted = new ArrayList<Freesite>();
		}

		// Store the database, replacing the old file
		try {
			FileStorage.save("Sharesite.db", getDatabaseAsMap());
		} catch (IOException e) {
			// TODO: maybe tell the user?
		}
	}

	public synchronized SmartMap getDatabaseAsMap() {
		// Create a new blank map, and put things in it
		SmartMap map = new SmartMap();

		map.putint("increasingCounter", nextUniqueKey);
		map.putlong("lastDeletedTime", lastDeletedTime);

		for (Freesite c : freesites) c.save(map);
		for (Freesite c : recentlyDeleted) c.save(map);

		// Put the keys in it
		ArrayList<Integer> keys = new ArrayList<Integer>();
		for (int i = 0; i < freesites.size(); i++) {
			keys.add(freesites.get(i).getUniqueKey());
		}
		map.putintary("keys", keys);

		ArrayList<Integer> deleted = new ArrayList<Integer>();
		for (int i = 0; i < recentlyDeleted.size(); i++) {
			deleted.add(recentlyDeleted.get(i).getUniqueKey());
		}
		map.putintary("deleted_keys", deleted);

		return map;
	}

	public synchronized void addFromMap(SmartMap map) {
		// Load some variables
		int appendUniqueKey = nextUniqueKey;
		nextUniqueKey += map.getint("increasingCounter", 0);
		long deletedTime = map.getlong("lastDeletedTime", 0);
		if (deletedTime > lastDeletedTime) lastDeletedTime = deletedTime;

		Plugin.instance.logger.putstr("nextUniqueKey=" + nextUniqueKey);

		// And load all freesites
		Plugin.instance.logger.putstr("keys=" + map.getstr("keys", "(none)"));
		ArrayList<Integer> keys = map.getintary("keys", new ArrayList<Integer>());
		for (int key : keys) {
			Plugin.instance.logger.putstr("Loading freesite key=" + key);

			Freesite c = new Freesite(appendUniqueKey + key);
			c.load(map, key);
			freesites.add(c);

			if (key >= nextUniqueKey) nextUniqueKey = key + 1;
		}

		Plugin.instance.logger.putstr("deleted_keys=" + map.getstr("deleted_keys", "(none)"));
		ArrayList<Integer> deleted = map.getintary("deleted_keys", new ArrayList<Integer>());
		for (int key : deleted) {
			Plugin.instance.logger.putstr("Loading freesite key=" + key);

			Freesite c = new Freesite(appendUniqueKey + key);
			c.load(map, key);
			recentlyDeleted.add(c);

			if (key >= nextUniqueKey) nextUniqueKey = key + 1;
		}
	}
}
