package outils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;
import org.openqa.selenium.support.ui.WebDriverWait;

import annotations.Step;
import beans.CasEssaiBean;
import beans.CibleBean;
import beans.CookieBean;
import beans.EcranBean;
import beans.ObjectifBean;
import beans.RGMBean;
import constantes.Clefs;
import constantes.Erreurs;
import exceptions.SeleniumException;
import moteurs.ChromeImpl;
import moteurs.FirefoxImpl;
import moteurs.GenericDriver;
import moteurs.IEImpl;

/**
 * Boite à outil contenant l'ensemble des fonctionnalités communes aux différentes implémentation du driver.
 * Il faut préciser à quelle type d'impl doit s'adapter le driver, sinon Firefoc est choisie par défaut.
 * @author Fabien Levieil
 *
 */
public class SeleniumOutils {

	/**
	 * Le driver utilisé par la boite à outils.
	 */
	private GenericDriver driver = null;

	/**
	 * Le driver utilisé par la boite à outils.
	 */
	private EventFiringWebDriver clone = null;
	
	/**
	 * Le type d'implémentation du driver manipuler par la boite à outils.
	 */
	private String typeImpl = null;
	
	/**
	 * Le repertoire racine pour l'écriture d'objet.
	 */
	private String repertoireRacine = ".";
	
	/**
	 * L'attente maximale autorisée pour les fonctions d'attentes.
	 */
	private int attenteMax = 20;
	
	public void ajouterListener() {
		EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
		//eventDriver.register(new SeleniumListener());
		
		// ((EventFiringWebDriver) driver).register(new SeleniumListener()); 
	}
	
	/**
	 * Fonction qui injecte un code dans la page pour retenir tout clic sur la page.
	 * Cette fonction n'est à utiliser qu'avec un navigateur le permettant.
	 * @throws SeleniumException en cas d'erreur lors du parcours des frames.
	 */
	public void detecterClicParJavascript() throws SeleniumException {
		List<String> listeFrame = extraireFrames();
		if (listeFrame != null && !listeFrame.isEmpty()) {
		for (String idFrame : listeFrame) {
			driver = changerDeFrame(idFrame);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("last_clic = 'test';"
					+ " function getClic() {return last_clic;}"
					+ " function setClic(toSet) {last_clic = toSet;}"
					+ " document.onclick= function(event) { if (event===undefined) event= window.event; var target= 'target' in event? event.target : event.srcElement;last_clic=target; alert('un clic a recuperer')};");
			}
		}
	}
	
	/**
	 * Renvoie le dernier WebElement qui ai subit un clic. Cette fonction dépend de la mise en place de la détection par javascript.
	 * @return le WebElement ayant subit le dernier clic ou null si aucun objet.
	 */
	public Object dernierClicParJavascript() {
		//org.openqa.selenium.remote.ErrorHandler$UnknownServerException
		JavascriptExecutor js = (JavascriptExecutor) driver;
		accepterAlerteJavascript();
		Object objet = js.executeScript("return last_clic;");
		System.out.println(objet);
		if (objet != null && objet.getClass() == WebElement.class) {
			return (WebElement) objet;
		} else {
			return objet;
		}
	}
	
	/**
	 * Permet de colorer brievement un élément pour le montrer sur l'interface.
	 * @param element l'élément à indiquer.
	 */
	public void surlignerElement(WebElement element) {
	    //for (int i = 0; i < 2; i++) {
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
	        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
	    //}
	}
	
	/**
	 * Permet de colorer brievement un élément pour le montrer sur l'interface.
	 * @param element l'élément à indiquer.
	 */
	public void deSurlignerElement(WebElement element) {
	    //for (int i = 0; i < 2; i++) {
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
	        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
	    //}
	}
	
	/**
	 * Permet de diriger la souris vers l'élément paramète.
	 * @param element l'élément paramètre vers lequel la souris doit se diriger.
	 */
	public void focusElement(WebElement element) {
		if (element.isDisplayed()) {
			Actions builder = new Actions(driver);
			Action moveTo = builder.moveToElement(element).build();
			moveTo.perform();
		}
	}
	
	/**
	 * Permet de diriger la souris vers l'élément paramète.
	 * @param element l'élément paramètre vers lequel la souris doit se diriger.
	 * @throws SeleniumException en cas d'erreur lors de la récupération de la cible.
	 */
	@Step(nom="focus")
	public void focusElement(CibleBean cible) throws SeleniumException {
		WebElement element = obtenirElement(cible);
		if (element.isDisplayed()) {
			Actions builder = new Actions(driver);
			Action moveTo = builder.moveToElement(element).build();
			moveTo.perform();
		}
	}
	
	/**
	 * Réalise une capture d'écran dans un fichier cible.
	 * @param cible le fichier cible pour la capture d'écran.
	 * @throws IOException en cas d'erreur d'accès au fichier.
	 */
	public void captureEcran(File cible) throws IOException {
		File screenShot  = driver.getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(screenShot, cible);
	}
	
	/**
	 * Effectue une capture d'écran ayant pour nom le paramètre.
	 * @param nomCapture le nom de la capture d'écran (sans l'extension).
	 */
	@Step(nom="capture")
	public void captureEcran(String nomCapture) {
		//logger("Capture d'écran : " + nomCapture);
		if (repertoireRacine != null) {
			captureEcran(nomCapture, repertoireRacine);
		} else {
			captureEcran(nomCapture, null);
		}
	}
	
	/**
	 * Effectue une capture d'écran ayant pour nom le paramètre.
	 * @param nomCapture le nom de la capture d'écran (sans l'extension).
	 * @param cheminSortie chemin vers le fichier devant receptionner la capture d'écran.
	 */
	public void captureEcran(String nomCapture, String cheminSortie) {
		//logger("Capture d'écran : " + nomCapture);
		try {
			String chemin = ".\\captures";
			if (cheminSortie != null) {
				chemin = cheminSortie + File.separator + "captures";
			}
			FileUtils.forceMkdir(new File(chemin));
			File cible = new File(chemin + File.separator + nomCapture + ".png");
			captureEcran(cible);
		} catch (IOException e) {
			logger("Impossible de faire une capture d'écran (" + e.getMessage() +")");
		} catch (Exception e) {
			logger("Erreur inatendue lors de la capture d'écran (" + e.getMessage() +")");
		}
	}
	
	// TODO Quand l'élément est dans un frame, ca décale les captures d'écrans.
	
	/**
	 * Réalise une capture d'écran pour un élément donné uniquement.
	 * @param element l'élément dont on souhaites capturer l'image.
	 * @param nomCapture le nom de la capture correspondante.
	 * @return true si la capture de l'élément à été possible, false sinon.
	 */
	public boolean captureEcran(WebElement element, String nomCapture) {
		boolean retour = false;
		if (element != null && element.isDisplayed()) {
			//logger("Capture d'écran pour un element : " + nomCapture);
	        File screenShot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			try {
		        Point positionElement = element.getLocation();
		        Rectangle surfaceElement = new Rectangle(element.getSize().getWidth() + 2, element.getSize().getHeight() + 2);
		        BufferedImage ecranComplet = ImageIO.read(screenShot);
		        BufferedImage imageElement = ecranComplet.getSubimage(positionElement.getX(), positionElement.getY(), surfaceElement.width, surfaceElement.height);
		        ImageIO.write(imageElement, "png", screenShot);
				FileUtils.forceMkdir(new File(repertoireRacine + "\\captures\\"));
				FileUtils.copyFile(screenShot, new File(repertoireRacine + "\\captures\\" + nomCapture + ".png"));
				retour = true;
			} catch (IOException e1) {
				logger("Impossible de faire une capture d'écran de l'élément (" + e1.getMessage() +")");
			} catch (RasterFormatException e3)  {
				// l'image est mal construite ! On réalise une capture complète de l'écran.
				surlignerElement(element);
				captureEcran(nomCapture);
				deSurlignerElement(element);
			} catch (Exception e2) {
				logger("Erreur inatendue lors de la capture d'écran de l'élément (" + e2.getMessage() +")");
			}
		}
		return retour;
	}
	
	/**
	 * Permet de sauvegarder les cookies dans un fichier cookie.ser.
	 */
	@Step(nom="sauverCookies")
	public void sauvegarderCookies() {
		sauvegarderCookies("cookie");
	}
	
	/**
	 * Permet de sauvegarder les cookies dans un fichier de nom choisis.
	 * @param nomCookie le nom du fichier cookie désiré.
	 */
	public void sauvegarderCookies(String nomCookie) {
		DriverOutils.sauvegarderCookies(driver, nomCookie);
	}
	
	/**
	 * Charge le cookie dont le nom est cookie.ser dans le driver.
	 * @throws SeleniumException en cas d'erreur.
	 */
	@Step(nom="chargerCookies")
	public void chargerCookie() throws SeleniumException {
		chargerCookie("cookie");
	}

	   
	/**
	 * Charge le fichier cookie dont le nom est spécifier dans le driver.
	 * @param nomCookie le nom du fichier cookie (sans le .ser).
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void chargerCookie(String nomCookie) throws SeleniumException {
		FileInputStream fichier;
		try {
			fichier = new FileInputStream(nomCookie + ".ser");
			ObjectInputStream ois = new ObjectInputStream(fichier);
			List<CookieBean> cookies  = (List<CookieBean>) ois.readObject();
			for (CookieBean cookie : cookies) {
				driver.manage().addCookie(cookie.toCookie());
			}
			
		} catch (IOException e) {
			throw new SeleniumException(Erreurs.E001, "Le cookie est incorrect");
		} catch (ClassNotFoundException e) {
			throw new SeleniumException(Erreurs.E002, "Le cookie est incorrect");
		}
	}
	
	/**
	 * Indique si le chargement de la page est terminé.
	 * @param titre le titre de la page à attendre.
	 * @return true si la page attendue à finie son chargement, false si non.
	 */
	public boolean verifierChargementPage(final String titre) {
		boolean retour = true;
		try {
			attendreChargementPage(titre, null);
		} catch (SeleniumException e) {
			retour = false;
		}
		return retour;
	}
	
	/**
	 * Attend le chargement d'une page dont le titre est connu.
	 * @param titre le titre de la page dont on attend le chargement.
	 * @throws SeleniumException en cas d'erreur.
	 */
	@Step(nom="attendrePage")
	public void attendreChargementPage(final String titre) throws SeleniumException {
		attendreChargementPage(titre, null);
	}
	
	/**
	 * Attend le chargement d'une page dont le titre est connu.
	 * @param titre le titre de la page dont on attend le chargement.
	 * @param casEssai le cas d'essai à renseigner avec l'information.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementPage(final String titre, final CasEssaiBean casEssai) throws SeleniumException {
		attendreChargementPage(titre, casEssai, true);
	}
	
	/**
	 * Attend le chargement d'une page en fonction de son titre.
	 * @param titre le titre de la page.
	 * @param casEssai le cas d'essai à renseigner.
	 * @param lowerCase indique si le test se fait sur des minuscules ou des majuscules.
	 * @throws SeleniumException
	 */
	public void attendreChargementPage(final String titre, final CasEssaiBean casEssai, final boolean lowerCase) throws SeleniumException {
		logger("On attend la page " + titre);
		final Long timestamp = new Date().getTime();
		
		// On dispatch les popup javascript bloquant eventuellement l'attente.
    	if (testerPresenceAlerteJavascript()) {
    		accepterAlerteJavascript();
    	}
		
		try {
			(new WebDriverWait(driver, 10)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	// En fait on ne respecte jamais la casse...
		        	boolean temp = lowerCase?supprimerCaracteresSpeciaux(d.getTitle().toLowerCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toLowerCase(Locale.ENGLISH))):supprimerCaracteresSpeciaux(d.getTitle().toUpperCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toUpperCase(Locale.ENGLISH)));
		        	if (temp) {
		        		if (casEssai != null) {
		        			if (driver.impl.equals(GenericDriver.IE_IMPL)) {
		        				casEssai.ajouterEcran((IEImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), true));
		        			} else if(driver.impl.equals(GenericDriver.FIREFOX_IMPL)) {
		        				casEssai.ajouterEcran((FirefoxImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), true));
		        			} else {
		        				casEssai.ajouterEcran((ChromeImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), true));
		        			}
		        		} else {
		        			ajouterObjectifObtenu(new ObjectifBean(d.getTitle(), titre, titre.replace(" ", "") + timestamp.toString(), true));
		        		}
		        		return true;
		        	} else {
		        		if (casEssai != null) {
		        			if (driver.impl.equals(GenericDriver.IE_IMPL)) {
		        				casEssai.ajouterEcran((IEImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), false));
		        			} else if(driver.impl.equals(GenericDriver.FIREFOX_IMPL)) {
		        				casEssai.ajouterEcran((FirefoxImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), false));
		        			} else {
		        				casEssai.ajouterEcran((ChromeImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), false));
		        			}
		        		} else {
		        			ajouterObjectifObtenu(new ObjectifBean(d.getTitle(), titre, titre.replace(" ", "") + timestamp.toString(), false));
		        		}
		        		return false;
		        	}
		        }
		    });
	    } catch (TimeoutException e) {
	    	// Si on arrive ici, alors le test à échoué, et la page n'as pas été chargée.
	    	throw new SeleniumException(Erreurs.E012, "On attendais : " + titre + " et non : " + driver.getTitle());
	    }
	}
	
	/**
	 * Permet de supprimer en avance de phase les alertes javascripts qui pourraient survenir sur la page en cours.
	 * Cette action s'effectue par une injection de code javascript, si les scripts sont bloquer, cela ne marchera pas.
	 */
	public void supprimerAlerteJavascript() {
		try {
			((JavascriptExecutor) driver).executeScript("window.confirm = function(msg){return true;}");
		} catch (WebDriverException ex) {
			logger("Impossible d'injecter le code javascript, un script js est probablement bloquer");
		}
	}
	
	/**
	 * Simule l'acceptation d'une alerte javacript déjà présente à l'écran.
	 * Si aucune popup n'est présente, l'action est ignorée.
	 */
	public void accepterAlerteJavascript() {
		try {
			// On tente de passer à l'alerte.
			Alert alert = driver.switchTo().alert();
			logger("On valide la popup dont le texte est : " + alert.getText());
			alert.accept();
		} catch (NoAlertPresentException ex) {
			logger("Aucune popup d'alerte n'est présente, il est impossible de valider une popup");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger("Impossible de valider un prompt ou une alerte javascript");
		}
	}
	
	/**
	 * Test la présence à l'écran d'une popup d'alerte javascript.
	 * @return true si une popup est présente, false sinon.
	 */
	public Boolean testerPresenceAlerteJavascript() {
		try {
			//logger("On teste la presence d'une popup");
			// On tente de passer à l'alerte.
			Alert alert = driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException ex) {
			// Pas d'alerte présente
			return false;
		} catch (WebDriverException ex) {
			// La fenetre est deja fermee.
			return false;
		} catch (ClassCastException ex) {
			// Une erreur du framework webdriver renvoie un boolean au lieu du contenue de l'alerte.
			// On considère qu'une alerte est bel et bien présente.
			return true;
		}
	}
	
	/**
	 * Permet d'obtenir le corps du message d'une alerte javascript à l'écran.
	 * Si aucune alerte n'est présente, l'action est ignorée.
	 * @return le corps du message d'alerte.
	 */
	public String obtenirTexteAlerteJavascript() {
		try {
			// On tente de passer à l'alerte.
			Alert alert = driver.switchTo().alert();
			logger("On recupere la popup dont le texte est : " + alert.getText());
			return alert.getText();
		} catch (NoAlertPresentException ex) {
			logger("Aucune popup d'alerte n'est présente, il est impossible de recuperer une popup");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger("Impossible de recuperer un prompt ou une alerte javascript");
		}
		return "";
	}
	
	/**
	 * Attend le chargement de la dernière page dont le titre est connue.
	 * @param titre le titre de la page à attendre.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementDernierePage(final String titre) throws SeleniumException {
	    try {	
	    	if (testerPresenceAlerteJavascript()) {
	    		accepterAlerteJavascript();
	    	}    	
			(new WebDriverWait(driver, attenteMax)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		            return changerDeFenetre(titre).getTitle().toLowerCase().startsWith(titre.toLowerCase());
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E012, "On attendais : " + titre + " et non : " + driver.getTitle());
	    }
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une période maximale de 10 secondes.
	 * @param texte le texte.
	 * @throws SeleniumException en cas d'erreur.
	 */
	@Step(nom="attendreTexte")
	public void attendrePresenceTexte(final String texte) throws SeleniumException {
		attendrePresenceTexte(null, texte);
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une période maximale précisée.
	 * @param texte le texte.
	 * @param attente le temps d'attente maximal.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String texte, final long attente) throws SeleniumException {
		attendrePresenceTexte(null, texte, attente);
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une période maximale de 10 secondes.
	 * @param frame la frame contenant le texte.
	 * @param texte le texte.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String frame, final String texte) throws SeleniumException {
		attendrePresenceTexte(frame, texte, attenteMax);
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte.
	 * @param frame la frame contenant le texte.
	 * @param texte le texte.
	 * @param attente le temps d'attente maximal.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String frame, final String texte, final long attente) throws SeleniumException {
		final Long timestamp = new Date().getTime();
	    try {
			(new WebDriverWait(driver, attente)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	return testerPresenceTexte(frame, texte, true, timestamp);
		        }
		    });
	    } catch (TimeoutException e) {
	    	//Assert.fail("Le texte " + texte + " n'est pas présent sur la page.");
	    	throw new SeleniumException(Erreurs.E017, texte);
	    }  catch (UnhandledAlertException e) {
	    	// Une popup est présente à l'écran et empêche de continuer la recherche.
	    	logger("Lors de l'attente de l'apparition du texte (" + texte + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue à eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de l'apparition du texte (" + texte + "), une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}
	
	/**
	 * Attend que la cible prenne une valeur non vide et non nulle
	 * @param cible la cible dont on attend la valorisation.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreValorisation(final CibleBean cible) throws SeleniumException  {
		attendreValorisation(cible, null);
	}
	
	/**
	 * Attend que la cible prenne la valeur donnée en paramètre.
	 * @param cible la cible dont on attend la valorisation.
	 * @param texte la valeur que doit prendre la cible, si à null alors n'importe quelle valeur non vide est acceptée.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreValorisation(final CibleBean cible, final String texte) throws SeleniumException  {
	    try {
				(new WebDriverWait(driver, attenteMax)).until(new Function<WebDriver, Boolean>() {
			        public Boolean apply(WebDriver d) {
			        	String retour;
						try {
							retour = obtenirValeur(cible);
						} catch (SeleniumException e) {
							return false;
						}
			        	if (texte != null) {
			        		return texte.equals(retour);
			        	} else {
			        		return (retour != null && !"".equals(retour));
			        	}
			            
			        }
				});
		} catch (TimeoutException e) {
			if (texte == null || "".equals(texte)) {
				throw new SeleniumException(Erreurs.E026, "On attendais n'importe quelle valorisation dans " + cible.lister());
			} else {
				throw new SeleniumException(Erreurs.E013, "On attendais " + texte + " dans " + cible.lister());
			}
	    	//Assert.fail("Le texte " + texte + " est présent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est présente à l'écran, le texte n'est probablement plus présente à l'écran.
	    	logger("Une alerte bloque le processus de vérification de la valorisation de la cible.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue à eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de valorisation, une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}

	/**
	 * Attend la disparition d'un texte pendant 30 seconde maximum.
	 * @param texte le texte dont on attend la disparition.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreNonPresenceTexte(final String texte) throws SeleniumException {
		attendreNonPresenceTexte(texte, attenteMax);
	}
	
	/**
	 * Attend la disparition d'un texte pendant une durée en seconde donnée.
	 * @param texte le texte dont on attend la disparition.
	 * @param time le temps maximal d'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreNonPresenceTexte(final String texte, Integer time) throws SeleniumException {
	    try {
			(new WebDriverWait(driver, time)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		            return !testerPresenceTexte(null, texte, true, null);
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E013, texte);
	    	//Assert.fail("Le texte " + texte + " est présent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est présente à l'écran, le texte n'est probablement plus présente à l'écran.
	    	logger("Lors de l'attente de la disparition du texte (" + texte + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue à eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la disparition du texte (" + texte + "), une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}
	
	/**
	 * Teste la présence d'un texte dans une page.
	 * @param frame la frame où se situe le texte.
	 * @param text le texte dont la présence est à tester.
	 * @param visible la condition de visibilité du texte.
	 * @param timestamp un timestamp de référence pour stocker l'évènement.
	 * @return true si le texte est présent, false sinon.
	 */
	public boolean testerPresenceTexte(String frame, String text, boolean visible, Long timestamp){
		logger("On teste la presence du texte " + (visible?"visible":"invisible") + " '" + text + "' (frame : " + frame + ")");
		// Utilisation d'un timeStamp pour l'unicité des objectifs.
		// On tente de localiser le texte tel quel (sans balise)
		List<WebElement> objets;
		if (frame == null) {
			objets = driver.findElements(By.xpath("//*[text()='" + text + "']"));
		} else {
			objets = driver.switchTo().frame(frame).findElements(By.xpath("//*[text()='" + text + "']"));
		}
		// Si on ne trouve pas le texte proprement dit, on tente de le localiser comme attribut d'une balise.
		if (objets.size() == 0) {
			if (frame == null) {
				objets = driver.findElements(By.xpath("//*[normalize-space()='" + text + "']"));
			} else {
				objets = driver.switchTo().frame(frame).findElements(By.xpath("//*[normalize-space()='" + text + "']"));
			}
		}
		// En dernier ressort , on parcours l'ensemble de la page et on cherche l'expression (dans le cas d'expression partielle, c'est la seule solution)
		if (objets.size() == 0) {
			if (frame == null) {
				objets = driver.findElements(By.xpath(".//*[contains(text(),'" + text + "')]"));
			} else {
				objets = driver.switchTo().frame(frame).findElements(By.xpath(".//*[contains(text(),'" + text + "')]"));
			}
		}
		// Si on cherche à ce que le texte soit visible à l'écran
		if (visible) {
			for (WebElement element : objets)  {
				// Si un seul des éléments trouvés est visible, alors le texte est visible
				if (element.isDisplayed()) {
					if (timestamp != null)
						ajouterObjectifObtenu(element, new ObjectifBean(text, text, timestamp.toString() + text, true));
					return true;
				}
			}
			// Si on arrive ici , alors l'élément est invisible, et on retourne faux.
			if (timestamp != null)
				ajouterObjectifObtenu(new ObjectifBean("", text, timestamp.toString() + text, false));
			return false;
		} else {
			Boolean temp = objets.size() > 0;
			// Si la visibilité n'as pas d'importance, il suffit que le texte existe.
			if (timestamp != null)
				ajouterObjectifObtenu(objets.get(0), new ObjectifBean(temp?text:"", text, timestamp.toString() + text, temp));
			return temp;
		}
	}

	/**
	 * Vérifie la présence d'un texte dans la page.
	 * @param text le texte à trouver.
	 * @param visible le critère de visibilité.
	 * @return true si le texte est trouvé, false sinon.
	 */
	public boolean testerPresenceTexte(String text, boolean visible){
		return testerPresenceTexte(null, text, visible, new Date().getTime());
	}
	
	/**
	 * Fonction expérimentale de test de la présence d'un élément dans la page en exploitant les expected exception.
	 * @param cible la cible de l'attente
	 * @throws SeleniumException en cas d'erreur ou d'indisponibilité de l'élément.
	 */
	public void attendreChargementElementViaCondition(final CibleBean cible) throws SeleniumException {
		//WebDriverWait wait = new WebDriverWait(driver, attenteMax);
		//WebElement element = wait.until(ExpectedConditions.elementToBeClickable(cible.creerBy()));
		//return element != null;
		appliquerCondition(cible, ExpectedConditions.elementToBeClickable(cible.creerBy()));
	}
	
	/**
	 * Attend le chargement d'un élément, que celui ci soit visible ou non.
	 * @param cible la cible désignant l'élement à attendre.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementElement(final CibleBean cible) throws SeleniumException {
		attendreChargementElement(cible, false, false);
	}
	
	/**
	 * Attend le chargement d'un élément dans la page avant de poursuivre le test.
	 * @param cible la cible désignant l'élément à attendre.
	 * @param visible l'élement doit il être visible ?
	 * @param actif l'élement doit il être actif (non disabled) ?
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementElement(final CibleBean cible, final boolean visible, final boolean actif) throws SeleniumException {
    	Long tempsAttendu = new Date().getTime();
		try {
	    	logger("On attend le chargement de l'élément " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	//final By critere = cible.creerBy();
			(new WebDriverWait(driver, attenteMax)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver driver) {
		        	try {
		        		Boolean retour = false;
		        		WebElement temp;
		        		// En fonction du critère visible on récupère l'élément.
		        		if (visible) {
		        			temp = obtenirElementVisible(cible);
		        		} else {
		        			temp = obtenirElement(cible.getFrame(), cible.creerBy());
		        		}
		        		retour = (temp != null);
		        		// Si on à un critère d'activité et que l'élément existe on vérifie.
		        		if (actif && retour) {
		        			retour = (temp.isEnabled() && temp.getAttribute("disabled") == null);
		        		}
		        		return retour;
		        	} catch (SeleniumException e) {
		        		//e.printStackTrace();
		        		// L'élément n'est pas encore présent.
		        		return false;
		        	} catch (StaleElementReferenceException ex) {
		        		//ex.printStackTrace();
		        		// L'élément n'est plus présent, la référence est donc obselète.
		        		return false;
		        	} catch (Exception ex2) {
		        		//ex2.printStackTrace();
		        		// Une erreur non prévue s'est déroulée. On ne fait que relancer jusqu'au timer.
		        		return false;
		        	}
		        }
		    });
	    } catch (TimeoutException e) {
	    	System.out.println("Temps attendu avant timeout : " + (tempsAttendu - new Date().getTime()));
	    	throw new SeleniumException(Erreurs.E009, "Element introuvable (ou désactivé) : " + cible.lister());
	    }
	}
	
	/**
	 * Fonction d'attente qui vérifie l'affichage et l'état "cliquable" d'un élément désigné par la cible.
	 * Cette fonction exploite les capacité des ExpectedConditions.
	 * @param cible la cible désignant l'élément dont on attend l'affichage
	 * @throws SeleniumException en cas d'erreur ou de non disponibilité de l'élément.
	 */
	public void attendreElement(final CibleBean cible) throws SeleniumException {
		
//		FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
//		        .withTimeout(30, TimeUnit.SECONDS)
//		        .pollingEvery(200, TimeUnit.MILLISECONDS)
//		        .ignoring(NoSuchElementException.class);
		
	    try {
	    	logger("On attend le chargement de l'élément " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	final By critere = cible.creerBy();
	    	// On cherche à obtenir l'élément. Pour être légitime il doit être clickable.
	    	new WebDriverWait(driver, attenteMax).until(
	    			new Function<WebDriver, Boolean>() {
	    	            public Boolean apply(WebDriver driver) {
	    	            	WebElement element = ExpectedConditions.elementToBeClickable(critere).apply(driver);
	    	            	return element != null;
	    	            }
	    	        });	
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "Element introuvable (ou désactivé) : " + cible.lister());
	    } catch (StaleElementReferenceException ex) {
    		// La référence à l'élément obselète mais celui ci existe bel et bien, on réinitialise l'attente.
    		attendreElement(cible);
    	}
	}
	
	/**
	 * Attend le chargement d'un élément visible dans la page avant de poursuivre le test.
	 * La recherche s'effectue dans la Frame active.
	 * @param cible la cible désignant l'élement à attendre.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreVisibiliteElement(CibleBean cible) throws SeleniumException {
	    WebDriverWait wait = new WebDriverWait(driver, attenteMax);
	    try {
	    	wait.until(			
	    			new Function<WebDriver, Boolean>() {
	    	            public Boolean apply(WebDriver driver) {
	    	            	WebElement element = ExpectedConditions.visibilityOfElementLocated(cible.creerBy()).apply(driver);
	    	            	return element != null;
	    	            }
	    	        });	
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "Element introuvable (ou désactivé) : " + cible.lister());
	    }
	}
	
	/**
	 * Fonction temporaire pour une attente. Meilleure solution à trouver.
	 * @param nbSec le nombre de seconde d'attente
	 * @deprecated cette fonction est à éviter, préférez les fonction d'attente d'éléments.
	 */
	@Deprecated
	public void attendre(long nbSec) {
	    try {;
	    	logger("On attend pendant " + nbSec + " secondes.");
			(new WebDriverWait(driver, nbSec)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver driver) {
		        	return false;
		        }
		    });
	    } catch (TimeoutException e) {
	    	logger("Le temps d'attente est écoulé.");
	    }
	}
	
	/**
	 * Attend la disparition d'un élément pendant un nombre donné de seconde.
	 * @param cible la cible désignant l'élément dont on attend la disparition.
	 * @param nbreSecondes le nombre de seconde maximum d'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreDisparitionElement(final CibleBean cible, Integer nbreSecondes) throws SeleniumException {
	    try {
	    	logger("On attend la disparition de l'élément " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	final By critere = cible.creerBy();
			(new WebDriverWait(driver, nbreSecondes)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver driver) {
	        		// Impossible d'atteindre l'élément. Donc l'élément n'est pas présent sur la page.
	        		// Il est aussi possible qu'un changement de page soit survenue (ex : loadingbar)
	        		// Une exception est donc considérée comme une réussite du test.
		        	try {
		        		WebElement temp = obtenirElement(cible.getFrame(), critere);
		        		return  (temp == null || !temp.isDisplayed());
		        	} catch (SeleniumException e) {
		        		return true;
		        	} catch (StaleElementReferenceException ex) {
		        		// La référence vers l'objet est obselète. L'objet n'est donc normalement plus présent
		        		return true;
		    	    }
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "L'Element est toujours présent : " + cible.lister());
	    } 
	}
	
	/**
	 * Fait attendre le drive le temps que la cible disparaisse.
	 * @param cible la cible dont on attend la disparition.
	 * @throws SeleniumException en cas d'erreur (ou si la cible ne disparais pas).
	 */
	public void attendreDisparitionElement(final CibleBean cible) throws SeleniumException {
		attendreDisparitionElement(cible, attenteMax);
	}
	
	/**
	 * Permet d'obtenir le driver associé à une frame dont l'id est connu.
	 * @param idFrame l'id de la frame à selectionner.
	 * @return le driver associé à la frame souhaitée.
	 * @throws SeleniumException si la frame n'existe pas.
	 */
	private GenericDriver changerDeFrame(final String idFrame) throws SeleniumException {
		try {
			if (typeImpl == GenericDriver.FIREFOX_IMPL) {
				return (FirefoxImpl) driver.switchTo().defaultContent().switchTo().frame(idFrame);
			} else if (typeImpl == GenericDriver.IE_IMPL) {
				return (IEImpl) driver.switchTo().defaultContent().switchTo().frame(idFrame);
			} else if (typeImpl == GenericDriver.CHROME_IMPL) {
				return (ChromeImpl) driver.switchTo().defaultContent().switchTo().frame(idFrame);
			} else {
				//TODO à réaliser.
				return null;
			}
		} catch (NoSuchFrameException ex) {
			throw new SeleniumException(Erreurs.E009, "Frame introuvable : " + idFrame);
		}
	}
	
	/**
	 * Permet de changer la fenêtre active lors du test.
	 * On active automatiquement la dernière fenêtre ouverte.
	 * @return un driver pointant sur la dernière fenêtre ouverte.
	 */
	public GenericDriver changerDeFenetre() {	
		
		if (this.testerPresenceAlerteJavascript()) {
    		System.out.println("On eneleve une alerte");
			this.accepterAlerteJavascript();
		}
		
		Iterator<String> i = driver.getWindowHandles().iterator();
		Integer compteur = 0;
		String window = "";
		while(i.hasNext()){
			compteur++;
			window = i.next();
			//System.out.println("Fenetre " + compteur + " : " + window);
		}
		driver.switchTo().window(window);
		logger("On passe sur la derniere fenêtre (" + driver.getTitle() + ")");
		
		if (testerPresenceAlerteJavascript()) {
    		//System.out.println("On eneleve une alerte");
    		accepterAlerteJavascript();
		}
		return driver;
	}
	
	/**
	 * Tente de reperer les popup javascript ouverte et de les valider.
	 */
	public void supprimerPopups() {	
		SeleniumOutils temp;
		if (this.testerPresenceAlerteJavascript()) {
			this.accepterAlerteJavascript();
		}
		Iterator<String> i = driver.getWindowHandles().iterator();
		GenericDriver retour = driver;
		Integer compteur = 0;
		String window = "";
		while(i.hasNext()){
			compteur++;
			window = i.next();
			retour = (GenericDriver) driver.switchTo().window(window);
			// Au passage on supprime les eventuelles alertes sur la page destination.
			temp = new SeleniumOutils(retour);
	    	if (retour != null && temp.testerPresenceAlerteJavascript()) {
	    		temp.accepterAlerteJavascript();
	    	}
		}
	}
	
	/**
	 * Permet de changer la fenêtre active lors du test.
	 * On active automatiquement la dernière fenêtre dont le titre correspond au paramètre.
	 * @param titre le titre de la fenêtre souhaitée.
	 * @return un driver pointant sur la fenêtre souhaitée. Si c'est impossible on reste sur la même fenêtre.
	 */
	public GenericDriver changerDeFenetre(String titre) {	
		String titrePageCourante;
		SeleniumOutils tempOutil;
		boolean temp = false;
		boolean lowerCase = true;
		
		// Au passage on supprime les eventuelles alertes sur la page origine.
    	if (testerPresenceAlerteJavascript()) {
    		accepterAlerteJavascript();
    	}
    	
    	try {
    		titrePageCourante = driver.getTitle();
    	} catch (WebDriverException ex) {
    		// Si on arrive ici, alors la page n'as plus de titre, et n'existe probablement plus.
    		titrePageCourante = null;
    	}
    	
    	// Si la page à un titre, on vérifie que ce ne soit pas le titre recherché.
    	if (titrePageCourante != null) {
	    	temp = lowerCase?supprimerCaracteresSpeciaux(driver.getTitle().toLowerCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toLowerCase(Locale.ENGLISH))):supprimerCaracteresSpeciaux(driver.getTitle().toUpperCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toUpperCase(Locale.ENGLISH)));
			if (temp) {
				logger("On etais deja sur la bonne fenetre (" + driver.getTitle() + ")");
				return driver;
			}
    	}

		Iterator<String> i = driver.getWindowHandles().iterator();
		GenericDriver retour = driver;
		Integer compteur = 0;
		String window = "";

		while(i.hasNext()){
			compteur++;
			window = i.next();
			retour = (GenericDriver) driver.switchTo().window(window);
			// Au passage on supprime les eventuelles alertes sur la page destination.
			tempOutil = new SeleniumOutils(retour);
	    	if (retour != null && tempOutil.testerPresenceAlerteJavascript()) {
	    		tempOutil.accepterAlerteJavascript();
	    	}
			temp = lowerCase?supprimerCaracteresSpeciaux(retour.getTitle().toLowerCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toLowerCase(Locale.ENGLISH))):supprimerCaracteresSpeciaux(retour.getTitle().toUpperCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toUpperCase(Locale.ENGLISH)));
			if (temp) {
				logger("On passe sur la derniere fenêtre (" + retour.getTitle() + ")");
				return retour;
			}
		}
		
		logger("Impossible de changer de page on reste sur la page courante (" + retour.getTitle() + ")");
		return retour;
	}
	
	/**
	 * Permet de fermer la fenêtre courante.
	 * On passe sur la prochaine fenêtre.
	 * @return le drive pointant sur la prochaine fenêtre.
	 */
	public GenericDriver fermerFenetreCourante() {
		logger("On ferme la fenetre courante " + driver.getTitle());
		driver.close();
		return changerDeFenetre();
	}
	
	/**
	 * Permet de cliquer sur un element.
	 * Il n'est pas possible d'interragir avec un élément invisible, aussi on clique toujours sur un élément visible.
	 * @param cible la cible du clic.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void cliquer(CibleBean cible) throws SeleniumException {
		logger("On clique sur " + cible.creerBy().toString() + " (idFrame : " + cible.getFrame() + ")");
		WebElement temp = obtenirElementVisible(cible);
		
		try {		
			if (temp != null) {
				temp.click();
			} else {
				// L'objet n'est pas trouvé dans la page.
				throw new SeleniumException(Erreurs.E017, "La cible " + cible.toString() + " du clic n'est pas visible (ou absente).");
			}
		} catch (StaleElementReferenceException ex) {
			// L'objet qu'on cherche à cliquer n'est plus présent : la référence est obselète
			try {
				// On tente à nouveau le clique avec une référence directe, si c'est à nouveau un échec on conclue sur une impossibilité.
				obtenirElementVisible(cible).click();
			} catch (StaleElementReferenceException ex2) {
				throw new SeleniumException(Erreurs.E017, "La cible " + cible.toString() + " du clic n'est plus disponible (rechargement?).");
			} catch (NullPointerException ex2) {
				throw new SeleniumException(Erreurs.E017, "La cible " + cible.toString() + " du clic n'est pas visible (ou absente).");
			} catch (SeleniumException ex2) {
				throw new SeleniumException(ex2.getInformations(), "La cible " + cible.toString() + " n'as pas pu être atteinte même après rechargement de la référence obselète.");
			}
		}
	}
	
	/**
	 * Permet un clic optionnel sur un objet potentiellement absent mais non obligatoire.
	 * @param cible la cible du clic.
	 */
	public void cliquerSiPossible(CibleBean cible) {
		try {	
			cliquer(cible);
		} catch (SeleniumException ex) {
			logger("Impossible de cliquer sur la cible " + cible.creerBy().toString() + " (idFrame : " + cible.getFrame() + ")");
		}
	}
	
	/**
	 * Permet de cliquer sur une liste d'elements.
	 * @param cible le critère de recherche des éléments.
	 * @param clicMax indique le nombre maximal d'élément à cliquer.
	 * @return le nombre d'éléments trouvés.
	 * @throws SeleniumException en cas d'erreur.
	 */
	private int cliquerMultiple(CibleBean cible, Integer clicMax) throws SeleniumException {
		int retour = 0;
		if (clicMax == null) {
			clicMax = 999;
		}
		for (WebElement element : obtenirElements(cible)) {
			// Pour cliquer sur l'élément il faut qu'il soit visible.
			if (element.isDisplayed() && element.isEnabled() && clicMax > 0) {
				//System.out.println("Intéraction avec l'élement " + element.getText());
				clicMax--;
				retour++;
				element.click();
			}
		}
		logger("Clics sur les éléments (visibles) " + cible.creerBy().toString() + " (frame : " + cible.getFrame() + ")");

		return retour;
	}
	
	/**
	 * Permet la saisie dans un element d'une frame.
	 * @param cible la cible de la saisie
	 * @param texte le texte à saisir.
	 * @param vider indiquer si on doit vider avant de saisir.
	 * @throws SeleniumException en cas d'erreur.
	 */
	private void saisir(String texte, CibleBean cible, boolean vider) throws SeleniumException {
		By by = cible.creerBy();
		logger("Saisie de " + texte + " dans le champ " + by.toString() + (cible.getFrame() != null?("pour la frame "+cible.getFrame()):"") +  (vider?(" (avec vidage)"):""));
		try {
			WebElement element = obtenirElement(cible.getFrame(), by);
			if (vider) {
				element.clear();
			}
			element.sendKeys(texte);
		} catch (InvalidElementStateException ex) {
			// Si on est ici , alors le champ n'est pas saisissable.
			throw new SeleniumException(Erreurs.E022, "L'element " + by.toString() + "n'est pas saisissable.");
		} catch (StaleElementReferenceException ex2) {
			// Si on est ici , alors le champ est pas visible, sans doute rechargé depuis le test.
			throw new SeleniumException(Erreurs.E023, "L'element " + by.toString() + " n'est plus sur la page.");
		}
			
	}
	
	/**
	 * Obtenir un élément via la frame concernée et le by.
	 * @param idFrame la frame si il y a lieu (null sinon)
	 * @param by le critère de recherche paramètré.
	 * @return l'élément.
	 * @throws SeleniumException si cela n'existe pas.
	 */
	private WebElement obtenirElement(String idFrame, By by) throws SeleniumException {
		try {
			if (idFrame != null) {
				return changerDeFrame(idFrame).findElement(by);
			} else {
				return driver.findElement(by);
			}
		} catch (NoSuchElementException ex) {
			throw new SeleniumException(Erreurs.E009, "L'element " + by.toString() + " n'existe pas");
		} catch (ElementNotVisibleException ex) {
			throw new SeleniumException(Erreurs.E016, "L'element " + by.toString() + " n'est pas visible");
		} catch (UnhandledAlertException ex) {
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + " Lors de la recherche de l'élément : " + by.toString() );
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "L'element " + by.toString() + " n'est plus sur la page");
		}
	}
	
	/**
	 * Obtenir des éléments via la frame concernée et le by.
	 * @param idFrame la frame si il y a lieu (null sinon)
	 * @param by le critère de recherche paramètré.
	 * @return la liste des éléments.
	 * @throws SeleniumException si cela n'existe pas.
	 */
	private List<WebElement> obtenirElements(String idFrame, By by) throws SeleniumException {
		try {
			if (idFrame != null) {
				System.out.println("On cherche les éléments dans la frame " + idFrame + " qui sont " + by);
				return changerDeFrame(idFrame).findElements(by);
			} else {
				return driver.findElements(by);
			}
		} catch (NoSuchElementException ex) {
			throw new SeleniumException(Erreurs.E009, "Aucun element " + by.toString() + " présent");
		} catch (ElementNotVisibleException ex) {
			throw new SeleniumException(Erreurs.E016, "Aucun element " + by.toString() + " visible");
		} catch (UnhandledAlertException ex) {
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + "  lors de la recherche de : " + by.toString());
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "Les elements " + by.toString() + " ne sont plus sur la page");
		}
	}
	
	/**
	 * Permet d'obtenir un élément à partir d'une cible.
	 * @param cible la cible désignant l'objet recherché.
	 * @return l'élément demandé.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElement(CibleBean cible) throws SeleniumException {
		return obtenirElement(cible.getFrame(), cible.creerBy());
	}
	
	/**
	 * Permet d'obtenir un élément à partir d'un autre élément et d'un chemin xpath.
	 * @param base l'élément de départ.
	 * @param xpath le chemin à appliquer depuis l'élément de départ pour obtenir la cible.
	 * @return l'élément demandé.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElement(CibleBean cible, String xpath) throws SeleniumException {
		return obtenirElement(obtenirElement(cible), xpath);
	}
	
	/**
	 * Permet d'obtenir un élément à partir d'un autre élément et d'un chemin xpath.
	 * @param base l'élément de départ.
	 * @param xpath le chemin à appliquer depuis l'élément de départ pour obtenir la cible.
	 * @return l'élément demandé.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElement(WebElement base, String xpath) throws SeleniumException {
		By by = By.xpath(xpath);
		try {
			return base.findElement(by);
		} catch (NoSuchElementException ex) {
			throw new SeleniumException(Erreurs.E009, "L'element " + by.toString() + " n'existe pas");
		} catch (ElementNotVisibleException ex) {
			throw new SeleniumException(Erreurs.E016, "L'element " + by.toString() + " n'est pas visible");
		} catch (UnhandledAlertException ex) {
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + " Lors de la recherche de l'élément : " + by.toString() );
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "L'element " + by.toString() + " n'est plus sur la page");
		}
	}
	
	/**
	 * Permet d'obtenir un élément visible à partir d'une cible.
	 * @param cible la cible désignant l'objet visible recherché.
	 * @return l'élément demandé (si il est visible).
	 * @throws SeleniumException en cas d'erreur ou si l'élément n'est pas visible.
	 */
	public WebElement obtenirElementVisible(CibleBean cible) throws SeleniumException {
		return obtenirElementVisible(cible, true);
	}
	
	/**
	 * Permet d'obtenir le premier element visible répondant aux critères.
	 * @param cible la cible des recherches.
	 * @param obligatoire true si le test échoue en cas de nous correspondance. False sinon.
	 * @return null si rien n'existe et que l'objet dévais être trouver. L'objet sinon.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElementVisible(CibleBean cible, boolean obligatoire) throws SeleniumException {
		
		// On essai d'obtenir la liste des éléments pouvant correspondre à la demande.
		List<WebElement> liste = new ArrayList<WebElement>();
		try {
			liste = obtenirElements(cible.getFrame(), cible.creerBy());		
		} catch (SeleniumException ex) {
			// Si la liste est vide, on génère une erreur ou un retour à vide.
			if (ex.getInformations() == Erreurs.E009 && !obligatoire) {
				logger("Pas d'objet répondants aux critères " + cible.toString());
				return null;
			} else {
				throw ex;
			}
		}
		
		// On parcours la liste jusqu'à obtenir un élément correspond à la demande.
		try {		
			for (WebElement element : liste) {
				try {
					if (element.isDisplayed() && element.isEnabled()) {
						return element;
					} 
				} catch (StaleElementReferenceException ex) {
					// Le fait qu'une référence à un des objets trouvé soit obselète ne signifie pas qu'on ne peux pas intéragir avec.
					// On rafraichie les référence et on tente à nouveau.
					liste = obtenirElements(cible.getFrame(), cible.creerBy());	
					for (WebElement elementBis : liste) {
						try {
							if (elementBis.isDisplayed() && elementBis.isEnabled()) {
								return elementBis;
							} 
						} catch (StaleElementReferenceException exBis) {
							// Si la nouvelle tentative redonne une erreur de référence on la signale.
							throw new SeleniumException(Erreurs.E023, "Impossible d'atteindre l'élement lors de la recherche de : " + cible.lister() + " la référence vers l'objet est obselète.");
						}
					}
					// Si la nouvelle tentative ne donne rien et ne génère pas d'exception, alors aucun élément ne répond au besoin, on renverra null.
					break;
				}
			}
		} catch (UnhandledAlertException ex) {
			// Si on recontre une popup bloquant l'IHM, on renvoie une erreur bloquante.
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + " lors de la recherche de : " + cible.lister() + "");
		} 
		
		// Si on est parvenu jusqu'ici, c'est qu'aucun élément trouvé ne correspond à la demande. Si la recherche étais obligatoire on génère une erreur
		// Sinon on renvoie null.
		if(!obligatoire) {
			logger("Pas d'objet répondants aux critères " + cible.toString());
		} else {
			throw new SeleniumException(Erreurs.E009, "Pas d'élément répondant aux criètres de recherche de : " + cible.lister());
		}
 		return null;
	}
	
	/**
	 * Permet d'obtenir une liste d'élément à partir d'un critère de ciblage.
	 * @param cible le critère de ciblage pour les éléments.
	 * @return la liste des éléments obtenu suite à la requête de ciblage.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public List<WebElement> obtenirElements(CibleBean cible) throws SeleniumException {
		return obtenirElements(cible.getFrame(), cible.creerBy());
	}
	
	/**
	 * Permet un clic sur un élément désigné par son ID.
	 * @param valeur l'ID de l'élément avec lequel on intérragit.
	 * @throws SeleniumException en cas d'erreur.
	 * @deprecated Dans l'idéal, toujour se référer à une cible, eviter les références directes à un ID.
	 */
	@Deprecated
	public void cliquer(String valeur) throws SeleniumException {
		cliquer(new CibleBean(null, Clefs.ID, new String[] {valeur}));
	}
	
	/**
	 * Permet un clic sur un élément désigné après attente d'affichage.
	 * @param cible la cible du clic.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreEtCliquer(CibleBean cible) throws SeleniumException {
		//attendreElement(cible);
		attendreChargementElement(cible, true, true);
		cliquer(cible);
	}
	
	/**
	 * Cette fonction attend l'affichage de la cible puis clique dessus.
	 * Ensuite elle ne "rend la main" qu'une fois que la cible de l'attente est présente à l'écran.
	 * @param cible la cible du clic.
	 * @param cibleAttente la cible de l'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void cliquerEtAttendre(CibleBean cible, CibleBean cibleAttente) throws SeleniumException {
		attendreEtCliquer(cible);
		attendreChargementElement(cibleAttente, true, true);
	}
	
	/**
	 * Cette fonction attend l'affichage de la cible puis clique dessus.
	 * Ensuite elle ne "rend la main" qu'une fois que la cible de l'attente est présente à l'écran.
	 * En cas d'echec de l'attente, on tente à nouveau le clic aussi souvent que nécessaire. Le nombre d'essai maximal est fixé par l'attribut "attenteMax".
	 * @param cible la cible du clic.
	 * @param cibleAttente la cible de l'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void cliquerJusqua(CibleBean cible, CibleBean cibleAttente) throws SeleniumException {
		attendreElement(cible);
		boolean boucle = true;
		int nbEssai = attenteMax;
		while(boucle) {
			nbEssai--;
			if (nbEssai > 0) {
				try {
					cliquer(cible);
					attendreElement(cibleAttente);
					// Cette ligne ne sera atteinte que si le test est concluant.
					boucle = false;
					System.out.println("La cible de l'attente est bien présente");
				} catch (Exception ex) {
					boucle = true;
				}
			} else {
				// On a pas réussi dans le délai apparti de faire apparaitre la cible d'attente
				throw new SeleniumException(Erreurs.E009, "La cible de l'attente n'est jamais apparue : " + cibleAttente.lister());
			}
		}
	}
	
	/**
	 * Permet de cliquer sur autant d'éléments que possible répondant aux critères de ciblage.
	 * @param cible la cible des clics.
	 * @return le nombre de clics effectués.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public int cliquerMultiple(CibleBean cible) throws SeleniumException {
		return cliquerMultiple(cible, null);	
	}
	
	/**
	 * Fonction appliquant une condition à une cible. On s'attend à un WebElement en retour de la condition
	 * Cette fonction exploite les capacité des ExpectedConditions.
	 * @param cible la cible désignant l'élément dont on attend l'affichage
	 * @param condition la condition à respecter, celle-ci doit renvoyée un WebElement, son "by" est renseigner avec les informations de la cibles.
	 * @throws SeleniumException en cas d'erreur ou de non disponibilité de l'élément.
	 */
	public void appliquerCondition(final CibleBean cible, final ExpectedCondition<WebElement> condition) throws SeleniumException {
	    try {
	    	logger("On applique la condition " +  condition + " sur la cible " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	
	    	// On cherche à obtenir l'élément. Pour être légitime il doit être clickable.
	    	new WebDriverWait(driver, attenteMax).until(
	    			new Function<WebDriver, Boolean>() {
	    	            public Boolean apply(WebDriver driver) {
	    	            	WebElement element = (WebElement) condition.apply(driver);
	    	            	return element != null;
	    	            }
	    	        });	
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "La condition " + condition + " n'as jamais été respectée ( cible : " + cible.lister() + ")");
	    } catch (StaleElementReferenceException ex) {
    		// La référence à l'élément obselète mais celui ci existe bel et bien, on réinitialise l'attente.
	    	appliquerCondition(cible, condition);
    	}
	}

	/**
	 * Saisie à la suite du texte présent dans une cible d'une nouvelle chaine.
	 * @param texte le texte à saisir
	 * @param cible la cible de la saisie
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisir(String texte, CibleBean cible) throws SeleniumException {
		saisir(texte, cible, false);	
	}
	
	/**
	 * Permet de saisir un texte dans un élément visible.
	 * @param texte le texte à saisie.
	 * @param cible l'élement visible cible de la saisie.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisirVisible(String texte, CibleBean cible) throws SeleniumException {
		obtenirElementVisible(cible).sendKeys(texte);
		logger("Saisie de " + texte + " dans le champ visible (" + cible.lister() + ") " + (cible.getFrame() != null?("pour la frame "+ cible.getFrame()):""));
	}

	/**
	 * Saisie à la place du texte présent dans une cible d'une nouvelle chaine.
	 * @param texte le texte à saisir
	 * @param cible la cible de la saisie
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void viderEtSaisir(String texte, CibleBean cible) throws SeleniumException {
		saisir(texte, cible, true);
	}
	
	/**
	 * Saisie à la suite du texte présent dans une cible d'une nouvelle chaine.
	 * @param texte le texte à saisir
	 * @param valeur l'id de la cible
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisir(String valeur, String texte) throws SeleniumException {
		saisir(texte, new CibleBean(null, Clefs.ID, valeur), false);
	}
	
	/**
	 * Selectionne dans la cible désignée le texte choisie.
	 * @param libelle le texte ou la valeur à selectionner
	 * @param cible la cible indiquant le sélecteur.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void selectionner(String libelle, CibleBean cible) throws SeleniumException {
		selectionner(libelle, cible, true);
	}
	
	/**
	 * Permet la selection d'une valeur dans une balise "select". On tente une selection directe vis à vis de la valeur ou du texte.
	 * Si cela ne fonctionne pas, une selection via le "innerhtml" aura lieu si le top vérification est à true.
	 * @param libelle le libelle ou la valeur à selectionner.
	 * @param cible la cible désignant le selecteur
	 * @param verification true si on souhaites vérifier que la selection est effective, false sinon.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void selectionner(String libelle, CibleBean cible, boolean verification) throws SeleniumException {
		Boolean success = false;
		try {
			// On obtiens le select (à condition qu'il soit visible)
			WebElement tempWebElement = obtenirElementVisible(cible);
			WebElement tempOption = null;
			// On créer un selecteur à partir du WebElement.
			Select temp = new Select(tempWebElement);
			// On séléctionne le texte ou la valeur.
			try {
				// On essai d'abord de selectionné par libelle d'option.
				temp.selectByVisibleText(libelle);
				// Si la selection est effective, il devient alors possible de cliquer dessus
				if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
					tempOption = obtenirElement(new CibleBean(null, cible, "*", "text=" + libelle));
				} else {
					tempOption = obtenirElement(new CibleBean(Clefs.TEXTE_PARTIEL, libelle));
				}
				
			}	catch (NoSuchElementException ex) {
				try {
					// Si le texte n'est pas visible, on essai de sélectionner par valeur.
					temp.selectByValue(libelle);
				} catch (NoSuchElementException ex2) {
					throw new SeleniumException(Erreurs.E009, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
				}
				// Si la selection est effective, il devient alors possible de cliquer dessus
				if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
					tempOption = obtenirElement(new CibleBean(null, cible, "*", "value=" + libelle));
				} else {
					tempOption = obtenirElement(new CibleBean(Clefs.VALEUR, libelle));
				}
			}
			// On doit ensuite envoyer une validation à la selection, sans quoi la selection n'est pas finalisée.
			if (tempOption != null) {
				try {
					tempOption.sendKeys(Keys.RETURN);
				} catch (ElementNotVisibleException ex) {
					// Si nous arrivons ici, c'est que l'élément n'est pas visible. La validation par la touche entrée n'est peu être pas nécessaire
					return;
				}
				
				// Si aucune vérification n'est demandée et que la selection est déjà effective, on s'arrête là.
				if (!verification) {
					return;
				}
			}
			
			// On vérifie que a saisie à bien été prise en compte (A vérifier).
			String tempText;
			String tempValue;
			// Une fois que l'on à interragie avec, on modifie la struture du select, il faut le re-obtenir.
			temp = new Select(obtenirElementVisible(cible));
			// On parcours les options selectionnées et on cherche la valeur choisie.
			for (WebElement element : temp.getAllSelectedOptions()) {
				if (element != null) {
					tempText = element.getText();
					tempValue = element.getAttribute("value");
					// On teste si le libelle ou la valeur on bien été sélectionnée.
					if (tempText != null || tempValue != null) {
						if (tempText.contains(libelle) || tempText.equals(libelle)) {
							success = true;
						} else if (tempValue.contains(libelle) || tempValue.equals(libelle)) {
							success = true;
						}
					}
					// Si ni la valeur ni le libelle ne sont bons, on essaie avec le innerHTML
					if (!success) {
						tempText = element.getAttribute("innerHTML");
						if (tempText != null) {
							if (tempText.contains(libelle) || tempText.equals(libelle)) {
								success = true;
							} 
						}
					}
					// On clique sur la valeur selectionnée.
					element.click();
					// Si on à prouver que la valeur est sélectionnée alors on quitte la fonction.
					if (success) {
						break;
					}
				}
			}		
			logger("Selection de la valeur " +  libelle + " pour le select : " +  cible.lister());
		} catch (NoSuchElementException ex) {	
			ex.printStackTrace();
			throw new SeleniumException(Erreurs.E009, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
		} catch (SeleniumException ex) {
			ex.printStackTrace();
			throw new SeleniumException(ex, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
		} catch (StaleElementReferenceException ex) {
			// Il s'agit d'un problème de référence, il faut renouveller la sélection.
			try {
				// On obtiens le select (à condition qu'il soit visible)
				WebElement tempWebElement = obtenirElementVisible(cible);
				WebElement tempOption = null;
				// On créer un selecteur à partir du WebElement.
				Select temp = new Select(tempWebElement);
				// On séléctionne le texte ou la valeur.
				try {
					// On essai d'abord de selectionné par libelle d'option.
					temp.selectByVisibleText(libelle);
					// Si la selection est effective, il devient alors possible de cliquer dessus
					if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
						tempOption = obtenirElement(new CibleBean(null, cible, "*", "text=" + libelle));
					} else {
						tempOption = obtenirElement(new CibleBean(Clefs.TEXTE_PARTIEL, libelle));
					}
					
				}	catch (NoSuchElementException ex2) {
					// Si le texte n'est pas visible, on essai de sélectionner par valeur.
					temp.selectByValue(libelle);
					// Si la selection est effective, il devient alors possible de cliquer dessus
					if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
						tempOption = obtenirElement(new CibleBean(null, cible, "*", "value=" + libelle));
					} else {
						tempOption = obtenirElement(new CibleBean(Clefs.VALEUR, libelle));
					}
				}
				// On doit ensuite envoyer une validation à la selection, sans quoi la selection n'est pas finalisée.
				if (tempOption != null) {
					tempOption.sendKeys(Keys.RETURN);
				}		
				// On effectue pas la vérification sur une deuxième tentative.
				success = true;
			} catch (Exception ex3) {
				// Quelle que soit l'erreur qui remonte, c'est la perte de référence qui l'emporte dans cette circonstance
				ex.printStackTrace();
				throw new SeleniumException(Erreurs.E023, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
			}
		}
		
		if (!success) {
			// Une erreur si la sélection n'est pas effective.
			throw new SeleniumException(Erreurs.E009, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
		}
	}
	
	/**
	 * Permet d'obtenir la valeur associée à une cible visible.
	 * @param cible la cible.
	 * @return la valeur associée à la cible (si la value est vide alors on renvoie le texte).
	 * @throws SeleniumException en cas d'erreur.
	 */
	public String obtenirValeur(CibleBean cible) throws SeleniumException {
		return obtenirValeur(obtenirElementVisible(cible));
	}
	
	/**
	 * Permet d'obtenir la valeur associée à une cible visible ou pas.
	 * @param cible la cible.
	 * @param visibilite true si la cible est attendues visible.
	 * @return la valeur associée à la cible (si la value est vide alors on renvoie le texte).
	 * @throws SeleniumException en cas d'erreur.
	 */
	public String obtenirValeur(CibleBean cible, boolean visibilite) throws SeleniumException {
		if (visibilite) {
			return obtenirValeur(cible);
		} else {
			return obtenirValeur(obtenirElement(cible));
		}
	}
	
	/**
	 * Permet d'obtenir la valeur d'un element.
	 * Si celui ci est un select, on récupère la valeur de la première option selectionnée.
	 * @param element l'element.
	 * @return la valeur (value) d'un élément ou son contenu texte.
	 */
	public String obtenirValeur(WebElement element) {
		String retour = null;
		if (element != null) {
			// Le cas ou c'est un select
    		if ("select".equals(element.getTagName().toLowerCase())) {
    			WebElement option = new Select(element).getFirstSelectedOption();
    			retour = option.getAttribute("value");
    			if (retour == null || "".equals(retour.trim())) {
    				retour = option.getText();
    			}
    		} else {		
				// Le cas où ce n'est pas un select
				retour = element.getAttribute("value");
				if (retour == null || "".equals(retour.trim())) {
					retour = element.getText();
				}
    		}
		}
		return retour;
	}
	
	/**
	 * Si les logs sont activés alors on ajoute la chaine de caractère aux logs.
	 * @param log la portion de log à ajoutée.
	 */
	public void logger(String log) {
		if (isActivationLog()) {
			getLog().append("\n"+log);
		}
	}

	/**
	 * Ajoute un objectif obtenu à la liste des objectifs et réalise la capture d'écran associée.
	 * @param objectif l'objectif à ajouter.
	 */
	public void ajouterObjectifObtenu(ObjectifBean objectif) {
		getObjectifsDriver().put(objectif.getClefUnique(), objectif);
		captureEcran(objectif.getClefUnique());
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
			captureEcran(element, objectif.getClefUnique());
		}
	}
	
	/////////////////////// GETTERS & SETTERS //////////////////////////////////


	public StringBuffer getLog() {
		return driver.getLog();
	}

	public void setLog(StringBuffer log) {
		driver.setLog(log);
	}

	/**
	 * Permet de savoir si les logs sont activés.
	 * @return true si activé, false sinon.
	 */
	public boolean isActivationLog() {
		return driver.isActivationLog();
	}

	/**
	 * Permet l'activation ou la désactivation des log pour les outils.
	 * @param activationLog true pour activation false pour désactivation.
	 */
	public void setActivationLog(boolean activationLog) {
		driver.setActivationLog(activationLog);
	}

	/**
	 * Permet d'obtenir les objectifs propres au driver.
	 * @return les objectifs du driver sous forme d'une Hashmap.
	 */
	public HashMap<String, ObjectifBean> getObjectifsDriver() {
		return driver.getObjectifsDriver();
	}

	/**
	 * Permet de fixer les objectifs propres à l'instance du driver.
	 * @param objectifsDriver les objectifs sous forme de Hashmap.
	 */
	public void setObjectifsDriver(HashMap<String, ObjectifBean> objectifsDriver) {
		driver.setObjectifsDriver(objectifsDriver);
	}
	
	/**
	 * Cette fonction supprime les caractères spéciaux des chaines de caractères pour faciliter les comparaisons.
	 * @param s la chaine de caractères à normaliser.
	 * @return le resultat de la normalisation.
	 */
	public static String supprimerCaracteresSpeciaux(String s) {
		s = s.toUpperCase();
		String temp = Normalizer.normalize(s, Normalizer.Form.NFKD);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}
	
	/**
	 * Permet d'attendre la disparition d'un élément donné.
	 * @param cible l'élément dont on attend la disparition.
	 * @throws SeleniumException en cas d'erreur ou de non disparition de l'élément.
	 */
	public void attendreNonPresenceElement(final CibleBean cible) throws SeleniumException {
	    Long tempsAttendu = new Date().getTime();
		try {
			(new WebDriverWait(driver, attenteMax)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	boolean retour = false;
		        	try {
		        		retour = (obtenirElementVisible(cible) == null);
		        	} catch (SeleniumException ex) {
		        		// Si on constate que l'objet n'est pas présent sur la page alors on renvoie vrai.
		        		// De même si on constate que la référence de l'objet n'est plus valide. On suppose alors que celui ci étais présent mais à disparu.
		        		if (ex.getInformations() == Erreurs.E009 || ex.getInformations() == Erreurs.E023) {
		        			retour = true;
		        		} else {
		        			logger(ex.toString());
		        		}
		        	}
		            return (retour);
		        }
		    });
	    } catch (TimeoutException e) {
	    	System.out.println("Temps attendu avant timeout : " + (tempsAttendu - new Date().getTime()));
	    	throw new SeleniumException(Erreurs.E013, cible.lister());
	    	//Assert.fail("Le texte " + texte + " est présent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est présente à l'écran, le texte n'est probablement plus présente à l'écran.
	    	logger("Lors de l'attente de la disparition de l'element (" + cible.lister() + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue à eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la disparition de l'element (" + cible.lister() + "), une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}

	/**
	 * Permet de faire patienter le driver pendant une durée déterminée.
	 * @param secondes le nombre de secondes à attendre
	 */
	public void patienter(long secondes) {
		DriverOutils outil = new DriverOutils(driver);
		try {
			outil.wait(secondes);
		} catch (InterruptedException e) {
			logger("L'attente à été interrompue");
		} catch (IllegalMonitorStateException e) {
			logger("On fait patienter une fenetre qui n'existe plus");
		}
	}

	/**
	 * Permet de vérifier une règle en fonction des critère définie dans celle ci .
	 * @param regle la règle dont on souhaites faire la vérification.
	 * @return true si la règle est vérifiée, false sinon.
	 */
	public boolean verifierRGM(RGMBean regle) {
		
		boolean retour = false;
		Object cible = regle.getCible();
		
		// Le règle de gestion teste la présence d'un élément ou d'un texte.
		if (regle.getTypeComparaison() == RGMBean.PRESENCE) {
			if (cible.getClass() == String.class) {
				retour = testerPresenceTexte((String) cible, true);
			}
			if (cible.getClass() == WebElement.class) {
				// Un webelement est forcement présent si il existe.
				retour = ((WebElement) cible).isDisplayed();
			}
			if (cible.getClass() == CibleBean.class) {
				retour = testerPresenceElement((CibleBean) cible); 
			}
		}
		
		if (regle.getTypeComparaison() == RGMBean.ABSENCE) {
			if (cible.getClass() == String.class) {
				retour = testerPresenceTexte((String) cible, false);
			}
			if (cible.getClass() == WebElement.class) {
				// Un webelement est forcement présent si il existe.
				retour = !((WebElement) cible).isDisplayed();
			}
			if (cible.getClass() == CibleBean.class) {
				retour = !testerPresenceElement((CibleBean) cible); 
			}
		}
		
		if (regle.getTypeComparaison() == RGMBean.INFERIEUR) {
			
		}
		
		if (regle.getTypeComparaison() == RGMBean.SUPERIEUR) {
			
		}
		
		if (regle.getTypeComparaison() == RGMBean.EGALITE) {
			
		}
		
		// TODO Auto-generated method stub
		return retour;
	}

	/**
	 * Permet de multiplier les clics sur une cible donnée.
	 * @param cible la cible des clics
	 * @param nombreDeClic le nombre de clics à effectuer
	 * @throws SeleniumException en cas d'erreur lors des clics.
	 */
	public void cliquer(CibleBean cible, Integer nombreDeClic) throws SeleniumException {
		if (nombreDeClic != null && nombreDeClic > 0) {
			for (int i = 0; i < nombreDeClic; i++) {
				cliquer(cible);
			}
		}
	}

	/**
	 * Permet de changer l'url courante utilisée par le driver.
	 * @param url la nouvelle url à visiter.
	 */
	public void chargerUrl(String url) {
		System.out.println("On charge : " + url);
		driver.get(url);
	}
	
	/**
	 * Permet de tester la présence d'une cible dans la page courante.
	 * Cette vérification se fait immédiatement, on n'attend pas d'éventuelles fins de chargement.
	 * @param cible la cible dont on teste la présence.
	 * @return true si l'élément est présent et visible, false sinon (ou en cas d'erreur).
	 */
	public boolean testerPresenceElement(CibleBean cible) {
		try {
			return obtenirElementVisible(cible) != null;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Permet de tester la présence d'une cible dans la page courante.
	 * Si l'élément n'est pas présent immédiatement, on attend un éventuel chargement.
	 * @param cible la cible dont on teste la présence.
	 * @return true si l'élément exite (visible ou non), false sinon (ou en cas d'erreur).
	 */
	public boolean testerPresenceElementDiffere(CibleBean cible) {
		return testerElementDiffere(cible, false);
	}
	
	/**
	 * Permet de tester la présence et la visibilité d'une cible dans la page courante.
	 * Si l'élément n'est pas présent (ou invisible) immédiatement, on attend un éventuel chargement.
	 * @param cible la cible dont on teste la présence.
	 * @return true si l'élément est présent et visible, false sinon (ou en cas d'erreur).
	 */
	public boolean testerVisibiliteElementDiffere(CibleBean cible) {
		return testerElementDiffere(cible, true);
	}
	
	/**
	 * Permet de tester la présence et/ou la visibilité d'une cible dans la page courante.
	 * Si l'élément n'est pas présent ou ne répond pas aux critères immédiatement, on attend un éventuel chargement.
	 * @param cible la cible dont on teste la présence.
	 * @param visiblite la cible doit elle être visible?
	 * @return true si l'élément est détecter selon les critères, false sinon (ou en cas d'erreur).
	 */
	private boolean testerElementDiffere(CibleBean cible, boolean visibilite) {
		try {
			attendreChargementElement(cible, visibilite, visibilite);
			return true;
		} catch (Exception ex) {
			// Quelle que soit l'erreur, l'objectif de cette fonction n'est pas de remonter une exception.
			// On ne considère que deux états : l'élément est présent ou l'élément est absent
			//System.out.println(ex.getMessage());
			return false;
		}
	}
	
	/**
	 * Extrait les frames présente dans la page courante pour manipulation.
	 * @return la liste des identifiants de frame, null si il n'existe pas de frame.
	 */
	private List<String> extraireFrames() {
		List<String> retour = new LinkedList<String>();
		String contenu = driver.getPageSource();
		String tempLigne;
		
		if (contenu.contains("<frame ")) {
			if (contenu != null & !"".equals(contenu)) {
				String[] temp = contenu.split("<frame ");
				for (String ligne : temp) {
					tempLigne = ligne.split("id=\"")[1];
					retour.add(tempLigne.substring(0, tempLigne.indexOf("\"")));
				}
			}
		} else {
			return null;
		}
		return retour;
	}
	
	/**
	 * Liste des code sources des différentes frame de page courante.
	 * @param framesId liste des id de frames dont ont souhaites les codes sources.
	 * @return la liste des codes sources de chaque frame dont l'id est présente en paramètre.
	 */
	public List<String> sourceParFrame(String[] framesId) {
		List<String> retour = new LinkedList<String>();
		retour.add(driver.getPageSource());
		String temp;

		for (String frame : framesId) {
			if (frame != null && !"".equals(frame)) {
				try {
					temp = changerDeFrame(frame).getPageSource();
					//System.out.println(temp);
					retour.add(temp);
				} catch (SeleniumException ex) {
					retour.add("");
				}
			}
		}
		driver.switchTo().defaultContent();
		
		return retour;
	}
	
	//TODO Remplacer dans le code source la partie FRAME par son contenu.
	
	/**
	 * Tente une saisie de texte intuitive à partir de l'indication passée en paramètre.
	 * @param indication l'indication permettant de trouver l'input qui doit accueuillir la saisie.
	 * @param texte le texte concerné par la saisie.
	 * @throws SeleniumException en cas d'erreur où d'impossibilité de saisie.
	 */
	public void saisirIntuitif(String indication, String texte) throws SeleniumException {
		// POUR CHAQUE TYPE DE CRITERE DE RECHERCHE
		CibleBean temp = null;
		String xpath = ".//*[contains(text(),'" + indication + "')]/../input";
		List<String> listeFrame = null;
		// ON EXTRAIT LES FRAMES
		listeFrame = extraireFrames();
		if (listeFrame == null || listeFrame.size() == 0) {
			listeFrame = new LinkedList<String>();
			listeFrame.add(null);
		}
		// POUR CHAQUE FRAME ON ON CHERCHE UN ELEMENT POUVANT CORRESPONDRE
		//Exemple : .//*[contains(text(),'dentifiant')]/../input
		// POUR CHAQUE FRAME
		for (String tempId : listeFrame) {
			try {
				temp = new CibleBean(tempId, Clefs.XPATH, xpath);
				// ON TESTE LA PRESENCE DE L'ELEMENT
				if (testerPresenceElement(temp)) {
					// SI IL EST PRESENT ALORS ON CLIQUE ET ON STOPPE LE PROCESSUS
					saisir(texte, temp);
					return;
				}
			} catch (Exception ex) {
				// ON A PROBABLEMENT PARCOURUE UNE FRAME N'EXISTANT PAS
				//temp.toString();
				break;
			}
		} 
		throw new SeleniumException(Erreurs.E017, "La saisie intuitive à échouée");
	}

	/**
	 * Permet de cliquer sur le premier élément répondant à l'indication. Un cycle sur les types de critères va être fait jusqu'à trouver un élément répondant à la demande.
	 * Si aucun élément n'est trouver une exception est lancée.
	 * @param indication l'indication qui devrais permettre de trouver la cible du clic.
	 * @throws SeleniumException en cas d'erreur ou d'impossibilité de trouver l'élément.
	 */
	public void cliquerIntuitif(String indication) throws SeleniumException {
		// POUR CHAQUE TYPE DE CRITERE DE RECHERCHE
		CibleBean temp = null;
		List<String> listeFrame = null;
		for (Clefs clef : Clefs.values()) {
			// ON NE TESTE QUE LES CLEFS POUVANT REPONDRE AUX CRITERES
			if (clef.getCode() < Clefs.XPATH.getCode()) {
				// ON EXTRAIT LES FRAMES
				listeFrame = extraireFrames();
				if (listeFrame == null || listeFrame.size() == 0) {
					listeFrame = new LinkedList<String>();
					listeFrame.add(null);
				}
				// POUR CHAQUE FRAME
				for (String tempId : listeFrame) {
					try {
						temp = new CibleBean(tempId, clef, indication);
						// ON TESTE LA PRESENCE DE L'ELEMENT
						if (testerPresenceElement(temp)) {
							// SI IL EST PRESENT ALORS ON CLIQUE ET ON STOPPE LE PROCESSUS
							cliquer(temp);
							return;
						}
					} catch (Exception ex) {
						// SI ON A DEPASSER LE NOMBRE DE FRAME CONNU ON STOPPE POUR CETTE CLEF
						//temp.toString();
						break;
					}
				}
			}
		}
		throw new SeleniumException(Erreurs.E017, "La recherche intuitive à échouée");
	}
	
	/**
	 * Saisie de manière instantannée une chaine de caractère dans le champs.
	 * Cette saisie s'effectue par injection de code javascript dans le navigateur.
	 * Attention toutes valeurs préalables est effacée.
	 * @param cible la cible de la saisie.
	 * @param valeur la valeur à saisie.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisieInstantanee(CibleBean cible, String valeur) throws SeleniumException {
        ((JavascriptExecutor) this).executeScript("arguments[0].value = arguments[1]", obtenirElement(cible), valeur);
    }

	/**
	 * Permet d'obtenir les différentes valeurs possibles pour un selecteur visible dans l'IHM.
	 * @param cible la cible désignant le selecteur visible.
	 * @return la liste des valeurs possibles sous forme de tableau.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public String[] obtenirValeurs(CibleBean cible) throws SeleniumException {
		try {
			Select element = new Select(obtenirElementVisible(cible));
			String tempString = null;
			Integer compteur = 0;
			String[] retour = new String[element.getOptions().size()];
			for (WebElement option : element.getOptions()) {
				if (option.isDisplayed() && option.isEnabled()) {
					// TODO Doit on verifier toutes le orthographe??
					tempString = option.getAttribute("value");
					if (tempString == null) {
						tempString = option.getAttribute("Value");
					}
					if (tempString == null) {
						tempString = option.getAttribute("VALUE");
					}
					if (tempString != null) {
						retour[compteur] = tempString;
						compteur++;
					}
				}
			}
			return retour;
		} catch (UnexpectedTagNameException ex) {
			throw new SeleniumException(Erreurs.E017, "L'élément demandé n'est pas un selecteur");
		}
	}
	
	/**
	 * Permet de déplacer un élément visible vers un autre élément destination.
	 * @param cible la cible à déplacer.
	 * @param destination l'élément sur lequel on déplace la cible.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void dragAndDrop(CibleBean cible, CibleBean destination) throws SeleniumException {
		try {
			// On creer une action donc l'objectif est de faire le drag and drop.
			Actions builder = new Actions(driver);
			// On compile l'action et on l'execute.
			builder.dragAndDrop(obtenirElementVisible(cible), obtenirElement(destination)).perform();
		} catch (Exception ex) {
			throw new SeleniumException(Erreurs.E030, "DragAndDrop Impossible : " + ex.getMessage());
		}
	}
	
	/**
	 * Permet de faire un clic contextuel (clic gauche) sur un élément visible.
	 * @param cible la cible à cliquer.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void cliquerGauche(CibleBean cible) throws SeleniumException {
		try {
			// On creer une action donc l'objectif est de faire le clic contextuel.
			Actions builder = new Actions(driver);
			// On compile l'action et on l'execute.
			builder.contextClick(obtenirElementVisible(cible)).perform();
		} catch (Exception ex) {
			throw new SeleniumException(Erreurs.E030, "Clic contextuel Impossible : " + ex.getMessage());
		}
	}
	
	/**
	 * Permet de déplacer le curseur de la souris sur un élément dont on connais la cible.
	 * @param cible l'élémnent sur lequel la souris doit se déplacer.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void deplacerCurseur(CibleBean cible) throws SeleniumException {
		try {
			// On creer une action donc l'objectif est de faire le déplacement.
			Actions builder = new Actions(driver);
			// On compile l'action et on l'execute.
			builder.moveToElement(obtenirElement(cible));
		} catch (Exception ex) {
			throw new SeleniumException(Erreurs.E030, "Déplacement de la souris Impossible : " + ex.getMessage());
		}
	}
	
	/**
	 * Permet de modifier un élément de style pour une cible donnée.
	 * Attention cette modification est réalisée par injection javascript dans la navigateur.
	 * @param cible la cible de la modification.
	 * @param elementStyle l'élément de style à modifier (ex : display).
	 * @param valeur la valeur à associé à l'élément à modifier (ex : block)
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void modifierStyle(CibleBean cible, String elementStyle, String valeur) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0].style." + elementStyle + " = arguments[1];", obtenirElement(cible), valeur);
	}
	
	/**
	 * Permet de modifier un attribut pour une cible donnée.
	 * Attention cette modification est réalisée par injection javascript dans la navigateur.
	 * @param cible la cible de la modification.
	 * @param elementStyle l'attribut à modifier (ex : style.display, value, innerHTML).
	 * @param valeur la valeur à associé à l'attribut à modifier.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void modifierAttribut(CibleBean cible, String attribut, String valeur) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0]." + attribut + " = arguments[1];", obtenirElement(cible), valeur);
	}
	
	/**
	 * Permet de modifier un attribut pour une cible donnée.
	 * Attention cette modification est réalisée par injection javascript dans la navigateur.
	 * @param origine la cible dont on prend l'attribut
	 * @param destination la cible de la modification.
	 * @param attribut l'attribut à recopier.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void copierAttribut(CibleBean destination, CibleBean origine, String attribut) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0]." + attribut + " = arguments[1]." + attribut + ";", obtenirElement(destination), obtenirElement(origine));
	}
	
	/**
	 * Effectue une activation de la touche entrée du clavier.
	 */
	public void presserEnter() {
		driver.getKeyboard().pressKey(Keys.RETURN);
	}
	
	/**
	 * Effectue une activation de la touche demandée du clavier.
	 * @param touche la touche à activer.
	 */
	public void presserTouche(Keys touche) {
		driver.getKeyboard().pressKey(touche);
	}
	
	/**
	 * Effectue les captures d'écrans à associées au bean d'écran et à ses cibles.
	 * @param ecran l'écran à renseigner avec des captures d'écrans pour chaque cibles.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void capturesEcrans(EcranBean ecran) throws SeleniumException {
		for (String clef : ecran.getCibles().keySet()) {
			CibleBean cible = ecran.getCibles().get(clef);
			if (cible != null) {
				WebElement test = obtenirElementVisible(cible, false);
				if (test != null) {
					driver = changerDeFrame(cible.getFrame());
					if (captureEcran(test, clef)) {
						cible.setCapture("captures" + File.separator + clef + ".png");
					} else {
						cible.setCapture(null);
					}
				} else {
					cible.setCapture(null);
				}
			}
		}
	}

	/**
	 * Fonction d'attente permettant de vérifier la valeur d'un champ comme prérequis d'autres actions.
	 * On s'intérresse ici au paramètre "value" du champs.
	 * @param cible la cible dont on vérifie la valeur.
	 * @param valeur la valeur attendue pour cette cible.
	 * @param temps le temps d'attente maximal
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreValeur(final CibleBean cible, final String valeur, int temps) throws SeleniumException {
	    try {
			(new WebDriverWait(driver, temps)).until(new Function<WebDriver, Boolean>() {
		        public Boolean apply(WebDriver d) {
		            try {
		            	return valeur.equals(obtenirValeur(cible, false));
					} catch (SeleniumException e) {
						// Pour une raison inconnue il n'as pas été possible d'établir la valeur de l'élément.
						return false;
					}
		        }
		    });
	    } catch (TimeoutException e) {
	    	logger("L'attente de la valorisation (" + valeur + ") est arrivée à son terme sans succès.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    	//Assert.fail("Le texte " + texte + " est présent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est présente à l'écran, le texte n'est probablement plus présent à l'écran.
	    	logger("Lors de l'attente de la valorisation (" + valeur + "), une popup est apparue.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue à eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la valorisation (" + valeur + "), une erreur inconnue est survenue.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    }
	}
	
	/**
	 * Fonction d'attente permettant de vérifier la valeur d'un champ comme prérequis d'autres actions.
	 * On s'intérresse ici au paramètre "value" du champs. L'attente max est de 5 secondes.
	 * @param cible la cible dont on vérifie la valeur.
	 * @param valeur la valeur attendue pour cette cible.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreValeur(final CibleBean cible, final String valeur) throws SeleniumException {
		attendreValeur(cible, valeur, 5);
	}

	/**
	 * Permet d'obtenir le repertoire racine.
	 * @return le repertoire racine.
	 */
	public String getRepertoireRacine() {
		return repertoireRacine;
	}

	/**
	 * Permet de fixer le repertoire racine.
	 * @param repertoireRacine le nouveau repertoire racine.
	 */
	public void setRepertoireRacine(String repertoireRacine) {
		this.repertoireRacine = repertoireRacine;
	}
	
	/**
	 * Fonction résolvant une action simple utilisée courrament sur des cibles/textes.
	 * @param action une action parmi celles disponibles dans l'énumération Actions.
	 * @param cibles la ou les cibles/textes concernées par l'action.
	 * @throws SeleniumException en cas d'erreur renvoyée par l'action effectuée.
	 */
	public void action(constantes.Actions action, Object... cibles) throws SeleniumException {
		switch(action) {
			case CLIQUER : attendreEtCliquer((CibleBean) cibles[0]); break;
			case CLIQUER_GAUCHE : cliquerGauche((CibleBean) cibles[0]); break;
			case CLIQUER_JUSQUA : cliquerJusqua((CibleBean) cibles[0], (CibleBean) cibles[1]); break;
			case CLIQUER_OPTIONNEL : cliquerSiPossible((CibleBean) cibles[0]); break;
			case CLIQUER_SI_TEXTE : 
				if (testerPresenceTexte((String) cibles[1], true)) {
					cliquer((CibleBean) cibles[0]);
				}
				break;
			case ATTENDRE : 
				if(cibles[0].getClass() == CibleBean.class) {
					//attendreElement((CibleBean) cibles[0]);
					attendreChargementElement((CibleBean) cibles[0], true, true);
				} else {
					attendrePresenceTexte((String) cibles[0]);
				}
				break;
			case ATTENDRE_VALEUR : 
				if(cibles[1] != null) {
					attendreValorisation((CibleBean) cibles[0], (String) cibles[1]);
				} else {
					attendreValorisation((CibleBean) cibles[0], null);
				}
				break;
			case VIDER_ET_SAISIR : viderEtSaisir((String) cibles[1], (CibleBean) cibles[0]); break;
			case SELECTIONNER : selectionner((String) cibles[1], (CibleBean) cibles[0]); break;
			case ATTENDRE_DISPONIBILITE_ELEMENT : attendreChargementElementViaCondition((CibleBean) cibles[0]);break;
			default : return;
		}
	}
	
	
	/**
	 * Constructeur pour la boite à outil.
	 * Fixe le temps d'attente implicite à 10 secondes.
	 * @param driver le driver émulant firefox.
	 */
	public SeleniumOutils(GenericDriver driver) {
		super();
		this.typeImpl = GenericDriver.FIREFOX_IMPL;
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	/**
	 * Constructeur pour la boite à outil.
	 * Fixe le temps d'attente implicite à 10 secondes.
	 * @param driver le driver.
	 * @param typeImpl l'implémentation à utiliser.
	 */
	public SeleniumOutils(GenericDriver driver, String typeImpl) {
		super();
		this.typeImpl = typeImpl;
		this.driver = driver;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}
	
	/**
	 * Permet de récuperer ke driver associé avec cet outil.
	 * @return le driver.
	 */
	public GenericDriver getDriver() {
		return driver;
	}

	/**
	 * Permet de fixer le driver associé à cet outil.
	 * @param driver le driver à mettre.
	 */
	public void setDriver(GenericDriver driver) {
		this.driver = driver;
	}
}
