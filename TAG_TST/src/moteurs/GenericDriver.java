package moteurs;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import beans.ObjectifBean;

/**
 * Classe chapeau generique pour les classes de silentdriver et seleniumdriver.
 * @author kcw293
 *
 */
public interface GenericDriver extends WebDriver, TakesScreenshot, HasInputDevices {

	/**
	 * Registre d'execution du driver.
	 */
	public StringBuffer log = new StringBuffer("");
	
	/**
	 * Indique si le log est activer.
	 */
	public boolean activationLog = true;
	
	/**
	 * Au fur et � mesure de l'execution, le driver renseigne ses propres objectifs.
	 */
	public HashMap<String, ObjectifBean> objectifsDriver = new LinkedHashMap<String, ObjectifBean>();
	
	/**
	 * Indicateur de recherche pere.
	 */
	public final static String PERE = "pere";
	/**
	 * Indicateur de recherche par texte.
	 */
	public final static String CRITERE_TEXTE = "text";
	/**
	 * Constante pour l'impl�mentation firefox du driver.
	 */
	public final static String FIREFOX_IMPL = "firefox";
	/**
	 * Constante pour l'impl�mentation IE du driver.
	 */
	public final static String IE_IMPL = "ie";
	/**
	 * Constante pour l'impl�mentation HTML du driver.
	 */
	public final static String HTML_IMPL = "html";
	/**
	 * Constante pour l'impl�mentation chrome du driver.
	 */
	public final static String CHROME_IMPL = "chrome";
	
	/**
	 *  Indicateur d'impl�mentation.
	 */
	public String impl = FIREFOX_IMPL;
	
	
	//////////////////////////// GETTERS ET SETTERS //////////////////////////////////////
	
	/**
	 * Permet d'obtenir les logs du driver.
	 * @return les log du driver.
	 */
	public StringBuffer getLog();

	/**
	 * Accesseur en saisie des logs.
	 * @param log le nouveau log.
	 */
	public void setLog(StringBuffer log);

	/**
	 * Indique si les log sont activ�s pour cette instance de driver.
	 * @return true si les logs sont activ�, false sinon.
	 */
	public boolean isActivationLog();

	/**
	 * Active ou d�sactive les log en fonction du param�tre.
	 * @param activationLog vrai si on souhaites activer les logs, false sinon.
	 */
	public void setActivationLog(boolean activationLog);

	/**
	 * Renvoie la liste des objectifs propre au driver.
	 * @return la liste des objectifs sous forme de HashMap.
	 */
	public HashMap<String, ObjectifBean> getObjectifsDriver();

	/**
	 * Fixe les objectifs du driver. 
	 * @param objectifsDriver les objectifs du driver sous forme de hashmap.
	 */
	void setObjectifsDriver(HashMap<String, ObjectifBean> objectifsDriver);
}