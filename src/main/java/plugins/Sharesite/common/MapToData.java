package plugins.Sharesite.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

/**
 * Converts a SmartMap to a byte-array for storing,
 * or parses a byte-array into a SmartMap.
 * On error a valid but empty object is returned.
 * 
 * The byte-array is prefixed with "ShareWiki-db-ver1".
 */
public class MapToData {
	private static byte[] intToByteArray(int value) {
		byte[] ret = new byte[4];
		ret[0] = (byte)(value >>> 24);
		ret[1] = (byte)(value >>> 16);
		ret[2] = (byte)(value >>> 8);
		ret[3] = (byte)(value);
		return ret;
	}
	
	private static int byteArrayToInt(byte[] bytes) {
		int ret;
		ret = (((int)bytes[0] & 0xff) << 24);
		ret |= (((int)bytes[1] & 0xff) << 16);
		ret |= (((int)bytes[2] & 0xff) << 8);
		ret |= (((int)bytes[3] & 0xff));
		return ret;
	}
	
	private static byte[] readBytes(ByteArrayInputStream in, int num) throws IOException {
		if (num < 0) throw new IOException();
		if (num > 100*1024*1024) throw new IOException(); // 100MiB
		byte[] ret = new byte[num];
		int got = in.read(ret);
		if (got != num) throw new IOException();
		return ret;
	}
	
	public static SmartMap dataToMap(byte[] data) {
		if (data == null) return new SmartMap();
		
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			SmartMap map = new SmartMap();
			
			String header = new String(readBytes(in, 17), "UTF-8");
			if (!header.equals("ShareWiki-db-ver1")) {
				throw new IOException();
			}
			
			int count = byteArrayToInt(readBytes(in, 4));
			
			for (int i = 0; i < count; i++) {
				int keylen = byteArrayToInt(readBytes(in, 4));
				String key = new String(readBytes(in, keylen), "UTF-8");
				int vallen = byteArrayToInt(readBytes(in, 4));
				String value = new String(readBytes(in, vallen), "UTF-8");

				map.putstr(key, value);
			}
			
			if (in.read() >= 0) throw new IOException();
			
			return map;
		} catch (Exception e) {
			return new SmartMap();
		}
	}
	
	public static byte[] mapToData(SmartMap map) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			out.write("ShareWiki-db-ver1".getBytes("UTF-8"));
			out.write(intToByteArray(map.size()));
			
			for (Entry<String,String> entry : map.entrySet()) {
				byte[] key = entry.getKey().getBytes("UTF-8");
				byte[] value = entry.getValue().getBytes("UTF-8");
				
				out.write(intToByteArray(key.length));
				out.write(key);
				out.write(intToByteArray(value.length));
				out.write(value);
			}
			
			return out.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}
	}
}
