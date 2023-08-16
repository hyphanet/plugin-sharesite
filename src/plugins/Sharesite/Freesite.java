package plugins.Sharesite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freenet.client.HighLevelSimpleClient;
import freenet.keys.FreenetURI;
import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.textile.TextileDialect;
import plugins.Sharesite.common.SmartMap;

/**
 * This is the freesite. Besides containing the actual configuration
 * and content, the freesite know which inserted edition we are on
 * and can generate all data that we need for inserting it.
 *
 * This class implements Comparable. The natural order for
 * this class is sorted by uniqueKey, that is the first
 * created is first, and most recent created last.
 * This is the order they appear in in the front end.
 */
public class Freesite implements Comparable<Freesite> {
	private String name;
	private String path;
	private Integer insertHour;
	private String description;
	private boolean pastebin;
	private String text;
	private String css;
	private String activelinkUri;

	private String requestSSK;
	private String insertSSK;
	private long edition;

	private String l10nStatus;
	private String l10nStatusChangeToOnRestart;

	private int uniqueKey;

	public Freesite(int uniqueKey) {
		this.uniqueKey = uniqueKey;

		name = "Sharesite freesite";
		path = "sharesite-freesite";
		Random r = new Random();
		insertHour = r.nextInt(24);
		description = "Write a short description shown in search results here.";
		activelinkUri = "";
		pastebin = false;

		String csstemplate = "/templates/style.css";
		String texttemplate = "/templates/content.txt";

		try (
			 InputStream is = Plugin.class.getClassLoader().getResourceAsStream(csstemplate);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			 ) {
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				sb.append(line + "\n");
			}

			reader.close();
			this.css= sb.toString();
		} catch (Exception e) {
			this.css= "";
		}

		try (
			 InputStream is = Plugin.class.getClassLoader().getResourceAsStream(texttemplate);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			 ) {
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				sb.append(line + "\n");
			}
			
			reader.close();
			this.text= sb.toString();
		} catch (Exception e) {
			this.text= "";
		}

		HighLevelSimpleClient simpleClient = Plugin.instance.pluginRespirator.getHLSimpleClient();
		FreenetURI[] keys = simpleClient.generateKeyPair("");
		requestSSK = keys[1].toString();
		insertSSK = keys[0].toString();
		edition = -1;  // first inserted edition is 0

		l10nStatus = "Status.New";
		l10nStatusChangeToOnRestart = "Status.New";
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized String getPath() {
		if (path != null) {
			return path;
		} else {
			return name;
		}
	}

	public synchronized void setPath(String path) {
		this.path = path;
	}

	public synchronized Integer getInsertHour() {
		if (insertHour != null) {
			return insertHour;
		} else {
			return -1; // meaning unrestricted
		}
	}

	public synchronized void setInsertHour(Integer insertHour) {
		if ((insertHour == null) || (insertHour.equals(-2))) {
			Random r = new Random();
			this.insertHour = r.nextInt(24);
		} else {
			this.insertHour = insertHour;
		}
	}

	public synchronized String getDescription() {
		return description;
	}

	public synchronized void setDescription(String description) {
		this.description = description;
	}

	public synchronized String getText() {
		return text;
	}

	public synchronized void setText(String text) {
		this.text = text;
	}

	public synchronized boolean isPastebin() {
		return pastebin;
	}

	public synchronized void setPastebin(boolean pastebin) {
		this.pastebin = pastebin;
	}

	public int getUniqueKey() {
		return uniqueKey;
	}

	public synchronized void setInsertSSK(String key) {
		this.insertSSK = key;
	}

	public synchronized void setRequestSSK(String key) {
		this.requestSSK=key;
	}

	public synchronized String getRequestSSK() {
		return requestSSK;
	}

	public synchronized String getInsertSSK() {
		return insertSSK;
	}

	public synchronized long getEdition() {
		return edition;
	}

	public synchronized void setEdition(long edition) {
		this.edition = edition;
	}

	/*
	 * Try to find all Freenet keys in the freesite source
	 */
	public synchronized String getKeys() {
		BufferedReader br=new BufferedReader(new StringReader(text));
		StringBuffer buf=new StringBuffer();

		try {
			String line="";
			while((line=br.readLine())!= null) {
				Pattern key= Pattern.compile("(?<=/)((SSK|USK|CHK)@.*)");

				Matcher sm=key.matcher(line);
				if(sm.find()) {
					String str=sm.group();

					// remove the rest of the wiki markup
					str=str.replaceAll("jpg!","jpg");
					str=str.replaceAll("JPG!","JPG");
					str=str.replaceAll("jpeg!","jpeg");
					str=str.replaceAll("JPEG!","JPEG");
					str=str.replaceAll("gif!","gif");
					str=str.replaceAll("GIF!","GIF");
					str=str.replaceAll("png!","png");
					str=str.replaceAll("PNG!","PNG");

					str=str.replaceAll("%20"," ");

					buf.append(str+"\r\n");
				}


			}
		} catch (IOException e) {
			Plugin.instance.logger.putstr(e.getMessage());
		}

		return buf.toString();
	}

	public synchronized String getHTML() throws Exception {
		// Prepare content
		//Plugin.instance.logger.putstr("textToHTML:\n=============");
		String descriptionHTML = textToHTML(description);
		String content = isPastebin() ? "<pre><code>" + escapeHtmlEntities(text) + "</code></pre>" : textToHTML(text);
		//Plugin.instance.logger.putstr("==============");

		// Generate URIs we need
		FreenetURI requestURI = new FreenetURI(requestSSK + path +"-" + (edition + 1) + "/");
		FreenetURI uskURI = requestURI.uskForSSK();
		String nextEdition = uskURI.toString();

		long updateEdition = (edition >= 0) ? -(edition + 1) : -1;
		String checkUpdates = uskURI.setSuggestedEdition(updateEdition).toString();

		// We need todays date too
		Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String insertDate = dateFormat.format(calendar.getTime());

		// Pass through the HTML file, substituting in the real content
		String template = "/templates/index.html";

		InputStream is = Plugin.class.getClassLoader().getResourceAsStream(template);
		if (is == null) throw new Exception("Couldn't load \"" + template + "\"");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();

		while (true) {
			String line = reader.readLine();
			if (line == null) break;

			line = line.replaceAll("\\$NAME\\$", Matcher.quoteReplacement(name));
			line = line.replaceAll("\\$DESCRIPTION\\$", Matcher.quoteReplacement(description));
			line = line.replaceAll("\\$DESCRIPTION_HTML\\$", Matcher.quoteReplacement(descriptionHTML));
			line = line.replaceAll("\\$CONTENT\\$", Matcher.quoteReplacement(content));
			line = line.replaceAll("\\$INSERT_URI\\$", Matcher.quoteReplacement(nextEdition));
			line = line.replaceAll("\\$CHECK_UPDATES_URI\\$", Matcher.quoteReplacement(checkUpdates));
			line = line.replaceAll("\\$INSERT_DATE\\$", Matcher.quoteReplacement(insertDate));

			sb.append(line + "\n");
		}

		reader.close();
		return sb.toString();
	}

	private static String escapeHtmlEntities(String text) {
		return text // see https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll("\"", "&quot;")
				.replaceAll("'", "&#x27");
	}

	public synchronized void setCSS(String css) {
		this.css = css;
	}

	public synchronized String getCSS() {
		return css;
	}


	public synchronized String getActivelinkUri() {
		return activelinkUri;
	}

	public synchronized void setActivelinkUri(String activelinkUri) {
		this.activelinkUri = activelinkUri;
	}


	public synchronized String getStatus() {
		return Plugin.instance.l10n.getString("Sharesite." + l10nStatus);
	}

	public synchronized String getRealStatus() {
		return l10nStatus;
	}

	public void setL10nStatus(String status) {
		setL10nStatus(status, status);
	}

	public synchronized void setL10nStatus(String status, String changeToOnRestart) {
		this.l10nStatus = status;
		this.l10nStatusChangeToOnRestart = changeToOnRestart;
	}

	synchronized void save(SmartMap map) {
		String prefix = "collection-" + uniqueKey + "/";

		map.putstr(prefix + "name", name);
		map.putstr(prefix + "path", path);
		map.putint(prefix + "insertHour", insertHour);
		map.putstr(prefix + "description", description);
		map.putbool(prefix + "pastebin", pastebin);
		map.putstr(prefix + "text", text);
		map.putstr(prefix + "css", css);
		map.putstr(prefix + "activelinkUri", activelinkUri);

		map.putstr(prefix + "requestSSK", requestSSK);
		map.putstr(prefix + "insertSSK", insertSSK);
		map.putlong(prefix + "edition", edition);

		map.putstr(prefix + "l10nStatus", l10nStatusChangeToOnRestart);
	}

	void load(SmartMap map, int uniqueKeyInMap) {
		String prefix = "collection-" + uniqueKeyInMap + "/";

		name = map.getstr(prefix + "name", name);
		path = map.getstr(prefix + "path", null);
		// to avoid breaking old sites, keep path set to the name.
		if (path == null) {
			path = name;
		}
		// to avoid changing behavior of old sites, keep insertHour as -1
		insertHour = map.getint(prefix + "insertHour", -1);
		description = map.getstr(prefix + "description", description);
		pastebin = map.getbool(prefix + "pastebin", pastebin);
		text = map.getstr(prefix + "text", text);
		css = map.getstr(prefix + "css", css);
		activelinkUri = map.getstr(prefix + "activelinkUri", activelinkUri);

		requestSSK = map.getstr(prefix + "requestSSK", requestSSK);
		insertSSK = map.getstr(prefix + "insertSSK", insertSSK);
		edition = map.getlong(prefix + "edition", edition);

		l10nStatus = map.getstr(prefix + "l10nStatus", l10nStatus);
		l10nStatusChangeToOnRestart = l10nStatus;
	}

	private static String textToHTML(String text) {

		String html="";
        // Hack: replace a leading space by a non-breaking space to avoid left-stripping of the text. This allows starting text with HTML-tags.
        if (text.startsWith(" ")) {
            text = " " + text.substring(1);
        }

		try {
			StringWriter writer = new StringWriter();
			MarkupParser parser = new MarkupParser();
			parser.setDialect(new TextileDialect());
			HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
			builder.setEmitAsDocument(false);// no <html> and <body>

			parser.setBuilder(builder);
			parser.parse(text);
			html=writer.toString();

			Plugin.instance.logger.putstr(html);

		} catch (Exception e) {
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);

			html=sw.toString();

			Plugin.instance.logger.putstr("textToHTML: "+e.getMessage());
		}

		return html;
	}

	@Override
	public int compareTo(Freesite other) {
		return uniqueKey - other.uniqueKey;
	}
}
