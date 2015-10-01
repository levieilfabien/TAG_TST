package outils;

import java.util.Map;

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
	 * Génère un fichier excel associé au cas d'essai passé en paramètre.
	 * @param template nom du template , exemple : exportTemplate.xls
	 * @param nom le nom du fichier de sortie.
	 * @param beans les informations à envoyer dans le fichier.
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
