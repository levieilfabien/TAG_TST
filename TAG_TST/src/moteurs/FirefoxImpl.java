package moteurs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import outils.SeleniumOutils;
import beans.CibleBean;
import beans.EcranBean;
import beans.ObjectifBean;
import constantes.Clefs;
import exceptions.SeleniumException;

/**
 * Classe d'extension du driver firefox pour la prise en charge de fonction simplifiées.
 * @author levieil_f
 *
 */
public class FirefoxImpl extends FirefoxDriver implements GenericDriver {
	
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
	public String impl = GenericDriver.FIREFOX_IMPL;
	
	/**
	 * Au fur et à mesure de l'execution, le driver renseigne ses propres objectifs.
	 */
	private HashMap<String, ObjectifBean> objectifsDriver = new LinkedHashMap<String, ObjectifBean>();
	
	
	///////////////////// LES CONSTRUCTEURS PROPRES A L'IMPLEMENTATION FIREFOX ///////////////////////////
	
	/**
	 * Constructeur par profile.
	 * @param profile le profile firefox.
	 */
	public FirefoxImpl(FirefoxProfile profile) {
		//super(new FirefoxDriver(profile));
		super(profile);
		this.profilFirefox = profile;
		manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		logger("Injection du profil utilisateur dans le driver");
	}
	
//	public void ecouter() {
//		register(new GenericListener(this));
//	}
	
	/**
	 * Constructeur par défaut du selenium driver.
	 */
	public FirefoxImpl() {
		//super(new FirefoxDriver(new FirefoxProfile()));
		super(new FirefoxProfile());
		manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		logger("Injection du profil utilisateur dans le driver");
	}
	
	/**
	 * Constructeur par défaut du selenium driver.
	 */
	public FirefoxImpl(FirefoxBinary binary, FirefoxProfile profile) {
		//super(new FirefoxDriver(new FirefoxProfile()));
		super(binary, profile);
		manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		logger("Injection du profil utilisateur dans le driver");
	}
	
	/**
	 * Configure un driver pour fonctionner avec un proxy autoconfiguré.
	 * @param autoConfigUrl url d'autoconfiguration.
	 */
	public FirefoxImpl(final String autoConfigUrl) {
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
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
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
	
	
	public static void main(String args[]) throws SeleniumException, IOException {
//		//SeleniumDriver driver = new SeleniumDriver("http://proxypac.log.intra.laposte.fr/proxyie.pac");
//		
//		//profile.setPreference("general.config.filename", new File("Test").getAbsolutePath() + File.separator + "test.cfg");
//		FirefoxImpl driver = new FirefoxImpl(configurerProfil());
//		SeleniumOutils outil = new SeleniumOutils(driver);
//		//SeleniumDriver driver = new SeleniumDriver("http://proxypac.log.intra.laposte.fr/proxyie.pac");
//		
//	    //Connection à l'intranet
//	    driver.get("http://www.wac.courrier.intra.laposte.fr/");
//	    
//	    //Connection à habilinet .
//	    outil.cliquer(new CibleBean("sommaire", Clefs.XPATH, ".//*[@id='hab']/a"));
//	    outil.attendreChargementPage("Service d'Authentification Courrier");
//	
//		//Identification de l'utilisateur
//		outil.saisir("dwu164", new CibleBean("username"));
//		outil.saisir("laposte35", new CibleBean("password"));
//		outil.cliquer(new CibleBean(Clefs.NAME, "submit"));
//	    outil.attendreChargementPage("Web d'Accueil du Courrier");
//	
//	    //Accès au Support SI
//	    outil.cliquer(new CibleBean("sommaire", Clefs.XPATH, "html/body/table/tbody/tr[12]/td[2]/a"));
//	    
//	    //Accès à l'instance de travail
//	    outil.cliquer(new CibleBean("principal", Clefs.LIENPARTIEL, "INSTANCE"));
//	    
//	    //On va sur la nouvelle fenêtre qui vient d'ouvrir.
//	    outil.changerDeFenetre();
//	    
//	    //Identification pour le bureau personalisé
//	    outil.saisir("dwu164", new CibleBean("principal", Clefs.ID, "password"));
//	    outil.saisir("dwu164", new CibleBean("principal", Clefs.ID, "username"));
//	    outil.cliquer(new CibleBean("principal", Clefs.NAME, "submit"));
//	    
//	    //Accès à TRACEO
//	    outil.cliquer(new CibleBean("principal", Clefs.TEXTE_PARTIEL, "Recette Traceo - Administrateur"));
//	    
//	    //On va sur la nouvelle fenêtre qui vient d'ouvrir.
//	    outil.changerDeFenetre();
//	    outil.attendreChargementPage("TRACEO - Selection du site");
//	    
//	    //Fermer la popup à l'écran.
//	    outil.cliquerMultiple(new CibleBean(Clefs.XPATH, "html/body/div[4]/div[11]/button"));
//	    outil.sauvegarderCookies();
//	    
//	    AspirateurEcran aspirateur = new AspirateurEcran();
//	    EcranBean temp = aspirateur.aspirerPage(false, new String[]{driver.getPageSource()}, new String[]{""});
//	    outil.capturesEcrans(temp);
//	    aspirateur.genererExcelEcran(temp);
//	    
//	    //Stopper le test.
//	    driver.quit();

		// Initialisation du driver


		String pathToBinaryS = new String("C:\\Users\\levieilfa\\AppData\\Local\\Mozilla Firefox\\firefox.exe");
		//String pathToBinaryS = new String("C:\\Users\\levieilfa\\AppData\\Local\\firefox\\firefox.exe");
		File pathToBinary = new File(pathToBinaryS);
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		//Process proc = Runtime.getRuntime().exec("cmd set PATH=%PATH%;" + pathToBinary);
 
		
		FirefoxImpl driver = new FirefoxImpl(ffBinary, configurerProfilNatixis());
		SeleniumOutils outil = new SeleniumOutils(driver, GenericDriver.FIREFOX_IMPL);
		
		
		// Chargement d'une page de test
		//driver.get("https://open-workplace.intranatixis.com/nfi/front-middle/wiki-izivente/Rfrentiel/Liens%20Izivente.aspx");
		
		driver.get("https://nfi80.rec.intranatixis.com/izivente-bp_recB_current/reroutage.action");
		
		//System.out.println(driver.getCapabilities().getCapability(CapabilityType.PROXY));
		
		// Attente de l'affichage du titre de la page
		outil.attendreChargementPage("izivente");
		
		//outil.saisir("Test", new CibleBean(Clefs.XPATH, ".//*[@Title='Rechercher']"));
		//driver.getKeyboard().sendKeys("test");
		
		WebElement test = outil.obtenirElementVisible(new CibleBean(Clefs.NAME, "reroutage:data:data"));
		
		outil.cliquer("Reroutage");
		
		System.out.println(test.getText());
		
		//outil.presserEnter();
	}
	
	
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
