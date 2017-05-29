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
 * Outils additionnels pour le driver sélénium.
 * Notament cette classe sert aux processus d'attente de présence dans l'écran d'élément.
 * Cette classe utilise les fonctions génériques de la librairie google guava.
 * @author levieil_f
 *
 */
public class DriverOutils {
 
	/**
	 * Délai d'attente.
	 */
    private int timeoutSeconds = 30000;

    /**
     * Outil de mise en attente de driver.
     */
    private WebDriverWait wait;
     
    /**
     * Contructeur de l'outil.
     * @param driver le driver à faire patienter.
     */
    public DriverOutils(WebDriver driver){
        this.wait = new WebDriverWait(driver, this.timeoutSeconds);
        //this.driver = driver;
    }
     
    /**
     * Contructeur de l'outil.
     * @param driver le driver à faire patienter.
     * @param timeoutSeconds le temps d'attente.
     */
    public DriverOutils(WebDriver driver, int timeoutSeconds){
        this.wait = new WebDriverWait(driver,timeoutSeconds);
    }
     
    /**
     * Indique la présence d'un élément dans la page.
     * @param locator le critère de recherche dans la page.
     * @return la fonction spécifique associée à un élément donné répondant au critère de recherche.
     */
    Function<WebDriver, Boolean> testerPresenceElement(final By locator) {
        /**
         * Cette fonction associée un élément au webdriver associé.
         */
    	return new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver) {
                return driver.findElement(locator) != null;
            }
        };
    }

    /**
     * Fait patienter le temps qu'un élément attendu soit présent sur la page.
     * @param locator le critère d'attente.
     */
    public void attentreElementPresent(final By locator){
    	// Si on peux associé un élément au webdriver via le locator
//    	this.wait.until(new Function<WebDriver, Boolean>() {
//            @Override
//            public Boolean apply(WebDriver driver) {
//            	return true;
//            }
//    	});    	
        this.wait.until(testerPresenceElement(locator));
    }
     
    /**
     * Attend pendant une durée donnée la présence d'un élément dans l'interface.
     * @param locator le critère de recherche de l'élement.
     * @param seconds le nombre de seconde à attendre au maximum avant l'affichage de l'élément attendue.
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
     * Attend pendant une durée donnée.
     * @param seconds le nombre de seconde à attendre.
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
	 * @param nomCookie nom du fichier serialisé à creer avec les données cookie.
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