package outils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import beans.CasEssaiBean;
import beans.ObjectifBean;
import net.sf.jxls.transformer.XLSTransformer;
import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.SeleniumALMRESTWrapper;
import extensions.alm.Entity;

/**
 * Classe d'outil pour la manipulation des fichier XLS dans le cadre des tests.
 * @author fabien.levieil
 *
 */
public class XLSOutils {

	/**
	 * Renseigne le fichier excel d'export avec les données issues du cas d'essai et des webservices.
	 * @param casEssai le cas d'essai en cours.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static void renseignerExcel(CasEssaiBean casEssai) throws SeleniumException {

		// Sauvegarde du cas d'essai sous forme de fichier serialiser.
		//sauvegarderCasEssai(casEssai);
		
		// Remplissage de la map de données necessaire à la génération d fichier excel d'export de cas d'essai.
		// Le type est raw pour des raison de compatibilité avec la librairie de génération d'excel.
		// Toutes les captures d'écran sont dans le repertoire capture, quelques soient leurs origines, inutile de verifier les chemins.
		if (casEssai != null) {
			String chemin = "." + File.separator + "captures" + File.separator;
	        Map<String, CasEssaiBean> beans = new HashMap<String, CasEssaiBean>();
	        // On parcours les objectifs stockés dans le driver.
	        for (ObjectifBean objectif : casEssai.getParcours().values()) {
	        	objectif.setCapture(chemin + objectif.getClefUnique() + ".png");
	        }
	        // On parcours les objectifs stockés dans le cas d'essai.
	        for (ObjectifBean objectif : casEssai.getObjectifs().values()) {
	        	objectif.setCapture(chemin + objectif.getClefUnique() + ".png");
	        }
	        beans.put("casEssai", casEssai);
	        // Si le repertoire de téléchargement est connu, on met le fichier de rapport dedans.
	        if (casEssai.getRepertoireTelechargement() != null) {
	        	XLSOutils.generationExcel("exportTemplate.xls", casEssai.getRepertoireTelechargement() + "\\export" + casEssai.getNomCasEssai() + ".xls", beans);
	        } else {
	        	XLSOutils.generationExcel("exportTemplate.xls", ".\\" + casEssai.getNomCasEssai() + "\\export" + casEssai.getNomCasEssai() + ".xls", beans);
	        }
		}
		
	}
	
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
			//e.printStackTrace();
			throw new SeleniumException(Erreurs.E011);
		}
	}
	
	public static void extraireInformationALM(String matrice, String positionTestLab, String positionTestPlan, SeleniumALMRESTWrapper wrapper) throws SeleniumException {
        try {
        	// On va stocker les infos
        	List<Entity> retour = new LinkedList<Entity>();
        	// On récupère l'onglet contenant les données utiles
			Workbook workbook = WorkbookFactory.create(new File(matrice));
			Sheet onglet = workbook.getSheet("Extracteur");
			// On parcours les colonnes
			for (int colonne = 0; colonne < 100; colonne ++) {
				// Si la colonne débute par un libellé, alors on lit celle ci sinon on stoppe le traitement.
				Cell titreScenarioCellule = onglet.getRow(0).getCell(colonne);
				if (titreScenarioCellule != null && !"".equals(titreScenarioCellule.getStringCellValue())) {
					// On génère le commentaire à partir des informations connues.
					String commentaire = genererCommentaire(onglet.getRow(1).getCell(colonne), 
							onglet.getRow(2).getCell(colonne),
							onglet.getRow(3).getCell(colonne),
							onglet.getRow(4).getCell(colonne),
							onglet.getRow(5).getCell(colonne),
							onglet.getRow(6).getCell(colonne),
							onglet.getRow(7).getCell(colonne));
					// On créer le scénario (sauf si il existe déjà).
					String scenarioID = wrapper.synchroScenarioDeTest(titreScenarioCellule.getStringCellValue(), positionTestLab, "", commentaire);
					// On explore les lignes relative au scénario afin de déterminer les liens entre SC et CT.
					for (int ligne = 8; ligne < onglet.getPhysicalNumberOfRows(); ligne ++) {
						if (onglet.getRow(ligne).getCell(colonne) != null) {
							String valeur = onglet.getRow(ligne).getCell(colonne).getStringCellValue();
							if (valeur != null && !"".equals(valeur)) {
								// C'est un cas de test, on effectue sa création (sauf si il existe)
								String urlCasTest = wrapper.extrationCasTest(valeur, positionTestPlan);
								String urlInstanceTest = wrapper.synchroInstanceTest("", scenarioID, urlCasTest);
							}
						}
					}
				} else {
					break;
				}
			}
			
			System.out.println("FIN");
			
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}

	private static String genererCommentaire(Cell description, Cell passant, Cell prerequis, Cell JDD, Cell valorisation, Cell environement, Cell distributeur) throws IOException {
		StringBuilder retour = new StringBuilder("<HTML><BODY>");
		//TODO Remplacer par une référence à une ressource
		File modele = new File("c:\\Temp\\contenudesc.xml");
		String modeleString = FileUtils.readFileToString(modele);
		
		
		if (description != null && !"".equals(description.getStringCellValue())) {
			modeleString = modeleString.replace("[A]", description.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[A]", "");
		}
		
		if (passant != null && !"".equals(passant.getStringCellValue())) {
			modeleString = modeleString.replace("[B]", passant.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[B]", "");
		}
		
		if (prerequis != null && !"".equals(prerequis.getStringCellValue())) {
			modeleString = modeleString.replace("[C]", prerequis.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[C]", "");
		}
		
		if (JDD != null && !"".equals(JDD.getStringCellValue())) {
			modeleString = modeleString.replace("[D]", JDD.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[D]", "");
		}
		
		if (valorisation != null && !"".equals(valorisation.getStringCellValue())) {
			modeleString = modeleString.replace("[E]", valorisation.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[E]", "");
		}
		
		if (environement != null && !"".equals(environement.getStringCellValue())) {
			modeleString = modeleString.replace("[F]", environement.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[F]", "");
		}
		
		if (distributeur != null && !"".equals(distributeur.getStringCellValue())) {
			modeleString = modeleString.replace("[G]", distributeur.getStringCellValue());
		} else {
			modeleString = modeleString.replace("[G]", "");
		}
		
		retour = retour.append(modeleString);
		retour = retour.append("</BODY></HTML>");
		
		//System.out.println(retour.toString());
		
		return retour.toString();
	}
}
