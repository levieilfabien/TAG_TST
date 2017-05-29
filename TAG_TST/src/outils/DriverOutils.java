package outils;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import beans.CookieBean;
 
/**
 * Outils additionnels pour le driver s�l�nium.
 * Notament cette classe sert aux processus d'attente de pr�sence dans l'�cran d'�l�ment.
 * Cette classe utilise les fonctions g�n�riques de la librairie google guava.
 * @author levieil_f
 *
 */
public class DriverOutils {
 
	/**
	 * D�lai d'attente.
	 */
    private int timeoutSeconds = 30000;

    /**
     * Outil de mise en attente de driver.
     */
    private WebDriverWait wait;
     
    /**
     * Contructeur de l'outil.
     * @param driver le driver � faire patienter.
     */
    public DriverOutils(WebDriver driver){
        this.wait = new WebDriverWait(driver, this.timeoutSeconds);
        //this.driver = driver;
    }
     
    /**
     * Contructeur de l'outil.
     * @param driver le driver � faire patienter.
     * @param timeoutSeconds le temps d'attente.
     */
    public DriverOutils(WebDriver driver, int timeoutSeconds){
        this.wait = new WebDriverWait(driver,timeoutSeconds);
    }
     
    /**
     * Indique la pr�sence d'un �l�ment dans la page.
     * @param locator le crit�re de recherche dans la page.
     * @return la fonction sp�cifique associ�e � un �l�ment donn� r�pondant au crit�re de recherche.
     */
    Function<WebDriver, Boolean> testerPresenceElement(final By locator) {
        /**
         * Cette fonction associ�e un �l�ment au webdriver associ�.
         */
    	return new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver) {
                return driver.findElement(locator) != null;
            }
        };
    }

    /**
     * Fait patienter le temps qu'un �l�ment attendu soit pr�sent sur la page.
     * @param locator le crit�re d'attente.
     */
    public void attentreElementPresent(final By locator){
    	// Si on peux associ� un �l�ment au webdriver via le locator
//    	this.wait.until(new Function<WebDriver, Boolean>() {
//            @Override
//            public Boolean apply(WebDriver driver) {
//            	return true;
//            }
//    	});    	
        this.wait.until(testerPresenceElement(locator));
    }
     
    /**
     * Attend pendant une dur�e donn�e la pr�sence d'un �l�ment dans l'interface.
     * @param locator le crit�re de recherche de l'�lement.
     * @param seconds le nombre de seconde � attendre au maximum avant l'affichage de l'�l�ment attendue.
     */
    public void attendreElementPresent(final By locator, long seconds){
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime<seconds*1000) { 
        	if (testerPresenceElement(locator) != null) {
        		break;
        	}
        }
    }
    
    /**
     * Attend pendant une dur�e donn�e.
     * @param seconds le nombre de seconde � attendre.
     */
    public void attendre(long seconds){
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < seconds*1000) { 
        	// On attend .
        	
        }
    }
    
	/**
	 * Permet de sauvegarder les cookies pour une utilisation future.
	 * @param driver le driver dont on extrait les cookies.
	 * @param nomCookie nom du fichier serialis� � creer avec les donn�es cookie.
	 */
	public static void sauvegarderCookies(WebDriver driver, String nomCookie) {
        Set<Cookie> cookies = driver.manage().getCookies();
        List<CookieBean> cookiesASauvegarder = new ArrayList<CookieBean>();
        Iterator<Cookie> iterateur = cookies.iterator();
        
        while (iterateur.hasNext()) {
        	cookiesASauvegarder.add(new CookieBean(iterateur.next()));
        }
        
        try {
        	FileOutputStream fichier = new FileOutputStream(nomCookie + ".ser");
        	ObjectOutputStream oos = new ObjectOutputStream(fichier);
        	oos.writeObject(cookiesASauvegarder);
        	oos.flush();
        	oos.close();
        }
        catch (java.io.IOException e) {
        	e.printStackTrace();
        }

	}
    
    ////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS												////
    ////////////////////////////////////////////////////////////////////////
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
 
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
 
//    public By getLocator() {
//        return locator;
//    }
// 
//    public void setLocator(By locator) {
//        this.locator = locator;
//    }
 
    ////////////////////////////////////////////////////////////////////////
}