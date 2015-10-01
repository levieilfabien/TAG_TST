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
