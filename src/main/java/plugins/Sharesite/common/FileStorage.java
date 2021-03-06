package plugins.Sharesite.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import plugins.Sharesite.Plugin;

/**
 * Handles loading and saving of a "SmartMap" to disk.
 * Used for storing the database. No encryption.
 * File is relative to Freenet's plugin folder.
 * 
 * Thread-safe, and power-failure-safe :)
 */
public class FileStorage {
	public static synchronized SmartMap load(String filename) {
		File userdir = Plugin.instance.pluginRespirator.getNode().getUserDir();
		File file = new File(userdir, filename);
		File temp = new File(userdir, filename + ".tmp");
		SmartMap map = new SmartMap();
		
		// If a old file exists, revert back to using it
		if (temp.exists()) {
			Plugin.instance.logger.putstr("Reverting database from unclean shutdown ...");
			file.delete();
			temp.renameTo(file);
		}
		
		// If the file does not exists, return an empty map
		if (!file.exists()) {
			Plugin.instance.logger.putstr("No file " + filename + " exists!");
			return map;
		}
		
		// Load everything, and return empty map if failed
		try {
			// Get the file's size, fail if to large
			long lsize = file.length();
			if (lsize > 1*1024*1024*1024) throw new IOException(); // 1GiB
			int size = (int)lsize;
			Plugin.instance.logger.putstr("File " + filename + " is of size " + size + " bytes");
			
			// Read the whole file into a byte-array
			FileInputStream in = new FileInputStream(file);
			byte[] data = new byte[size];
			int read = 0;
			
			while (read < size) {
				int got = in.read(data, read, size - read);
				if (got < 0) throw new IOException();
				read += got;
			}
			
			// Check everything read, and close file
			if (in.read() >= 0) throw new IOException();
			in.close();
			
			map = MapToData.dataToMap(data);
			if (map.size() == 0) throw new IOException();
		} catch (IOException e) {
			// Backup the corrupted file for debugging, and return empty map
			Plugin.instance.logger.putstr("File appears corrupted!!!");
			
			File corrupted = new File(userdir, filename + ".corrupted");
			corrupted.delete();
			file.renameTo(corrupted);
			map = new SmartMap();
		}
		
		return map;
	}
	
	public static synchronized void save(String filename, SmartMap map) throws IOException {
		File userdir = Plugin.instance.pluginRespirator.getNode().getUserDir();
		File file = new File(userdir, filename);
		File temp = new File(userdir, filename + ".tmp");
		
		Plugin.instance.logger.putstr("Saving as " + filename + " ...");
		
		// If the file exists, keep it, but move it away
		temp.delete();
		if (file.exists()) file.renameTo(temp);
		
		// Create the new file and verify it is written to disk
		FileOutputStream out = new FileOutputStream(file);
		out.write(MapToData.mapToData(map));
		out.flush();
		out.getFD().sync();
		out.close();
		
		// The delete the old file
		temp.delete();
	}
}
