package moteurs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import beans.ObjectifBean;
import outils.SeleniumOutils;

/**
 * Classe d'extension du driver firefox pour la prise en charge de fonction simplifiées.
 * @author levieil_f
 *
 */
public class ChromeImpl extends ChromeDriver implements GenericDriver {
	
	/**
	 * Registre d'execution du driver.
	 */
	public StringBuffer log = new StringBuffer("");
	
	/**
	 * Indique si le log est activer.
	 */
	public boolean activationLog = true;
	
	/**
	 * Le profil utilise.
	 */
	public FirefoxProfile profilFirefox;
	
	/**
	 * Spécification de l'implémentation.
	 */
	public String impl = GenericDriver.CHROME_IMPL;
	
	/**
	 * Au fur et à mesure de l'execution, le driver renseigne ses propres objectifs.
	 */
	private HashMap<String, ObjectifBean> objectifsDriver = new LinkedHashMap<String, ObjectifBean>();
	
	
	///////////////////// LES CONSTRUCTEURS PROPRES A L'IMPLEMENTATION FIREFOX ///////////////////////////
	
//	public void ecouter() {
//		register(new GenericListener(this));
//	}
	
	/**
	 * Constructeur par défaut du selenium driver.
	 */
	public ChromeImpl() {
		//super(new FirefoxDriver(new FirefoxProfile()));
		super();
		manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		logger("Injection du profil utilisateur dans le driver");
	}
	
	/**
	 * Constructeur par défaut du selenium driver.
	 */
	public ChromeImpl(ChromeOptions option) {
		super(option);
	}
	
	/**
	 * Configure un driver pour fonctionner avec un proxy autoconfiguré.
	 * @param autoConfigUrl url d'autoconfiguration.
	 */
	public ChromeImpl(final String autoConfigUrl) {
		//super(new FirefoxDriver(configurerProxy(autoConfigUrl)));
		super(configurerProxy(autoConfigUrl));
	}
	
	/**
	 * Paramètre des capacité proxy de driver à partir d'une url de configuration automatique PAC.
	 * @param autoConfigUrl l'url vers l'autoconfiguration.
	 * @return les capacités pour le driver.
	 */
	private static Capabilities configurerProxy(final String autoConfigUrl) {
		Proxy proxy = new Proxy();
		proxy.setProxyType(ProxyType.PAC);
		proxy.setProxyAutoconfigUrl(autoConfigUrl);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.PROXY, proxy);
		return capabilities;
	}
	
	/**
	 * Permet de logger de informations dans les logs du driver.
	 * @param log la ligne de log à ajouter.
	 */
	public void logger(String log) {
		if (isActivationLog()) {
			getLog().append("\n"+log);
		}
	}
	
	/**
	 * Permet d'ajouter un objectif au objectifs du driver.
	 * Cet objectif doit être valider ou invalider au moment de l'ajout sans quoi la capture d'écran n'est pas significative.
	 * @param objectif l'objectif.
	 */
	public void ajouterObjectifObtenu(ObjectifBean objectif) {
		getObjectifsDriver().put(objectif.getClefUnique(), objectif);
		new SeleniumOutils(this).captureEcran(objectif.getClefUnique());
	}
	
	/**
	 * Permet d'ajouter un objectif au objectifs du driver.
	 * Cet objectif doit être valider ou invalider au moment de l'ajout sans quoi la capture d'écran n'est pas significative.
	 * @param element l'élément concerné par l'objectif.
	 * @param objectif l'objectif.
	 */
	private void ajouterObjectifObtenu(WebElement element, ObjectifBean objectif) {
		getObjectifsDriver().put(objectif.getClefUnique(), objectif);
		if (element != null) {
			new SeleniumOutils(this).captureEcran(element, objectif.getClefUnique());
		}
	}
	
	/////////////////////// GETTERS & SETTERS //////////////////////////////////

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#getLog()
	 */
	@Override
	public StringBuffer getLog() {
		return log;
	}

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#setLog(java.lang.StringBuffer)
	 */
	@Override
	public void setLog(StringBuffer log) {
		this.log = log;
	}

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#isActivationLog()
	 */
	@Override
	public boolean isActivationLog() {
		return activationLog;
	}

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#setActivationLog(boolean)
	 */
	@Override
	public void setActivationLog(boolean activationLog) {
		this.activationLog = activationLog;
	}

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#getObjectifsDriver()
	 */
	@Override
	public HashMap<String, ObjectifBean> getObjectifsDriver() {
		return objectifsDriver;
	}

	/* (non-Javadoc)
	 * @see moteur.GenericDriver#setObjectifsDriver(java.util.HashMap)
	 */
	@Override
	public void setObjectifsDriver(HashMap<String, ObjectifBean> objectifsDriver) {
		this.objectifsDriver = objectifsDriver;
	}
	
	////////////////////////////// TODO A SUPPRIMER /////////////////////////////////////////////////
	
	public static FirefoxProfile configurerProfilNatixis() {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("app.update.enabled", Boolean.FALSE);
		profile.setPreference("network.negotiate-auth.trusted-uris", "https://open-workplace.intranatixis.com/nfi/front-middle/wiki-izivente/Rfrentiel/Liens%20Izivente.aspx");
		profile.setPreference("network.automatic-ntlm-auth.trusted-uris", "https://open-workplace.intranatixis.com/nfi/front-middle/wiki-izivente/Rfrentiel/Liens%20Izivente.aspx");
		return profile;
	}
	
	public static FirefoxProfile configurerProfil() {
		FirefoxProfile profile = new FirefoxProfile();
		Proxy proxy = new Proxy();
		proxy = proxy.setAutodetect(false);
		proxy = proxy.setProxyType(ProxyType.MANUAL);
		// TODO : Remplace la prise en compte du proxy
		//profile.setProxyPreferences(proxy);
		
		profile.setPreference("network.http.phishy-userpass-length", 255);
		profile.setPreference("network.proxy.autoconfig_url", "http://proxypac.log.intra.laposte.fr/proxyie.pac");
		profile.setPreference("network.proxy.no_proxies_on", "*,*.intra.laposte.fr,localhost,127.0.0.1,93.93*,187.0.22.240,187.0.22.238,79.125.41.167,.spt.net2-courrier.extra.laposte.fr,.refprod.net-courrier.extra.laposte.fr,.refprod.pprd.net2-courrier.extra.laposte.fr,rao.net-courrier.extra.laposte.fr,gestri.net-courrier.extra.laposte.fr,refprod.net-courrier.extra.laposte.fr,.reftournees.pprd.net2-courrier.extra.laposte.fr,.gestri.pprd.net2-courrier.extra.laposte.fr");
		profile.setPreference("network.proxy.ftp", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.http", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.ssl", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.socks", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.gopher", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.gopher_port", 8080);
		profile.setPreference("network.proxy.ftp_port", 8080);
		profile.setPreference("network.proxy.socks_port", 8080);
		profile.setPreference("network.proxy.http_port", 8080);
		profile.setPreference("network.proxy.ssl_port", 8080);
		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.share_proxy_settings", Boolean.TRUE);
		//profile.setPreference("network.negotiate-auth.allow-proxies", Boolean.FALSE);
		//profile.setPreference("network.automatic-ntlm-auth.allow-proxies", Boolean.FALSE);
		profile.setPreference("network.auth.use-sspi", Boolean.FALSE);
		profile.setPreference("capability.policy.strict.Window.alert", "noAccess");
		profile.setPreference("network.negotiate-auth.trusted-uris", "http://www.wac.courrier.intra.laposte.fr/,http://idp.si-tri.com/,https://gestri.recdtc.dip.courrier.intra.laposte.fr/gestri/");
		profile.setPreference("network.automatic-ntlm-auth.trusted-uris", "http://www.wac.courrier.intra.laposte.fr/,http://idp.si-tri.com/,gestri.assemblage.net3-courrier.extra.laposte.fr,https://gestri.recdtc.dip.courrier.intra.laposte.fr/gestri/");
		return profile;
	}
}
