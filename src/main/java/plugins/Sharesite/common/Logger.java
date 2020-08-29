package plugins.Sharesite.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import plugins.Sharesite.Plugin;

/**
 * This logger logs data to a specific file for debugging purpose.
 * It will only log data if the file already exists.
 * 
 * The log file will be put in Freenet's folder.
 */
public class Logger {
	private boolean enabled;
	private FileWriter out;
	
	public Logger(String filename) {
		enabled = false;
		
		// If the log file exists, open it for appending
		File userdir = Plugin.instance.pluginRespirator.getNode().getUserDir();
		File logfile = new File(userdir, filename);
		
		if (logfile.exists()) {
			try {
				out = new FileWriter(logfile, true);
				enabled = true;
			} catch (IOException e) {
			}
		}
	}
	
	public void close() {
		if (!enabled) return;
		
		try {
			out.close();
		} catch (IOException e) {
		}
	}
	
	public void putstr(String str) {
		if (!enabled) return;
		
		try {
			out.write(str + '\n');
			out.flush();
		} catch (IOException e) {
			enabled = false;			
		}
	}
}
