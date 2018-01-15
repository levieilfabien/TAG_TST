package extensions.alm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;


/**
 * Cette classe sert d'interface pour la connexion à ALM. 
 * Elle stocke les informations de connexion essentielles.
 * @author levieilfa
 *
 */
public class RestConnector {

    protected Map<String, String> cookies;
    /**
     * Il s'agit de l'url vers le serveur ALM (sans slash à la fin) 
     * Ex : http://myhost:8080/qcbin
     */
    protected String serverUrl;
    /**
     * Le domaine sur lequel on se connecte.
     */
    protected String domain;
    /**
     * Le projet sur lequel on se connecte.
     */
    protected String project;
    /**
     * Le proxy si il existe.
     */
    protected Proxy proxy = null;
    /**
     * L'instance de connecteur REST qui sera manipulée. Elle est initialisée à vide.
     */
    private static RestConnector instance = new RestConnector();

    /**
     * Initialise le connecteur de service REST ALM.
     * @param cookies les cookies à ajouter lors de l'accès au service REST.
     * @param serverUrl l'url de serveur de connexion
     * @param domain le domaine 
     * @param project le projet
     * @return le connecteur initialisé et paramtètré.
     */
    public RestConnector init(Map<String, String> cookies, String serverUrl, String domain, String project) {
        this.cookies = cookies;
        this.serverUrl = serverUrl;
        this.domain = domain;
        this.project = project;

        return this;
    }

    /**
     * Initialise le connecteur de service REST Confluence.
     * @param cookies les cookies à ajouter lors de l'accès au service REST.
     * @param serverUrl l'url de serveur de connexion
     * @return le connecteur initialisé et paramtètré.
     */
    public RestConnector init(Map<String, String> cookies, String serverUrl) {
        this.cookies = cookies;
        this.serverUrl = serverUrl;
        return this;
    }

    
    /**
     * Constructeur privé sans paramètre n'initialisant pas les critères de connexions.
     */
    private RestConnector() {
    	
    }

    /**
     * Permet d'obtenir l'instance initialisée.
     * @return l'instance de connecteur REST.
     */
    public static RestConnector getInstance() {
        return instance;
    }

    /**
     * Construit une url permettant de consulter la liste des entitées disponibles pour un type donnée.
     * Cette url est utilisée pour les connexions à ALM.
     * @param entityType le type d'entité (sans "s") que l'on souhaites consulter.
     * @return l'url permettant l'accès à la liste d'entité.
     */
    public String buildALMEntityCollectionUrl(String entityType) {
        return buildUrl("rest/domains/"
                        + domain
                        + "/projects/"
                        + project
                        + "/"
                        + entityType
                        + "s");
    }
    
    /**
     * Construit une url permettant de consulter la liste des entitées disponibles pour un type donnée.
     * Cette url est utilisée pour les connexions à Confluence.
     * @param entityType le type d'entité (ex : content, user) que l'on souhaites consulter.
     * @return l'url permettant l'accès à la liste d'entité.
     */
    public String buildConfluenceEntityCollectionUrl(String entityType) {
    	// Si on pointe déjà sur l'API, inutile de rajouté le suffixe.
    	if (serverUrl.endsWith("api")) {
    		return buildUrl(entityType);
    	} else {
    		return buildUrl("rest/api/" + entityType);
    	}
        
    }

    /**
     * Fonction construisante une URL vers ALM.
     * @param path le chemin à ajouter à l'url du serveur pour accèder à la ressource demandée.
     * @return l'url complètée avec le paramètre path.
     */
    public String buildUrl(String path) {
        return String.format("%1$s/%2$s", serverUrl, path);
    }

    /**
     * @return the cookies
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * @param cookies
     *            the cookies to set
     */
    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
    
    /**
     * Renvoie l'url vers le serveur ALM paramètrée dans le connecteur.
     * @return l'url vers le serveur.
     */
    public String getServerUrl() {
		return serverUrl;
	}

    /**
     * Renvoie le domaine paramètré lors de l'initialisation.
     * @return le domaine
     */
	public String getDomain() {
		return domain;
	}

    /**
     * Renvoie le projet paramètré lors de l'initialisation.
     * @return le projet
     */
	public String getProject() {
		return project;
	}

	/**
     * Effectue une commande HTTP Put.
     * @param url l'url destinatrice du PUT.
     * @param data les données à transmettre dans le PUT.
     * @param headers les entêtes à transmettre dans le PUT.
     * @return la réponse HTTP au PUT envoyé.
     * @throws Exception en cas d'erreur.
     */
    public Response httpPut(String url, byte[] data, Map<String, String> headers) throws Exception {
        return doHttp("PUT", url, null, data, headers, cookies);
    }

    /**
     * Effectue une commande HTTP POST.
     * @param url l'url destinatrice du POST.
     * @param data les données à transmettre dans le POST.
     * @param headers les entêtes à transmettre dans le POST.
     * @return la réponse HTTP au PUT envoyé.
     * @throws Exception en cas d'erreur.
     */
    public Response httpPost(String url, byte[] data, Map<String, String> headers) throws Exception {
        return doHttp("POST", url, null, data, headers, cookies);
    }

    /**
     * Effectue une requête de type DELETE pour des entêtes données.
     * @param url l'url destinatrice du DELETE.
     * @param headers les entêtes contenant les informations relative au DELETE.
     * @return la réponse HTTP à la requête.
     * @throws Exception en cas d'erreur.
     */
    public Response httpDelete(String url, Map<String, String> headers) throws Exception {
        return doHttp("DELETE", url, null, null, headers, cookies);
    }

    /**
     * Requête de type GET pour obtenir des informations à partir d'une URL Donnée.
     * @param url l'url destinatrice du GET
     * @param queryString la requête à envoyée via le GET
     * @param headers les entêtes à utilisés lors du GET.
     * @return la réponse HTTP suite à la requête GET.
     * @throws Exception en cas d'erreur.
     */
    public Response httpGet(String url, String queryString, Map<String, String> headers)throws Exception {
        return doHttp("GET", url, queryString, null, headers, cookies);
    }

    /**
     * Effectue une requête HTTP à partir des informations passées en paramètre.
     * @param type le type de requête (GET, PUT, POST, DELETE)
     * @param url l'url sur laquelle on travaille
     * @param queryString la requête pour les requête de type GET (null sinon)
     * @param data les données à transmettre si la requête est une écriture (pour POST et PUT, null sinon)
     * @param headers les entêtes à utilisés lors des requêtes.
     * @param cookies les cookies à utiliser dans la requête et à mettre à jour depuis la réponse
     * @return http response http à la requête.
     * @throws Exception en cas d'erreur.
     */
    private Response doHttp(String type, String url, String queryString, byte[] data, Map<String, String> headers, Map<String, String> cookies) throws Exception {
    	
    	// Si une requête est ajoutée, elle est mise après un caractère "?" en fin d'URL
        if ((queryString != null) && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        Response ret;

        if (url.startsWith("https")) {
	        HttpsURLConnection con;
	        // On initialise la connexion et on positionne le type de requête
	        if (proxy != null) {
	        	con = (HttpsURLConnection) new URL(url).openConnection(proxy);
	        } else {
	        	con = (HttpsURLConnection) new URL(url).openConnection();
	        }
	        con.setConnectTimeout(15000);
	        con.setRequestMethod(type);
	        // On récupère les cookie et on prépare la requête à partir des données data et headers
	        String cookieString = getCookieString();
	        prepareHttpRequest(con, headers, data, cookieString);
	        
	        // On effectue la requête et on récupère le retour
	        con.connect();
	        ret = retrieveHtmlResponse(con);
        } else {
	        HttpURLConnection con;
	        // On initialise la connexion et on positionne le type de requête
	        if (proxy != null) {
	        	con = (HttpURLConnection) new URL(url).openConnection(proxy);
	        } else {
	        	con = (HttpURLConnection) new URL(url).openConnection();
	        }
	        con.setConnectTimeout(15000);
	        con.setRequestMethod(type);
	        // On récupère les cookie et on prépare la requête à partir des données data et headers
	        String cookieString = getCookieString();
	        prepareHttpRequest(con, headers, data, cookieString);
	        
	        // On effectue la requête et on récupère le retour
	        con.connect();
	        ret = retrieveHtmlResponse(con);
        }
        updateCookies(ret);

        return ret;
    }

    /**
     * Prépare la requête HTTP à partir des informations connues.
     * @param con la connexion qui va accueillir les données et entêtes
     * @param headers les entêtes à utilisés lors de la requête (tel que le content-type)
     * @param bytes les données à transmettre (post)
     * @param cookieString les données de cookie identifiée côté client, telles que lwsso, qcsession, jsession etc.
     * @throws java.io.IOException en cas d'erreur
     */
    private void prepareHttpRequest(HttpURLConnection con, Map<String, String> headers, byte[] bytes, String cookieString) throws IOException {
        String contentType = null;

        // Si il existe des cookie, on les ajoute à la requête
        if ((cookieString != null) && !cookieString.isEmpty()) {
            con.setRequestProperty("Cookie", cookieString);
        }

        // On récupère le contenu des entêtes pour l'envoyer
        if (headers != null) {
            // Par défaut on retire l'entête content-type  - Celui ci ne devrais être envoyé que si on compte réllement envoyer des données.
            contentType = headers.remove("Content-Type");
            // Pour permettre d'utiliser ALM 12 on le fait qu'on accepte les fichiers XML => Nécessaire lors de l'obtention d'une session QC.
            //headers.put("Accept", "application/xml");
            //headers.put("Accept", "application/json");
            // On traite les différents entêtes pour les ajouter à la connexion.
            Iterator<Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Entry<String, String> header = headersIterator.next();
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            
        }

        //Map<String, List<String>> test = con.getRequestProperties();
        
        // Si il y a des données à transmettre on les traites ici et on remet le content type.
        if ((bytes != null) && (bytes.length > 0)) {
            con.setDoOutput(true);
            //con.setDoInput(true);
            if (contentType != null) {
                con.setRequestProperty("Content-Type", contentType);
            }

            OutputStream out = con.getOutputStream();
            out.write(bytes);
            out.flush();
            out.close();
        }
    }

    /**
     * Permet d'obtenir la réponse HTML suite à une requête HTTP.
     * @param con la connexion déjà effectuée pour laquelle une réponse existe.
     * @return une réposnse à la requête déjà effectuée
     * @throws Exception en cas d'erreur
     */
    private Response retrieveHtmlResponse(HttpURLConnection con) throws Exception {

        Response ret = new Response();

        ret.setStatusCode(con.getResponseCode());
        ret.setResponseHeaders(con.getHeaderFields());

        InputStream inputStream;
        //select the source of the input bytes, first try 'regular' input
        try {
            inputStream = con.getInputStream();
        }

        /*
         If the connection to the server somehow failed, for example 404 or 500,
         con.getInputStream() will throw an exception, which we'll keep.
         We'll also store the body of the exception page, in the response data.
         */
        catch (Exception e) {

            inputStream = con.getErrorStream();
            ret.setFailure(e);
        }

        // This actually takes the data from the previously set stream
        // (error or input) and stores it in a byte[] inside the response
        ByteArrayOutputStream container = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int read;
        while ((read = inputStream.read(buf, 0, 1024)) > 0) {
            container.write(buf, 0, read);
        }

        ret.setResponseData(container.toByteArray());

        return ret;
    }

    /**
     * Met à jour les cookie du connecteur à partir des éléments présents dans la réponse.
     * @param response la réponse à la requête précédement effectuée.
     */
    private void updateCookies(Response response) {

        Iterable<String> newCookies = response.getResponseHeaders().get("Set-Cookie");
        if (newCookies != null) {

            for (String cookie : newCookies) {
                int equalIndex = cookie.indexOf('=');
                int semicolonIndex = cookie.indexOf(';');

                String cookieKey = cookie.substring(0, equalIndex);
                String cookieValue = cookie.substring(equalIndex + 1, semicolonIndex);

                if (cookies == null) {
                	cookies = new HashMap<String, String>();
                }
                
                cookies.put(cookieKey, cookieValue);
            }
        }
    }

    /**
     * Permet d'obtenir la chaine de caractère issues des cookies du connecteur.
     * @return la chaine de caractère issues des cookies.
     */
    public String getCookieString() {
        StringBuilder sb = new StringBuilder();
        // Si il existe des cookie on récupère les informations contenues dans ceux ci et on les restitue sous forme de chaine.
        if (cookies != null && !cookies.isEmpty()) {
            Set<Entry<String, String>> cookieEntries = cookies.entrySet();
            for (Entry<String, String> entry : cookieEntries) {
            	sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }
        String ret = sb.toString();
        return ret;
    }
    
    /**
     * Permet d'ouvrir la session QC.
     * La fonction précise dans les entêtes qu'on va manipuler des fichiers XML lors des échanges.
     */
    public void getQCSession(){
        String qcsessionurl = this.buildUrl("rest/site-session");
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml; charset=UTF-8");
        requestHeaders.put("Accept", "application/xml");
        try {
            Response resp = this.httpPost(qcsessionurl, null, requestHeaders);
            this.updateCookies(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

    
}