package outils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import outils.DiffOutils.Diff;

import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfContentParser;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
//import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import constantes.Erreurs;
import difflib.Delta;
import difflib.DiffUtils;
import exceptions.SeleniumException;
import extensions.CompareMode;
import extensions.PDFUtil;

/**
 * Classe de manipulation des fichiers PDF dans le cadre des tests automatisés.
 * Cette classe expose une autre classe "PDFUtil" faisant parties des extensions.
 * @author levieilfa
 *
 */
public class PDFOutils extends PDFUtil {

	/**
	 * Permet de lire un PDF généré automatiquement (ex : par SEFAS) et de renvoyer une liste de contenu textuel de blocs ou par lignes.
	 * Ce mode de lecture ne permet pas le décodage des fichiers cryptés et nécessite la présence de balises.
	 * @param fichier le chemin vers le pdf
	 * @param listePage les pages à extraire (ex : 1,2,3)
	 * @param parLigne si vrai on extrait par ligne, si non par bloc
	 * @return une liste de chaines de caractères correspondant aux différents blocs
	 */
	private static List<String> lirePDFBalise(String fichier, List<Integer> listePage, boolean parLigne) {
		StringBuilder str = new StringBuilder();
		List<String> retour = new LinkedList<String>();
		String temp = new String();
		byte[] tempBrut;
		TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(fichier);
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			PdfContentParser contentParser;

			for (Integer page : listePage) {

				strategy = parser.processContent(page, strategy);

				// Obtenir le texte "brut" avec balises
				tempBrut = reader.getPageContent(page);
				
				//System.out.println(new String(tempBrut));
				
				PRTokeniser tokenizer = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(tempBrut)));
				contentParser = new PdfContentParser(tokenizer);

				while (tokenizer.nextToken()) {
					// String indique une chaine de caractère du document,
					// number une position et other une balise
					
					// Fin de balise = stockage des chaines obtenues
					if (!parLigne) {
						if (tokenizer.getTokenType() == PRTokeniser.TokenType.OTHER && tokenizer.getStringValue().toUpperCase().equals("ET")) {
							retour.add(temp);
							temp = new String();
						}
					}
					// Detection de fin de ligne
					if (tokenizer.getTokenType() == PRTokeniser.TokenType.OTHER && (tokenizer.getStringValue().toUpperCase().equals("TJ") || tokenizer.getStringValue().toUpperCase().equals("TD"))) {
						if (parLigne) {
							retour.add(temp);
							temp = new String();
						} else {
							temp = temp.concat("\n");
						}
					}
					// Ajout de la chaine courante au bloc courant
					if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
						temp = temp.concat(tokenizer.getStringValue());
					}
				}

				// Obtenir le texte sans balises
				// temp = PdfTextExtractor.getTextFromPage(reader, page);

				// Ajouter dans le retour la chaine travaillée
				// str.append(temp);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		return retour; // String.format("%s", str);
	}

	/**
	 * Permet de lire le texte complet d'un PDF sans tenir compte des balises
	 * 
	 * @param fichier le chemin vers le pdf
	 * @param listePage les pages à extraire (ex : 1,2,3)
	 * @return une chaine du contenu du document.
	 */
	private static String lirePDF(String fichier, List<Integer> listePage) {
		StringBuilder str = new StringBuilder();
		String temp = new String();
		TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(fichier);
			// PdfReaderContentParser parser = new
			// PdfReaderContentParser(reader);

			for (Integer page : listePage) {
				temp = PdfTextExtractor.getTextFromPage(reader, page);
				str.append(temp);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		return str.toString(); // String.format("%s", str);
	}

	// /**
	// * Obtenir le PDF et en lire le contenu
	// *
	// * @param pdf_url
	// * le chemin vers le PDF
	// * @param listePage
	// * liste des pages à extraire
	// * @return le contenu du pdf
	// */
	// private static String ReadPDF(String pdf_url, List<Integer> listePage) {
	// StringBuilder str = new StringBuilder();
	//
	// TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
	// try {
	//
	// PdfReader reader = new PdfReader(pdf_url);
	// PdfReaderContentParser parser = new PdfReaderContentParser(reader);
	// int n = reader.getNumberOfPages();
	// if (listePage == null) {
	// for (int i = 1; i <= n; i++) {
	// // str.append(PdfTextExtractor.getTextFromPage(reader, i));
	// parser.processContent(i, strategy);
	// str.append(strategy.getResultantText());
	// }
	// } else {
	// for (Integer page : listePage) {
	// // str.append(PdfTextExtractor.getTextFromPage(reader,
	// // page));
	// parser.processContent(page, strategy);
	// str.append(strategy.getResultantText());
	// }
	// }
	// } catch (Exception err) {
	// err.printStackTrace();
	// }
	// return str.toString(); // String.format("%s", str);
	// }

	/**
	 * Effectue un diff entre deux series d'extractions de PDF (sous forme de liste des chaines).
	 * 
	 * @param chaine1 liste de chaines correspondant au premier fichier
	 * @param chaine2 liste de chaines correspondant au deuxième fichier
	 */
	public static List<Delta<String>> comparaison(List<String> chaine1, List<String> chaine2) {

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.

		difflib.Patch<String> patch = DiffUtils.diff(chaine1, chaine2);

//		for (difflib.Delta<String> delta : patch.getDeltas()) {
//			System.out.println(delta);
//		}

		return patch.getDeltas();
	}
	
	
	public static List<String> comparerListePDF(File repertoire1, File repertoire2) throws SeleniumException {
		List<String> retour = new LinkedList<String>();
		PDFUtil pdfutil = new PDFUtil();
		pdfutil.setCheminSauvegarde(".");
		pdfutil.setOptionComparaisonComplete(true);
		pdfutil.setOptionSurlignerDifferences(true);
		pdfutil.setModeDeComparaison(CompareMode.VISUAL_MODE);
		
		// On vérifie que l'on manipule bien des répertoires.
		if (repertoire1.isDirectory() && repertoire2.isDirectory()) {
			// On va parcourir le premier repertoire et chercher dans le second repertoire tous les fichiers portant le même nom
			for (File fichierPDF : repertoire1.listFiles()) {
				if (fichierPDF.getName().toUpperCase().contains(".PDF")) {
					// On parcours le second répertoire
					for (File fichierPDFCible : repertoire2.listFiles()) {
						// On choisit le fichier dont le nom est le même
						if (fichierPDF.getName().toUpperCase().equals(fichierPDFCible.getName().toUpperCase())) {
							try {
								pdfutil.compare(fichierPDF.getAbsolutePath(), fichierPDFCible.getAbsolutePath());
								break;
							} catch (IOException e) {
								throw new SeleniumException(Erreurs.E021, "Les fichiers PDF ne sont pas lisibles où le repertoire de sauvegarde n'est pas accessible.");
							}
						}
					}
				}
			}
		} else {
			throw new SeleniumException(Erreurs.E021, "Les fichiers fournit ne sont pas des repertoires");
		}
		
		return retour;
	}

	public static void main(String[] args) {
		
		try {
			PDFOutils.comparerListePDF(new File("C:\\work\\PDF V15.11"), new File("C:\\work\\PDF V16.03"));
		} catch (SeleniumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
//			DiffOutils dmp = new DiffOutils();
//			dmp.Diff_EditCost = 10;
//			
//			//List<String> test = dmp.my_diff_Test("my dog is rich", "dogsitter", 0);
//			
//			List<Integer> listePage = new LinkedList<Integer>();
//			listePage.add(5);
//			listePage.add(6);
//			listePage.add(7);
//	
//			//List<String> blocsPDF = ReadGeneratedPDF("PDF_PDF.pdf", listePage, false);
//	
//			List<Integer> listePage2 = new LinkedList<Integer>();
//			listePage2.add(1);
//			listePage2.add(2);
//	
//			//List<String> blocsPDF2 = ReadGeneratedPDF("modele_FIP.pdf", listePage2, false);
//			
//			//String completPDF2 = ReadPDF("modele_FIP.pdf", listePage2);
//			
//			List<Integer> listePage3 = new LinkedList<Integer>();
//			listePage3.add(1);
//			
//			String completCRYPTED = lirePDF("CRYPTED.pdf", listePage3);
//			
//			System.out.println(completCRYPTED);
//	
//			LinkedList<Diff> diffList = new LinkedList<Diff>();
//			
//			String best_match =  "";
//			
	//		for (String bloc : blocsPDF) {
	//			
	//			//System.out.println("On cherche :" + bloc);
	//			
	//			// On compare le contenu du bloc avec les différents blocs disponible dans le modèle d'origine
	//			for (String bloc_origine : blocsPDF2) {
	//				// On compare bloc à bloc
	//				LinkedList<Diff> temp = dmp.diff_main(bloc_origine, bloc, false);
	//				dmp.diff_cleanupEfficiency(temp);
	//				// On calcule la distance entre l'ancien enregistrement et le nouveau.
	//				int distance_new = dmp.diff_levenshtein(temp);
	//				int distance_old = dmp.diff_levenshtein(diffList);
	//				
	//				// On conserve le "meilleur" diff entre l'ancien et le nouveau
	//				if (diffList.size() == 0 || distance_new < distance_old) {
	//					diffList.clear();
	//					diffList.addAll(temp);
	//					best_match = bloc_origine;
	//				}
	//			}
	//			//System.out.println("On trouve : " + best_match);
	//			// On montre le meilleur diff.
	//			String retour = dmp.diff_prettyHtml(diffList);
	//			System.out.println(retour);
	//			diffList.clear();
	//		}
	
			// dmp.diff_cleanupSemantic(diffList);
			//dmp.diff_cleanupEfficiency(diffList);
			// dmp.diff_cleanupMerge(diffList);
			// dmp.diff_cleanupSemanticLossless(diffList);
	
			//String retour = dmp.diff_prettyHtml(diffList);
	
			//System.out.println(retour);
		}
}
