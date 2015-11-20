package beans;

import outils.PropertiesOutil;

/**
 * Singleton initialisé avec les informations de connection ALM.
 * @author levieilfa
 *
 */
public class ALMBean {

	public static final String LOGIN_ALM = PropertiesOutil.getInfoConstante("ALM.login");
	public static final String PASSWORD_ALM = PropertiesOutil.getInfoConstante("ALM.password");
	public static final String PROJECT_ALM = PropertiesOutil.getInfoConstante("ALM.project");
	public static final String DOMAIN_ALM = PropertiesOutil.getInfoConstante("ALM.domain");
	public static final String URL_ALM = PropertiesOutil.getInfoConstante("ALM.url");
}
