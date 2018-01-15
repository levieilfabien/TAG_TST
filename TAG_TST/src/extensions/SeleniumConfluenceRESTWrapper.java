package extensions;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import beans.CasEssaiBean;
import beans.ObjectifBean;
import beans.RESTBean;
import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.alm.Base64Encoder;
import extensions.alm.Constants;
import extensions.alm.Response;
import extensions.alm.RestConnector;

public class SeleniumConfluenceRESTWrapper extends SeleniumALMRESTWrapper {

    /**
     * Effectue la pr�paration du wrapper � partir des informations de connexion.
     * @throws SeleniumException en cas d'erreur.
     */
    public void preparerWrapper(String url, String user, String password) throws SeleniumException {
    	initialisation(url);

    	// Si une connexion est �tablie (o� d�j� pr�sente)
    	try {
	    	if (!login(user, password)) {
	    		throw new SeleniumException(Erreurs.E036, "Le login � �chou�.");
	    	}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw new SeleniumException(Erreurs.E036, "La pr�paration du wrapper � �chou�e (" + ex.getMessage() + ")");
    	}
    }
    
    /**
     * Effectue le n�cessaire pour param�trer le connecteur.
     * @param serverUrl l'url vers le serveur
     */
    private void initialisation(final String serverUrl) {
    	setCon(RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl));
    }
    
    /**
     * D�connecte Confluence et supprime les cookies dans la session.
     * NB : Il n'y a pas de d�connexion avec confluence, cette fonction est donc sans effet.
     * @return true syst�matiquement
     * @throws Exception en cas d'erreur.
     */
    @Deprecated
    @Override
    public boolean deconnexion() throws Exception {
        return true;
    }

    /**
     * Effectue une connexion � partir des informations en param�tres.
     * Cette fonction utilise l'url de v�rification de connexion pour trouver l'url de login.
     * @param username le login utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return true si l'identification s'est effectu�e avec succ�s, false sinon.
     * @throws Exception en cas d'erreur.
     */
    @Override
    public boolean login(String username, String password) throws Exception {
    	//On utilise l'url contenue dans le fichier properties. Si celle ci n'est pas disponible on utilise celle par d�faut.
    	String url = RESTBean.URL_CONFLUENCE;
    	if (url == null || "".equals(url)) {
    		url = Constants.HOST_CONFLUENCE;
    	}

    	//Il faut acceder � un content ou une autre entit� pour pouvoir se logger.
    	if (url.endsWith("api")) {
    		url = url.concat("/content");
    	}
    	
    	return this.login(url, username, password);
    }
    
    /**
     * Effectue une connexion � partir des informations en param�tres.
     * @param loginUrl l'url de connexion
     * @param username le login utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return true si l'identification s'est effectu�e avec succ�s, false sinon.
     * @throws Exception en cas d'erreur.
     */
    public boolean login(String loginUrl, String username, String password) throws Exception {

    	// Configuration du proxy pour Confluence
    	System.setProperty("http.proxyUser", username);
    	System.setProperty("http.proxyPassword", password);
    	String proxyUrl = RESTBean.URL_PROXY;
    	if ("".equals(proxyUrl) || proxyUrl == null) {
    		proxyUrl = Constants.HOST_PROXY;
    	}
    	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl, 8080));
    	Authenticator.setDefault(new CustomAuthenticator());
        getCon().setProxy(proxy);
    	
        // Cr�ation de la chaine de connexion (encod�e)
        byte[] credBytes = (username + ":" + password).getBytes("UTF-8");
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

        // Enrichissement des ent�tes
        Map<String, String> map = new HashMap<String, String>();
        map.put("Username", username);
        map.put("Authorization", credEncodedString);

        // Connexion via un httpGet, on r�cup�re le code retour pour connaitre le statut de connexion.
        Response response = getCon().httpGet(loginUrl, /*"os_authType=basic"*/ null, map);
        
        //System.out.println(response.getStatusCode());
        
        boolean ret = response.getStatusCode() == HttpURLConnection.HTTP_OK;

        return ret;
    }
    
    /**
     * Renvoie une entit� dont on connais l'id ou les crit�re de recherche sous forme de texte (json)
     * Les crit�re d'ID et de QUERY ne sont pas mutuellement exclusif pour confluence.
     * @param id l'id de l'entite que l'on souhaites trouver 
     * @param query une requ�te permettant de trouver l'entit�
     * @param typeEntite le type d'entit� � r�cup�rer (ex : content)
     * @throws Exception en cas d'erreur.
     */
    public String obtenirEntiteConfluence(String id, String query, String typeEntite) throws Exception {
        // On pr�pare les ent�te pour la requ�te, en pr�cisant qu'on travaille en XML (normalement inutile, car d�j� param�tr�)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/json");
        // On pr�cise que le type d'entity � manipuler est un defect
        String resourceWeWantToRead = getCon().buildConfluenceEntityCollectionUrl(typeEntite);
        // On effectue la connexion vers l'entit� dont � fournit l'id est on r�cup�re la r�ponse.
        // Si il y a une requ�te on la prend en compte :
        String urlComplete = resourceWeWantToRead;
        if (id != null) {
        	urlComplete = urlComplete + "/" + id;
        } 
        if (query != null) {
        	urlComplete = urlComplete.concat("?" + query);
        }
        System.out.println(urlComplete);
        Response response = getCon().httpGet(urlComplete, null, requestHeaders);
        
        
        // On extrait l'entity de la r�ponse 
        String entity = response.toString();
        System.out.println(entity);
        
        return entity;
    }
    
    /**
     * Renvoie le titre d'une page confluence dont l'id est connu.
     * @param id l'id de la page confluence
     * @return le titre de la page confluence
     * @throws SeleniumException en cas d'erreur.
     */
    public String getTitrePageConfluence(String id) throws SeleniumException {
    	String titre = null;
    	try {
	    	//ETAPE 0 : Obtenir l'entit� � mettre � jour
	    	String json = obtenirEntiteConfluence(id, "expand=body.storage,version", "content");
	    	//ETAPE 1 : Obtenir la titre du document
	    	titre = (String) getJsonObject(json, "title");
	    	System.out.println("Titre :" + titre);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SeleniumException(Erreurs.E037, ex.getMessage());
		}
    	
    	return titre;
    }
    
    
    /**
     * Cette fonction effectue une mise � jour de contenu de page confluence simple.
     * @param id l'identifiant de la page confluence � mettre � jour
     * @param nouveauContenu le nouveau contenu (en fait la value du body storage) � utilis�, attention les guillements ne sont pas ajout� automatiquement.
     * @return la r�ponse de la requ�te de mise � jour
     * @throws SeleniumException en cas d'erreur.
     */
    public Response majContenuPageConfluence(String id, String nouveauContenu) throws SeleniumException {
    	return majContenuPageConfluence(id,nouveauContenu, null);
    }
    
    /**
     * Cette fonction effectue une mise � jour de contenu de page confluence simple.
     * @param id l'identifiant de la page confluence � mettre � jour
     * @param nouveauContenu le nouveau contenu (en fait la value du body storage) � utilis�, attention les guillements ne sont pas ajout� automatiquement.
     * @param nouveauTitre le nouveau titre (si il y a lieu, null sinon).
     * @return la r�ponse de la requ�te de mise � jour
     * @throws SeleniumException en cas d'erreur.
     */
    public Response majContenuPageConfluence(String id, String nouveauContenu, String nouveauTitre) throws SeleniumException {
    	String retour = "";
    	Response putResponse = null;
    	try {
	    	//ETAPE 0 : Obtenir l'entit� � mettre � jour
	    	String json = obtenirEntiteConfluence(id, "expand=body.storage,version", "content");
	    	//ETAPE 1 : Obtenir la version actuelle du document
	    	String version = (String) getJsonObject(json, "version#number");
	    	String newVersion = String.valueOf(Integer.parseInt(version) + 1);
	    	//ETAPE 2 : Obtenir la titre du document
	    	String titre = nouveauTitre;
	    	if (titre == null || "".equals(titre)) {
	    		titre = (String) getJsonObject(json, "title");
	    	}
	    	//System.out.println("Nouveau titre : " + titre);
	    	
	    	
	    	//ETAPE 3 : Produire le flux � envoyer en PUT
	    	retour = "{ \"version\": { \"number\": " +newVersion + " }, \"title\": " + titre + ", \"type\": \"page\", \"body\": {\"storage\": {\"value\": " + nouveauContenu + ",\"representation\": \"storage\"}}}";
	    	
	      	// On cr�er l'ent�te permettant le transfert de donn�es JSON.
	        Map<String, String> requestHeaders = new HashMap<String, String>();
	        requestHeaders.put("Content-Type", "application/json");
	        requestHeaders.put("Accept", "application/json");
	        // On effectue une requ�te de type "PUT" pour mettre � jour l'entit�.
	        String urlComplete = getCon().buildConfluenceEntityCollectionUrl("content") + "/" + id;
	        putResponse = getCon().httpPut(urlComplete, retour.getBytes(), requestHeaders);
	
//	        if (putResponse.getStatusCode() == HttpURLConnection.HTTP_MOVED_PERM) {
//	        	// On rencontre une erreur 301. Le chemin vers l'entit� � �t� modifi�e, on va suivre ce chemin
//	        	String newUri = putResponse.getResponseHeaders().get("Location").iterator().next();
//	        	// On r�tablie le header indiquant le type de contenu envoy�.
//	        	requestHeaders.put("Content-Type", "application/json");
//	        	putResponse = getCon().httpPut(newUri, retour.getBytes(), requestHeaders);
//	        }
    	
	        // Si on rencontre autre chose que le code retour OK et ce m�me apr�s avoir g�rer le code retour 301, c'est qu'on � une erreur.
	        if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
	        	System.out.println(putResponse.toString());
	            throw new Exception(putResponse.toString());
	        }
        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw new SeleniumException(Erreurs.E037, ex.getMessage());
    	}

        return putResponse;
    }
    
    /**
     * Permet de remplacer le contenu texte d'une balise json par un autre contenu texte.
     * @param json l'objet json
     * @param entiteARemplacer l'entit� � trouver dans le JSON au format objet#sousobjet#soussousobjet
     * @param remplacement la chaine de remplacement (attention, les guillements ne sont pas automatiquement ajout�s)
     * @return la chaine json apr�s remplacement.
     * @throws SeleniumException en cas d'erreur.
     */
    public String remplacerElementJSON(String json, String entiteARemplacer, String remplacement) throws SeleniumException {	
    	try {
    		// On extrait la partie texte � remplac�e et on la remplace dans la chaine globable du JSON.
    		String chaineARemplacer = (String) getJsonObject(json, entiteARemplacer);
    		return json.replace(chaineARemplacer, remplacement);
    	} catch (ClassCastException ex) {
    		// Si l'objet � remplacer n'est une chaine de caract�re mais un objet JSON on renvoie une erreur.
    		throw new SeleniumException(Erreurs.E037);
    	}    	
    }
    
    /**
     * Extrait le contenu d'un object JSON pour une entit� dont le chemin est connu.
     * Renvoie soit un objet JSON soit une chaine de caract�re suivant le d�gr� de prodondeur de la recherche.
     * @param json une chaine repr�sentant le JSON.
     * @param entite le chemin vers l'entit� s�par� par des # (ex : content#storage#value)
     * @return l'objet JSON ou la chaine de caract�re correspondant � la recherche.
     */
    public Object getJsonObject(String json, String entite) throws SeleniumException  {
    	Object retour = null;
    	JsonElement temp;
    	String[] entites = entite.split("#");
    	JsonObject  manipulateur = new JsonParser().parse(json).getAsJsonObject();
    	
    	for(String balise : entites) {
    		temp = manipulateur.get(balise);
    		
    		// Si le r�sultat de la recherche de la balise est un objet on continu, sinon on obtient une chaine de caract�re.
    		if (temp != null) {
	    		if(temp.isJsonObject()) {
	    			manipulateur = manipulateur.get(balise).getAsJsonObject();
	    			retour = manipulateur;
	    		} else {
	    			retour = temp.toString();
	    		}
    		} else {
    			throw new SeleniumException(Erreurs.E038, entite);
    		}
    	}
    	return retour;
    }
    
    /**
     * Effectue une mise � jour de la page confluence du cas de test � partir des infos d'�xecution.
     * Attention : le contenu actuel de la page sera effac� !
     * @param casEssai le cas d'essai dont on souhaites extraire les r�sultats dans Confluence.
     * @throws SeleniumException en cas d'erreur.
     */
 	public static void miseAJourConfluence(CasEssaiBean casEssai) throws SeleniumException {
 		System.out.println("Mise � jour page : " + casEssai.getIdConfluence()); 
 		if (casEssai.getIdConfluence() != null && !"".equals(casEssai.getIdConfluence())) {
	    	SeleniumConfluenceRESTWrapper wrapper = new SeleniumConfluenceRESTWrapper();
	    	wrapper.preparerWrapper(RESTBean.URL_CONFLUENCE, RESTBean.LOGIN_CONFLUENCE, RESTBean.PASSWORD_CONFLUENCE);
	    	String nouveauContenu = "";
	    	String environement = "";
	    	if (casEssai.getEnvironement() == null) {
	    		environement = RESTBean.ENVIRONEMENT;
	    	} else {
	    		environement = casEssai.getEnvironement().getEnvironement();
	    	}
	    	// On met � jour le titre avec l'�tat final du sc�nario
	    	String titre = wrapper.getTitrePageConfluence(casEssai.getIdConfluence());
	    	//System.out.println("Etat final : " + casEssai.getEtatFinal());
	    	if(casEssai.getEtatFinal()) {
		    	if(titre.contains("[KO]")) {
		    		titre = titre.replace("[KO]", "[OK]");
		    	} else if (!titre.contains("[OK]")) {
		    		titre = titre.substring(0, titre.lastIndexOf("\"")).concat(" [OK]\"");
		    	}
	 		} else {
		    	if(titre.contains("[OK]")) {
		    		titre = titre.replace("[OK]", "[KO]");
		    	} else if (!titre.contains("[KO]")) {
		    		titre = titre.substring(0, titre.lastIndexOf("\"")).concat(" [KO]\"");
		    	}
	 		}
	    	// On parcours les diff�rentes �tapes pour produire le nouveau contenu.
	    	for (CasEssaiBean casTest : casEssai.getTests()) {
	    		nouveauContenu = nouveauContenu.concat("<p><strong>"+wrapper.encode("Cas de test en " + environement + ": ") + wrapper.encode(casTest.getNomCasEssai() + " : " + casTest.getDescriptif()) + "</strong></p>");
				for (ObjectifBean step : casTest.getObjectifs().values()) {
					if (step.isStep()) {
						nouveauContenu = nouveauContenu.concat("<p><strong>Etape</strong> " + wrapper.encode(": " + step.getDescriptif()) + "</p><p> <strong>Attendu</strong> " + wrapper.encode(": " +step.getAttendu()) + " <strong>" + wrapper.encode("=>") + "</strong> ");
						if (step.getEtat() == null) {
							nouveauContenu = nouveauContenu.concat("<strong><span style=\\\"color: rgb(108,39,228);\\\">Not Run</span></strong></p>");
						} else if (step.getEtat()) {
							nouveauContenu = nouveauContenu.concat("<strong><span style=\\\"color: rgb(0,225,25);\\\">Passed</span></strong></p>");
						} else {
							nouveauContenu = nouveauContenu.concat("<strong><span style=\\\"color: rgb(255,0,0);\\\">Failed</span></strong></p>");
						}
					}
				}
	    	}
	    	//System.out.println(nouveauContenu);
	    	//System.out.println("Maj confluence en cours");
	    	// On mest � jour le contenu de la page confluence � partir des informations recueillies.
	    	wrapper.majContenuPageConfluence(casEssai.getIdConfluence(), "\"<p>" + nouveauContenu + "</p>\"", titre);
 		}
	    	
 	}
    
    
    public static void main(String[] args) throws Exception {
    	SeleniumConfluenceRESTWrapper wrapper = new SeleniumConfluenceRESTWrapper();
    	wrapper.preparerWrapper(Constants.HOST_CONFLUENCE, Constants.USERNAME, Constants.PASSWORD);
    	//String retour = wrapper.obtenirEntiteConfluence("68944055", "expand=body.storage,version", "content");
    	
    	//System.out.println(wrapper.remplacerElementJSON(retour, "body#storage#value", "\"<p>BODY TEST2</p>\""));
    	System.out.println(wrapper.majContenuPageConfluence("68944055", "\"<p>BODY TEST2</p>\""));

    }
    
    /**
     * Authentification du proxy pour l'acc�s � confluence.
     * Ne remplace pas l'authentification dans l'application elle m�me.
     * @author levieilfa
     *
     */
    public static class CustomAuthenticator extends Authenticator {
    	protected PasswordAuthentication getPasswordAuthentication() {
    		String prompt = getRequestingPrompt();
    		String hostname = getRequestingHost();
    		InetAddress ipaddr = getRequestingSite();
    		int port = getRequestingPort();
    		
    		// R�cup�ration des identifiants
    		String username = RESTBean.LOGIN_CONFLUENCE;
    		String password = RESTBean.PASSWORD_CONFLUENCE;
    		if ("".equals(username) || "".equals(password) || password == null || username == null) {
    			username = Constants.USERNAME;
    			password = Constants.PASSWORD;
    		}
    		
    		System.out.println("Informations : " + prompt + " ; " + hostname + " ; " + ipaddr + " ; " + port);
    		
    		return new PasswordAuthentication(username, password.toCharArray());
    	}
    }
}
