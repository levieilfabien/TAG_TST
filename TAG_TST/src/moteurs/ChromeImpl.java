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
	public void ajouterObjectifObtenu(WebElement element, ObjectifBean objectif) {
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

}
