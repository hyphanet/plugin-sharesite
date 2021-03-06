package plugins.Sharesite.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A HashMap with intelligence!
 * Simulates some of the behavior of Freenet's PluginStore,
 * but does it much better.
 */
public class SmartMap {
	private HashMap<String,String> map;
	
	public SmartMap() {
		map = new HashMap<String,String>();
	}
	
	public int size() {
		return map.size();
	}
	
	public void putstr(String key, String value) {
		map.put(key, value);
	}
	
	public void putlong(String key, long value) {
		map.put(key, Long.toString(value));
	}
	
	public void putint(String key, int value) {
		map.put(key, Integer.toString(value));
	}
	
	public void putintary(String key, ArrayList<Integer> array) {
		String str = "";
		if (array.size() > 0) {
			str = Integer.toString(array.get(0));
			for (int i = 1; i < array.size(); i++) {
				str += " " + Integer.toString(array.get(i));
			}
		}
		map.put(key, str);
	}
	
	public String getstr(String key, String defval) {
		String value = map.get(key);
		return (value != null) ? value : defval;
	}
	
	public long getlong(String key, long defval) {
		String str = map.get(key);
		if (str == null) return defval;
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			return defval;
		}
	}
	
	public int getint(String key, int defval) {
		return (int)getlong(key, defval);
	}
	
	public ArrayList<Integer> getintary(String key, ArrayList<Integer> defary) {
		String str = map.get(key);
		if (str == null) return defary;

		String[] split = str.split(" ");
		ArrayList<Integer> ret = new ArrayList<Integer>();
		try {
			for (int i = 0; i < split.length; i++) {
				ret.add(Integer.parseInt(split[i]));
			}
		} catch (NumberFormatException e) {
			return defary;
		}
		
		return ret;
	}
	
	// TODO: used by MapToData, but not really clean ...
	public Set<Entry<String,String>> entrySet() {
		return map.entrySet();
	}
}
