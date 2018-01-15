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
     * Effectue la préparation du wrapper à partir des informations de connexion.
     * @throws SeleniumException en cas d'erreur.
     */
    public void preparerWrapper(String url, String user, String password) throws SeleniumException {
    	initialisation(url);

    	// Si une connexion est établie (où déjà présente)
    	try {
	    	if (!login(user, password)) {
	    		throw new SeleniumException(Erreurs.E036, "Le login à échoué.");
	    	}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		throw new SeleniumException(Erreurs.E036, "La préparation du wrapper à échouée (" + ex.getMessage() + ")");
    	}
    }
    
    /**
     * Effectue le nécessaire pour paramètrer le connecteur.
     * @param serverUrl l'url vers le serveur
     */
    private void initialisation(final String serverUrl) {
    	setCon(RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl));
    }
    
    /**
     * Déconnecte Confluence et supprime les cookies dans la session.
     * NB : Il n'y a pas de déconnexion avec confluence, cette fonction est donc sans effet.
     * @return true systèmatiquement
     * @throws Exception en cas d'erreur.
     */
    @Deprecated
    @Override
    public boolean deconnexion() throws Exception {
        return true;
    }

    /**
     * Effectue une connexion à partir des informations en paramètres.
     * Cette fonction utilise l'url de vérification de connexion pour trouver l'url de login.
     * @param username le login utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return true si l'identification s'est effectuée avec succès, false sinon.
     * @throws Exception en cas d'erreur.
     */
    @Override
    public boolean login(String username, String password) throws Exception {
    	//On utilise l'url contenue dans le fichier properties. Si celle ci n'est pas disponible on utilise celle par défaut.
    	String url = RESTBean.URL_CONFLUENCE;
    	if (url == null || "".equals(url)) {
    		url = Constants.HOST_CONFLUENCE;
    	}

    	//Il faut acceder à un content ou une autre entité pour pouvoir se logger.
    	if (url.endsWith("api")) {
    		url = url.concat("/content");
    	}
    	
    	return this.login(url, username, password);
    }
    
    /**
     * Effectue une connexion à partir des informations en paramètres.
     * @param loginUrl l'url de connexion
     * @param username le login utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return true si l'identification s'est effectuée avec succès, false sinon.
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
    	
        // Création de la chaine de connexion (encodée)
        byte[] credBytes = (username + ":" + password).getBytes("UTF-8");
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

        // Enrichissement des entêtes
        Map<String, String> map = new HashMap<String, String>();
        map.put("Username", username);
        map.put("Authorization", credEncodedString);

        // Connexion via un httpGet, on récupère le code retour pour connaitre le statut de connexion.
        Response response = getCon().httpGet(loginUrl, /*"os_authType=basic"*/ null, map);
        
        //System.out.println(response.getStatusCode());
        
        boolean ret = response.getStatusCode() == HttpURLConnection.HTTP_OK;

        return ret;
    }
    
    /**
     * Renvoie une entité dont on connais l'id ou les critère de recherche sous forme de texte (json)
     * Les critère d'ID et de QUERY ne sont pas mutuellement exclusif pour confluence.
     * @param id l'id de l'entite que l'on souhaites trouver 
     * @param query une requête permettant de trouver l'entité
     * @param typeEntite le type d'entité à récupérer (ex : content)
     * @throws Exception en cas d'erreur.
     */
    public String obtenirEntiteConfluence(String id, String query, String typeEntite) throws Exception {
        // On prépare les entête pour la requête, en précisant qu'on travaille en XML (normalement inutile, car déjà paramètré)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/json");
        // On précise que le type d'entity à manipuler est un defect
        String resourceWeWantToRead = getCon().buildConfluenceEntityCollectionUrl(typeEntite);
        // On effectue la connexion vers l'entité dont à fournit l'id est on récupère la réponse.
        // Si il y a une requête on la prend en compte :
        String urlComplete = resourceWeWantToRead;
        if (id != null) {
        	urlComplete = urlComplete + "/" + id;
        } 
        if (query != null) {
        	urlComplete = urlComplete.concat("?" + query);
        }
        System.out.println(urlComplete);
        Response response = getCon().httpGet(urlComplete, null, requestHeaders);
        
        
        // On extrait l'entity de la réponse 
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
	    	//ETAPE 0 : Obtenir l'entité à mettre à jour
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
     * Cette fonction effectue une mise à jour de contenu de page confluence simple.
     * @param id l'identifiant de la page confluence à mettre à jour
     * @param nouveauContenu le nouveau contenu (en fait la value du body storage) à utilisé, attention les guillements ne sont pas ajouté automatiquement.
     * @return la réponse de la requête de mise à jour
     * @throws SeleniumException en cas d'erreur.
     */
    public Response majContenuPageConfluence(String id, String nouveauContenu) throws SeleniumException {
    	return majContenuPageConfluence(id,nouveauContenu, null);
    }
    
    /**
     * Cette fonction effectue une mise à jour de contenu de page confluence simple.
     * @param id l'identifiant de la page confluence à mettre à jour
     * @param nouveauContenu le nouveau contenu (en fait la value du body storage) à utilisé, attention les guillements ne sont pas ajouté automatiquement.
     * @param nouveauTitre le nouveau titre (si il y a lieu, null sinon).
     * @return la réponse de la requête de mise à jour
     * @throws SeleniumException en cas d'erreur.
     */
    public Response majContenuPageConfluence(String id, String nouveauContenu, String nouveauTitre) throws SeleniumException {
    	String retour = "";
    	Response putResponse = null;
    	try {
	    	//ETAPE 0 : Obtenir l'entité à mettre à jour
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
	    	
	    	
	    	//ETAPE 3 : Produire le flux à envoyer en PUT
	    	retour = "{ \"version\": { \"number\": " +newVersion + " }, \"title\": " + titre + ", \"type\": \"page\", \"body\": {\"storage\": {\"value\": " + nouveauContenu + ",\"representation\": \"storage\"}}}";
	    	
	      	// On créer l'entête permettant le transfert de données JSON.
	        Map<String, String> requestHeaders = new HashMap<String, String>();
	        requestHeaders.put("Content-Type", "application/json");
	        requestHeaders.put("Accept", "application/json");
	        // On effectue une requête de type "PUT" pour mettre à jour l'entité.
	        String urlComplete = getCon().buildConfluenceEntityCollectionUrl("content") + "/" + id;
	        putResponse = getCon().httpPut(urlComplete, retour.getBytes(), requestHeaders);
	
//	        if (putResponse.getStatusCode() == HttpURLConnection.HTTP_MOVED_PERM) {
//	        	// On rencontre une erreur 301. Le chemin vers l'entité à été modifiée, on va suivre ce chemin
//	        	String newUri = putResponse.getResponseHeaders().get("Location").iterator().next();
//	        	// On rétablie le header indiquant le type de contenu envoyé.
//	        	requestHeaders.put("Content-Type", "application/json");
//	        	putResponse = getCon().httpPut(newUri, retour.getBytes(), requestHeaders);
//	        }
    	
	        // Si on rencontre autre chose que le code retour OK et ce même après avoir gérer le code retour 301, c'est qu'on à une erreur.
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
     * @param entiteARemplacer l'entité à trouver dans le JSON au format objet#sousobjet#soussousobjet
     * @param remplacement la chaine de remplacement (attention, les guillements ne sont pas automatiquement ajoutés)
     * @return la chaine json après remplacement.
     * @throws SeleniumException en cas d'erreur.
     */
    public String remplacerElementJSON(String json, String entiteARemplacer, String remplacement) throws SeleniumException {	
    	try {
    		// On extrait la partie texte à remplacée et on la remplace dans la chaine globable du JSON.
    		String chaineARemplacer = (String) getJsonObject(json, entiteARemplacer);
    		return json.replace(chaineARemplacer, remplacement);
    	} catch (ClassCastException ex) {
    		// Si l'objet à remplacer n'est une chaine de caractère mais un objet JSON on renvoie une erreur.
    		throw new SeleniumException(Erreurs.E037);
    	}    	
    }
    
    /**
     * Extrait le contenu d'un object JSON pour une entité dont le chemin est connu.
     * Renvoie soit un objet JSON soit une chaine de caractère suivant le dégré de prodondeur de la recherche.
     * @param json une chaine représentant le JSON.
     * @param entite le chemin vers l'entité séparé par des # (ex : content#storage#value)
     * @return l'objet JSON ou la chaine de caractère correspondant à la recherche.
     */
    public Object getJsonObject(String json, String entite) throws SeleniumException  {
    	Object retour = null;
    	JsonElement temp;
    	String[] entites = entite.split("#");
    	JsonObject  manipulateur = new JsonParser().parse(json).getAsJsonObject();
    	
    	for(String balise : entites) {
    		temp = manipulateur.get(balise);
    		
    		// Si le résultat de la recherche de la balise est un objet on continu, sinon on obtient une chaine de caractère.
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
     * Effectue une mise à jour de la page confluence du cas de test à partir des infos d'éxecution.
     * Attention : le contenu actuel de la page sera effacé !
     * @param casEssai le cas d'essai dont on souhaites extraire les résultats dans Confluence.
     * @throws SeleniumException en cas d'erreur.
     */
 	public static void miseAJourConfluence(CasEssaiBean casEssai) throws SeleniumException {
 		System.out.println("Mise à jour page : " + casEssai.getIdConfluence()); 
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
	    	// On met à jour le titre avec l'état final du scénario
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
	    	// On parcours les différentes étapes pour produire le nouveau contenu.
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
	    	// On mest à jour le contenu de la page confluence à partir des informations recueillies.
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
     * Authentification du proxy pour l'accès à confluence.
     * Ne remplace pas l'authentification dans l'application elle même.
     * @author levieilfa
     *
     */
    public static class CustomAuthenticator extends Authenticator {
    	protected PasswordAuthentication getPasswordAuthentication() {
    		String prompt = getRequestingPrompt();
    		String hostname = getRequestingHost();
    		InetAddress ipaddr = getRequestingSite();
    		int port = getRequestingPort();
    		
    		// Récupération des identifiants
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
