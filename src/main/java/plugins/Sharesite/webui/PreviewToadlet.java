package plugins.Sharesite.webui;

import java.io.IOException;
import java.net.URI;

import plugins.Sharesite.Freesite;
import plugins.Sharesite.Plugin;

import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.api.HTTPRequest;

public class PreviewToadlet extends Toadlet {
	protected PreviewToadlet() {
		super(null);
	}

	@Override
	public String path() {
		return "/Sharesite/Preview/";
	}

	// Gets called by the freenet node
	public void handleMethodGET(URI uri, HTTPRequest req, ToadletContext ctx) throws ToadletContextClosedException, IOException {
		// Get freesite and requested file's name, or return to the menu
		String[] split = uri.getPath().split("/");
		int siteId = -1;
		String filename = null;
		
		try {
			siteId = Integer.parseInt(split[3]);
			filename = split[4];
		} catch (Exception e) {
		}
		
		Freesite freesite = Plugin.instance.database.getFreesiteWithUniqueKey(siteId);
		
		if (freesite == null || filename == null) {
			writeTemporaryRedirect(ctx, "Redirecting...", "/Sharesite/");
			return;
		}
		
		// Get the preview of the file, or return a blank page
		try {
			if (filename.equals("index.html")) {
				writeReply(ctx, 200, "text/html", "OK", freesite.getHTML());
				return;
			}
			
			if (filename.equals("style.css")) {
				writeReply(ctx, 200, "text/css", "OK", freesite.getCSS());
				return;
			}
			
			if (filename.equals("source.txt")) {
				writeReply(ctx, 200, "text/plain", "OK", freesite.getText());
				return;
			}
		} catch (Exception e) {
		}
		
		writeHTMLReply(ctx, 200, "OK", "");
	}
}
