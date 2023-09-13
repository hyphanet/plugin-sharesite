package plugins.Sharesite;

import freenet.l10n.BaseL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.l10n.PluginL10n;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginRealVersioned;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Ticker;
import plugins.Sharesite.common.Logger;
import plugins.Sharesite.webui.WebInterface;

/**
 * This is the main class, the first one executed by Freenet.
 * Only one instance exists, and can be accessed from anywhere using
 * the global variable. The most logical way to reach different things.
 */
public class Plugin implements FredPlugin, FredPluginVersioned, FredPluginRealVersioned, FredPluginL10n, FredPluginBaseL10n, FredPluginThreadless {
	private static final String version = "0.5.0";
	public static final long realVersion = 7; // the version given to fred to decide whether to reload on update
	public static final String freesite = "USK@dCnkUL22fAmKbKg-Cftx9j2m4IwyWB0QbGoiq1RSLP8,4d1TDqwRr4tYlsubLrQK~c4h0~FtmE-OXCDmFiI8BB4,AQACAAE/Sharesite/-36/";
	public static boolean isPreRelease = false;

	private PluginL10n plugL10n;

	public static Plugin instance;
	public PluginRespirator pluginRespirator;
	public Ticker ticker;
	public BaseL10n l10n;

	public Logger logger;
	public Inserter inserter;
	public Database database;
	public WebInterface webInterface;

	public Plugin() {
		instance = this;
	}

	@Override
	public void runPlugin(PluginRespirator pr) {
		pluginRespirator = pr;
		ticker = pr.getNode().getTicker();

		logger = new Logger("Sharesite.log");
		logger.putstr("Loading of Sharesite " + version + " begins!");

		logger.putstr("Setting GraphicsEnvironment to headless");
		System.setProperty("java.awt.headless","true");

		logger.putstr("Preparing the inserter ...");
		inserter = new Inserter(ticker);

		logger.putstr("Loading the database ...");
		database = new Database();

		logger.putstr("Registering the web interface ...");
		webInterface = new WebInterface();
		webInterface.createInterface();

		logger.putstr("Successfully started!");
	}

	@Override
	public void terminate() {
		logger.putstr("Terminating ...");
		webInterface.removeInterface();
		inserter.terminate();
		ticker.removeQueuedJob(inserter);
		logger.close();
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public long getRealVersion() {
		return realVersion;
	}

	@Override
	public String getString(String key) {
		return l10n.getString(key);
	}

	@Override
	public void setLanguage(LANGUAGE newLanguage) {
		plugL10n = new PluginL10n(this, newLanguage);
		l10n = plugL10n.getBase();
	}

	@Override
	public String getL10nFilesBasePath() {
		return "plugins/Sharesite/l10n/";
	}

	@Override
	public String getL10nFilesMask() {
		return "lang_${lang}.l10n";
	}

	@Override
	public String getL10nOverrideFilesMask() {
		return "Sharesite_lang_${lang}.override.l10n";
	}

	@Override
	public ClassLoader getPluginClassLoader() {
		return Plugin.class.getClassLoader();
	}
}
