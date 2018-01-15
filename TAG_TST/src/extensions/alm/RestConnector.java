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
 * Cette classe sert d'interface pour la connexion � ALM. 
 * Elle stocke les informations de connexion essentielles.
 * @author levieilfa
 *
 */
public class RestConnector {

    protected Map<String, String> cookies;
    /**
     * Il s'agit de l'url vers le serveur ALM (sans slash � la fin) 
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
     * L'instance de connecteur REST qui sera manipul�e. Elle est initialis�e � vide.
     */
    private static RestConnector instance = new RestConnector();

    /**
     * Initialise le connecteur de service REST ALM.
     * @param cookies les cookies � ajouter lors de l'acc�s au service REST.
     * @param serverUrl l'url de serveur de connexion
     * @param domain le domaine 
     * @param project le projet
     * @return le connecteur initialis� et paramt�tr�.
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
     * @param cookies les cookies � ajouter lors de l'acc�s au service REST.
     * @param serverUrl l'url de serveur de connexion
     * @return le connecteur initialis� et paramt�tr�.
     */
    public RestConnector init(Map<String, String> cookies, String serverUrl) {
        this.cookies = cookies;
        this.serverUrl = serverUrl;
        return this;
    }

    
    /**
     * Constructeur priv� sans param�tre n'initialisant pas les crit�res de connexions.
     */
    private RestConnector() {
    	
    }

    /**
     * Permet d'obtenir l'instance initialis�e.
     * @return l'instance de connecteur REST.
     */
    public static RestConnector getInstance() {
        return instance;
    }

    /**
     * Construit une url permettant de consulter la liste des entit�es disponibles pour un type donn�e.
     * Cette url est utilis�e pour les connexions � ALM.
     * @param entityType le type d'entit� (sans "s") que l'on souhaites consulter.
     * @return l'url permettant l'acc�s � la liste d'entit�.
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
     * Construit une url permettant de consulter la liste des entit�es disponibles pour un type donn�e.
     * Cette url est utilis�e pour les connexions � Confluence.
     * @param entityType le type d'entit� (ex : content, user) que l'on souhaites consulter.
     * @return l'url permettant l'acc�s � la liste d'entit�.
     */
    public String buildConfluenceEntityCollectionUrl(String entityType) {
    	// Si on pointe d�j� sur l'API, inutile de rajout� le suffixe.
    	if (serverUrl.endsWith("api")) {
    		return buildUrl(entityType);
    	} else {
    		return buildUrl("rest/api/" + entityType);
    	}
        
    }

    /**
     * Fonction construisante une URL vers ALM.
     * @param path le chemin � ajouter � l'url du serveur pour acc�der � la ressource demand�e.
     * @return l'url compl�t�e avec le param�tre path.
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
     * Renvoie l'url vers le serveur ALM param�tr�e dans le connecteur.
     * @return l'url vers le serveur.
     */
    public String getServerUrl() {
		return serverUrl;
	}

    /**
     * Renvoie le domaine param�tr� lors de l'initialisation.
     * @return le domaine
     */
	public String getDomain() {
		return domain;
	}

    /**
     * Renvoie le projet param�tr� lors de l'initialisation.
     * @return le projet
     */
	public String getProject() {
		return project;
	}

	/**
     * Effectue une commande HTTP Put.
     * @param url l'url destinatrice du PUT.
     * @param data les donn�es � transmettre dans le PUT.
     * @param headers les ent�tes � transmettre dans le PUT.
     * @return la r�ponse HTTP au PUT envoy�.
     * @throws Exception en cas d'erreur.
     */
    public Response httpPut(String url, byte[] data, Map<String, String> headers) throws Exception {
        return doHttp("PUT", url, null, data, headers, cookies);
    }

    /**
     * Effectue une commande HTTP POST.
     * @param url l'url destinatrice du POST.
     * @param data les donn�es � transmettre dans le POST.
     * @param headers les ent�tes � transmettre dans le POST.
     * @return la r�ponse HTTP au PUT envoy�.
     * @throws Exception en cas d'erreur.
     */
    public Response httpPost(String url, byte[] data, Map<String, String> headers) throws Exception {
        return doHttp("POST", url, null, data, headers, cookies);
    }

    /**
     * Effectue une requ�te de type DELETE pour des ent�tes donn�es.
     * @param url l'url destinatrice du DELETE.
     * @param headers les ent�tes contenant les informations relative au DELETE.
     * @return la r�ponse HTTP � la requ�te.
     * @throws Exception en cas d'erreur.
     */
    public Response httpDelete(String url, Map<String, String> headers) throws Exception {
        return doHttp("DELETE", url, null, null, headers, cookies);
    }

    /**
     * Requ�te de type GET pour obtenir des informations � partir d'une URL Donn�e.
     * @param url l'url destinatrice du GET
     * @param queryString la requ�te � envoy�e via le GET
     * @param headers les ent�tes � utilis�s lors du GET.
     * @return la r�ponse HTTP suite � la requ�te GET.
     * @throws Exception en cas d'erreur.
     */
    public Response httpGet(String url, String queryString, Map<String, String> headers)throws Exception {
        return doHttp("GET", url, queryString, null, headers, cookies);
    }

    /**
     * Effectue une requ�te HTTP � partir des informations pass�es en param�tre.
     * @param type le type de requ�te (GET, PUT, POST, DELETE)
     * @param url l'url sur laquelle on travaille
     * @param queryString la requ�te pour les requ�te de type GET (null sinon)
     * @param data les donn�es � transmettre si la requ�te est une �criture (pour POST et PUT, null sinon)
     * @param headers les ent�tes � utilis�s lors des requ�tes.
     * @param cookies les cookies � utiliser dans la requ�te et � mettre � jour depuis la r�ponse
     * @return http response http � la requ�te.
     * @throws Exception en cas d'erreur.
     */
    private Response doHttp(String type, String url, String queryString, byte[] data, Map<String, String> headers, Map<String, String> cookies) throws Exception {
    	
    	// Si une requ�te est ajout�e, elle est mise apr�s un caract�re "?" en fin d'URL
        if ((queryString != null) && !queryString.isEmpty()) {
            url += "?" + queryString;
        }
        Response ret;

        if (url.startsWith("https")) {
	        HttpsURLConnection con;
	        // On initialise la connexion et on positionne le type de requ�te
	        if (proxy != null) {
	        	con = (HttpsURLConnection) new URL(url).openConnection(proxy);
	        } else {
	        	con = (HttpsURLConnection) new URL(url).openConnection();
	        }
	        con.setConnectTimeout(15000);
	        con.setRequestMethod(type);
	        // On r�cup�re les cookie et on pr�pare la requ�te � partir des donn�es data et headers
	        String cookieString = getCookieString();
	        prepareHttpRequest(con, headers, data, cookieString);
	        
	        // On effectue la requ�te et on r�cup�re le retour
	        con.connect();
	        ret = retrieveHtmlResponse(con);
        } else {
	        HttpURLConnection con;
	        // On initialise la connexion et on positionne le type de requ�te
	        if (proxy != null) {
	        	con = (HttpURLConnection) new URL(url).openConnection(proxy);
	        } else {
	        	con = (HttpURLConnection) new URL(url).openConnection();
	        }
	        con.setConnectTimeout(15000);
	        con.setRequestMethod(type);
	        // On r�cup�re les cookie et on pr�pare la requ�te � partir des donn�es data et headers
	        String cookieString = getCookieString();
	        prepareHttpRequest(con, headers, data, cookieString);
	        
	        // On effectue la requ�te et on r�cup�re le retour
	        con.connect();
	        ret = retrieveHtmlResponse(con);
        }
        updateCookies(ret);

        return ret;
    }

    /**
     * Pr�pare la requ�te HTTP � partir des informations connues.
     * @param con la connexion qui va accueillir les donn�es et ent�tes
     * @param headers les ent�tes � utilis�s lors de la requ�te (tel que le content-type)
     * @param bytes les donn�es � transmettre (post)
     * @param cookieString les donn�es de cookie identifi�e c�t� client, telles que lwsso, qcsession, jsession etc.
     * @throws java.io.IOException en cas d'erreur
     */
    private void prepareHttpRequest(HttpURLConnection con, Map<String, String> headers, byte[] bytes, String cookieString) throws IOException {
        String contentType = null;

        // Si il existe des cookie, on les ajoute � la requ�te
        if ((cookieString != null) && !cookieString.isEmpty()) {
            con.setRequestProperty("Cookie", cookieString);
        }

        // On r�cup�re le contenu des ent�tes pour l'envoyer
        if (headers != null) {
            // Par d�faut on retire l'ent�te content-type  - Celui ci ne devrais �tre envoy� que si on compte r�llement envoyer des donn�es.
            contentType = headers.remove("Content-Type");
            // Pour permettre d'utiliser ALM 12 on le fait qu'on accepte les fichiers XML => N�cessaire lors de l'obtention d'une session QC.
            //headers.put("Accept", "application/xml");
            //headers.put("Accept", "application/json");
            // On traite les diff�rents ent�tes pour les ajouter � la connexion.
            Iterator<Entry<String, String>> headersIterator = headers.entrySet().iterator();
            while (headersIterator.hasNext()) {
                Entry<String, String> header = headersIterator.next();
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            
        }

        //Map<String, List<String>> test = con.getRequestProperties();
        
        // Si il y a des donn�es � transmettre on les traites ici et on remet le content type.
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
     * Permet d'obtenir la r�ponse HTML suite � une requ�te HTTP.
     * @param con la connexion d�j� effectu�e pour laquelle une r�ponse existe.
     * @return une r�posnse � la requ�te d�j� effectu�e
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
     * Met � jour les cookie du connecteur � partir des �l�ments pr�sents dans la r�ponse.
     * @param response la r�ponse � la requ�te pr�c�dement effectu�e.
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
     * Permet d'obtenir la chaine de caract�re issues des cookies du connecteur.
     * @return la chaine de caract�re issues des cookies.
     */
    public String getCookieString() {
        StringBuilder sb = new StringBuilder();
        // Si il existe des cookie on r�cup�re les informations contenues dans ceux ci et on les restitue sous forme de chaine.
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
     * La fonction pr�cise dans les ent�tes qu'on va manipuler des fichiers XML lors des �changes.
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