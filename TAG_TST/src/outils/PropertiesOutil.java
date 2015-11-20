package outils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import beans.CasEssaiBean;
import constantes.Erreurs;
import exceptions.SeleniumException;

public class PropertiesOutil {
	
	/**
	 * Le nom du fichier properties.
	 */
	public static String fichierProperties ="selenium.properties";
	
	/**
	 * Constructeur de l'outil fixant le nom du properties.
	 * @param nom le nom du fichier de properties contenu à la racine du projet de test.
	 */
	public PropertiesOutil(String nom) {
		fichierProperties = nom;
	}
	
	/**
	 * Fonction pour l'initialisation d'une constante.
	 * ATTENTION : Cette fonction bypass la gestion d'erreur, à n'utiliser qu'en cas de certitude de la disponibilité de la propriété.
	 * @param clef la clef dans le fichier properties.
	 * @return la valeur associée à la clef ou null si la valeur n'est pas trouvée.
	 */
	public static String getInfoConstante(final String clef) {
		try {
			return getInfo(clef);
		} catch (SeleniumException e) {
			return "";
		}
	}
	
	/**
	 * Renvoie la clef associée à l'environement par défaut.
	 * ATTENTION : Cette fonction bypass la gestion d'erreur, à n'utiliser qu'en cas de certitude de la disponibilité de la propriété.
	 * @param clef la clef à recupérée.
	 * @return la chaine résultat.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static String getInfoEnvConstante(String clef) {
		try {
			return getInfoEnv(clef, null);
		} catch (SeleniumException e) {
			return "";
		}
	}
	
	/**
	 * Renvoie la clef associée à l'environement par défaut qui est passer en paramètre.
	 * Cet environement doit exister dans le fichier properties et doit respecter le format EnvTestPreConstruit = <Valeur>.
	 * @param clef la clef à recupérée on fera <clef>.
	 * @return la chaine résultat.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static String getInfoEnv(String clef, CasEssaiBean casEssai) throws SeleniumException {
		String envDefaut = "";
		if (casEssai != null && casEssai.getEnvironement() != null) {
			// Si l'environement est préciser dans le cas d'essai, on l'utilise comme environement de référence.
			envDefaut = getInfo(casEssai.getEnvironement().getNom());
		} else {
			// Sinon on récupère celui dans la propriété "EnvTestPreConstruit" du properties..
			envDefaut = getInfo("EnvTestPreConstruit");
		}
		if (envDefaut != null && !"".equals(envDefaut)) {
			// On recupère la clef associée à l'environement trouver.
			return getInfo(envDefaut + "." + clef);
		} else {
			return getInfo("RECETTE." + clef);
		}
	}
	
	/**
	 * Renvoie la clef associée à l'environement par défaut.
	 * @param clef la clef à recupérée.
	 * @return la chaine résultat.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static String getInfoEnv(String clef) throws SeleniumException {
		return getInfoEnv(clef, null);
	}
	
	/**
	 * Récupère les infos du fichier de propriétés.
	 * @param clef la clef à rechercher dans le fichier de propriétés.
	 * @return la chain associée à la clef passée en paramètre.
	 * @throws SeleniumException en cas d'erreur (notament absence du fichier de propriété dans le même répertoire que le fichier jar)
	 */
	public static String getInfo(final String clef) throws SeleniumException {
		try {
			// Accède au fichier de properties et extrait le libelle associé au code demandé.
		    Properties properties = new Properties();
		    String fichier = getProperties();
		    String cheminJar = "";
		    String retour = null;
		    ZipEntry entry  = null;
		    File file = new File(fichier);
		    JarFile zip = null;
		    InputStream fip = null;
		   
		    if (!file.exists()) {
		    	// Si le fichier est un fichier dans un jar , il faut l'extraire.
		    	if (fichier.contains("!")) {
		    		cheminJar = fichier.replace("file:/", "");
		    		cheminJar = cheminJar.split("!")[0];
		    		zip = new JarFile(new File(cheminJar));
		    		entry = zip.getEntry(fichierProperties);
		    		fip = zip.getInputStream(entry);
		    	}
		    } else {
		    	fip = new FileInputStream(fichier);
		    }
		    // Si on a bien ouvert le flux vers l'objet on extrait la properties.
		    if (fip != null) {
		    	properties.load(fip);
		    	retour = properties.getProperty(clef);
		    	System.out.println(clef + " = " + retour);	
		    	if (zip != null) {
		    		zip.close();
		    	}
				return properties.getProperty(clef);
		    } else {
		    	System.out.println(clef + " = " + retour);
		    	if (zip != null) {
		    		zip.close();
		    	}
		    	return retour;
		    }
		} catch (IOException e) {
			e.printStackTrace();
			// Si le fichier n'est pas trouvé on lance une erreur.
			throw new SeleniumException(Erreurs.E010, "Vérifiez la présence du fichier de propriétées. " + new File(getProperties()).getAbsolutePath());
		}
	}
	
	/**
	 * Permet d'obtenir le chemin vers le fichier de properties.
	 * @return le chemin vers le fichier de properties.
	 */
	@SuppressWarnings("deprecation")
	private static String getProperties() {
		String retour = PropertiesOutil.class.getClassLoader().getResource(fichierProperties).getFile().substring(1);
		try {
			retour = URLDecoder.decode(retour, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			retour = URLDecoder.decode(retour);
		}
		return retour;
	}
	
	/**
	 * Permet d'obtenir le chemin vers le repertoire de properties.
	 * @return le chemin vers le fichier de properties.
	 */
	public static String getRepertoireProjet() {
		return new File("").getAbsolutePath();
	}
}
