package extensions;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import extensions.alm.Assert;
import extensions.alm.Base64Encoder;
import extensions.alm.Constants;
import extensions.alm.Entity;
import extensions.alm.Entity.Fields.Field;
import extensions.alm.EntityDescriptor;
import extensions.alm.EntityMarshallingUtils;
import extensions.alm.Response;
import extensions.alm.RestConnector;

public class SeleniumALMRESTWrapper {

	/**
	 * Connecteur REST.
	 */
    private RestConnector con;
	
    /**
     * Fonction de test pour tenter une connexion ALM.
     * @param args les arguments, non exploiter ici.
     * @throws Exception en cas d'erreur.
     */
    public static void main(String[] args) throws Exception {
    	
    	// On prépare l'URL. Si le port n'est pas précisé c'est qu'on en utilise pas.
    	String url = "https://" + Constants.HOST;
    	if (Constants.PORT != null) {
    		url = url.concat(":" + Constants.PORT);
    	}
    	url = url.concat("/qcbin");
    	
        //new SeleniumALMRESTWrapper().testConnexion(url, Constants.DOMAIN, Constants.PROJECT, Constants.USERNAME, Constants.PASSWORD);
    	
    	SeleniumALMRESTWrapper wrapper = new SeleniumALMRESTWrapper();
    	wrapper.initialisation(url, "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");

    	// Si une connexion est établie (où déjà présente)
    	if (wrapper.login(Constants.USERNAME, Constants.PASSWORD)) {
	    	try {
	    		wrapper.ouvrirSessionQC();
	    		//wrapper.obtenirDefect("15110");
//	        	run.ajouterChamp("name", "Run_TEST" + new Date().getTime());
//	        	run.ajouterChamp("testcycl-id", "417693");
//	        	run.ajouterChamp("test-id", "76408");
//	        	run.ajouterChamp("owner", "levieilfa");
//	        	run.ajouterChamp("subtype-id", "hp.qc.run.MANUAL");
//	        	run.ajouterChamp("status", "Passed");
	    		String url_run = wrapper.creerRun("Run_TEST", "417693", "76408", "levieilfa", true);
	    		
	    		System.out.println(url_run);
	    		
	    		String url_step = wrapper.creerStep("TESTSTEP", "TEST_DESC", "ACTUAL", "EXCEPTED", url_run, null, true);
	    		
	    		System.out.println(url_step);
	    		
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    		System.out.println("Impossible !");
	    	}
    	}
    	wrapper.logout();
    }

    
    /**
     * Effectue le nécessaire pour paramètrer le connecteur avec un domaine, un projet et une url vers ALM.
     * @param serverUrl l'url vers le serveur (finissant par /qcbin)
     * @param domain le domaine concernée
     * @param project le projet concerné
     * @throws Exception en cas d'erreur.
     */
    public void initialisation(final String serverUrl, final String domain, final String project) throws Exception {
    	con = RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl, domain, project);
    }
    
    /**
     * Ouvre une session vers ALM pour permettre des intérraction.
     * @throws Exception en cas d'erreur
     */
    public void ouvrirSessionQC() throws Exception {
    	con.getQCSession();
    }
    
    /**
     * Test une connexion et une déconnexion l'une après l'autre.
     * @param serverUrl l'url du serveur
     * @param domain le domaine
     * @param project le projet
     * @param username l'utilisateur (le login)
     * @param password le mot de passe
     * @throws Exception en cas d'erreur lors de la connexion.
     */
    public void testConnexion(final String serverUrl, final String domain, final String project, String username, String password) throws Exception {
    	
    	// On configure la connexion REST.
        RestConnector con = RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl, domain, project);
        // Initialisation de l'outil
        SeleniumALMRESTWrapper example = new SeleniumALMRESTWrapper();

        //On utilise une fonction nous indiquant si la connexion est active ou non. Si elle ne l'est pas on obtiens l'url de connexion.
        String authenticationPoint = example.obtenirURLConnexion();
        Assert.assertTrue("La connexion est déjà active. Impossible à cette étape.", authenticationPoint != null);

        //Une connexion s'effectue vers l'url renvoyer lors de l'étape précédente.
        boolean loginResponse = example.login(authenticationPoint, username, password);
        Assert.assertTrue("La connexion est impossible, mauvais mot de passe?", loginResponse);
        Assert.assertTrue("La connexion n'as pas creer l'indispensable Light Weight Single Sign On(LWSSO) cookie.", con.getCookieString().contains("LWSSO_COOKIE_KEY"));

        //Vérification de la connexion : la fonction isAuthenticated renvoie null.
        Assert.assertNull("La connexion à été perdue (où n'as jamais été établie).", example.obtenirURLConnexion());

        //On se déconnecte.
        example.logout();

        //Vérification de la déconnexion
        Assert.assertNotNull("La connexion est toujours active malgré la demande de déconnexion.", example.obtenirURLConnexion());
    }

	/**
	 * Constructeur par défaut de l'outil.
	 */
    public SeleniumALMRESTWrapper() {
        con = RestConnector.getInstance();
    }

    /**
     * Effectue une connexion à partir des informations en paramètres.
     * Cette fonction utilise l'url de vérification de connexion pour trouver l'url de login.
     * @param username le login utilisateur
     * @param password le mot de passe de l'utilisateur
     * @return true si l'identification s'est effectuée avec succès, false sinon.
     * @throws Exception en cas d'erreur.
     */
    public boolean login(String username, String password) throws Exception {

        String authenticationPoint = this.obtenirURLConnexion();
        if (authenticationPoint != null) {
            return this.login(authenticationPoint, username, password);
        }
        return true;
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

        // Création de la chaine de connexion (encodée)
        byte[] credBytes = (username + ":" + password).getBytes();
        String credEncodedString = "Basic " + Base64Encoder.encode(credBytes);

        System.out.println("LoginURL : " + loginUrl);
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("Authorization", credEncodedString);

        // Connexion via un httpGet, on récupère le code retour pour connaitre le statut de connexion.
        Response response = con.httpGet(loginUrl, null, map);
        boolean ret = response.getStatusCode() == HttpURLConnection.HTTP_OK;

        return ret;
    }

    /**
     * Déconnecte ALM et supprime les cookies dans la session.
     * @return true si la déconnexion à fonctionée, false sinon
     * @throws Exception en cas d'erreur.
     */
    public boolean logout() throws Exception {

	    //note the get operation logs us out by setting authentication cookies to:
	    // LWSSO_COOKIE_KEY="" via server response header Set-Cookie
        Response response = con.httpGet(con.buildUrl("authentication-point/logout"), null, null);

        return (response.getStatusCode() == HttpURLConnection.HTTP_OK);
    }

    /**
     * Appel l'url de vérification de connexion à l'API REST d'ALM.
     * Renvoie null si la connexion est effective, l'url de connexion sinon.
     * @return null si identifié.<br>
     *         l'url d'identification sinon
     * @throws Exception en cas d'erreur.
     */
    public String obtenirURLConnexion() throws Exception {

        String isAuthenticateUrl = con.buildUrl("rest/is-authenticated");
        String ret;
        
        //https://hpalm.intranatixis.com/qcbin/rest/is-authenticated
        
        Response response = con.httpGet(isAuthenticateUrl, null, null);
        int responseCode = response.getStatusCode();

        //if already authenticated
        if (responseCode == HttpURLConnection.HTTP_OK) {
            ret = null;
        }

        //if not authenticated - get the address where to authenticate
        // via WWW-Authenticate
        else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {

            Iterable<String> authenticationHeader = response.getResponseHeaders().get("WWW-Authenticate");

            String newUrl = authenticationHeader.iterator().next().split("=")[1];
            newUrl = newUrl.replace("\"", "");
            newUrl += "/authenticate";
            ret = newUrl;
        	
        } else {
        	//Not ok, not unauthorized. An error, such as 404, or 500
            throw response.getFailure();
        }
        
        //TODO à positioner sous forme d'options de la fonction d'identification.
        
        // La connexion s'effectue volontier en HTTPS si il y a présence d'un certificats (ex : SSL)
        if (ret != null && !ret.contains("https")) {
        	ret = ret.replace("http", "https");
        }
        
        // Si on utilise une connexion https, il ne faut pas spécifier le port
        if (ret != null  && Constants.PORT == null && ret.contains(":80")) {
        	ret = ret.replace(":80", "");
        }

        return ret;
    }
    
    public void obtenirDefect(String id) throws Exception {
        //String query = "query={project[IZIVENTE];detected-by[doublibounouais OR mongenetla OR lottena];name[V14.03*];user-06[*CE* OR *BP*];id[6511]}";
        //String query = "query={id["+id+"]}";
        
    	// On prépare les entête pour la requête, en précisant qu'on travaille en XML (normalement inutile, car déjà paramètré)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");
        // On précise que le type d'entity à manipuler est un defect
        String resourceWeWantToRead = con.buildEntityCollectionUrl("defect");
        // On effectue la connexion vers le defect dont à fournit l'id est on récupère la réponse.
        String responseStr = con.httpGet(resourceWeWantToRead + "/" + id, null, requestHeaders).toString();
        // On extrait de la réponse l'entity (en l'occurence une QC/defect)
        Entity entity = EntityMarshallingUtils.marshal(Entity.class, responseStr);
        
        // On va parcourir l'ensemble des champs renvoyés
        List<Field> fields = entity.getFields().getField();
        System.out.print("listing fields from marshalled object: ");
        for (Field field : fields) {
            System.out.print(field.getName() + "=" + field.getValue() + ", ");
        }
    }
    
    /**
     * Effectue la création d'un RUN en fonction des paramètres demandés.
     * @param nomRun Le nom à appliquer au RUN, sera concaténé à un timestamp
     * @param idCycle L'id du cycle de rattachement dans le Test Lab (attention non visible dans l'IHM)
     * @param idTest L'id du cas de test dans le Test Plan (Test ID).
     * @param propretaire le propriétaire de l'éxécution.
     * @param etat true si l'éxécution est à Passed, false sinon.
     * @return l'url de consultation du RUN.
     */
    public String creerRun(String nomRun, String idCycle, String idTest, String propretaire, Boolean etat) {
    	Entity run = new Entity();   	
    	// On précise le type d'entité
    	run.setType("run");
    	// On renseigne les champs obligatoire
    	run.ajouterChamp("name", nomRun + "_" + new Date().getTime());
    	run.ajouterChamp("testcycl-id", idCycle);
    	run.ajouterChamp("test-id", idTest);
    	run.ajouterChamp("owner", propretaire);
    	run.ajouterChamp("subtype-id", "hp.qc.run.MANUAL");
    	run.ajouterChamp("status", etat?"Passed":"Failed");
    	// On prépare les paramètres de la création
    	String chaine = convertirEntiteEnChaine(run);
    	String resourceWeWantToWrite = con.buildEntityCollectionUrl("run");
    	//System.out.println(chaine);
    	try {
			return creationEntite(resourceWeWantToWrite, chaine);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    /**
     * Permet de créer une step pour un run donné.
     * @param nom nom du step
     * @param description la description de l'étape
     * @param actual l'attendu lors de l'étape
     * @param expected le constaté lors de l'étape
     * @param idRun l'identifiant du run, ou l'url d'accès au run
     * @param idTest l'identifiant du test (facultatif, mais nécessaire pour le bon affichage dans ALM)
     * @param etat true si Passed, false si Failed.
     * @return l'url vers le step créee.
     */
    public String creerStep(String nom, String description, String actual, String expected, String idRun, String idTest, Boolean etat) {
    	//<Qc URL>/qcbin/rest/domains/<Domain>/projects/<project>/design-steps?query={parent-id[<test_id>]}
    	//https://hpalm.intranatixis.com/qcbin/rest/domains/NATIXIS_FINANCEMENT/projects/CREDIT_CONSOMMATION/design-steps
    	//https://hpalm.intranatixis.com/qcbin/rest/domains/NATIXIS_FINANCEMENT/projects/CREDIT_CONSOMMATION/customization/entities/run-step/fields?required=true
    	//https://hpalm.intranatixis.com/qcbin/rest/domains/NATIXIS_FINANCEMENT/projects/CREDIT_CONSOMMATION/runs/183539/run-steps
    		
    	Entity step = new Entity();   	
    	// On précise le type d'entité
    	step.setType("run-step");
    	//ST_STEP_NAME - name
    	step.ajouterChamp("name", nom);
    	//ST_STATUS - status
    	step.ajouterChamp("status", etat?"Passed":"Failed");
    	//ST_DESCRIPTION - description
    	step.ajouterChamp("description", description);
    	//ST_ACTUAL - actual
    	step.ajouterChamp("actual", actual);
    	//ST_EXPECTED - expected
    	step.ajouterChamp("expected", expected);
    	// test-id (ex: 18475) --> Le même que celui du RUN, c'est l'id du test parent
    	if (idTest != null) {
    		step.ajouterChamp("test-id", idTest);
    	}
    	// parent-id (ex: 23115) --> L'Id du Run, champ obligatoire
    	if (idRun.startsWith("http")) {
    		// Si on à fournit l'url on coupe celle ci pour ne prendre que la dernière valeur.
    		step.ajouterChamp("parent-id", idRun.substring(idRun.lastIndexOf("/") + 1));
    	} else {
    		step.ajouterChamp("parent-id", idRun);
    	}
    	
    	// On prépare les paramètres de la création
    	String chaine = convertirEntiteEnChaine(step);
    	String resourceWeWantToWrite = con.buildEntityCollectionUrl("run-step");
    	//System.out.println(chaine);
    	try {
			return creationEntite(resourceWeWantToWrite, chaine);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    	
    	// step-order
    	// desstep-id (ex : 35259)
    	
    }
    
    /**
     * Permet la création d'une entity à partir d'une URL de collection et d'un flux XML.
     * @param collectionUrl l'url vers la collection à alimentée
     * @param postedEntityXml le flux XML à transmettre à ALM pour créer la nouvelle entité
     * @return l'url permettant la consultation de la nouvelle entité
     * @exception Exception en cas d'erreur.
     */
    public String creationEntite(String collectionUrl, String postedEntityXml) throws Exception {
    	// On initialise les entêtes avec les informations sur le contenu (XML)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");

        // On effectue une requête "Post" à partir des bytes composant le XML et les headers précisant le contenu XML.
        Response response = con.httpPost(collectionUrl, postedEntityXml.getBytes(), requestHeaders);

        // On génère une exception en cas d'erreur lors de la connexion (404, 400, 500). 
        Exception failure = response.getFailure();
        if (failure != null) {
            throw failure;
        }

        // On récupère depuis la réponse l'url de consultation de l'objet créer
        return response.getResponseHeaders().get("Location").iterator().next();
    }
    
    /**
     * Permet la suppression côté serveur d'une entité spécifiée.
     * @param entityUrl l'url vers l'entité à supprimer.
     * @return la représentation XML de l'objet supprimé.
     */
    public String deleteEntity(String entityUrl) throws Exception {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");

        Response serverResponse = con.httpDelete(entityUrl, requestHeaders);
        if (serverResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception(serverResponse.toString());
        }

        return serverResponse.toString();
    }
    
    /**
     * Génère une chaine de caractère représentant le Field désigné.
     * @param field le nom du champs
     * @param value une valeur de champ.
     * @return la chaine de caractère.
     */
    public static String convertirChampEnChaine(String field, String value) {
    	List<String> values = new ArrayList<String>();
    	values.add(value);
    	return convertirChampEnChaine(field, values);
    }
    
    /**
     * Génère une chaine de caractère représentant le Field désigné.
     * @param field le nom du champs
     * @param values les valeurs associée à ce champ.
     * @return la chaine de caractère.
     */
    public static String convertirChampEnChaine(String field, List<String> values) {
 	   // On initialise avec le nom du champ.
        String retour = "<Field Name=\"" + field + "\">";
        // Pour chaque value on rajoute une valeur
        for (String value : values) {
     	   retour = retour.concat("<Value>" + value + "</Value>");
        }
        // On ferme la balise.
        retour = retour.concat("</Field>");
        return retour;
    }
    
    /**
     * Génère une chaine de caracètre représentant l'entité paramètre.
     * @param entity l'entity dont on souhaites avoir la représentation sous forme de chaine.
     * @return la chaine résultante.
     */
    public static String convertirEntiteEnChaine(Entity entity) {
 	   String retour = "<Entity Type=\"" + entity.getType() + "\"> <Fields>";
 	   // Pour chaque champ, on ajoute la conversion en chaine correspondante
 	   for (Field champ : entity.getFields().getField()) {
 		   retour = retour.concat(convertirChampEnChaine(champ.getName(), champ.getValue()));
 	   }
 	   // On cloture le flux XML
 	   retour = retour.concat("</Fields></Entity>");
 	   return retour;
    }
    
    /**
     * Permet de savoir une entity accepte le versionning où non.
     * @param entityType le type d'entité à vérifier.
     * @param domain le domaine concerné
     * @param project le projet concerné
     * @return true si l'entity supporte le versionning, false sinon
     * @throws Exception en cas d'erreur.
     */
     public static boolean isVersioned(String entityType, final String domain, final String project) throws Exception {
    	 // Initilisation du connecteur pour accèder au éléments de customization.
         RestConnector con = RestConnector.getInstance();
         String descriptorUrl = con.buildUrl("rest/domains/"
                  + domain
                  + "/projects/"
                  + project
                  + "/customization/entities/"
                  + entityType);
         // Récupération du descriptif XML de l'entity demandée.
         String descriptorXml = con.httpGet(descriptorUrl, null, null).toString();
         // Extraction du descripteur à partir du fichier XML, et récupération de la valeur associé à l'attribut de versionning.
         EntityDescriptor descriptor = EntityMarshallingUtils.marshal(EntityDescriptor.class, descriptorXml);
         return descriptor.getSupportsVC().getValue();
     }
     
     /**
      * Permet d'obtenir la description sous forme de chaine d'une entité qu'on à extrait (checkout).
      * @param entityUrl l'url vers l'entité à extraire
      * @param comment le commentaire à laisser au serveur lorsqu'on extrait l'entité
      * @param version la version à obtenir de l'entité (à -1 pour obtenir la dernière version).
      * @return une chaine de caractère représentant l'entité extraite.
      * @throws Exception en cas d'erreur.
      */
     public String checkout(String entityUrl, String comment, int version) throws Exception {

    	 // On transforme le commentaire et la version en entrées XML pour les manipuler.
         String commentXmlBit = ((comment != null) && !comment.isEmpty() ? "<Comment>" + comment + "</Comment>" : "");
         String versionXmlBit = (version >= 0 ? "<Version>" + version + "</Version>" : "");
         // On créer les données à manipuler à partir du commentaire et de la version.
         String xmlData = commentXmlBit + versionXmlBit;
         String xml = xmlData.isEmpty() ? "" : "<CheckOutParameters>" + xmlData + "</CheckOutParameters>";
         // On créer l'entête permettant le transfert de données XML.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "application/xml");
         requestHeaders.put("Accept", "application/xml");

         // On appel la fonction HTTP POST pour transmettre le flux XML de check Out et obtenir l'objet en retour.
         Response response = con.httpPost(entityUrl + "/versions/check-out", xml.getBytes(), requestHeaders);

         if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(response.toString());
         }

         // La chaine représentant l'entité demandée.
         return response.toString();
     }

     /**
      * Opération inverse du checkout permettant d'écraser une version d'une entité avec une nouvelle version.
      * @param entityUrl l'entité que l'on cherche à mettre à jour
      * @param comment le commentaire pour remplacer le commentaire laissé lors du checkout
      * @param overrideLastVersion écrase la dernière version présente
      * @return true si l'opération c'est bien passée, false sinon.
      * @throws Exception en cas d'erreur.
      */
     public boolean checkin(String entityUrl, String comment, boolean overrideLastVersion) throws Exception {

    	// On transforme le commentaire et la version en entrées XML pour les manipuler.
         final String commentXmlBit = ((comment != null) && !comment.isEmpty() ? "<Comment>" + comment + "</Comment>" : "");
         final String overrideLastVersionBit = overrideLastVersion == true ? "<OverrideLastVersion>true</OverrideLastVersion>" : "" ;
         // On créer les données à manipuler à partir du commentaire et de la version.
         final String xmlData = commentXmlBit + overrideLastVersionBit;
         final String xml = xmlData.isEmpty() ? "" : "<CheckInParameters>" + xmlData + "</CheckInParameters>";
         // On créer l'entête permettant le transfert de données XML.
         final Map<String, String> requestHeaders =
                 new HashMap<String, String>();
         requestHeaders.put("Content-Type", "application/xml");

         //Effectue l'opération de checkin pour l'entité.
         Response response = con.httpPost(entityUrl + "/versions/check-in", xml.getBytes(), requestHeaders);

         return response.getStatusCode() == HttpURLConnection.HTTP_OK;
     }

     /**
      * Permet de vérouiller une entité.
      * @param entityUrl l'url vers l'entité à vérouillée.
      * @return l'entité vérouillée.
      * @throws Exception en cas d'erreur.
      */
     public String lock(String entityUrl) throws Exception {
    	 // On créer l'entête permettant le transfert de données XML.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Accept", "application/xml");
         // On effectue la pose du verrou sur l'entité.
         Response lockResponse = con.httpPost(entityUrl + "/lock", null, requestHeaders);
         if (lockResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(lockResponse.toString());
         }
         return lockResponse.toString();
     }

     /**
      * Permet de dévérouiller une entité précédement vérouillée.
      * @param entityUrl l'url vers l'entité à dévérouillée.
      * @return true si l'entité est dévérouillée suite à l'opération, false sinon.
      * @throws Exception en cas d'erreur.
      */
     public boolean unlock(String entityUrl) throws Exception {
         return con.httpDelete(entityUrl + "/lock", null).getStatusCode() == HttpURLConnection.HTTP_OK;
     }
     
     /**
      * Effectue une mise à jour sur l'entité paramètre. Seuls les champs mentionné dans le XML seront mis à jour.
      * @param entityUrl l'url vers l'entité à mettre à jour.
      * @param updatedEntityXml description de l'entité sous format XML. Les champs présents seront mis à jour.
      * @return la description XML de l'entité après l'étape de mise à jour.
      * @throws Exception en cas d'erreur.
      */
     private Response update(String entityUrl, String updatedEntityXml) throws Exception {
    	 // On créer l'entête permettant le transfert de données XML.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "application/xml");
         requestHeaders.put("Accept", "application/xml");
         // On effectue une requête de type "PUT" pour mettre à jour l'entité.
         Response putResponse = con.httpPut(entityUrl, updatedEntityXml.getBytes(), requestHeaders);

         if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(putResponse.toString());
         }

         return putResponse;
     }
     
     
     /**
      * Permet de mettre à jour une PJ de l'entité en paramètre.
      * @param entityUrl l'url vers l'entité sur laquelle on met à jour les attachements. 
      * @param bytes les données qui remplacent les anciennes données.
      * @param attachmentFileName le nom de fichier à donné dans le serveur.
      * @return la représentation sous forme de chaine de la réponse du serveur.
      */
     private String updateAttachmentData(String entityUrl, byte[] bytes, String attachmentFileName) throws Exception {
         // On créer l'entête permettant le transfert de données XML, mais sous forme de données
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "application/octet-stream");
         requestHeaders.put("Accept", "application/xml");
         // On effectue une requête "PUT" pour mettre à jour les informations d'attachement de l'entité avec les bytes de l'objet.
         Response putResponse = con.httpPut(entityUrl + "/attachments/" + attachmentFileName, bytes, requestHeaders);

         if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(putResponse.toString());
         }
         byte[] ret = putResponse.getResponseData();

         return new String(ret);
     }

     /**
      * Permet de mettre à jour la description d'une PJ connue d'une entité.
      * @param entityUrl l'url vers l'entité dont on veux mettre à jour la descrption de l'attachement.
      * @param description la description à positionner sur l'attachement.
      * @param attachmentFileName le nom de l'attachement côté serveur dont on souhaites mettre à jour la description.
      * @return la représentation sous forme de chaine de la réponse du serveur.
      */
     private String updateAttachmentDescription(String entityUrl, String description, String attachmentFileName) throws Exception {
    	 // On créer l'entête permettant le transfert de données XML, mais sous forme de données
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "application/xml");
         requestHeaders.put("Accept", "application/xml");
         
         //TODO remplacer par un appel au convertisseur.
         Response putResponse = con.httpPut(entityUrl + "/attachments/" + attachmentFileName, ("<Entity Type=\"attachment\"><Fields><Field Name=\"description\"><Value>" + description + "</Value></Field></Fields></Entity>").getBytes(), requestHeaders);

         if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(putResponse.toString());
         }

         byte[] ret = putResponse.getResponseData();
         return new String(ret);
     }
     

     /**
      * Permet d'obtenir le XML des metadata de l'attachement spécifié.
      * @param attachmentUrl l'url vers l'entité attachement
      * @return le XML représentant l'attachement spécifié.
      * @throws Exception en cas d'erreur.
      */
     private String readAttachmentDetails(String attachmentUrl) throws Exception {
    	 // Gestion des headers XML
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Accept", "application/xml");
         // On récupère les information de l'attachement avec une requête "Get"
         Response readResponse = con.httpGet(attachmentUrl, null, requestHeaders);

         if (readResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(readResponse.toString());
         }
         return readResponse.toString();
     }

     /**
      * Permet d'obtenir les données qui compose l'attachement spécifié (en fait le contenu en byte du document).
      * @param attachmentUrl l'url vers l'attachement concerné.
      * @return le contenu du fichier en PJ.
      * @throws Exception en cas d'erreur.
      */
     private byte[] readAttachmentData(String attachmentUrl) throws Exception {
    	// Gestion des headers XML
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Accept", "application/octet-stream");
         // On récupère les information de l'attachement avec une requête "Get"
         Response readResponse = con.httpGet(attachmentUrl, null, requestHeaders);

         if (readResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(readResponse.toString());
         }
         return readResponse.getResponseData();
     }

     /**
      * Permet d'obtenir un XML représentant l'ensemble des PJ de l'entité.
      * @param entityUrl l'url vers l'entité dont on souhaites les PJ
      * @return un XML représentant tous les attachement de l'entité paramètre.
      * @throws Exception en cas d'erreur.
      */
     private String readAttachments(String entityUrl) throws Exception {
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Accept", "application/xml");

         Response readResponse = con.httpGet(entityUrl + "/attachments", null, requestHeaders);
         if (readResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
             throw new Exception(readResponse.toString());
         }
         return readResponse.toString();
     }

     /**
      * Permet de rattacher à une entité une nouvelle PJ sous forme de données (byte[]).
      * @param entityUrl l'url vers l'entité sur laquelle on ajoute une PJ.
      * @param fileData les données qui composent le fichier.
      * @param filename le nom de fichier à utilisé.
      * @return l'url vers l'attachement ainsi créer.
      */
     private String attachWithOctetStream(String entityUrl, byte[] fileData, String filename) throws Exception {

         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Slug", filename);
         requestHeaders.put("Content-Type", "application/octet-stream");

         Response response = con.httpPost(entityUrl + "/attachments", fileData, requestHeaders);

         if (response.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
             throw new Exception(response.toString());
         }
         return response.getResponseHeaders().get("Location").iterator().next();
     }
     
     /**
      * Permet l'ajout de PJ en passant par la description de plusieurs content Type (nomn, description, data) d'une PJ à une entité.
      * @param entityUrl l'url vers l'entité à laquelle ajoutée la PJ.
      * @param fileData le contenu du fichier (en byte[])
      * @param contentType le contentType du contenu du fichier (ex : txt/html or xml, or octetstream etc..)
      * @param filename le nom à donné à la PJ.
      * @return l'url vers l'entité d'attachement créer à partir de la PJ.
      */
     private String attachWithMultipart(String entityUrl, byte[] fileData, String contentType, String filename, String description) throws Exception {
         /*
			 headers:
			 Content-Type: multipart/form-data; boundary=<boundary>
			
			 //template for file mime part:
			 --<boundary>\r\n
			 Content-Disposition: form-data; name="<fieldName>"; filename="<filename>"\r\n
			 Content-Type: <mime-type>\r\n
			 \r\n
			 <file-data>\r\n
			 <boundary>--
			
			 //template for post parameter mime part, such as description and/or filename:
			 --<boundary>\r\n
			     Content-Disposition: form-data; name="<fieldName>"\r\n
			     \r\n
			     <value>\r\n
			 <boundary>--
			
			 //end of parts:
			 --<boundary>--
			
			 we need 3 parts:
			 filename(template for parameter), description(template for parameter),
			 and file data(template for file).
          */

         // This can be pretty much any string.
         // It's used to mark the different mime parts
         String boundary = "exampleboundary";

         //template to use when sending field data (assuming none-binary data)
         String fieldTemplate =
                 "--%1$s\r\n"
                         + "Content-Disposition: form-data; name=\"%2$s\" \r\n\r\n"
                         + "%3$s"
                         + "\r\n";

         // Template to use when sending file data.
         // Binary data still needs to be suffixed.
         String fileDataPrefixTemplate =
                 "--%1$s\r\n"
                         + "Content-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\n"
                         + "Content-Type: %4$s\r\n\r\n";

         // On génère les partie de XML décrivant le nom, la description, et le contenu du fichier
         String filenameData = String.format(fieldTemplate, boundary, "filename", filename);
         String descriptionData = String.format(fieldTemplate, boundary, "description", description);
         String fileDataSuffix = "\r\n--" + boundary + "--";
         String fileDataPrefix = String.format(fileDataPrefixTemplate, boundary, "file", filename, contentType);

         // On écrit toujours le Filename et la description avant les données.
         // Les valeur "name" et "filename" dans les flux doivent être les mêmes.
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         bytes.write(filenameData.getBytes());
         bytes.write(descriptionData.getBytes());
         bytes.write(fileDataPrefix.getBytes());
         bytes.write(fileData);
         bytes.write(fileDataSuffix.getBytes());
         bytes.close();

         // On spécifie les entête en précisant quele contenu est constitué de plusieurs parties.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "multipart/form-data; boundary=" + boundary);

         Response response = con.httpPost(entityUrl + "/attachments", bytes.toByteArray(), requestHeaders);
         if (response.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
             throw new Exception(response.toString());
         }

         return response.getResponseHeaders().get("Location").iterator().next();
     }

     
     public void attachmentsExample(final String urlEntite, String domain, String project) throws Exception {

    	 //AuthenticateLoginLogoutExample login = new AuthenticateLoginLogoutExample();
    	 //CreateDeleteExample writeExample = new CreateDeleteExample();
    	 
    	 // Nous avons déjà créer un objet, c'est à cet objet qu'on souhaite ajouter une pièce jointe
    	 
    	 //AttachmentsExample example = new AttachmentsExample();

    	 // Avant de modifier une entité on la vérouille si elle est soumise au versioning, sinon on l'extrait (checkout)
    	 boolean isVersioned = isVersioned("requirement", domain, project);
    	 
    	 String preModificationXml = null;
    	 if (isVersioned) {
    		 // Cette entité supporte le versionning, on effectue une opération de "checkout".
    		 String firstCheckoutComment = "check out comment1";
    		 preModificationXml = checkout(urlEntite, firstCheckoutComment, -1);
    		 Assert.assertTrue("checkout comment missing", preModificationXml.contains(convertirChampEnChaine("vc-checkout-comments", firstCheckoutComment)));
    	 } else {
    		 // Si l'entité ne supporte pas le versionning on passe par une pose de verrou.
    		 preModificationXml = lock(urlEntite);
    	 }

    	 //Assert.assertTrue("posted field value not found", preModificationXml.contains(Constants.entityToPostFieldXml));

    	 //The file names to use on the server side
    	 String multipartFileName = "multiPartFileName.txt";
    	 String octetStreamFileName = "octetStreamFileName.txt";

    	 // Attach the file data to entity
    	 String multipartFileDescription = "some random description";
    	 String octetstreamFileContent = "a completely different file";
    	 String multipartFileContent = "content of file";

    	 String newMultiPartAttachmentUrl =
    			 attachWithMultipart(
    					 urlEntite,
    					 multipartFileContent.getBytes(),
    					 "text/plain",
    					 multipartFileName,
    					 multipartFileDescription);

    	 String newOctetStreamAttachmentUrl = attachWithOctetStream(urlEntite, octetstreamFileContent.getBytes(), octetStreamFileName);

    	 // Changes aren't visible to other users until we check them
    	 //  in if versioned
    	 if (isVersioned) {
    		 String firstCheckinComment = "check in comment1";
    		 boolean checkin = checkin(urlEntite, firstCheckinComment, false);
    		 Assert.assertTrue("checkin failed", checkin);
    	 } else {
    		 boolean unlock = unlock(urlEntite);
    		 Assert.assertTrue("unlock failed", unlock);
    	 }

    	 //read the data and it's metadata back from the server
    	 String readAttachments = readAttachments(urlEntite);
    	 Assert.assertTrue("multipart attachment description missing", readAttachments.contains(convertirChampEnChaine("description", multipartFileDescription)));
    	 //Assert.assertTrue("attachment count incorrect or missing", readAttachments.contains("<Entities TotalResults=\"2\">"));

    	 byte[] readAttachmentData = readAttachmentData(newOctetStreamAttachmentUrl);
    	 String readAttachmentsString = new String(readAttachmentData);
    	 Assert.assertEquals("uploaded octet stream file content differs from read file content", readAttachmentsString, octetstreamFileContent);

    	 readAttachmentData = readAttachmentData(newMultiPartAttachmentUrl);
    	 readAttachmentsString = new String(readAttachmentData);
    	 Assert.assertEquals("uploaded multipart stream file content differs from read file content", readAttachmentsString, multipartFileContent);

    	 String readAttachmentDetails = readAttachmentDetails(newMultiPartAttachmentUrl);
    	 Assert.assertTrue("multipart attachment description missing", readAttachmentDetails.contains(convertirChampEnChaine("description", multipartFileDescription)));

    	 //again with the checkout checkin procedure
    	 if (isVersioned) {
    		 // Note that we selected an entity that supports versioning
    		 // on a project that supports versioning. Would fail otherwise.
    		 String firstCheckoutComment = "check out comment1";
    		 preModificationXml = checkout(urlEntite, firstCheckoutComment, -1);
    		 Assert.assertTrue("checkout comment missing", preModificationXml.contains(convertirChampEnChaine("vc-checkout-comments", firstCheckoutComment)));
    	 } else {
    		 preModificationXml = lock(urlEntite);
    	 }

    	 //Assert.assertTrue("posted field value not found", preModificationXml.contains(Constants.entityToPostFieldXml));

    	 //update data of file
    	 String updatedOctetStreamFileData = "updated file contents";
    	 String updatedOctetstreamFileDescription = "completely new description";

    	 updateAttachmentData(urlEntite,
    			 updatedOctetStreamFileData.getBytes(),
    			 octetStreamFileName);

    	 readAttachmentsString = new String(readAttachmentData(newOctetStreamAttachmentUrl));
    	 Assert.assertEquals("updated octet stream data not changed", updatedOctetStreamFileData, readAttachmentsString);

    	 //update description of file
    	 String attachmentMetadataUpdateResponseXml =
    			 updateAttachmentDescription(urlEntite, updatedOctetstreamFileDescription, octetStreamFileName);

    	 Assert.assertTrue("updated octet stream description not changed", attachmentMetadataUpdateResponseXml.contains(updatedOctetstreamFileDescription));

    	 //checkin
    	 if (isVersioned) {
    		 final String firstCheckinComment = "check in comment1";
    		 boolean checkin = checkin(urlEntite, firstCheckinComment, false);
    		 Assert.assertTrue("checkin failed", checkin);
    	 } else {
    		 boolean unlock = unlock(urlEntite);
    		 Assert.assertTrue("unlock failed", unlock);
    	 }

    	 //cleanup

    	 //check out attachment owner
    	 if (isVersioned) {
    		 checkout(urlEntite, "", -1);
    	 } else {
    		 lock(urlEntite);
    	 }

    	 //delete attachments
    	 deleteEntity(newOctetStreamAttachmentUrl);
    	 deleteEntity(newMultiPartAttachmentUrl);

    	 //checkin attachment owner
    	 if (isVersioned) {
    		 checkin(urlEntite, "", false);
    	 } else {
    		 unlock(urlEntite);
    	 }

    	 //delete attachment owner
    	 deleteEntity(urlEntite);
    	 logout();
     }
}
