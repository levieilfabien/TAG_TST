package outils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import moteurs.FirefoxImpl;
import moteurs.GenericDriver;
import moteurs.IEImpl;

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
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;
import org.openqa.selenium.support.ui.WebDriverWait;

import beans.CasEssaiBean;
import beans.CibleBean;
import beans.CookieBean;
import beans.EcranBean;
import beans.ObjectifBean;
import beans.RGMBean;
import constantes.Clefs;
import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.SeleniumListener;

/**
 * Boite � outil contenant l'ensemble des fonctionnalit�s communes aux diff�rentes impl�mentation du driver.
 * Il faut pr�ciser � quelle type d'impl doit s'adapter le driver, sinon Firefoc est choisie par d�faut.
 * @author Fabien Levieil
 *
 */
public class SeleniumOutils {

	/**
	 * Le driver utilis� par la boite � outils.
	 */
	private GenericDriver driver = null;

	/**
	 * Le driver utilis� par la boite � outils.
	 */
	private EventFiringWebDriver clone = null;
	
	/**
	 * Le type d'impl�mentation du driver manipuler par la boite � outils.
	 */
	private String typeImpl = null;
	
	/**
	 * Le repertoire racine pour l'�criture d'objet.
	 */
	private String repertoireRacine = ".";
	
	public void ajouterListener() {
		EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
		eventDriver.register(new SeleniumListener());
		
		// ((EventFiringWebDriver) driver).register(new SeleniumListener()); 
	}
	
	/**
	 * Fonction qui injecte un code dans la page pour retenir tout clic sur la page.
	 * @throws SeleniumException 
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
	 * Renvoie le dernier WebElement qui ai subit un clic.
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
	 * Permet de colorer brievement un �l�ment pour le montrer sur l'interface.
	 * @param element l'�l�ment � indiquer.
	 */
	public void surlignerElement(WebElement element) {
	    //for (int i = 0; i < 2; i++) {
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
	        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
	    //}
	}
	
	/**
	 * Permet de colorer brievement un �l�ment pour le montrer sur l'interface.
	 * @param element l'�l�ment � indiquer.
	 */
	public void deSurlignerElement(WebElement element) {
	    //for (int i = 0; i < 2; i++) {
	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        //js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
	        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
	    //}
	}
	
	/**
	 * Permet de diriger la souris vers l'�l�ment param�te.
	 * @param element l'�l�ment param�tre vers lequel la souris doit se diriger.
	 */
	public void focusElement(WebElement element) {
		if (element.isDisplayed()) {
			Actions builder = new Actions(driver);
			Action moveTo = builder.moveToElement(element).build();
			
			moveTo.perform();
		}
	}
	
	/**
	 * R�alise une capture d'�cran dans un fichier cible.
	 * @param cible le fichier cible pour la capture d'�cran.
	 * @throws IOException en cas d'erreur d'acc�s au fichier.
	 */
	public void captureEcran(File cible) throws IOException {
		File screenShot  = driver.getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(screenShot, cible);
	}
	
	/**
	 * Effectue une capture d'�cran ayant pour nom le param�tre.
	 * @param nomCapture le nom de la capture d'�cran (sans l'extension).
	 */
	public void captureEcran(String nomCapture) {
		//logger("Capture d'�cran : " + nomCapture);
		if (repertoireRacine != null) {
			captureEcran(nomCapture, repertoireRacine);
		} else {
			captureEcran(nomCapture, null);
		}
	}
	
	/**
	 * Effectue une capture d'�cran ayant pour nom le param�tre.
	 * @param nomCapture le nom de la capture d'�cran (sans l'extension).
	 * @param cheminSortie chemin vers le fichier devant receptionner la capture d'�cran.
	 */
	public void captureEcran(String nomCapture, String cheminSortie) {
		//logger("Capture d'�cran : " + nomCapture);
		try {
			String chemin = ".\\captures";
			if (cheminSortie != null) {
				chemin = cheminSortie + File.separator + "captures";
			}
			FileUtils.forceMkdir(new File(chemin));
			File cible = new File(chemin + File.separator + nomCapture + ".png");
			captureEcran(cible);
		} catch (IOException e) {
			logger("Impossible de faire une capture d'�cran (" + e.getMessage() +")");
		} catch (Exception e) {
			logger("Erreur inatendue lors de la capture d'�cran (" + e.getMessage() +")");
		}
	}
	
	// TODO Quand l'�l�ment est dans un frame, ca d�cale les captures d'�crans.
	
	/**
	 * R�alise une capture d'�cran pour un �l�ment donn� uniquement.
	 * @param element l'�l�ment dont on souhaites capturer l'image.
	 * @param nomCapture le nom de la capture correspondante.
	 * @return true si la capture de l'�l�ment � �t� possible, false sinon.
	 */
	public boolean captureEcran(WebElement element, String nomCapture) {
		boolean retour = false;
		if (element != null && element.isDisplayed()) {
			//logger("Capture d'�cran pour un element : " + nomCapture);
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
				logger("Impossible de faire une capture d'�cran de l'�l�ment (" + e1.getMessage() +")");
			} catch (RasterFormatException e3)  {
				// l'image est mal construite ! On r�alise une capture compl�te de l'�cran.
				surlignerElement(element);
				captureEcran(nomCapture);
				deSurlignerElement(element);
			} catch (Exception e2) {
				logger("Erreur inatendue lors de la capture d'�cran de l'�l�ment (" + e2.getMessage() +")");
			}
		}
		return retour;
	}
	
	/**
	 * Permet de sauvegarder les cookies dans un fichier cookie.ser.
	 */
	public void sauvegarderCookies() {
		sauvegarderCookies("cookie");
	}
	
	/**
	 * Permet de sauvegarder les cookies dans un fichier de nom choisis.
	 * @param nomCookie le nom du fichier cookie d�sir�.
	 */
	public void sauvegarderCookies(String nomCookie) {
		DriverOutils.sauvegarderCookies(driver, nomCookie);
	}
	
	/**
	 * Charge le cookie dont le nom est cookie.ser dans le driver.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void chargerCookie() throws SeleniumException {
		chargerCookie("cookie");
	}

	   
	/**
	 * Charge le fichier cookie dont le nom est sp�cifier dans le driver.
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
	 * Indique si le chargement de la page est termin�.
	 * @param titre le titre de la page � attendre.
	 * @return true si la page attendue � finie son chargement, false si non.
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
	public void attendreChargementPage(final String titre) throws SeleniumException {
		attendreChargementPage(titre, null);
	}
	
	/**
	 * Attend le chargement d'une page dont le titre est connu.
	 * @param titre le titre de la page dont on attend le chargement.
	 * @param casEssai le cas d'essai � renseigner avec l'information.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementPage(final String titre, final CasEssaiBean casEssai) throws SeleniumException {
		attendreChargementPage(titre, casEssai, true);
	}
	
	/**
	 * Attend le chargement d'une page en fonction de son titre.
	 * @param titre le titre de la page.
	 * @param casEssai le cas d'essai � renseigner.
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
			(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	// En fait on ne respecte jamais la casse...
		        	boolean temp = lowerCase?supprimerCaracteresSpeciaux(d.getTitle().toLowerCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toLowerCase(Locale.ENGLISH))):supprimerCaracteresSpeciaux(d.getTitle().toUpperCase(Locale.ENGLISH)).startsWith(supprimerCaracteresSpeciaux(titre.toUpperCase(Locale.ENGLISH)));
		        	if (temp) {
		        		if (casEssai != null) {
		        			if (driver.impl.equals(GenericDriver.IE_IMPL)) {
		        				casEssai.ajouterEcran((IEImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), true));
		        			} else {
		        				casEssai.ajouterEcran((FirefoxImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), true));
		        			}
		        		} else {
		        			ajouterObjectifObtenu(new ObjectifBean(d.getTitle(), titre, titre.replace(" ", "") + timestamp.toString(), true));
		        		}
		        		return true;
		        	} else {
		        		if (casEssai != null) {
		        			if (driver.impl.equals(GenericDriver.IE_IMPL)) {
		        				casEssai.ajouterEcran((IEImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), false));
		        			} else {
		        				casEssai.ajouterEcran((FirefoxImpl)d, titre.replace(" ", "") + timestamp.toString(), new ObjectifBean(d.getTitle(), titre, titre + timestamp.toString(), false));
		        			}
		        		} else {
		        			ajouterObjectifObtenu(new ObjectifBean(d.getTitle(), titre, titre.replace(" ", "") + timestamp.toString(), false));
		        		}
		        		return false;
		        	}
		        }
		    });
	    } catch (TimeoutException e) {
	    	// Si on arrive ici, alors le test � �chou�, et la page n'as pas �t� charg�e.
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
	 * Simule l'acceptation d'une alerte javacript d�j� pr�sente � l'�cran.
	 * Si aucune popup n'est pr�sente, l'action est ignor�e.
	 */
	public void accepterAlerteJavascript() {
		try {
			// On tente de passer � l'alerte.
			Alert alert = driver.switchTo().alert();
			logger("On valide la popup dont le texte est : " + alert.getText());
			alert.accept();
		} catch (NoAlertPresentException ex) {
			logger("Aucune popup d'alerte n'est pr�sente, il est impossible de valider une popup");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger("Impossible de valider un prompt ou une alerte javascript");
		}
	}
	
	/**
	 * Test la pr�sence � l'�cran d'une popup d'alerte javascript.
	 * @return true si une popup est pr�sente, false sinon.
	 */
	public Boolean testerPresenceAlerteJavascript() {
		try {
			//logger("On teste la presence d'une popup");
			// On tente de passer � l'alerte.
			Alert alert = driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException ex) {
			// Pas d'alerte pr�sente
			return false;
		} catch (WebDriverException ex) {
			// La fenetre est deja fermee.
			return false;
		} catch (ClassCastException ex) {
			// Une erreur du framework webdriver renvoie un boolean au lieu du contenue de l'alerte.
			// On consid�re qu'une alerte est bel et bien pr�sente.
			return true;
		}
	}
	
	/**
	 * Permet d'obtenir le corps du message d'une alerte javascript � l'�cran.
	 * Si aucune alerte n'est pr�sente, l'action est ignor�e.
	 * @return le corps du message d'alerte.
	 */
	public String obtenirTexteAlerteJavascript() {
		try {
			// On tente de passer � l'alerte.
			Alert alert = driver.switchTo().alert();
			logger("On recupere la popup dont le texte est : " + alert.getText());
			return alert.getText();
		} catch (NoAlertPresentException ex) {
			logger("Aucune popup d'alerte n'est pr�sente, il est impossible de recuperer une popup");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger("Impossible de recuperer un prompt ou une alerte javascript");
		}
		return "";
	}
	
	/**
	 * Attend le chargement de la derni�re page dont le titre est connue.
	 * @param titre le titre de la page � attendre.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementDernierePage(final String titre) throws SeleniumException {
	    try {	
	    	if (testerPresenceAlerteJavascript()) {
	    		accepterAlerteJavascript();
	    	}    	
			(new WebDriverWait(driver, 7)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		            return changerDeFenetre(titre).getTitle().toLowerCase().startsWith(titre.toLowerCase());
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E012, "On attendais : " + titre + " et non : " + driver.getTitle());
	    }
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une p�riode maximale de 10 secondes.
	 * @param texte le texte.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String texte) throws SeleniumException {
		attendrePresenceTexte(null, texte);
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une p�riode maximale pr�cis�e.
	 * @param texte le texte.
	 * @param attente le temps d'attente maximal.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String texte, final long attente) throws SeleniumException {
		attendrePresenceTexte(null, texte, attente);
	}
	
	/**
	 * Fait attendre le driver le temps de l'affichage d'un texte pour une p�riode maximale de 10 secondes.
	 * @param frame la frame contenant le texte.
	 * @param texte le texte.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendrePresenceTexte(final String frame, final String texte) throws SeleniumException {
		attendrePresenceTexte(frame, texte, 10);
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
			(new WebDriverWait(driver, attente)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	return testerPresenceTexte(frame, texte, true, timestamp);
		        }
		    });
	    } catch (TimeoutException e) {
	    	//Assert.fail("Le texte " + texte + " n'est pas pr�sent sur la page.");
	    	throw new SeleniumException(Erreurs.E017, texte);
	    }  catch (UnhandledAlertException e) {
	    	// Une popup est pr�sente � l'�cran et emp�che de continuer la recherche.
	    	logger("Lors de l'attente de l'apparition du texte (" + texte + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue � eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de l'apparition du texte (" + texte + "), une erreur inconnue est survenue.");
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
		attendreNonPresenceTexte(texte, 30);
	}
	
	/**
	 * Attend la disparition d'un texte pendant une dur�e en seconde donn�e.
	 * @param texte le texte dont on attend la disparition.
	 * @param time le temps maximal d'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreNonPresenceTexte(final String texte, Integer time) throws SeleniumException {
	    try {
			(new WebDriverWait(driver, time)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		            return !testerPresenceTexte(null, texte, true, null);
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E013, texte);
	    	//Assert.fail("Le texte " + texte + " est pr�sent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est pr�sente � l'�cran, le texte n'est probablement plus pr�sente � l'�cran.
	    	logger("Lors de l'attente de la disparition du texte (" + texte + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue � eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la disparition du texte (" + texte + "), une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}
	
	/**
	 * Teste la pr�snece d'un texte dans une page.
	 * @param frame la frame o� se situe le texte.
	 * @param text le texte dont la pr�sence est � tester.
	 * @param visible la condition de visibilit� du texte.
	 * @param timestamp un timestamp de r�f�rence pour stocker l'�v�nement.
	 * @return true si le texte est pr�sent, false sinon.
	 */
	public boolean testerPresenceTexte(String frame, String text, boolean visible, Long timestamp){
		logger("On teste la presence du texte " + (visible?"visible":"invisible") + " '" + text + "' (frame : " + frame + ")");
		// Utilisation d'un timeStamp pour l'unicit� des objectifs.
		// On tente de localis� le texte tel quel (sans balise)
		List<WebElement> objets;
		if (frame == null) {
			objets = driver.findElements(By.xpath("//*[text()='" + text + "']"));
		} else {
			objets = driver.switchTo().frame(frame).findElements(By.xpath("//*[text()='" + text + "']"));
		}
		// Si on ne trouve pas le texte proprement dit, on tente de le localis� comme attribu� d'une balise.
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
		// Si on cherche � ce que le texte soit visible � l'�cran
		if (visible) {
			for (WebElement element : objets)  {
				// Si un seul des �l�ments trouv�s est visible, alors le texte est visible
				if (element.isDisplayed()) {
					if (timestamp != null)
						ajouterObjectifObtenu(element, new ObjectifBean(text, text, timestamp.toString() + text, true));
					return true;
				}
			}
			// Si on arrive ici , alors l'�l�ment est invisible, et on retourne faux.
			if (timestamp != null)
				ajouterObjectifObtenu(new ObjectifBean("", text, timestamp.toString() + text, false));
			return false;
		} else {
			Boolean temp = objets.size() > 0;
			// Si la visibilit� n'as pas d'importance, il suffit que le texte existe.
			if (timestamp != null)
				ajouterObjectifObtenu(objets.get(0), new ObjectifBean(temp?text:"", text, timestamp.toString() + text, temp));
			return temp;
		}
	}

	/**
	 * V�rifie la pr�sence d'un texte dans la page.
	 * @param text le texte � trouver.
	 * @param visible le crit�re de visibilit�.
	 * @return true si le texte est trouv�, false sinon.
	 */
	public boolean testerPresenceTexte(String text, boolean visible){
		Long timestamp = new Date().getTime();
		return testerPresenceTexte(null, text, visible, timestamp);
	}
	
	/**
	 * Attend le chargement d'un �l�ment, que celui ci soit visible ou non.
	 * @param cible la cible d�signant l'�lement � attendre.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementElement(final CibleBean cible) throws SeleniumException {
		attendreChargementElement(cible, false, false);
	}
	
	/**
	 * Attend le chargement d'un �l�ment dans la page avant de poursuivre le test.
	 * @param cible la cible d�signant l'�l�ment � attendre.
	 * @param visible l'�lement doit il �tre visible ?
	 * @param actif l'�lement doit il �tre actif (non disabled) ?
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreChargementElement(final CibleBean cible, final boolean visible, final boolean actif) throws SeleniumException {
	    try {
	    	logger("On attend le chargement de l'�l�ment " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	final By critere = cible.creerBy();
			(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver driver) {
		        	try {
		        		Boolean retour = false;
		        		WebElement temp;
		        		// En fonction du crit�re visible on r�cup�re l'�l�ment.
		        		if (visible) {
		        			temp = obtenirElementVisible(cible);
		        		} else {
		        			temp = obtenirElement(cible.getFrame(), critere);
		        		}
		        		retour = (temp != null);
		        		// Si on � un crit�re d'activit� et que l'�l�ment existe on v�rifie.
		        		if (actif && retour) {
		        			retour = (temp.isEnabled() && temp.getAttribute("disabled") == null);
		        		}
		        		return retour;
		        	} catch (SeleniumException e) {
		        		// L'�l�ment n'est pas encore pr�sent.
		        		return false;
		        	}
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "Element introuvable (ou d�sactiv�) : " + cible.lister());
	    }
	}
	
	/**
	 * Fonction temporaire pour une attente. Meilleure solution � trouver.
	 * @param nbSec le nombre de seconde d'attente
	 */
	public void attendre(long nbSec) {
	    try {;
	    	logger("On attend pendant " + nbSec + " secondes.");
			(new WebDriverWait(driver, nbSec)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver driver) {
		        	return false;
		        }
		    });
	    } catch (TimeoutException e) {
	    	logger("Le temps d'attente est �coul�.");
	    }
	}
	
	/**
	 * Attend la disparition d'un �l�ment pendant un nombre donn� de seconde.
	 * @param cible la cible d�signant l'�l�ment dont on attend la disparition.
	 * @param nbreSecondes le nombre de seconde maximum d'attente.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreDisparitionElement(final CibleBean cible, Integer nbreSecondes) throws SeleniumException {
	    try {
	    	logger("On attend la disparition de l'�l�ment " + cible.getClef() + " : " + cible.lister() + " (Frame : " + cible.getFrame() + ")");
	    	final By critere = cible.creerBy();
			(new WebDriverWait(driver, nbreSecondes)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver driver) {
	        		// Impossible d'atteindre l'�l�ment. Donc l'�l�ment n'est pas pr�sent sur la page.
	        		// Il est aussi possible qu'un changement de page soit survenue (ex : loadingbar)
	        		// Une exception est donc consid�r�e comme une r�ussite du test.
		        	try {
		        		WebElement temp = obtenirElement(cible.getFrame(), critere);
		        		return  (temp == null || !temp.isDisplayed());
		        	} catch (SeleniumException e) {
		        		return true;
		        	} catch (StaleElementReferenceException ex) {
		        		return true;
		    	    }
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E009, "L'Element est toujours pr�sent : " + cible.lister());
	    } 
	}
	
	/**
	 * Fait attendre le drive le temps que la cible disparaisse.
	 * @param cible la cible dont on attend la disparition.
	 * @throws SeleniumException en cas d'erreur (ou si la cible ne disparais pas).
	 */
	public void attendreDisparitionElement(final CibleBean cible) throws SeleniumException {
		attendreDisparitionElement(cible, 10);
	}
	
	/**
	 * Permet d'obtenir le driver associ� � une frame dont l'id est connu.
	 * @param idFrame l'id de la frame � selectionner.
	 * @return le driver associ� � la frame souhait�e.
	 * @throws SeleniumException si la frame n'existe pas.
	 */
	private GenericDriver changerDeFrame(final String idFrame) throws SeleniumException {
		try {
			if (typeImpl == GenericDriver.FIREFOX_IMPL) {
				return (FirefoxImpl) driver.switchTo().defaultContent().switchTo().frame(idFrame);
			} else if (typeImpl == GenericDriver.IE_IMPL) {
				return (IEImpl) driver.switchTo().defaultContent().switchTo().frame(idFrame);
			} else {
				//TODO � r�aliser.
				return null;
			}
		} catch (NoSuchFrameException ex) {
			throw new SeleniumException(Erreurs.E009, "Frame introuvable : " + idFrame);
		}
	}
	
	/**
	 * Permet de changer la fen�tre active lors du test.
	 * On active automatiquement la derni�re fen�tre ouverte.
	 * @return un driver pointant sur la derni�re fen�tre ouverte.
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
		logger("On passe sur la derniere fen�tre (" + driver.getTitle() + ")");
		
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
	 * Permet de changer la fen�tre active lors du test.
	 * On active automatiquement la derni�re fen�tre dont le titre correspond au param�tre.
	 * @param titre le titre de la fen�tre souhait�e.
	 * @return un driver pointant sur la fen�tre souhait�e. Si c'est impossible on reste sur la m�me fen�tre.
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
    	
    	// Si la page � un titre, on v�rifie que ce ne soit pas le titre recherch�.
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
				logger("On passe sur la derniere fen�tre (" + retour.getTitle() + ")");
				return retour;
			}
		}
		
		logger("Impossible de changer de page on reste sur la page courante (" + retour.getTitle() + ")");
		return retour;
	}
	
	/**
	 * Permet de fermer la fen�tre courante.
	 * On passe sur la prochaine fen�tre.
	 * @return le drive pointant sur la prochaine fen�tre.
	 */
	public GenericDriver fermerFenetreCourante() {
		logger("On ferme la fenetre courante " + driver.getTitle());
		driver.close();
		return changerDeFenetre();
	}
	
	/**
	 * Permet de cliquer sur un element.
	 * Il n'est pas possible d'interragir avec un �l�ment invisible, aussi on clique toujours sur un �l�ment visible.
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
				throw new SeleniumException(Erreurs.E017, "La cible " + cible.toString() + " du clic n'est pas visible.");
			}
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E017, "La cible " + cible.toString() + " du clic n'est plus disponible (rechargement?).");
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
	 * @param cible le crit�re de recherche des �l�ments.
	 * @param clicMax indique le nombre maximal d'�l�ment � cliquer.
	 * @return le nombre d'�l�ments trouv�s.
	 * @throws SeleniumException en cas d'erreur.
	 */
	private int cliquerMultiple(CibleBean cible, Integer clicMax) throws SeleniumException {
		int retour = 0;
		if (clicMax == null) {
			clicMax = 999;
		}
		for (WebElement element : obtenirElements(cible)) {

			if (element.isDisplayed() && element.isEnabled() && clicMax > 0) {
				//System.out.println("Int�raction avec l'�lement " + element.getText());
				clicMax--;
				retour++;
				element.click();
			}
		}
		logger("Clics sur les �l�ments (visibles) " + cible.creerBy().toString() + " (frame : " + cible.getFrame() + ")");

		return retour;
	}
	
	/**
	 * Permet la saisie dans un element d'une frame.
	 * @param cible la cible de la saisie
	 * @param texte le texte � saisir.
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
			throw new SeleniumException(Erreurs.E022, by.toString());
		}
			
	}
	
	/**
	 * Obtenir un �l�ment via la frame concern�e et le by.
	 * @param idFrame la frame si il y a lieu (null sinon)
	 * @param by le crit�re de recherche param�tr�.
	 * @return l'�l�ment.
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
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + " Lors de la recherche de l'�l�ment : " + by.toString() );
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "L'element " + by.toString() + " n'est plus sur la page");
		}
	}
	
	/**
	 * Obtenir des �l�ments via la frame concern�e et le by.
	 * @param idFrame la frame si il y a lieu (null sinon)
	 * @param by le crit�re de recherche param�tr�.
	 * @return la liste des �l�ments.
	 * @throws SeleniumException si cela n'existe pas.
	 */
	private List<WebElement> obtenirElements(String idFrame, By by) throws SeleniumException {
		try {
			if (idFrame != null) {
				System.out.println("On cherche les �l�ments dans la frame " + idFrame + " qui sont " + by);
				return changerDeFrame(idFrame).findElements(by);
			} else {
				return driver.findElements(by);
			}
		} catch (NoSuchElementException ex) {
			throw new SeleniumException(Erreurs.E009, "Aucun element " + by.toString() + " pr�sent");
		} catch (ElementNotVisibleException ex) {
			throw new SeleniumException(Erreurs.E016, "Aucun element " + by.toString() + " visible");
		} catch (UnhandledAlertException ex) {
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + "  lors de la recherche de : " + by.toString());
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "Les elements " + by.toString() + " ne sont plus sur la page");
		}
	}
	
	/**
	 * Permet d'obtenir un �l�ment � partir d'une cible.
	 * @param cible la cible d�signant l'objet recherch�.
	 * @return l'�l�ment demand�.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElement(CibleBean cible) throws SeleniumException {
		return obtenirElement(cible.getFrame(), cible.creerBy());
	}
	
	/**
	 * Permet d'obtenir un �l�ment visible � partir d'une cible.
	 * @param cible la cible d�signant l'objet visible recherch�.
	 * @return l'�l�ment demand� (si il est visible).
	 * @throws SeleniumException en cas d'erreur ou si l'�l�ment n'est pas visible.
	 */
	public WebElement obtenirElementVisible(CibleBean cible) throws SeleniumException {
		return obtenirElementVisible(cible, true);
	}
	
	/**
	 * Permet d'obtenir le premier element visible r�pondant aux crit�res.
	 * @param cible la cible des recherches.
	 * @param obligatoire true si le test �choue en cas de nous correspondance. False sinon.
	 * @return null si rien n'existe et que l'objet d�vais �tre trouver. L'objet sinon.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public WebElement obtenirElementVisible(CibleBean cible, boolean obligatoire) throws SeleniumException {
		try {
			for (WebElement element : obtenirElements(cible.getFrame(), cible.creerBy())) {
				if (element.isDisplayed() && element.isEnabled()) {
					return element;
				}
			}
		} catch (UnhandledAlertException ex) {
			throw new SeleniumException(Erreurs.E019, "Popup " + ex.getAlertText() + " lors de la recherche de : " + cible.lister() + "");
		} catch (StaleElementReferenceException ex) {
			throw new SeleniumException(Erreurs.E023, "Impossible d'atteindre l'�lement lors de la recherche de : " + cible.lister() + "");
		} catch (SeleniumException ex) {
			if (ex.getInformations() == Erreurs.E009 && !obligatoire) {
				logger("Pas d'objet r�pondants aux crit�res " + cible.toString());
				return null;
			} else {
				throw ex;
			}
		}
 		return null;
	}
	
	/**
	 * Permet d'obtenir une liste d'�l�ment � partir d'un crit�re de ciblage.
	 * @param cible le crit�re de ciblage pour les �l�ments.
	 * @return la liste des �l�ments obtenu suite � la requ�te de ciblage.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public List<WebElement> obtenirElements(CibleBean cible) throws SeleniumException {
		return obtenirElements(cible.getFrame(), cible.creerBy());
	}
	
	/**
	 * Permet un clic sur un �l�ment d�sign� par son ID.
	 * @param valeur l'ID de l'�l�ment avec lequel on int�rragit.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void cliquer(String valeur) throws SeleniumException {
		//cliquer(null, creerBy(Clefs.ID, valeur));	
		cliquer(new CibleBean(null, Clefs.ID, new String[] {valeur}));
	}
	
	/**
	 * Permet un clic sur un �l�ment d�sign� apr�s attente d'affichage.
	 * @param cible la cible du clic.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreEtCliquer(CibleBean cible) throws SeleniumException {
		attendreChargementElement(cible, true, true);
		cliquer(cible);
	}
	
	/**
	 * Permet de cliquer sur autant d'�l�ments que possible r�pondant aux crit�res de ciblage.
	 * @param cible la cible des clics.
	 * @return le nombre de clics effectu�s.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public int cliquerMultiple(CibleBean cible) throws SeleniumException {
		return cliquerMultiple(cible, null);	
	}

	/**
	 * Saisie � la suite du texte pr�sent dans une cible d'une nouvelle chaine.
	 * @param texte le texte � saisir
	 * @param cible la cible de la saisie
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisir(String texte, CibleBean cible) throws SeleniumException {
		saisir(texte, cible, false);	
	}
	
	public void saisirVisible(String texte, CibleBean cible) throws SeleniumException {
		obtenirElementVisible(cible).sendKeys(texte);
		logger("Saisie de " + texte + " dans le champ visible (" + cible.lister() + ") " + (cible.getFrame() != null?("pour la frame "+ cible.getFrame()):""));
	}

	/**
	 * Saisie � la place du texte pr�sent dans une cible d'une nouvelle chaine.
	 * @param texte le texte � saisir
	 * @param cible la cible de la saisie
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void viderEtSaisir(String texte, CibleBean cible) throws SeleniumException {
		saisir(texte, cible, true);
	}
	
	/**
	 * Saisie � la suite du texte pr�sent dans une cible d'une nouvelle chaine.
	 * @param texte le texte � saisir
	 * @param valeur l'id de la cible
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisir(String valeur, String texte) throws SeleniumException {
		saisir(texte, new CibleBean(null, Clefs.ID, valeur), false);
	}

//	public void selectionner(String libelle, CibleBean cible) throws SeleniumException {
//		try {
//			Select temp = new Select(obtenirElementVisible(cible));
//			try {
//				temp.selectByVisibleText(libelle);
//			}	catch (Exception ex) {
//				temp.selectByValue(libelle);
//			}
//			logger("Selection de la valeur " +  libelle + " pour le select : " +  cible.lister());
//		}  catch (Exception ex) {
//			throw new SeleniumException(Erreurs.E009, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
//		}
//	}
	
	/**
	 * Saisie dans la cible d�sign�e d'un texte � la place du texte pr�sent.
	 * @param libelle le texte � saisir
	 * @param cible la cible de la saisie
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void selectionner(String libelle, CibleBean cible) throws SeleniumException {
		selectionner(libelle, cible, true);
	}
	
	/**
	 * Permet la selection d'une valeur dans une balise "select". On tente une selection directe vis � vis de la valeur ou du texte.
	 * Si cela ne fonctionne pas, une selection via le "innerhtml" aura lieu si le top v�rificaiton est � true.
	 * @param libelle le libelle ou la valeur � selectionner.
	 * @param cible la cible d�signant le selecteur
	 * @param verification true si on souhaites v�rifier que la selection est effective, false sinon.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void selectionner(String libelle, CibleBean cible, boolean verification) throws SeleniumException {
		Boolean success = false;
		try {
			// On obtiens le select (� condition qu'il soit visible)
			WebElement tempWebElement = obtenirElementVisible(cible);
			WebElement tempOption = null;
			// On cr�er un selecteur � partir du WebElement.
			Select temp = new Select(tempWebElement);
			// On s�l�ctionne le texte ou la valeur.
			try {
				// On essai d'abord de selectionn� par libelle d'option.
				temp.selectByVisibleText(libelle);
				// Si la selection est effective, il devient alors possible de cliquer dessus
				if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
					tempOption = obtenirElement(new CibleBean(null, cible, "*", "text=" + libelle));
				} else {
					tempOption = obtenirElement(new CibleBean(Clefs.TEXTE_PARTIEL, libelle));
				}
				
			}	catch (NoSuchElementException ex) {
				// Si le texte n'est pas visible, on essai de s�lectionner par valeur.
				temp.selectByValue(libelle);
				// Si la selection est effective, il devient alors possible de cliquer dessus
				if (cible.getClef() == Clefs.CRITERES_ITERATIF) {
					tempOption = obtenirElement(new CibleBean(null, cible, "*", "value=" + libelle));
				} else {
					tempOption = obtenirElement(new CibleBean(Clefs.VALEUR, libelle));
				}
			}
			// On doit ensuite envoyer une validation � la selection, sans quoi la selection n'est pas finalis�e.
			if (tempOption != null) {
				tempOption.sendKeys(Keys.RETURN);
				
				// Si aucune v�rification n'est demand�e et que la selection est d�j� effective, on s'arr�te l�.
				if (!verification) {
					return;
				}
			}
			
			// On v�rifie que a saisie � bien �t� prise en compte (A v�rifier).
			String tempText;
			String tempValue;
			// Une fois que l'on � interragie avec, on modifie la struture du select, il faut le re-obtenir.
			temp = new Select(obtenirElementVisible(cible));
			// On parcours les options selectionn�es et on cherche la valeur choisie.
			for (WebElement element : temp.getAllSelectedOptions()) {
				if (element != null) {
					tempText = element.getText();
					tempValue = element.getAttribute("value");
					// On teste si le libelle ou la valeur on bien �t� s�lectionn�e.
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
					// On clique sur la valeur selectionn�e.
					element.click();
					// Si on � prouver que la valeur est s�lectionn�e alors on quitte la fonction.
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
			//ex.printStackTrace();
			throw new SeleniumException(Erreurs.E023, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
		}
		
		if (!success) {
			// Une erreur si la s�lection n'est pas effective.
			throw new SeleniumException(Erreurs.E009, "(Selecteur : " + cible.lister() + ", Valeur : " + libelle + ")");
		}
	}
	
	/**
	 * Permet d'obtenir la valeur associ�e � une cible visible.
	 * @param cible la cible.
	 * @return la valeur associ�e � la cible (si la value est vide alors on renvoie le texte).
	 * @throws SeleniumException en cas d'erreur.
	 */
	public String obtenirValeur(CibleBean cible) throws SeleniumException {
		return obtenirValeur(obtenirElementVisible(cible));
	}
	
	/**
	 * Permet d'obtenir la valeur associ�e � une cible visible ou pas.
	 * @param cible la cible.
	 * @param visibilite true si la cible est attendues visible.
	 * @return la valeur associ�e � la cible (si la value est vide alors on renvoie le texte).
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
	 * Si celui ci est un select, on r�cup�re la valeur de la premi�re option selectionn�e.
	 * @param element l'element.
	 * @return la valeur (value) d'un �l�ment ou son contenu texte.
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
				// Le cas o� ce n'est pas un select
				retour = element.getAttribute("value");
				if (retour == null || "".equals(retour.trim())) {
					retour = element.getText();
				}
    		}
		}
		return retour;
	}
	
	public void logger(String log) {
		if (isActivationLog()) {
			getLog().append("\n"+log);
		}
	}

	public void ajouterObjectifObtenu(ObjectifBean objectif) {
		getObjectifsDriver().put(objectif.getClefUnique(), objectif);
		captureEcran(objectif.getClefUnique());
	}
	
	/**
	 * Permet d'ajouter un objectif au objectifs du driver.
	 * Cet objectif doit �tre valider ou invalider au moment de l'ajout sans quoi la capture d'�cran n'est pas significative.
	 * @param element l'�l�ment concern� par l'objectif.
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
	 * Permet de savoir si les logs sont activ�s.
	 * @return true si activ�, false sinon.
	 */
	public boolean isActivationLog() {
		return driver.isActivationLog();
	}

	/**
	 * Permet l'activation ou la d�sactivation des log pour les outils.
	 * @param activationLog true pour activation false pour d�sactivation.
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
	 * Permet de fixer les objectifs propres � l'instance du driver.
	 * @param objectifsDriver les objectifs sous forme de Hashmap.
	 */
	public void setObjectifsDriver(HashMap<String, ObjectifBean> objectifsDriver) {
		driver.setObjectifsDriver(objectifsDriver);
	}
	
	/**
	 * Cette fonction supprime les caract�res sp�ciaux des chaines de caract�res pour faciliter les comparaisons.
	 * @param s la chaine de caract�res � normaliser.
	 * @return le resultat de la normalisation.
	 */
	public static String supprimerCaracteresSpeciaux(String s) {
		s = s.toUpperCase();
		String temp = Normalizer.normalize(s, Normalizer.Form.NFKD);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}
	
	/**
	 * Permet d'attendre la disparition d'un �l�ment donn�.
	 * @param cible l'�l�ment dont on attend la disparition.
	 * @throws SeleniumException en cas d'erreur ou de non disparition de l'�l�ment.
	 */
	public void attendreNonPresenceElement(final CibleBean cible) throws SeleniumException {
	    try {
			(new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		        	boolean retour = false;
		        	try {
		        		retour = (obtenirElementVisible(cible) == null);
		        	} catch (SeleniumException ex) {
		        		logger(ex.toString());
		        	}
		            return (retour);
		        }
		    });
	    } catch (TimeoutException e) {
	    	throw new SeleniumException(Erreurs.E013, cible.lister());
	    	//Assert.fail("Le texte " + texte + " est pr�sent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est pr�sente � l'�cran, le texte n'est probablement plus pr�sente � l'�cran.
	    	logger("Lors de l'attente de la disparition de l'element (" + cible.lister() + "), une popup est apparue.");
	    	return;
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue � eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la disparition de l'element (" + cible.lister() + "), une erreur inconnue est survenue.");
	    	ex.printStackTrace();
	    	return;
	    }
	}

	/**
	 * Permet de faire patienter le driver pendant une dur�e d�termin�e.
	 * @param secondes le nombre de secondes � attendre
	 */
	public void patienter(long secondes) {
		DriverOutils outil = new DriverOutils(driver);
		try {
			outil.wait(secondes);
		} catch (InterruptedException e) {
			logger("L'attente � �t� interrompue");
		} catch (IllegalMonitorStateException e) {
			logger("On fait patienter une fenetre qui n'existe plus");
		}
	}

	/**
	 * Permet de v�rifier une r�gle en fonction des crit�re d�finie dans celle ci .
	 * @param regle la r�gle dont on souhaites faire la v�rification.
	 * @return true si la r�gle est v�rifi�e, false sinon.
	 */
	public boolean verifierRGM(RGMBean regle) {
		
		boolean retour = false;
		Object cible = regle.getCible();
		
		// Le r�gle de gestion teste la pr�sence d'un �l�ment ou d'un texte.
		if (regle.getTypeComparaison() == RGMBean.PRESENCE) {
			if (cible.getClass() == String.class) {
				retour = testerPresenceTexte((String) cible, true);
			}
			if (cible.getClass() == WebElement.class) {
				// Un webelement est forcement pr�sent si il existe.
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
				// Un webelement est forcement pr�sent si il existe.
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
	 * Permet de multiplier les clics sur une cible donn�e.
	 * @param cible la cible des clics
	 * @param nombreDeClic le nombre de clics � effectuer
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
	 * Permet de changer l'url courante utilis�e par le driver.
	 * @param url la nouvelle url � visiter.
	 */
	public void chargerUrl(String url) {
		driver.get(url);
	}
	
	/**
	 * Permet de tester la pr�sence d'une cible dans la page courante.
	 * @param cible la cible dont on teste la pr�sence.
	 * @return true si l'�l�ment est pr�sent et visible, false sinon (ou en cas d'erreur).
	 */
	public boolean testerPresenceElement(CibleBean cible) {
		try {
			return obtenirElementVisible(cible) != null;
		} catch (Exception ex) {
			return false;
		}
	}
	
	/**
	 * Extrait les frames pr�sente dans la page courante pour manipulation.
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
	 * Liste des code sources des diff�rentes frame de page courante.
	 * @param framesId liste des id de frames dont ont souhaites les codes sources.
	 * @return la liste des codes sources de chaque frame dont l'id est pr�sente en param�tre.
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
				temp.toString();
				break;
			}
		} 
		throw new SeleniumException(Erreurs.E017, "La recherche intuitive � �chou�e");
	}

	/**
	 * Permet de cliquer sur le premier �l�ment r�pondant � l'indication. Un cycle sur les types de crit�res va �tre fait jusqu'� trouver un �l�ment r�pondant � la demande.
	 * Si aucun �l�ment n'est trouver une exception est lanc�e.
	 * @param indication l'indication qui devrais permettre de trouver la cible du clic.
	 * @throws SeleniumException en cas d'erreur ou d'impossibilit� de trouver l'�l�ment.
	 */
	public void cliquerIntuitif(String indication) throws SeleniumException {
		// POUR CHAQUE TYPE DE CRITERE DE RECHERCHE
		CibleBean temp = null;
		List<String> listeFrame = null;
		for (Clefs clef : Clefs.values()) {
			// ON NE TESTE QUE LES CLEFS POUVANT REPONDRE AUX CRITERES
			if (clef.getCode() < 10) {
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
						// SI ON A DEPASER LE NOMBRE DE FRAME CONNU ON STOPPE POUR CETTE CLEF
						temp.toString();
						break;
					}
				}
			}
		}
		throw new SeleniumException(Erreurs.E017, "La recherche intuitive � �chou�e");
	}
	
	/**
	 * Saisie de mani�re instantann�e une chaine de caract�re dans le champs.
	 * Attention toutes valeurs pr�alables est effac�e.
	 * @param cible la cible de la saisie.
	 * @param valeur la valeur � saisie.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void saisieInstantanee(CibleBean cible, String valeur) throws SeleniumException {
        ((JavascriptExecutor) this).executeScript("arguments[0].value = arguments[1]", obtenirElement(cible), valeur);
    }

	/**
	 * Permet d'obtenir les diff�rentes valeurs possibles pour un selecteur visible dans l'IHM.
	 * @param cible la cible d�signant le selecteur visible.
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
			throw new SeleniumException(Erreurs.E017, "L'�l�ment demand� n'est pas un selecteur");
		}
	}
	
	/**
	 * Permet de d�placer un �l�ment visible vers un autre �l�ment destination.
	 * @param cible la cible � d�placer.
	 * @param destination l'�l�ment sur lequel on d�place la cible.
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
	 * Permet de faire un clic contextuel (clic gauche) sur un �l�ment visible.
	 * @param cible la cible � cliquer.
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
	 * Permet de d�placer le curseur de la souris sur un �l�ment dont on connais la cible.
	 * @param cible l'�l�mnent sur lequel la souris doit se d�placer.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void deplacerCurseur(CibleBean cible) throws SeleniumException {
		try {
			// On creer une action donc l'objectif est de faire le d�placement.
			Actions builder = new Actions(driver);
			// On compile l'action et on l'execute.
			builder.moveToElement(obtenirElement(cible));
		} catch (Exception ex) {
			throw new SeleniumException(Erreurs.E030, "D�placement de la souris Impossible : " + ex.getMessage());
		}
	}
	
	/**
	 * Permet de modifier un �l�ment de style pour une cible donn�e.
	 * @param cible la cible de la modification.
	 * @param elementStyle l'�l�ment de style � modifier (ex : display).
	 * @param valeur la valeur � associ� � l'�l�ment � modifier (ex : block)
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void modifierStyle(CibleBean cible, String elementStyle, String valeur) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0].style." + elementStyle + " = arguments[1];", obtenirElement(cible), valeur);
	}
	
	/**
	 * Permet de modifier un attribut pour une cible donn�e.
	 * @param cible la cible de la modification.
	 * @param elementStyle l'attribut � modifier (ex : style.display, value, innerHTML).
	 * @param valeur la valeur � associ� � l'attribut � modifier.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void modifierAttribut(CibleBean cible, String attribut, String valeur) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0]." + attribut + " = arguments[1];", obtenirElement(cible), valeur);
	}
	
	/**
	 * Permet de modifier un attribut pour une cible donn�e.
	 * @param origine la cible dont on prend l'attribut
	 * @param destination la cible de la modification.
	 * @param attribut l'attribut � recopier.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void copierAttribut(CibleBean destination, CibleBean origine, String attribut) throws SeleniumException {
		((JavascriptExecutor) driver).executeScript("arguments[0]." + attribut + " = arguments[1]." + attribut + ";", obtenirElement(destination), obtenirElement(origine));
	}
	
	/**
	 * Effectue une activation de la touche entr�e du clavier.
	 */
	public void presserEnter() {
		driver.getKeyboard().pressKey(Keys.RETURN);
	}
	
	/**
	 * Effectue une activation de la touche demand�e du clavier.
	 * @param touche la touche � activer.
	 */
	public void presserTouche(Keys touche) {
		driver.getKeyboard().pressKey(touche);
	}
	
	/**
	 * Effectue les captures d'�crans � associ�es au bean d'�cran et � ses cibles.
	 * @param ecran l'�cran � renseigner avec des captures d'�crans pour chaque cibles.
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
	 * Constructeur pour la boite � outil.
	 * @param driver le driver �mulant firefox.
	 */
	public SeleniumOutils(GenericDriver driver) {
		super();
		this.typeImpl = GenericDriver.FIREFOX_IMPL;
		this.driver = driver;
	}

	/**
	 * Constructeur pour la boite � outil.
	 * @param driver le driver.
	 * @param typeImpl l'impl�mentation � utiliser.
	 */
	public SeleniumOutils(GenericDriver driver, String typeImpl) {
		super();
		this.typeImpl = typeImpl;
		this.driver = driver;
	}
	
	/**
	 * Permet de r�cuperer ke driver associ� avec cet outil.
	 * @return le driver.
	 */
	public GenericDriver getDriver() {
		return driver;
	}

	/**
	 * Permet de fixer le driver associ� � cet outil.
	 * @param driver le driver � mettre.
	 */
	public void setDriver(GenericDriver driver) {
		this.driver = driver;
	}

	/**
	 * Fonction d'attente permettant de v�rifier la valeur d'un champ comme pr�requis d'autres actions.
	 * On s'int�rresse ici au param�tre "value" du champs.
	 * @param cible la cible dont on v�rifie la valeur.
	 * @param valeur la valeur attendue pour cette cible.
	 * @param temps le temps d'attente maximal
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void attendreValeur(final CibleBean cible, final String valeur, int temps) throws SeleniumException {
	    try {
			(new WebDriverWait(driver, temps)).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver d) {
		            try {
		            	return valeur.equals(obtenirValeur(cible, false));
					} catch (SeleniumException e) {
						// Pour une raison inconnue il n'as pas �t� possible d'�tablir la valeur de l'�l�ment.
						return false;
					}
		        }
		    });
	    } catch (TimeoutException e) {
	    	logger("L'attente de la valorisation (" + valeur + ") est arriv�e � son terme sans succ�s.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    	//Assert.fail("Le texte " + texte + " est pr�sent et visible sur la page.");
	    } catch (UnhandledAlertException e) {
	    	// Une popup est pr�sente � l'�cran, le texte n'est probablement plus pr�sent � l'�cran.
	    	logger("Lors de l'attente de la valorisation (" + valeur + "), une popup est apparue.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    } catch (Exception ex) {
	    	// Une erreur technique inconnue � eu lieue, cela ne doit pas stopper le test.
	    	logger("Lors de l'attente de la valorisation (" + valeur + "), une erreur inconnue est survenue.");
	    	throw new SeleniumException(Erreurs.E034, valeur);
	    }
	}
	
	/**
	 * Fonction d'attente permettant de v�rifier la valeur d'un champ comme pr�requis d'autres actions.
	 * On s'int�rresse ici au param�tre "value" du champs. L'attente max est de 5 secondes.
	 * @param cible la cible dont on v�rifie la valeur.
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
	
}
