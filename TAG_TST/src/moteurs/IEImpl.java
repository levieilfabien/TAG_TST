package moteurs;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import constantes.Clefs;
import outils.SeleniumOutils;
import beans.CibleBean;
import beans.ObjectifBean;
import exceptions.SeleniumException;

public class IEImpl extends InternetExplorerDriver implements GenericDriver {

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
	public String impl = GenericDriver.IE_IMPL;
	
	/**
	 * Au fur et à mesure de l'execution, le driver renseigne ses propres objectifs.
	 */
	private HashMap<String, ObjectifBean> objectifsDriver = new LinkedHashMap<String, ObjectifBean>();
	
	
	/**
	 * Constructeur par défaut du selenium driver.
	 */
	public IEImpl() {
		//super(new FirefoxDriver(new FirefoxProfile()));
		super(configurerProxy(""));
		manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
		logger("Injection du profil utilisateur dans le driver");
	}
	
	/**
	 * Paramètre des capacité proxy de driver à partir d'une url de configuration automatique PAC.
	 * @param autoConfigUrl l'url vers l'autoconfiguration.
	 * @return les capacités pour le driver.
	 */
	private static Capabilities configurerProxy(final String autoConfigUrl) {
//		Proxy proxy = new Proxy();
//		proxy.setProxyType(ProxyType.PAC);
//		proxy.setProxyAutoconfigUrl(autoConfigUrl);
		DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
        ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, false);
		//capabilities.setCapability(CapabilityType.PROXY, proxy);
		return ieCapabilities;
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
	
	
	public static void main(String args[]) throws SeleniumException {
		// Récupération de l'executable du driver IE.
		File file = new File("IEDriverServer.exe");
		System.setProperty("webdriver.ie.driver", file.getAbsolutePath());

		// Initialisation du driver
		IEImpl driver = new IEImpl();
		SeleniumOutils outil = new SeleniumOutils(driver, GenericDriver.IE_IMPL);
		
		
		
		// Chargement d'une page de test
		driver.get("http://google.fr/");
		
		//System.out.println(driver.getCapabilities().getCapability(CapabilityType.PROXY));
		
		// Attente de l'affichage du titre de la page
		outil.attendreChargementPage("Google");
		outil.saisir("Test", new CibleBean(Clefs.XPATH, ".//*[@Title='Rechercher']"));
		//driver.getKeyboard().sendKeys("test");
		
		outil.presserEnter();
	}

	
//	Required Configuration
//	The IEDriverServer exectuable must be downloaded and placed in your PATH. 
//	On IE 7 or higher on Windows Vista or Windows 7, you must set the Protected Mode settings for each zone to be the same value. The value can be on or off, as long as it is the same for every zone. To set the Protected Mode settings, choose "Internet Options..." from the Tools menu, and click on the Security tab. For each zone, there will be a check box at the bottom of the tab labeled "Enable Protected Mode". 
//	Additionally, "Enhanced Protected Mode" must be disabled for IE 10 and higher. This option is found in the Advanced tab of the Internet Options dialog. 
//	The browser zoom level must be set to 100% so that the native mouse events can be set to the correct coordinates. 
//	For IE 11 only, you will need to set a registry entry on the target computer so that the driver can maintain a connection to the instance of Internet Explorer it creates. For 32-bit Windows installations, the key you must examine in the registry editor is HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_BFCACHE. For 64-bit Windows installations, the key is HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_BFCACHE. Please note that the FEATURE_BFCACHE subkey may or may not be present, and should be created if it is not present. Important: Inside this key, create a DWORD value named iexplore.exe with the value of 0. 

}