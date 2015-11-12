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

import constantes.Erreurs;
import exceptions.SeleniumException;

public class PropertiesOutil {
	
	/**
	 * Le nom du fichier properties.
	 */
	public static String fichierProperties ="selenium.properties";
	
	/**
	 * Constructeur de l'outil fixant le nom du properties.
	 * @param nom le nom du fichier de properties contenu � la racine du projet de test.
	 */
	public PropertiesOutil(String nom) {
		fichierProperties = nom;
	}
	
	/**
	 * R�cup�re les infos du fichier de propri�t�s.
	 * @param clef la clef � rechercher dans le fichier de propri�t�s.
	 * @return la chain associ�e � la clef pass�e en param�tre.
	 * @throws SeleniumException en cas d'erreur (notament absence du fichier de propri�t� dans le m�me r�pertoire que le fichier jar)
	 */
	public static String getInfo(final String clef) throws SeleniumException {
		try {
			// Acc�de au fichier de properties et extrait le libelle associ� au code demand�.
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
			// Si le fichier n'est pas trouv� on lance une erreur.
			throw new SeleniumException(Erreurs.E010, "V�rifiez la pr�sence du fichier de propri�t�es. " + new File(getProperties()).getAbsolutePath());
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
