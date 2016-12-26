package outils;

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

import difflib.DiffUtils;

public class PDFOutils {

	public static void main(String[] args) {
		
		DiffOutils dmp = new DiffOutils();
		dmp.Diff_EditCost = 10;
		
		//List<String> test = dmp.my_diff_Test("my dog is rich", "dogsitter", 0);
		
		List<Integer> listePage = new LinkedList<Integer>();
		listePage.add(5);
		listePage.add(6);
		listePage.add(7);

		//List<String> blocsPDF = ReadGeneratedPDF("PDF_PDF.pdf", listePage, false);

		List<Integer> listePage2 = new LinkedList<Integer>();
		listePage2.add(1);
		listePage2.add(2);

		//List<String> blocsPDF2 = ReadGeneratedPDF("modele_FIP.pdf", listePage2, false);
		
		//String completPDF2 = ReadPDF("modele_FIP.pdf", listePage2);
		
		List<Integer> listePage3 = new LinkedList<Integer>();
		listePage3.add(1);
		
		String completCRYPTED = ReadPDF("CRYPTED.pdf", listePage3);
		
		System.out.println(completCRYPTED);

		LinkedList<Diff> diffList = new LinkedList<Diff>();
		
		String best_match =  "";
		
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

	/**
	 * Permet de lire un PDF généré automatiquement (ex : par SEFAS).
	 * @param pdf_url l'url du pdf
	 * @param listePage les pages à extraire.
	 * @return une liste de chaines de caractères correspondant aux différents blocs
	 */
	private static List<String> ReadGeneratedPDF(String pdf_url, List<Integer> listePage, boolean parLigne) {
		StringBuilder str = new StringBuilder();
		List<String> retour = new LinkedList<String>();
		String temp = new String();
		byte[] tempBrut;
		TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(pdf_url);
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
	 * @param pdf_url
	 *            l'url vers le pdf
	 * @param listePage
	 *            la liste des pages
	 * @return une chaine du contenu du document.
	 */
	private static String ReadPDF(String pdf_url, List<Integer> listePage) {
		StringBuilder str = new StringBuilder();
		String temp = new String();
		TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(pdf_url);
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

	public static void comparaison(List<String> chaine1, List<String> chaine2) {

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.

		difflib.Patch<String> patch = DiffUtils.diff(chaine1, chaine2);

		for (difflib.Delta<String> delta : patch.getDeltas()) {
			System.out.println(delta);
		}

	}
}
