package outils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import beans.CasEssaiBean;
import beans.ObjectifBean;
import net.sf.jxls.transformer.XLSTransformer;
import constantes.Erreurs;
import exceptions.SeleniumException;

/**
 * Classe d'outil pour la manipulation des fichier XLS dans le cadre des tests.
 * @author fabien.levieil
 *
 */
public class XLSOutils {

	/**
	 * Renseigne le fichier excel d'export avec les donn�es issues du cas d'essai et des webservices.
	 * @param casEssai le cas d'essai en cours.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static void renseignerExcel(CasEssaiBean casEssai) throws SeleniumException {

		// Sauvegarde du cas d'essai sous forme de fichier serialiser.
		//sauvegarderCasEssai(casEssai);
		
		// Remplissage de la map de donn�es necessaire � la g�n�ration d fichier excel d'export de cas d'essai.
		// Le type est raw pour des raison de compatibilit� avec la librairie de g�n�ration d'excel.
		// Toutes les captures d'�cran sont dans le repertoire capture, quelques soient leurs origines, inutile de verifier les chemins.
		if (casEssai != null) {
			String chemin = "." + File.separator + "captures" + File.separator;
	        Map<String, CasEssaiBean> beans = new HashMap<String, CasEssaiBean>();
	        // On parcours les objectifs stock�s dans le driver.
	        for (ObjectifBean objectif : casEssai.getParcours().values()) {
	        	objectif.setCapture(chemin + objectif.getClefUnique() + ".png");
	        }
	        // On parcours les objectifs stock�s dans le cas d'essai.
	        for (ObjectifBean objectif : casEssai.getObjectifs().values()) {
	        	objectif.setCapture(chemin + objectif.getClefUnique() + ".png");
	        }
	        beans.put("casEssai", casEssai);

	        XLSOutils.generationExcel("exportTemplate.xls", ".\\" + casEssai.getNomCasEssai() + "\\export" + casEssai.getNomCasEssai() + ".xls", beans);
		}
		
	}
	
	/**
	 * G�n�re un fichier excel associ� au cas d'essai pass� en param�tre.
	 * @param template nom du template , exemple : exportTemplate.xls
	 * @param nom le nom du fichier de sortie.
	 * @param beans les informations � envoyer dans le fichier.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static void generationExcel(String template, String nom, Map beans) throws SeleniumException {
		System.out.println("Debut de renseignement excel.");	
        XLSTransformer transformer = new XLSTransformer();
        try {
			transformer.transformXLS(template, beans, nom);
			System.out.println("Fin de renseignement excel.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E011);
		}
	}
	
	
}
