package extensions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.io.Files;

import beans.CasEssaiBean;
import beans.ObjectifBean;
import beans.RESTBean;
import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.alm.Assert;
import extensions.alm.Base64Encoder;
import extensions.alm.Constants;
import extensions.alm.Entities;
import extensions.alm.Entity;
import extensions.alm.Entity.Fields.Field;
import extensions.alm.EntityDescriptor;
import extensions.alm.EntityMarshallingUtils;
import extensions.alm.Response;
import extensions.alm.RestConnector;
import outils.XLSOutils;

public class SeleniumALMRESTWrapper {

	/**
	 * Connecteur REST.
	 */
    private RestConnector con;
    
    /**
     * Encodeur HEXA pour les contenu HTML.
     */
    public static final CharEncoder hexHtmlEncoder = new CharEncoder("&#x",";",16);
    
    /**
     * Encodeur HEXA pour les URL.
     */
    public static final CharEncoder hexUrlEncoder = new CharEncoder("%","",16);
    
    /**
     * Encodeur DECIMAL pour les contenu HTML.
     */
    public static final CharEncoder decimalHtmlEncoder = new CharEncoder("&#",";",10);
    
    /**
     * Fonction de test pour tenter une connexion ALM.
     * @param args les arguments, non exploiter ici.
     * @throws Exception en cas d'erreur.
     */
    public static void main(String[] args) throws Exception {
    	
    	// On prépare l'URL. Si le port n'est pas précisé c'est qu'on en utilise pas.
    	String url = "https://" + Constants.HOST_ALM;
    	if (Constants.PORT != null) {
    		url = url.concat(":" + Constants.PORT);
    	}
    	url = url.concat("/qcbin");
    	
//    	CasEssaiBean casEssai = new CasEssaiBean();
//    	casEssai.setAlm(true);
//    	casEssai.setEtatFinal(false);
//    	//54199//78516
//    	casEssai.setIdUniqueTestLab(49375);
//    	casEssai.setIdUniqueTestPlan(76408);
//    	casEssai.setNomCasEssai("TEST-ALM");
//    	casEssai.ajouterStep("OBJ1", "OBJ1", "Attendu");
//    	casEssai.setRepertoireTelechargement("./TEST-ALM");
    	
    	SeleniumALMRESTWrapper wrapper = new SeleniumALMRESTWrapper();
    	Scanner input = new Scanner( System.in );
    	
    	System.out.print("Enter login : ");
    	String login = input.next( );
    	System.out.print("Enter password : ");
    	String password = input.next( );
    	
    	wrapper.preparerWrapper(url, Constants.DOMAIN, Constants.PROJECT, login, password);
    	
    	input.close();
    	
    	//String urlTestLab = wrapper.synchroScenarioDeTest("CAS_TEST_AUTO5", "10954", "Ceci est un cas de test créer automatiquement.", "Une description par défaut.");
    	//String urlCasTest = wrapper.synchroCasDeTest("CASTEST2", "A supprimer", "Equipe T&R", "levieilfa", "", "19394");

    	//String urlDesignStep = wrapper.synchroDesignStep("STEP1", "Description", "Excepted", "78961");
    	

    	
//    	String exemple = "CASTEST2" + "/n"
//    	+ "<Step1>"  + "/n"
//    	+ "A : Décrire l'action à mener (description du step)"  + "/n"
//    	+ "R : Décrire le résultat attendu (attendu du step)"  + "/n"
//    	+ "Nom du step2" + "/n"
//    	+ "A : Décrire l'action à mener (description du step)"  + "/n"
//    	+ "R : Décrire le résultat attendu (attendu du step)";
    	
    	//List<Entity> liste = extraireStep(exemple);
    	
    	//String urlCasTest = wrapper.extrationCasTest(exemple, "Description du CT", "Equipe T&R", "levieilfa", "", "19394");
    	
    	//System.out.println(urlCasTest);
    	
    	//XLSOutils.extraireInformationALM("c:\\Temp\\test.xlsx", "10954", "19394", wrapper);
    		
    	
    	XLSOutils.extraireInformationALM("c:\\Temp\\Recette Déléguée ITCE V18_03_matrice.xlsm", "11112", "19476", wrapper);
    	
    	wrapper.deconnexion();
    	
//    	System.exit(0);
//    	
//    	
//    	String run = wrapper.creerRun("RunAuto", null, "49375", "76408", Constants.USERNAME, true);
//		
//    	run = run.replaceAll("http:", "https:");
//    	run = run.replaceAll(":80", "");
//    	
//    	// Si le cas d'essai est associé à un repertoire on cherche à obtenir les pièces jointes.
//    	String step = wrapper.creerStep("StepAuto", "TEST", "école", "lécole", run, "76408", true, false);    	
//		File repertoire = new File(casEssai.getRepertoireTelechargement());
//
//		// Pour chaque fichier non repertoire dans le repertoire, on met en pièce jointe.
//		for (File file : repertoire.listFiles()) {
//			if (!file.isDirectory()) {
//				String chemin = file.getAbsolutePath();
//				// On remplace les référence au repertoire local \.\ par un simple \
//				chemin = chemin.replaceAll("\\\\.\\\\", "\\\\");
//				wrapper.ajoutPJ(run, "run", Files.toByteArray(file), file.getName());
//			}
//		}
    }
    
    /**
     * Effectue la préparation du wrapper à partir des informations de connexion.
     * @throws SeleniumException en cas d'erreur.
     */
    public void preparerWrapper(String urlALM, String domaine, String projet, String user, String password) throws SeleniumException {
//    	String url = "https://" + Constants.HOST;
//    	if (Constants.PORT != null) {
//    		url = url.concat(":" + Constants.PORT);
//    	}
//    	url = url.concat("/qcbin");

    	initialisation(urlALM, domaine, projet);

    	// Si une connexion est établie (où déjà présente)
    	try {
	    	if (login(user, password)) {
		    	ouvrirSessionQC();
	    	} else {
	    		throw new SeleniumException(Erreurs.E032, "Le login à échoué.");
	    	}
    	} catch (Exception ex) {
    		throw new SeleniumException(Erreurs.E032, "La préparation du wrapper à échouée (" + ex.getMessage() + ")");
    	}
    }

    
    /**
     * Effectue le nécessaire pour paramètrer le connecteur avec un domaine, un projet et une url vers ALM.
     * @param serverUrl l'url vers le serveur (finissant par /qcbin)
     * @param domain le domaine concernée
     * @param project le projet concerné
     */
    private void initialisation(final String serverUrl, final String domain, final String project) {
    	con = RestConnector.getInstance().init(new HashMap<String, String>(), serverUrl, domain, project);
    }
    
    /**
     * Ouvre une session vers ALM pour permettre des intérraction.
     * @throws Exception en cas d'erreur
     */
    private void ouvrirSessionQC() throws Exception {
    	con.getQCSession();
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

        //System.out.println("LoginURL : " + loginUrl);
        
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
    public boolean deconnexion() throws Exception {

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
    	//https://hpalm.intranatixis.com/qcbin/rest/is-authenticated
        String isAuthenticateUrl = con.buildUrl("rest/is-authenticated");
        String ret;
        
        // On effectue la requête en "GET" sur l'URL d'authentitication
        Response response = con.httpGet(isAuthenticateUrl, null, null);
        int responseCode = response.getStatusCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	// Si l'identification est déjà active, alors on ne renvoie pas l'url de connexion.
            ret = null;
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
        	 // Si l'identification n'est pas encore effectuée, on récupère des entête l'url d'authentification
            Iterable<String> authenticationHeader = response.getResponseHeaders().get("WWW-Authenticate");
            String newUrl = authenticationHeader.iterator().next().split("=")[1];
            newUrl = newUrl.replace("\"", "");
            newUrl += "/authenticate";
            ret = newUrl;
        } else {
        	//En cas d'erreur (ex : Code 404 ou 500) on renvoie une exception
            throw response.getFailure();
        }
        
        //TODO à positioner sous forme d'options de la fonction d'identification.
        
        // La connexion s'effectue en HTTPS si il y a présence d'un certificats (ex : SSL)
        if (ret != null && !ret.contains("https")) {
        	ret = ret.replace("http", "https");
        }
        // Si on utilise une connexion https, il ne faut pas spécifier le port 80
        if (ret != null  && Constants.PORT == null && ret.contains(":80")) {
        	ret = ret.replace(":80", "");
        }

        return ret;
    }
    
    /**
     * Renvoie une entité dont on connais l'id ou les critère de recherche sous forme d'Entity.
     * Les critère d'ID et de QUERY sont mutuellement exclusif.
     * @param id l'id de l'entite que l'on souhaites trouver 
     * @param query une requête permettant de trouver l'entité (ex : query={cycle-id[49375];test-id[76408];})
     * @param typeEntite le type d'entité à récupérer (ex : defect)
     * @return l'entité représentant le defect ou null si aucune entité n'as été trouvée.
     * @throws Exception en cas d'erreur.
     */
    public Entity obtenirEntite(String id, String query, String typeEntite) throws Exception {
        //String query = "query={project[IZIVENTE];detected-by[doublibounouais OR mongenetla OR lottena];name[V14.03*];user-06[*CE* OR *BP*];id[6511]}";
        //String query = "query={id["+id+"]}";
        
    	// On prépare les entête pour la requête, en précisant qu'on travaille en XML (normalement inutile, car déjà paramètré)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");
        // On précise que le type d'entity à manipuler est un defect
        String resourceWeWantToRead = con.buildALMEntityCollectionUrl(typeEntite);
        // On effectue la connexion vers l'entité dont à fournit l'id est on récupère la réponse.
        // Si il y a une requête on la prend en compte :
        String urlComplete = resourceWeWantToRead;
        if (id != null) {
        	urlComplete = urlComplete + "/" + id;
        } else if (query != null) {
        	urlComplete = urlComplete.concat("?" + query);
        }
        System.out.println(urlComplete);
        String responseStr = con.httpGet(urlComplete, null, requestHeaders).toString();
        
        //System.out.println(responseStr);
        
        // On extrait l'entity de la réponse 
        Entity entity = null;
        if (query != null) {
        	// On a une réponse à une requête il faut extraire le premier enregistrement
        	Entities entities = EntityMarshallingUtils.marshal(Entities.class, responseStr);
        	if (entities.getEntities() != null && entities.getEntities().size() > 0) {
        		entity = entities.getEntities().get(0);
        	} else {
        		entity = null;
        	}
        } else {
        	// On a un enregistrmement unique qu'on renvoie.
        	entity = EntityMarshallingUtils.marshal(Entity.class, responseStr);
        }
        
        // On va parcourir l'ensemble des champs renvoyés
//        List<Field> fields = entity.getFields().getField();
//        System.out.print("listing fields from marshalled object: ");
//        for (Field field : fields) {
//            System.out.print(field.getName() + "=" + field.getValue() + ", ");
//        }
        
        return entity;
    }
    
    /**
     * Effectue la création d'un RUN en fonction des paramètres demandés.
     * @param nomRun Le nom à appliquer au RUN, sera concaténé à un timestamp
     * @param idCycle L'id de l'instance du test dans le test lab (différent de l'id du test dans Test Plan)
     * @param idScenario l'id du scénario dans le test lab(à null si inconnu)
     * @param idTest L'id du cas de test dans le Test Plan (Test ID).
     * @param propretaire le propriétaire de l'éxécution.
     * @param etat true si l'éxécution est à Passed, false sinon.
     * @return l'url de consultation du RUN.
     * @throws SeleniumException en cas d'impossibilité de créer le run.
     */
    public String creerRun(String nomRun, String idCycle, String idScenario, String idTest, String propretaire, Boolean etat) throws SeleniumException {
    	Entity run = new Entity();   	
    	// On précise le type d'entité et on renseigne les champs obligatoire
    	run.setType("run");
    	String nomRunComplet = nomRun + "_" + new Date().getTime();
    	run.ajouterChamp("name", nomRunComplet);
    	run.ajouterChamp("test-id", idTest);
    	run.ajouterChamp("owner", propretaire);
    	run.ajouterChamp("subtype-id", "hp.qc.run.MANUAL");
    	run.ajouterChamp("status", "No Run");
    	//run.ajouterChamp("jenkins-job-name", "A venir");
    	//run.ajouterChamp("jenkins-url", "A venir");
    	//Si l'id du scénario (test set) n'est pas connu, cela entrainera une erreur dans l'affichage du RUN. On evite cela en le rendant obligatoire bien qu'il ne le soit pas.
    	run.ajouterChamp("cycle-id", idScenario);
    	if (idCycle != null) {
    		run.ajouterChamp("testcycl-id", idCycle);
    	} else {
    		try {
    			// L'instance de test n'est pas toujours connue. En effet elle n'est pas visible dans ALM, mais uniquement dans pas copier coller de l'URL.
	    		// Dans ce cas on récupère l'instance de test afin d'obtenir son id.
	    		Entity testInstance = obtenirEntite(null, "query={cycle-id[" + idScenario + "];test-id[" + idTest + "];}", "test-instance");
	    		run.ajouterChamp("testcycl-id", testInstance.obtenirChamp("id").getValue().get(0));
    		} catch (Exception ex) {
    			throw new SeleniumException(Erreurs.E032, "Impossible d'extraire l'id de l'instance de test  (" + ex.getMessage() + ")");
    		}
    	}
    	
    	try {
        	// On prépare les paramètres de la création et on l'effectue
    		String urlConsult = creerEntite(con.buildALMEntityCollectionUrl("run"), convertirEntiteEnChaine(run));
    		// On met à jour l'état du RUN afin de mettre à jour le test instance ne même temps. C'est le seul moyen d'éviter la création d'un run auto inutile.
    		Entity runMaj = new Entity();
    		runMaj.setType("run");
    		//runMaj.ajouterChamp("name", nomRunComplet);
    		// En fonction de la valeur de etat, on indique que le run est Passed, Failed ou Not Completed.
    		if (etat != null) {
    			runMaj.ajouterChamp("status", etat?"Passed":"Failed");
    		} else {
    			runMaj.ajouterChamp("status", "Not Completed");
    		}
    		// Mise à jour de l'entité avec le nouvel état.
    		majEntite(urlConsult, convertirEntiteEnChaine(runMaj));
			return urlConsult;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible de creer le run (" + e.getMessage() + ")");
		}
    }
    
    /**
     * Applique l'état paramètre a l'instance de test dont l'id de test set et l'id de scénario est passé en paramètre.
     * Attention l'instance de test est la version du test contenue dans le scénario (il est possible par exemple d'avoir plusieurs exemplaire de test dans un scénario, chacun aura une instance de test).
     * @param idTest l'identifiant du test à mettre à jour
     * @param idSenario l'identifiant du scenario
     * @param etat l'état à appliquer.
     * @throws SeleniumException en cas d'erreur lors de la mise à jour du cas de test.
     */
    public void majTestInstance(String idTest, String idScenario, Boolean etat) throws SeleniumException {
    	if (etat != null) {
			String testInstanceID = "";
			try {
				// On récupère l'instance de test liée au test set
				try {
					Entity testInstance = obtenirEntite(null, "query={cycle-id[" + idScenario + "];test-id[" + idTest + "];}", "test-instance");
					testInstanceID = testInstance.obtenirChamp("id").getValue().get(0);
	    		} catch (Exception ex) {
	    			throw new SeleniumException(Erreurs.E032, "Impossible d'extraire l'id de l'instance de test  (" + ex.getMessage() + ")");
	    		}
	    		//Entity testInstance = obtenirEntite(null, "query={id[" + idTestSet + "];}", "test-set");
	    		// On créer une instance de mise à jour de test-set
		    	Entity testSetMaj = new Entity();   
	    		testSetMaj.setType("test-instance");
	    		testSetMaj.ajouterChamp("status", etat?"Passed":"Failed");
	    		// On effectue la mise à jour en modifier le test set à partir de l'entité de mise à jour
	    		majEntite(con.buildALMEntityCollectionUrl("test-instance") + "/" + testInstanceID, convertirEntiteEnChaine(testSetMaj));
			} catch (Exception ex) {
				throw new SeleniumException(Erreurs.E032, "Impossible de mettre à jour le test set "+ idTest + " pour l'instance " + testInstanceID + " (" + ex.getMessage() + ")");
			}
    	}
    }
    //https://hpalm.intranatixis.com/qcbin/rest/domains/NATIXIS_FINANCEMENT/projects/CREDIT_CONSOMMATION/customization/entities/test-set/fields?login-form-required=y
    
    /**
     * Fonction ayant pour objectif de créer un scénario de test (test-set) dans ALM.
     * @param nom nom du cas de test
     * @param position l'id du test-set-folder dans le test lab
     * @param description description technique de l'objet, n'apparais pas dans l'IHM ALM.
     * @param commentaire description fonctionnelle de l'objet, apparais dans l'IHM ALM.
     * @return l'url vers l'objet test ainsi créer.
     * @throws SeleniumException en cas d'erreur.
     */
    public String creerScenarioDeTest(String nom, String position, String description, String commentaire) throws SeleniumException {
    	// On créer la coquille vide du test.
    	Entity test = new Entity();
    	test.setType("test-set");
    	test.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
    	// Ici il s'agit du repertoire parent où on créer le cas de test, test-set-folder pour le test lab, test-folder pour le test plan.
    	test.ajouterChamp("parent-id", position);
    	// Attention, le champs description ne contient pas le description chez CCO, mais le commentaire
    	if (description != null) {
    		test.ajouterChamp("description", encode(description, hexHtmlEncoder)); 
    	}
    	if (commentaire != null) {
    		test.ajouterChamp("comment", encode(commentaire, hexHtmlEncoder)); 
    	}
    	// Champs obligatoire à renseigner
    	test.ajouterChamp("status", "Open"); 
    	test.ajouterChamp("subtype-id", "hp.qc.test-set.default"); 
    	
    	System.out.println(convertirEntiteEnChaine(test));
    	
    	try {
			return creerEntite(con.buildALMEntityCollectionUrl("test-set"), convertirEntiteEnChaine(test));
		} catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible de creer le test (" + e.getMessage() + ")");
		}
    }
    
    /**
     * Fonction ayant pour objectif de créer un scénario de test (test-set) dans ALM si il n'existe pas ou de renvoyer l'url vers celui ci si il existe.
     * @param nom nom du cas de test
     * @param position l'id du test-set-folder dans le test lab
     * @param description description technique de l'objet, n'apparais pas dans l'IHM ALM.
     * @param commentaire description fonctionnelle de l'objet, apparais dans l'IHM ALM.
     * @return l'url vers l'entité de scénario.
     * @throws SeleniumException en cas d'erreur.
     */
    public String synchroScenarioDeTest(String nom, String position, String description, String commentaire) throws SeleniumException {
    	try {
	    	Entity retour = obtenirEntite(null, "query={name[\"" + URLEncoder.encode(nom, "UTF-8") + "\"];parent-id[" + position + "];}", "test-set");
	    	String urlTestLab = "";
	    	if (retour == null) {
	    		urlTestLab = creerScenarioDeTest(nom, position, description, commentaire);
	    	} else {
	    		urlTestLab = con.buildALMEntityCollectionUrl("test-set") + "/" + retour.getFields().getFieldValue("id");
	    	}
	    	return urlTestLab;
		}  catch (SeleniumException ex) {
			throw ex;
		}  catch (Exception e) {
			throw new SeleniumException(Erreurs.E032, "Impossible d'obtenir le scénario dans le test lab (" + e.getMessage() + ")");
		}
    }
    
    /**
     * Permet de créer un design step pour un test donc l'id est connu. Si un step possedant le même nom existe déjà, celui ci n'est pas remplacer.
     * @param nom le nom du design step
     * @param description la description de l'étape
     * @param expected l'attendu lors de l'étape
     * @param idTest l'identifiant du test qui accueille le design step (ou l'url de consultation du dit test)
     * @return l'url vers le design step qu'il est été créer ou non.
     * @throws SeleniumException en cas d'erreur
     */
    public String synchroDesignStep(String nom, String description, String expected, String idTest) throws SeleniumException {
    	String url = "";
    	// Si on fournit une url en paramètre, on en extrait l'id.
    	if (idTest.startsWith("http")) {
    		idTest = idTest.substring(idTest.lastIndexOf("/") + 1);
    	}
    	
    	try {
    		Entity retour = obtenirEntite(null, "query={name[\"" + URLEncoder.encode(nom, "UTF-8") + "\"];parent-id[" + idTest + "];}", "design-step");
	    	if (retour == null) {
	        	// On précise le type d'entité et on remplis les champs obligatoires
	        	Entity step = new Entity();   	
	        	step.setType("design-step");
	        	step.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
	        	// Les informations textuelles sont supposées être de l'HTML. Mais pour les envoyer via les services REST il faut les encoder en hexa.
	        	step.ajouterChamp("description", encode(description, hexHtmlEncoder));
	        	step.ajouterChamp("expected", encode(expected, hexHtmlEncoder));
	        	step.ajouterChamp("parent-id", idTest);
	        	// On effectue la création proprement dites
	    		url = creerEntite(con.buildALMEntityCollectionUrl("design-step"), convertirEntiteEnChaine(step));
	    	} else {
	    		url = con.buildALMEntityCollectionUrl("design-step") + "/" + retour.getFields().getFieldValue("id");
	    	}
		}  catch (SeleniumException ex) {
			throw ex;
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible d'obtenir ou de créer le design step dans le test plan (" + e.getMessage() + ")");
		}
    	return url;
    }
    
    public String synchroInstanceTest(String nom, String idScenario, String idTest) throws SeleniumException {
    	String url = "";
    	// Si on fournit une url en paramètre, on en extrait l'id.
    	if (idTest.startsWith("http")) {
    		idTest = idTest.substring(idTest.lastIndexOf("/") + 1);
    	}
    	
    	if (idScenario.startsWith("http")) {
    		idScenario = idScenario.substring(idScenario.lastIndexOf("/") + 1);
    	}
    	
    	try {
    		Entity retour = obtenirEntite(null, "query={cycle-id[" + idScenario + "];test-id[" + idTest + "];}", "test-instance");
	    	if (retour == null) {
	        	// On précise le type d'entité et on remplis les champs obligatoires
	        	Entity testInstance = new Entity();   	
	        	testInstance.setType("test-instance");
	        	//testInstance.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
	        	// Les informations textuelles sont supposées être de l'HTML. Mais pour les envoyer via les services REST il faut les encoder en hexa.
	        	testInstance.ajouterChamp("test-id", idTest);
	        	testInstance.ajouterChamp("cycle-id", idScenario);
	        	testInstance.ajouterChamp("subtype-id", "hp.qc.test-instance.MANUAL");
	        	testInstance.ajouterChamp("status", "No Run");
	        	// On effectue la création proprement dites
	    		url = creerEntite(con.buildALMEntityCollectionUrl("test-instance"), convertirEntiteEnChaine(testInstance));
	    	} else {
	    		url = con.buildALMEntityCollectionUrl("test-instance") + "/" + retour.getFields().getFieldValue("id");
	    	}
		}  catch (SeleniumException ex) {
			throw ex;
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible d'obtenir ou de créer l'instance de test dans le test lab (" + e.getMessage() + ")");
		}
    	return url;
    }
    
    /**
     * Permet de synchroniser un cas de test du test plan. Si celui ci existe on renvoie l'url. Sinon on créer un nouveau cas de test.
     * @param nom le nom du cas de test
     * @param description la description du cas de test
     * @param emetteur l'équipe émetrice
     * @param proprietaire le login ALM du propriétaire.
     * @param application l'application sur laquelle s'applique le test
     * @param idTestFolder l'identifiant unique du dossier qui accueille le cas de test.
     * @return l'url vers le cas de test existant ou nouvellement créer.
     * @throws SeleniumException en cas d'erreur.
     */
    public String synchroCasDeTest(String nom, String description, String emetteur, String proprietaire, String application, String idTestFolder) throws SeleniumException {
    	String url = "";
    	// Si on fournit une url en paramètre, on en extrait l'id.
    	if (idTestFolder.startsWith("http")) {
    		idTestFolder = idTestFolder.substring(idTestFolder.lastIndexOf("/") + 1);
    	}
    	
    	try {
    		Entity retour = obtenirEntite(null, "query={name[\"" + URLEncoder.encode(nom, "UTF-8") + "\"];parent-id[" + idTestFolder + "];}", "test");
	    	if (retour == null) {
	        	// On précise le type d'entité et on remplis les champs obligatoires
	        	Entity test = new Entity();   	
	        	test.setType("test");
	        	test.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
	        	// Les informations textuelles sont supposées être de l'HTML. Mais pour les envoyer via les services REST il faut les encoder en hexa.
	        	test.ajouterChamp("description", encode(description, hexHtmlEncoder));
	        	test.ajouterChamp("parent-id", idTestFolder);
	        	test.ajouterChamp("user-template-01", encode(emetteur, hexHtmlEncoder));
	        	test.ajouterChamp("owner", proprietaire);
	        	if (application != null && !"".equals(application)) {
	        		test.ajouterChamp("user-05", encode(application, hexHtmlEncoder));
	        	}
	        	// Champs obligatoire à renseigner
	        	test.ajouterChamp("status", "En cours"); 
	        	test.ajouterChamp("subtype-id", "MANUAL"); 
	        	
	        	System.out.println(convertirEntiteEnChaine(test));

	        	// On effectue la création proprement dites
	    		url = creerEntite(con.buildALMEntityCollectionUrl("test"), convertirEntiteEnChaine(test));
	    	} else {
	    		url = con.buildALMEntityCollectionUrl("test") + "/" + retour.getFields().getFieldValue("id");
	    	}
		}  catch (SeleniumException ex) {
			throw ex;
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible d'obtenir ou de créer le cas de test dans le test plan (" + e.getMessage() + ")");
		}
    	return url;
    }
    
    
    //test-set-folder => Répertoire du test lab
    //test-folder => Répertoire du test plan
    //test-set => Scénario
    //test => Cas de test
    //design-step => Step des cas de tests
    //test-instance => Instance du cas de test pour un scenario
    
    /**
     * Permet de créer une step pour un run donné.
     * @param nom nom du step
     * @param description la description de l'étape
     * @param actual le constaté lors de l'étape
     * @param expected l'attendu lors de l'étape
     * @param idRun l'identifiant du run, ou l'url d'accès au run
     * @param idTest l'identifiant du test
     * @param etat true si Passed, false si Failed.
     * @param maj true la step est décrite dans ALM, false sinon (non exploité pour le moment).
     * @return l'url vers le step créee.
     * @throws SeleniumException en cas d'erreur dans le processus de création du step.
     */
    public String creerStep(String nom, String description, String actual, String expected, String idRun, String idTest, Boolean etat, boolean maj) throws SeleniumException {    		
    	Entity step = new Entity();   	
    	// On précise le type d'entité et on remplis les champs obligatoires
    	step.setType("run-step");
    	step.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
    	if (null == etat) {
    		step.ajouterChamp("status", "Not Completed");
    	} else {
    		step.ajouterChamp("status", etat?"Passed":"Failed");
    	} 
    	// Les informations textuelles sont supposées être de l'HTML. Mais pour les envoyer via les services REST il faut les encoder en hexa.
    	step.ajouterChamp("description", encode(description, hexHtmlEncoder));
    	step.ajouterChamp("actual", encode(actual, hexHtmlEncoder));
    	step.ajouterChamp("expected", encode(expected, hexHtmlEncoder));
    	// test-id (ex: 18475) --> Le même que celui du RUN, c'est l'id du test parent. Il est facultatif mais son absence entraine des erreurs.
    	step.ajouterChamp("test-id", idTest);
    	// parent-id (ex: 23115) --> L'Id du Run, champ obligatoire
    	if (idRun.startsWith("http")) {
    		// Si on à fournit l'url on coupe celle ci pour ne prendre que la dernière valeur.
    		step.ajouterChamp("parent-id", idRun.substring(idRun.lastIndexOf("/") + 1));
    	} else {
    		step.ajouterChamp("parent-id", idRun);
    	}
    	
    	//System.out.println(convertirEntiteEnChaine(step));
    	
    	// On prépare les paramètres de la création et on l'effectue
    	//System.out.println(convertirEntiteEnChaine(step));
    	try {
			return creerEntite(con.buildALMEntityCollectionUrl("run-step"), convertirEntiteEnChaine(step));
		} catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E032, "Impossible de creer le step (" + e.getMessage() + ")");
		}
    }
    
    /**
     * Permet la création d'une entity à partir d'une URL de collection et d'un flux XML.
     * @param collectionUrl l'url vers la collection à alimentée
     * @param postedEntityXml le flux XML à transmettre à ALM pour créer la nouvelle entité
     * @return l'url permettant la consultation de la nouvelle entité
     * @exception Exception en cas d'erreur.
     */
    public String creerEntite(String collectionUrl, String postedEntityXml) throws Exception {
    	// On initialise les entêtes avec les informations sur le contenu (XML)
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml; charset=UTF-8");
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
     * Effectue une mise à jour sur l'entité paramètre. Seuls les champs mentionné dans le XML seront mis à jour.
     * @param entityUrl l'url vers l'entité à mettre à jour.
     * @param updatedEntityXml description de l'entité sous format XML. Les champs présents seront mis à jour.
     * @return la description XML de l'entité après l'étape de mise à jour.
     * @throws Exception en cas d'erreur.
     */
    public Response majEntite(String entityUrl, String updatedEntityXml) throws Exception {
   	 // On créer l'entête permettant le transfert de données XML.
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");
        // On effectue une requête de type "PUT" pour mettre à jour l'entité.
        Response putResponse = con.httpPut(entityUrl, updatedEntityXml.getBytes(), requestHeaders);

        if (putResponse.getStatusCode() == HttpURLConnection.HTTP_MOVED_PERM) {
        	// On rencontre une erreur 301. Le chemin vers l'entité à été modifiée, on va suivre ce chemin
        	String newUri = putResponse.getResponseHeaders().get("Location").iterator().next();
        	// On rétablie le header indiquant le type de contenu envoyé.
        	requestHeaders.put("Content-Type", "application/xml");
        	putResponse = con.httpPut(newUri, updatedEntityXml.getBytes(), requestHeaders);
        }
        
        // Si on rencontre autre chose que le code retour OK et ce même après avoir gérer le code retour 301, c'est qu'on à une erreur.
        if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception(putResponse.toString());
        }

        return putResponse;
    }
    
    /**
     * Permet la suppression côté serveur d'une entité spécifiée.
     * @param entityUrl l'url vers l'entité à supprimer.
     * @return la représentation XML de l'objet supprimé.
     */
    public String supprimerEntite(String entityUrl) throws Exception {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", "application/xml");

        Response serverResponse = con.httpDelete(entityUrl, requestHeaders);
        
        if (serverResponse.getStatusCode() == HttpURLConnection.HTTP_MOVED_PERM) {
        	// On rencontre une erreur 301. Le chemin vers l'entité à été modifiée, on va suivre ce chemin
        	String newUri = serverResponse.getResponseHeaders().get("Location").iterator().next();
        	// On rétablie le header indiquant le type de contenu envoyé.
        	requestHeaders.put("Content-Type", "application/xml");
        	serverResponse = con.httpDelete(newUri, requestHeaders);
        }
        
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
 	   String retour = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Entity Type=\"" + entity.getType() + "\"> <Fields>";
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
     * @return true si l'entity supporte le versionning, false sinon
     * @throws Exception en cas d'erreur.
     */
     public boolean isVersioned(String entityType) throws Exception {
    	 // Initilisation du connecteur pour accèder au éléments de customization.
         //RestConnector con = RestConnector.getInstance();
         String descriptorUrl = con.buildUrl("rest/domains/"
                  + con.getDomain()
                  + "/projects/"
                  + con.getProject()
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
     public String verrouiller(String entityUrl) throws Exception {
    	 // On créer l'entête permettant le transfert de données XML.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Accept", "application/xml");
         //requestHeaders.put("Content-Type", "application/xml");

         // On effectue la pose du verrou sur l'entité.
         Response lockResponse = con.httpGet(entityUrl + "/lock", null, requestHeaders);
         
         // On effectue la pose du verrou sur l'entité.
         lockResponse = con.httpPost(entityUrl + "/lock", null, requestHeaders);
         
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
     public boolean deverouiller(String entityUrl) throws Exception {
         return con.httpDelete(entityUrl + "/lock", null).getStatusCode() == HttpURLConnection.HTTP_OK;
     }
     
     
     /**
      * Permet de mettre à jour une PJ de l'entité en paramètre.
      * @param entityUrl l'url vers l'entité sur laquelle on met à jour les attachements. 
      * @param bytes les données qui remplacent les anciennes données.
      * @param attachmentFileName le nom de fichier à donné dans le serveur.
      * @return la représentation sous forme de chaine de la réponse du serveur.
      */
     public String majPJDonnees(String entityUrl, byte[] bytes, String attachmentFileName) throws Exception {
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
     public String majPJDescription(String entityUrl, String description, String attachmentFileName) throws Exception {
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
     public String lirePJDetails(String attachmentUrl) throws Exception {
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
     public byte[] lirePJDonnees(String attachmentUrl) throws Exception {
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
     public String lirePJs(String entityUrl) throws Exception {
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
     private String joindrePJ(String entityUrl, byte[] fileData, String filename) throws Exception {

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
      * Permet l'ajout de PJ en passant par la description de plusieurs content Type (nom, description, data) d'une PJ à une entité.
      * @param entityUrl l'url vers l'entité à laquelle ajoutée la PJ.
      * @param fileData le contenu du fichier (en byte[])
      * @param contentType le contentType du contenu du fichier (ex : txt/html or xml, or octetstream etc..)
      * @param filename le nom à donné à la PJ.
      * @return l'url vers l'entité d'attachement créer à partir de la PJ.
      */
     private String joindrePJ(String entityUrl, byte[] fileData, String contentType, String filename, String description) throws Exception {
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

         // La chaine à utilisée pour démarquer differentes parties mime
         String boundary = "boundary";

         //Template pour décrire les données (supposée non binaire)
         String fieldTemplate = "--%1$s\r\n" + "Content-Disposition: form-data; name=\"%2$s\" \r\n\r\n" + "%3$s" + "\r\n";

         // Template pour décrire l'envoie des données fichier
         // Les données binaires ont toujours besoin d'être suffixée.
         String fileDataPrefixTemplate = "--%1$s\r\n" + "Content-Disposition: form-data; name=\"%2$s\"; filename=\"%3$s\"\r\n" + "Content-Type: %4$s\r\n\r\n";

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

         // On spécifie les entête en précisant que le contenu est constitué de plusieurs parties.
         Map<String, String> requestHeaders = new HashMap<String, String>();
         requestHeaders.put("Content-Type", "multipart/form-data; boundary=" + boundary);
         // On effectue la réquête "POST".
         Response response = con.httpPost(entityUrl + "/attachments", bytes.toByteArray(), requestHeaders);
         if (response.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
             throw new Exception(response.toString());
         }

         return response.getResponseHeaders().get("Location").iterator().next();
     }
     
 	/**
 	 * Fonction permettant la mise à jour dans ALM du cas de test à partir des informations saisies.
 	 * Un nouveau RUN vas être créer pour chaque cas de test du scénario, et les steps vont être renseignée dans ce run.
 	 * Cette fonction suppose que le scénario dans TESTLAB n'adresse qu'un seul exemplaire de chaque CT dans TESTPLAN
 	 * @param wrapper le connecteur à ALM de Type REST
 	 * @param casEssai le cas d'essai contenant les informations obligatoire (id du scénario et id des cas de test).
 	 * @param etat l'état final du cas de test.
 	 * @throws SeleniumException en cas d'erreur.
 	 */
 	public static void miseAJourTestSet(CasEssaiBean casEssai, Boolean etat) throws SeleniumException {
 		// On vérifie les données pour ALM.
 		if (casEssai.getAlm()) {
			// Si l'état n'est pas renseigné, on l'obtient de la somme des états des sous cas.
 			// Si on rencontre un step à failed le scénario est failed, sinon si un step est à null alors on est à not completed.
			if (etat == null) {
				etat = true;
				for (ObjectifBean step : casEssai.getObjectifs().values()) {
					if (step.isStep()) {
						if (null == step.getEtat()) {
							etat = null;
						} else if(step.getEtat() == false) {
							etat = false;
							break;
						}
					}
				}
			}

 			// Si le cas est unique on met à jour le "Cas de test" (Test) dans le "scénario" (Test Set) en lui ajoutant un "Run".
 			if (casEssai.getTests().isEmpty()) {
 	 			// On vérifie la validité des informations :
 	 			if (casEssai.getIdUniqueTestLab() == -1 || casEssai.getIdUniqueTestPlan() == -1) {
 	 				throw new SeleniumException(Erreurs.E033, "Impossible de mettre à jour l'état du cas de test " + casEssai.getNomCasEssai() + "  dans ALM : Les ID doivent être renseignés.");
 	 			}
 	 			
 				try {
 		 			// Si les informations sont valides on initialise la connexion.
 					SeleniumALMRESTWrapper wrapper = new SeleniumALMRESTWrapper();
 					wrapper.preparerWrapper(RESTBean.URL_ALM, RESTBean.DOMAIN_ALM, RESTBean.PROJECT_ALM, RESTBean.LOGIN_ALM, RESTBean.PASSWORD_ALM);
 					
 					// En premier lieu, on met à jour le test set
 					//wrapper.majTestInstance(casEssai.getIdUniqueTestPlan().toString(), casEssai.getIdUniqueTestLab().toString(), etat);
 					
 					// Mise à jour de l'état général du cas de test via la création d'un nouveau RUN lié.
 					String run = wrapper.creerRun("RunAuto", null, casEssai.getIdUniqueTestLab().toString(), casEssai.getIdUniqueTestPlan().toString(), RESTBean.LOGIN_ALM, etat);
 					//TODO On rétablie le protocole autorisé (https, sans précision de port)
 					run = run.replaceAll("http:", "https:");
 			    	run = run.replaceAll(":80", "");
 			    	
 					// On met à jour les STEP (sous forme d'ajout)
 					for (ObjectifBean step : casEssai.getObjectifs().values()) {
 						if (step.isStep()) {
 							//wrapper.addStep(execution_run, step.getCode(), step.getEtat()?StatusAs.PASSED:StatusAs.FAILED, step.getDescriptif(), step.getAttendu(), step.getObtenu());
 							wrapper.creerStep(step.getCode(), step.getDescriptif(), step.getObtenu(), step.getAttendu(), run, casEssai.getIdUniqueTestPlan().toString(), step.getEtat(), true);
 							//wrapper.creerStep("StepAuto", "TEST", "", "", run, "76408", true, false);
 						}
 					}
 			    	
 					// Si le cas d'essai est associé à un repertoire on cherche à obtenir les pièces jointes.
 					if (casEssai.getRepertoireTelechargement() != null) {
 						File repertoire = new File(casEssai.getRepertoireTelechargement());
 						// On vérifie qu'il s'agit bien d'un repertoire
 						if (repertoire.isDirectory()) {
 							// Pour chaque fichier non repertoire dans le repertoire, on met en pièce jointe.
 							for (File file : repertoire.listFiles()) {
 								if (!file.isDirectory()) {
 									String chemin = file.getAbsolutePath();
 									// On remplace les référence au repertoire local \.\ par un simple \
 									chemin = chemin.replaceAll("\\\\.\\\\", "\\\\");
 									wrapper.ajoutPJ(run, "run", Files.toByteArray(file), file.getName());
 								}
 							}
 						}
 					}

 					// On ferme la connexion avec ALM.
 					if (!wrapper.deconnexion()) {
 						throw new SeleniumException(Erreurs.E032, "Impossible de clôturer la session ALM !");
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					throw new SeleniumException(Erreurs.E032, "Impossible de mettre à jour l'état du cas de test dans ALM : " + e.getMessage());
 				}
 			} else {
 				//System.out.println("C'est un cas MULTIPLE (TestPlan non renseigné)");
 				// Si le cas d'essai en contient d'autres, on boucle sur chaucun d'entre eux.
 				for (CasEssaiBean sousCas : casEssai.getTests()) {
 					System.out.println("=> Mise à jour dans ALM du cas de test " + sousCas.getIdUniqueTestLab() + ":" + sousCas.getIdUniqueTestPlan());
 					miseAJourTestSet(sousCas, sousCas.getEtatFinal());
 				}
 			}
 		} else {
 			System.out.println("La mise à jour ALM est désactivée pour le scénario/cas de test " + casEssai.getNomCasEssai());
 		}
 	}
     
 	/**
 	 * Effectue l'ajout d'une PJ sur l'entité choisie.
 	 * @param urlEntite l'url vers l'entité concernée. 
 	 * @param typeEntite le type de l'entité (afin de savoir si l'entité est versionnée).
 	 * @param donneesPJ les données qui composent la PJ (en byte)
 	 * @param nomPJ le nom de la PJ à ajoutée.
 	 * @return l'url vers la nouvelle entité qui représente la nouvelle PJ.
 	 * @throws SeleniumException en cas d'erreur.
 	 */
 	public String ajoutPJ(final String urlEntite, String typeEntite, byte[] donneesPJ, String nomPJ) throws SeleniumException {
 		// Initialisation des variables
	   	boolean remiseEnplace = true; 
	   	String retour = null;
 		try {
 			//TODO Chez Natixis, le système de lock ne fonctionne pas.. Il faut directement mettre les PJ.
		   	 boolean isVersioned = isVersioned(typeEntite);
//		   	 if (isVersioned) {
//		   		 // Cette entité supporte le versionning, on effectue une opération de "checkout".
//		   		 checkout(urlEntite, "Extraction temporaire", -1);
//		   	 } else {
//		   		 // Si l'entité ne supporte pas le versionning on passe par une pose de verrou.
//		   		 verrouiller(urlEntite);
//		   	 }
		   	 // On effectue la mise en PJ et on récupère l'url vers la dites PJ.
	    	 retour = joindrePJ(urlEntite, donneesPJ, nomPJ);
//	    	 // On remet les choses en place.
//	    	 if (isVersioned) {
//	    		 String firstCheckinComment = "Remise en place";
//	    		 remiseEnplace = checkin(urlEntite, firstCheckinComment, false);
//	    	 } else {
//	    		 remiseEnplace = deverouiller(urlEntite);
//	    	 }
	   	 } catch (Exception ex) {
	   		 ex.printStackTrace();
	   		 throw new SeleniumException(Erreurs.E032, "Impossible d'ajouter la PJ (" + ex.getMessage() + ")");
	   	 }
	   	 // On vérifie que le checkin ou le dévérouillage à fonctioner, dans le cas contraire on envoie une exception.
	   	 if (!remiseEnplace) {
	   		throw new SeleniumException(Erreurs.E032, "La remise en place ALM à échouée, les modifications ne seront pas prises en compte");
	   	 }   	 
    	 return retour;
 	}
 	
 	
     /**
      * Fonction de test effectuant des manipulation des PJ de type texte sur l'entité donnée.
      * Il faut déjà avoir initialisé le connecteur REST avant de lancer cette fonction.
      * @param urlEntite l'url vers l'entité sur laquelle le test est effectué
      * @param typeEntite le type d'entité (ex : requirement) concernée.
      * @throws Exception en cas d'erreur.
      */
     public void testPJ(final String urlEntite, String typeEntite) throws Exception {

    	 //AuthenticateLoginLogoutExample login = new AuthenticateLoginLogoutExample();
    	 //CreateDeleteExample writeExample = new CreateDeleteExample();
    	 
    	 // Nous avons déjà créer un objet, c'est à cet objet qu'on souhaite ajouter une pièce jointe
    	 
    	 //AttachmentsExample example = new AttachmentsExample();

    	 // Avant de modifier une entité on la vérouille si elle est soumise au versioning, sinon on l'extrait (checkout)
    	 //TODO faire en sorte qu'on vérifie le type de l'entité paramètre
    	 boolean isVersioned = isVersioned(typeEntite);
    	 
    	 String preModificationXml = null;
    	 if (isVersioned) {
    		 // Cette entité supporte le versionning, on effectue une opération de "checkout".
    		 String firstCheckoutComment = "check out comment1";
    		 preModificationXml = checkout(urlEntite, firstCheckoutComment, -1);
    		 Assert.assertTrue("checkout comment missing", preModificationXml.contains(convertirChampEnChaine("vc-checkout-comments", firstCheckoutComment)));
    	 } else {
    		 // Si l'entité ne supporte pas le versionning on passe par une pose de verrou.
    		 preModificationXml = verrouiller(urlEntite);
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
    			 joindrePJ(
    					 urlEntite,
    					 multipartFileContent.getBytes(),
    					 "text/plain",
    					 multipartFileName,
    					 multipartFileDescription);

    	 String newOctetStreamAttachmentUrl = joindrePJ(urlEntite, octetstreamFileContent.getBytes(), octetStreamFileName);

    	 // Changes aren't visible to other users until we check them
    	 //  in if versioned
    	 if (isVersioned) {
    		 String firstCheckinComment = "check in comment1";
    		 boolean checkin = checkin(urlEntite, firstCheckinComment, false);
    		 Assert.assertTrue("checkin failed", checkin);
    	 } else {
    		 boolean unlock = deverouiller(urlEntite);
    		 Assert.assertTrue("unlock failed", unlock);
    	 }

    	 //read the data and it's metadata back from the server
    	 String readAttachments = lirePJs(urlEntite);
    	 Assert.assertTrue("multipart attachment description missing", readAttachments.contains(convertirChampEnChaine("description", multipartFileDescription)));
    	 //Assert.assertTrue("attachment count incorrect or missing", readAttachments.contains("<Entities TotalResults=\"2\">"));

    	 byte[] readAttachmentData = lirePJDonnees(newOctetStreamAttachmentUrl);
    	 String readAttachmentsString = new String(readAttachmentData);
    	 Assert.assertEquals("uploaded octet stream file content differs from read file content", readAttachmentsString, octetstreamFileContent);

    	 readAttachmentData = lirePJDonnees(newMultiPartAttachmentUrl);
    	 readAttachmentsString = new String(readAttachmentData);
    	 Assert.assertEquals("uploaded multipart stream file content differs from read file content", readAttachmentsString, multipartFileContent);

    	 String readAttachmentDetails = lirePJDetails(newMultiPartAttachmentUrl);
    	 Assert.assertTrue("multipart attachment description missing", readAttachmentDetails.contains(convertirChampEnChaine("description", multipartFileDescription)));

    	 //again with the checkout checkin procedure
    	 if (isVersioned) {
    		 // Note that we selected an entity that supports versioning
    		 // on a project that supports versioning. Would fail otherwise.
    		 String firstCheckoutComment = "check out comment1";
    		 preModificationXml = checkout(urlEntite, firstCheckoutComment, -1);
    		 Assert.assertTrue("checkout comment missing", preModificationXml.contains(convertirChampEnChaine("vc-checkout-comments", firstCheckoutComment)));
    	 } else {
    		 preModificationXml = verrouiller(urlEntite);
    	 }

    	 //Assert.assertTrue("posted field value not found", preModificationXml.contains(Constants.entityToPostFieldXml));

    	 //update data of file
    	 String updatedOctetStreamFileData = "updated file contents";
    	 String updatedOctetstreamFileDescription = "completely new description";

    	 majPJDonnees(urlEntite, updatedOctetStreamFileData.getBytes(), octetStreamFileName);

    	 readAttachmentsString = new String(lirePJDonnees(newOctetStreamAttachmentUrl));
    	 Assert.assertEquals("updated octet stream data not changed", updatedOctetStreamFileData, readAttachmentsString);

    	 //update description of file
    	 String attachmentMetadataUpdateResponseXml = majPJDescription(urlEntite, updatedOctetstreamFileDescription, octetStreamFileName);

    	 Assert.assertTrue("updated octet stream description not changed", attachmentMetadataUpdateResponseXml.contains(updatedOctetstreamFileDescription));

    	 //checkin
    	 if (isVersioned) {
    		 final String firstCheckinComment = "check in comment1";
    		 boolean checkin = checkin(urlEntite, firstCheckinComment, false);
    		 Assert.assertTrue("checkin failed", checkin);
    	 } else {
    		 boolean unlock = deverouiller(urlEntite);
    		 Assert.assertTrue("unlock failed", unlock);
    	 }

    	 //cleanup

    	 //check out attachment owner
    	 if (isVersioned) {
    		 checkout(urlEntite, "", -1);
    	 } else {
    		 verrouiller(urlEntite);
    	 }

    	 //delete attachments
    	 supprimerEntite(newOctetStreamAttachmentUrl);
    	 supprimerEntite(newMultiPartAttachmentUrl);

    	 //checkin attachment owner
    	 if (isVersioned) {
    		 checkin(urlEntite, "", false);
    	 } else {
    		 deverouiller(urlEntite);
    	 }

    	 //delete attachment owner
    	 supprimerEntite(urlEntite);
    	 deconnexion();
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
         example.deconnexion();

         //Vérification de la déconnexion
         Assert.assertNotNull("La connexion est toujours active malgré la demande de déconnexion.", example.obtenirURLConnexion());
     }
     
     /**
      * Extrait d'une chaine de caractère respetant le formalisme prévu un nom de cas de test ainsi que des steps.
      * Si un des éléments existe déjà, la création est ignorée pour celui-ci.
      * @param chaineCasDeTest la chaine à découper.
      * @param descriptionCT la description du cas de test à créer
      * @param emetteur l'équipe émettrice du cas de test 
      * @param proprietaire le propriétaire du cas de test
      * @param application l'application concernée par le cas de test
      * @param idTestFolder l'identifiant unique du répertoire dans lequel on doit créer le cas de test.
      * @return l'url vers le cas de test.
      * @throws SeleniumException en cas d'erreur lors des créations ou des obtentions.
      */
     public String extrationCasTest(String chaineCasDeTest, String idTestFolder) throws SeleniumException {
    	 // EXEMPLE :
			//    	 Nom du cas de test
			//    	 <Nom du step1>
			//    	 A : Décrire l'action à mener (description du step)
			//    	 R : Décrire le résultat attendu (attendu du step)
			//    	 <Nom du step2>
			//    	 A : Décrire l'action à mener (description du step)
			//    	 R : Décrire le résultat attendu (attendu du step)
    	 //String retour = "";
    	 // Récupération des lignes de la description via un découpage du paragraphe
    	 String[] lignes = chaineCasDeTest.split("\\n");
    	 List<String> listeLigne = Arrays.asList(lignes); 
    	 String ligne = "";
    	 Iterator<String> iterator =  listeLigne.iterator();
    	 // Extraction du nom du cas de test
    	 String nomCasTest = iterator.next();
    	 String emetteurCT = "";
    	 String proprietaireCT = "";
    	 String applicationCT = "";
    	 String descriptionCT = "";
    	 // On vas extraire le descriptif du cas de test à partir des infos préfixée de "=XX=" :
    	 for (String ligneTemp : listeLigne) {
    		 if (ligneTemp.startsWith("=EX=")) {
    			 descriptionCT = descriptionCT.concat("Exigence testée : " + ligneTemp.replace("=EX=", ""));
    		 }
    		 if (ligneTemp.startsWith("=RD=")) {
    			 descriptionCT = descriptionCT.concat("\nRègle de gestion testée : " + ligneTemp.replace("=RD=", ""));
    		 }
    		 if (ligneTemp.startsWith("=NA=")) {
    			 descriptionCT = descriptionCT.concat("\nNature du cas de test : " + ligneTemp.replace("=NA=", ""));
    		 }
    		 if (ligneTemp.startsWith("=TY=")) {
    			 descriptionCT = descriptionCT.concat("\nType de cas de test : " + ligneTemp.replace("=TY=", ""));
    		 }
    		 if (ligneTemp.startsWith("=EM=")) {
    			 emetteurCT = ligneTemp.replace("=EM=", "");
    		 }
    		 if (ligneTemp.startsWith("=AU=")) {
    			 proprietaireCT = ligneTemp.replace("=AU=", "");
    		 }
    	 }
    	 
    	 // Synchronisation pour le cas de test
    	 String idCasTest = "";
    	 if (nomCasTest.startsWith("@")) {
    		 // Si la ligne commence par arobase c'est une référence à un id de cas de test déjà existant, on ne créer ni le CT ni les steps.
    		 idCasTest = nomCasTest.substring(1);
    	 } else {
    		 // On extrait les step que si on est en train de créer un cas de test, sinon on passe directement à l'étape suivante.
    		 idCasTest = this.synchroCasDeTest(nomCasTest, descriptionCT, emetteurCT, proprietaireCT, applicationCT, idTestFolder);	 

	    	 // Extraction des steps
    		 String nom = "";
    		 String description = "";
    		 String expected = "";
    		 boolean nomExtrait = false;
    		 // On parcours les lignes suivantes pour connaitre le contenu des steps du cas de test.
    		 while(iterator.hasNext()) {
    			 // On extrait une ligne
    			 ligne = iterator.next();
    			 // On ne traite pas les lignes débutant par un signe "="
    			 //if (!ligne.startsWith("=") && !ligne.startsWith("@")) {
			    		if (ligne.startsWith("A :")) {
	    				 	// Si la ligne débute par un A on alimente la description
			    			if (!"".equals(description)) {
			    				description = description.concat("\n" + ligne.replaceFirst("A :", ""));
			    			} else {
			    				description = ligne;
			    			}
			    		} else if (ligne.startsWith("R :")) {
	    				 	// Si la ligne débute par un R on alimente le résultat
			    			if (!"".equals(expected)) {
			    				expected = expected.concat("\n" + ligne.replaceFirst("R :", ""));
			    			} else {
			    				expected = ligne;
			    			}
			    		} else if (!nomExtrait) {
			    			// Si la ligne ne débute ni par un A ni par un R, c'est qu'il s'agit du nom du step
			    			nom = ligne;
			    			nomExtrait = true;
			    		} else {
			    			// Si on arrive ici c'est qu'on à rencontrer une deuxième ligne sans préfixe (qui est donc le nom du prochain step)
			    			// On créer donc le step courant avec les infos extraites
			    			this.synchroDesignStep(nom, description, expected, idCasTest);

			    			// Si la ligne en cours d'analyse contient les symbole interdit, on sort.
			    			if (!ligne.startsWith("=") && !ligne.startsWith("@")) {
				    			// Ensuite on utilise la ligne pour alimiter le nom du prochain step et on réinitialise les description et résultats
				    			nom = ligne;
				    			description = "";
				    			expected = "";
				    			nomExtrait = true;
			    			} else {
			    				break;
			    			}
			    		}
			    		
			    		
    			 //}
    		 }
    		
    	 }
    	 // Rattachement du cas de test au scenario
    	 return idCasTest;
     }
     
     
	 // Extraction des steps
//    	 while(iterator.hasNext()) {		 
//    		 // Les lignes marchent par trois : Nom, Action (Description), Resultat (Excepted)
//	    	String nom = iterator.next();
//	    	if (!nom.startsWith("=")) {
//	    		 
//	    		 String description = "";
//	    		 String expected = "";
//	    		 ligne = iterator.next();
//	    		 if (ligne.startsWith("A")) {
//	    			 description = ligne; 
//	    		 }
//	    		 ligne = iterator.next();
//	    		 if (ligne.startsWith("R")) {
//	    			 expected = ligne; 
//	    		 } 
//	    		 // On effectue la synchronisation pour le design step
//	    		 this.synchroDesignStep(nom, description, expected, idCasTest);
//    		 }
//    	 } 	 
     
//     static List<Entity> extraireStep(String chaineCasDeTest) {
//    	 // EXEMPLE :
//			//    	 Nom du cas de test
//			//    	 <Nom du step1>
//			//    	 A : Décrire l'action à mener (description du step)
//			//    	 R : Décrire le résultat attendu (attendu du step)
//			//    	 <Nom du step2>
//			//    	 A : Décrire l'action à mener (description du step)
//			//    	 R : Décrire le résultat attendu (attendu du step)
//    	 // On initialise une liste ordonnée pour stocker le nom du cas de test et les steps.
//    	 List<Entity> retour = new LinkedList<Entity>();
//    	 // Récupération des lignes de la description via un découpage du paragraphe
//    	 String[] lignes = chaineCasDeTest.split("/n");
//    	 List<String> listeLigne = Arrays.asList(lignes); 
//    	 String ligne = "";
//    	 Iterator<String> iterator =  listeLigne.iterator();
//    	 // Extraction du nom du cas de test
//    	 String nomCasTest = iterator.next();
//    	 // Extraction des steps
//    	 while(iterator.hasNext()) {		 
//    		 // Les lignes marchent par trois : Nom, Action (Description), Resultat (Excepted)
//    		 String nom = iterator.next();
//    		 String description = "";
//    		 String expected = "";
//    		 ligne = iterator.next();
//    		 if (ligne.startsWith("A")) {
//    			 description = ligne; 
//    		 }
//    		 ligne = iterator.next();
//    		 if (ligne.startsWith("R")) {
//    			 expected = ligne; 
//    		 }
//    		Entity step = new Entity();	
//        	step.setType("design-step");
//        	step.ajouterChamp("name", StringEscapeUtils.escapeHtml4(nom));
//        	step.ajouterChamp("description", encode(description, hexHtmlEncoder));
//        	step.ajouterChamp("expected", encode(expected, hexHtmlEncoder));
//        	//step.ajouterChamp("parent-id", idTest);
//        	retour.add(step);
//    	 }
//    	 
//    	 return retour;
//     }
     
     
     /**
      * Fonction qui encore une chaine de caractère pour qu'elle soit compatible avec ALM.
      * @param str la chaine à encoder.
      * @param encoder l'encodeur à utiliser.
      * @return la chaine une fois encoder.
      */
     static private String encode(String str, CharEncoder encoder)
     {
         StringBuilder buff = new StringBuilder();
         for ( int i = 0; i < str.length(); i++)
             encoder.encode(str.charAt(i), buff);
         return ""+buff;
     }
     
     /**
      * Fonction qui encore une chaine de caractère pour qu'elle soit compatible avec ALM en utilisant l'encodeur hexhtml.
      * @param str la chaine à encoder.
      * @return la chaine une fois encoder.
      */
     static public String encode(String str)
     {
    	 return encode(str, hexHtmlEncoder);
     }
     
     /**
      * Classe interne permetant l'encodage des caractères.
      * @author levieilfa
      *
      */
     private static class CharEncoder
     {
    	 /**
    	  * Le préfixe à utilisé.
    	  */
         String prefix; 
         /**
          * Le suffixe à utilisé.
          */
         String suffix;
         /**
          * La base à utilisée.
          */
         int radix;
         
         /**
          * Constructeur pour l'encodeur.
          * @param prefix le préfixe
          * @param suffix le suffixe
          * @param radix la base
          */
         public CharEncoder(String prefix, String suffix, int radix)        {
             this.prefix = prefix;
             this.suffix = suffix;
             this.radix = radix;
         }
         
         /**
          * Fonction qui effectue l'encodage pour un char.
          * @param c le char à encoder
          * @param buff le constructeur de string qui va accueuillir le char une fois encodé.
          */
         void encode(char c, StringBuilder buff)     {
             buff.append(prefix).append(Integer.toString(c, radix)).append(suffix);
         }
     }

	public RestConnector getCon() {
		return con;
	}

	public void setCon(RestConnector con) {
		this.con = con;
	}

     
}
