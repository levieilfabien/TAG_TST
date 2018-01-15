package beans;

import outils.PropertiesOutil;

/**
 * Singleton initialisé avec les informations de connection ALM & CONFLUENCE.
 * @author levieilfa
 *
 */
public class RESTBean {

	// Constantes relatives à la configuration d'ALM
	public static final String LOGIN_ALM = PropertiesOutil.getInfoConstante("ALM.login");
	public static final String PASSWORD_ALM = PropertiesOutil.getInfoConstante("ALM.password");
	public static final String PROJECT_ALM = PropertiesOutil.getInfoConstante("ALM.project");
	public static final String DOMAIN_ALM = PropertiesOutil.getInfoConstante("ALM.domain");
	public static final String URL_ALM = PropertiesOutil.getInfoConstante("ALM.url");
	
	// Constante relative à l'environement de travail.
	public static final String ENVIRONEMENT = PropertiesOutil.getInfoConstante("EnvTestPreConstruit");
	
	// Constantes relatives à la configuration de CONFLUENCE
	public static final String LOGIN_CONFLUENCE = PropertiesOutil.getInfoConstante("CONFLUENCE.login");
	public static final String PASSWORD_CONFLUENCE = PropertiesOutil.getInfoConstante("CONFLUENCE.password");
	public static final String IDPAGE_CONFLUENCE = PropertiesOutil.getInfoConstante("CONFLUENCE.idPage");
	public static final String URL_CONFLUENCE = PropertiesOutil.getInfoConstante("CONFLUENCE.url");
	
	// Constantes relatives au proxy
	public static final String URL_PROXY = PropertiesOutil.getInfoConstante("PROXY.url");
}
