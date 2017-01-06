package outils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.StringOutputStream;

import outils.DiffOutils.Diff;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.UnsupportedPdfException;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PRTokeniser;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfContentParser;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
//import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextMarginFinder;

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
	 * @param parLigne si vrai on extrait par ligne, si non par bloc (dès qu'on rencontre une balise fermante)
	 * @return une liste de chaines de caractères correspondant aux différents blocs
	 */
	private static List<String> lirePDFBalise(String fichier, boolean parLigne) {
		return lirePDFBalise(fichier, null, parLigne);
	}
	
	/**
	 * Permet de lire un PDF généré automatiquement (ex : par SEFAS) et de renvoyer une liste de contenu textuel de blocs ou par lignes.
	 * Ce mode de lecture ne permet pas le décodage des fichiers cryptés et nécessite la présence de balises.
	 * @param fichier le chemin vers le pdf
	 * @param listePage les pages à extraire (ex : 1,2,3)
	 * @param parLigne si vrai on extrait par ligne, si non par bloc (dès qu'on rencontre une balise fermante)
	 * @return une liste de chaines de caractères correspondant aux différents blocs
	 */
	private static List<String> lirePDFBalise(String fichier, List<Integer> listePage, boolean parLigne) {
		StringBuilder str = new StringBuilder();
		List<String> retour = new LinkedList<String>();
		List<String> tempListe = new LinkedList<String>();
		String temp = new String();
		
		// Stocker le précédent bloc
		float tempX = -1;
		float tempY = -1;
		float tempL = -1;
		float tempH = -1;
		
		// Stocker les retangles
		List<Float[]> rectangles = new LinkedList<Float[]>(); 
		
		byte[] tempBrut;
		TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(fichier);
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			PdfContentParser contentParser;
			
			// Si la liste n'est pas renseignée on créer une liste contenant toutes les pages.
			if (listePage == null) {
				listePage = new LinkedList<Integer>();
				for (int i = 1; i <= reader.getNumberOfPages(); i++) {
					listePage.add(i);
				}
			}

			// Pour chaque page demandées on effectue une analyse séparée
			for (Integer page : listePage) {
				strategy = parser.processContent(page, strategy);
				
				//Media Box: The 'page size'. The size of the media this PDF page should be printed on.
				//Crop Box: The size of the finished page. Any difference between media and crop will be cut off in a post processing step. Defaults to the media box.
				
				TextMarginFinder finder = parser.processContent(page, new TextMarginFinder());
				Rectangle mediaBox = reader.getPageSize(page);

				System.out.println("MARGINLeft  : " + finder.getLlx());
				System.out.println("MARGINBottom: " + finder.getLly());
				System.out.println("MARGINHeight: " + finder.getHeight());
				System.out.println("MARGINWidth : " + finder.getWidth());
				System.out.println("MARGINRight : " + finder.getUrx());
				System.out.println("MARGINTop   : " + finder.getUry());
				
				System.out.println("MEDIALEFT   = " + mediaBox.getLeft());
				System.out.println("MEDIATOP    = " + mediaBox.getTop());
				System.out.println("MEDIARIGHT  = " + mediaBox.getRight());
				System.out.println("MEDIAWIDTH  = " + mediaBox.getWidth());

				
//		        PdfObject obj;
//                StringWriter sos = new  StringWriter();
//		        for (int i = 1; i <= reader.getXrefSize(); i++) {
//		            obj = reader.getPdfObject(i);
//		            if (obj != null && obj.isStream()) {
//		                PRStream stream = (PRStream)obj;
//		                byte[] b;
//		                try {
//		                    b = PdfReader.getStreamBytes(stream);
//		                } catch(UnsupportedPdfException e) {
//		                    b = PdfReader.getStreamBytesRaw(stream);
//		                }
//		                String s = new String(b, "UTF-8");
//		                sos.append(s);
//		            }
//		        }
//		        System.out.println(sos);
				
				// Obtenir le texte "brut" avec balises
				tempBrut = reader.getPageContent(page);
				//System.out.println(new String(tempBrut));	
				PRTokeniser tokenizer = new PRTokeniser(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(tempBrut)));
				contentParser = new PdfContentParser(tokenizer);
				// Pour chaque balise réparée dans la page extraite on effectue une analyse.
				while (tokenizer.nextToken()) {
					// String indique une chaine de caractère du document,
					// number une position et other une balise
					
					// Detection de fin de balise = stockage des chaines obtenues, la balise ET est "endtoken".
					if (!parLigne) {
						if (tokenizer.getTokenType() == PRTokeniser.TokenType.OTHER && tokenizer.getStringValue().toUpperCase().equals("ET")) {
							retour.add(temp + "\t");
							temp = new String();
						}
					} 
					// Detection de fin de ligne (TJ TD sont des fin de lignes)
					if (tokenizer.getTokenType() == PRTokeniser.TokenType.OTHER && (tokenizer.getStringValue().toUpperCase().equals("TJ") || tokenizer.getStringValue().toUpperCase().equals("TD"))) {
						if (parLigne) {
							retour.add(temp + "\t");
							temp = new String();
						} else {
							temp = temp.concat("\n");
						}
					}
					
					if (tokenizer.getTokenType() == PRTokeniser.TokenType.NUMBER) {
						// Les numbers sont en fait des informations numériques sur le bloc
						int compteur = 0;
						// On stock les informations
						while (tokenizer.getTokenType() == PRTokeniser.TokenType.NUMBER) {
							tempListe.add(tokenizer.getStringValue());
							tokenizer.nextToken();	
						}
						// On regarde si la serie contient les informations de coordonnées
						if (tempListe.size() > 4) {
							temp = temp.concat("[" + tempListe.get(tempListe.size() - 2) + "," +  tempListe.get(tempListe.size() - 1) + "]\t");
							
//							if (tempX != -1) {
//								Float[] rectangle = new Float[4];
//								rectangle[0] = tempListe.get(tempListe.size() - 2)
//							}
							
							// On stocke les informations 
							tempX = Float.parseFloat(tempListe.get(tempListe.size() - 2)) + finder.getLlx();
							tempY = Math.abs(finder.getLly() - finder.getUry()) + Float.parseFloat(tempListe.get(tempListe.size() - 1)) ;
							tempL = 5;
							tempH = 15;
							
							// 7888
							// 180 pdf = 113 pixel => rapport de 1.6 ?
							
							System.out.println("Avant : " + tempListe.get(tempListe.size() - 1) + ", après : " + tempY);
							
							Float[] rectangle = new Float[4];
							rectangle[0] = tempX;
							rectangle[1] = tempY;
							rectangle[2] = tempL;
							rectangle[3] = tempH;
							rectangles.add(rectangle);
						}
						tempListe = new LinkedList<String>();
						
					}

					
					// Si le token indique une chaine de carctère (ou une valeur numérique) ou ajoute de la chaine courante au bloc courant
					if (tokenizer.getTokenType() == PRTokeniser.TokenType.STRING) {
						temp = temp.concat(tokenizer.getStringValue()  + "\t");
					}  
				}

				// Obtenir le texte sans balises
				// temp = PdfTextExtractor.getTextFromPage(reader, page);

				// Ajouter dans le retour la chaine travaillée
				// str.append(temp);
			}
			
			surligner(fichier, fichier.replace(".pdf", "_RECT.pdf"), rectangles);
			
		} catch (Exception err) {
			err.printStackTrace();
		}
		return retour; // String.format("%s", str);
	}
	
	/**
	 * Permet de lire le contenu sous forme de chaine de caractère du texte d'un PDF sans tenir compte des balises.
	 * @param fichier le chemin vers le pdf
	 * @return une chaine de caractère du contenu du document (pour les pages demandées).
	 */
	private static String getTextePDF(String fichier) {
		return getTextePDF(fichier, null);
	}

    /**
     * Parses a PDF and ads a rectangle showing the text margin.
     * @param src the source PDF
     * @param dest the resulting PDF
     */
    public static void ajouterMarges(String src, String dest)
        throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        TextMarginFinder finder;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            finder = parser.processContent(i, new TextMarginFinder());
            PdfContentByte cb = stamper.getOverContent(i);
            cb.rectangle(finder.getLlx(), finder.getLly(),
                finder.getWidth(), finder.getHeight());
            cb.stroke();
        }
        stamper.close();
        reader.close();
    }
	
    public static void surligner(String src, String dest, List<Float[]> rectangles) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        PdfContentByte canvas = stamper.getUnderContent(1);
        canvas.saveState();
        canvas.setColorFill(BaseColor.YELLOW);
        for (Float[] rectangle : rectangles) {
	        canvas.rectangle(rectangle[0], rectangle[1], rectangle[2], rectangle[3]);
	        canvas.fill();
        }
        canvas.restoreState();
        stamper.close();
        reader.close();
    }
    
	/**
	 * Permet de lire le contenu sous forme de chaine de caractère du texte d'un PDF sans tenir compte des balises.
	 * Les pages spécifiées seront les seules extraites.
	 * 
	 * @param fichier le chemin vers le pdf
	 * @param listePage une liste d'entier indiquant les pages à extraire (ex : 1,2,3). Si null, on parcours toutes les pages.
	 * @return une chaine de caractère du contenu du document (pour les pages demandées).
	 */
	private static String getTextePDF(String fichier, List<Integer> listePage) {
		StringBuilder str = new StringBuilder();
		String temp = new String();
		//TextExtractionStrategy strategy = new LocationTextExtractionStrategy();
		try {

			PdfReader reader = new PdfReader(fichier);
			// PdfReaderContentParser parser = new PdfReaderContentParser(reader);

			if (listePage == null) {
				for (int page = 1 ; page <= reader.getNumberOfPages() ; page++) {
					temp = PdfTextExtractor.getTextFromPage(reader, page);
					str.append(temp);
				}
			} else {
				for (Integer page : listePage) {
					temp = PdfTextExtractor.getTextFromPage(reader, page);
					str.append(temp);
				}
			}

		} catch (Exception err) {
			err.printStackTrace();
		}
		return str.toString(); // String.format("%s", str);
	}

	 /**
	 * Obtenir le PDF et en lire le contenu
	 *
	 * @param pdf_url
	 * le chemin vers le PDF
	 * @param listePage
	 * liste des pages à extraire
	 * @return le contenu du pdf
	 */
	 private static String ReadPDF(String pdf_url, List<Integer> listePage) {
	 StringBuilder str = new StringBuilder();
	
	 TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
	 try {
	
	 PdfReader reader = new PdfReader(pdf_url);
	 PdfReaderContentParser parser = new PdfReaderContentParser(reader);
	 int n = reader.getNumberOfPages();
	 if (listePage == null) {
	 for (int i = 1; i <= n; i++) {
	 // str.append(PdfTextExtractor.getTextFromPage(reader, i));
	 parser.processContent(i, strategy);
	 str.append(strategy.getResultantText());
	 }
	 } else {
	 for (Integer page : listePage) {
	 // str.append(PdfTextExtractor.getTextFromPage(reader,
	 // page));
	 parser.processContent(page, strategy);
	 str.append(strategy.getResultantText());
	 }
	 }
	 } catch (Exception err) {
	 err.printStackTrace();
	 }
	 return str.toString(); // String.format("%s", str);
	 }
	
	/**
	 * Effectue une comparaison entre deux fichier PDF pour produire une liste de delta à analyser ultérieurement.
	 * Si le nombre de page est différent, cela peux produire des diff illogiques.
	 * @param fichier1 le fichier "révisé".
	 * @param fichier2 le fichier de "référence".
	 * @return la liste des différences si il y a lieu.
	 * @throws SeleniumException en cas d'erreur lors de la lecture des PDF.
	 */
	public static List<Delta<String>> getDeltas(String fichier1, String fichier2) throws SeleniumException {
		// Initialisation des variables
		int nbPages1 = 0;
		int nbPages2 = 0;
		boolean nbPagesDifferents = false;
		int maxComparaison = 0;
		try {
			// On obtiens le nombre de page du fichier1 qui servira de référence pour comparaison avec le fichier 2
			PdfReader reader1 = new PdfReader(fichier1);
			nbPages1 = reader1.getNumberOfPages();
			// On obtiens le nombre de page du fichier2 qui servira de référence pour comparaison avec le fichier 1
			PdfReader reader2 = new PdfReader(fichier1);
			nbPages2 = reader2.getNumberOfPages();
			// Si le nombre de page n'est le même alors cela impacte la comparaison.
			if (nbPages1 != nbPages2) {
				nbPagesDifferents = true;
				maxComparaison = Math.max(nbPages1, nbPages2);
			} else {
				nbPagesDifferents = false;
				maxComparaison = nbPages1;
			}
			// On extrait les liste des blocs des différents fichiers page à page
			return getDeltas(lirePDFBalise(fichier1, false), lirePDFBalise(fichier2, false));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E021, "Au moins un des fichiers PDF n'est pas exploitables : " + e.getMessage());
		}	
	}
	
	/**
	 * Effectue une comparaison du contenu textuel entre deux fichier en traivaillant bloc à bloc.
	 * Cette comparaison rapproche des blocs dont la distance (au sens de Levenshtein) est le plus faible possible et fait donc abstraction du positionnement des blocs dans le fichier. 
	 * @param fichier1 le fichier "révisé".
	 * @param fichier2 le fichier de "référence".
	 * @return renvoie une chaine de caractère HTML présentant le résultat des comparaisons bloc à bloc des deux fichiers.
	 * @deprecated Fonction non performante, à éviter.
	 */
	@Deprecated
	public static String comparaisonTextuelleParBloc(String fichier1, String fichier2) {
		
		DiffOutils dmp = new DiffOutils();
		dmp.Diff_EditCost = 10;
		String best_match =  "";
		String retour = "";
		//List<String> test = dmp.my_diff_Test("my dog is rich", "dogsitter", 0);

		List<String> blocsPDF = lirePDFBalise(fichier1, true);
		List<String> blocsPDF2 = lirePDFBalise(fichier2, true);
		

		LinkedList<Diff> diffList = new LinkedList<Diff>();

		for (String bloc : blocsPDF) {
			
			//System.out.println("On cherche :" + bloc);
			
			// On compare le contenu du bloc avec les différents blocs disponible dans le modèle d'origine
			for (String bloc_origine : blocsPDF2) {
				// On compare bloc à bloc
				LinkedList<Diff> temp = dmp.diff_main(bloc_origine, bloc, false);
				dmp.diff_cleanupEfficiency(temp);
				// On calcule la distance entre l'ancien enregistrement et le nouveau.
				int distance_new = dmp.diff_levenshtein(temp);
				int distance_old = dmp.diff_levenshtein(diffList);
				
				// On conserve le "meilleur" diff entre l'ancien et le nouveau
				if (diffList.size() == 0 || distance_new < distance_old) {
					diffList.clear();
					diffList.addAll(temp);
					best_match = bloc_origine;
				}
			}
			//System.out.println("On trouve : " + best_match);
			// On montre le meilleur diff.
			retour = dmp.diff_prettyHtml(diffList);
			System.out.println(retour);
			diffList.clear();
		}

		// dmp.diff_cleanupSemantic(diffList);
		//dmp.diff_cleanupEfficiency(diffList);
		// dmp.diff_cleanupMerge(diffList);
		// dmp.diff_cleanupSemanticLossless(diffList);
		//String retour = dmp.diff_prettyHtml(diffList);
		
		return retour;
	}

	/**
	 * Effectue un diff entre deux chaines de caractères et le produit un fichier HTML  
	 * @param revision la "nouvelle version"
	 * @param reference la version "de référence"
	 * @param fichier l'emplacement pour le fichier de diff
	 * @throws SeleniumException en cas de problème de lecture ou d'écriture de fichier (ou de problème d'encoding PDF)
	 */
	public static void publierDiff(File revision, File reference, String fichier) throws SeleniumException {
		try {
			String diff = produireDiff(getTextePDF(revision.getAbsolutePath()), getTextePDF(reference.getAbsolutePath()));
			PrintWriter sw = new PrintWriter(fichier + File.separator + "diff" + revision.getName() + ".html", "UTF-8");
			sw.append("REVISED : " + revision.getAbsolutePath() + " , REFERENCE : " + reference.getAbsolutePath() + " <br>");
			sw.append(diff);
			sw.close();
		} catch (Exception ex) {
			throw new SeleniumException(Erreurs.E021, "Les fichiers PDF ne sont pas lisibles où le repertoire de sauvegarde n'est pas accessible : " + ex.getMessage());
		}
	}
	
	/**
	 * Effectue un diff entre deux chaines de caractères et le renvoie au format HTML 
	 * @param revision la "nouvelle version"
	 * @param reference la version "de référence"
	 * @return le diff entre les deux chaines (sous format HTML).
	 */
	public static String produireDiff(String revision, String reference) {
		// Initialisation des variables.
		DiffOutils dmp = new DiffOutils();
		//dmp.Diff_EditCost = 10;
		
		// On fait la comparaison
		LinkedList<Diff> diffList = new LinkedList<Diff>();
		diffList = dmp.diff_main(reference, revision, false);
		dmp.diff_cleanupEfficiency(diffList);

		// Mise ne forme du diff pour être exploitable.
		String retour = dmp.diff_prettyHtml(diffList);
		diffList.clear();
		
		return retour;
	}
	
	/**
	 * Effectue un diff entre deux series d'extractions de PDF (sous forme de liste des chaines).
	 * Le résultat est une liste de Delta entre des chaines de caractères.
	 * 
	 * @param chaine1 liste de chaines correspondant au premier fichier
	 * @param chaine2 liste de chaines correspondant au deuxième fichier
	 * @return la liste des Deltas (les différences) entre des chaines de caractères.
	 */
	public static List<Delta<String>> getDeltas(List<String> chaine1, List<String> chaine2) {
		difflib.Patch<String> patch = DiffUtils.diff(chaine1, chaine2);
//		for (difflib.Delta<String> delta : patch.getDeltas()) {
//			System.out.println(delta);
//		}
		return patch.getDeltas();
	}
	
	/**
	 * Effectue une comparaison entre deux fichier.
	 * La comparaison produit un fichier de diff visuel sous forme d'image PNG pour les pages où des différences sont trouvées.
	 * @param repertoire1 le répertoire de révision (la "nouvelle version")
	 * @param repertoire2 le répertoire de référence (l'"ancienne version")
	 * @return la racine commune à tous les fichiers de diff produit.
	 * @throws SeleniumException en cas d'impossibilité d'accès ou de lecture d'un fichier ou d'un répertoire.
	 */
	public static String comparerPDF(File fichier1, File fichier2) throws SeleniumException {
		return comparerPDF(fichier1, fichier2, -1, -1, true, true, true, 50);
	}
	
	/**
	 * Effectue une comparaison entre deux fichier.
	 * La comparaison produit un fichier de diff visuel sous forme d'image PNG pour les pages où des différences sont trouvées.
	 * @param repertoire1 le répertoire de révision (la "nouvelle version")
	 * @param repertoire2 le répertoire de référence (l'"ancienne version")
	 * @param startPage la page de départ de la comparaison
	 * @param endPage la page de fin de comparaison
	 * @param surligner indique si on surligne les différence (et on produit une image)
	 * @param transparence indique si on met en transparence les objets communs aux deux fichiers
	 * @param tolerance indique si on tolère une différence de couleur entre deux pixels (evite les faux positif)
	 * @param seuilTolerance le seuil de tolérance de différence entreles pixels (50 pour un seuil logique).
	 * @return la racine commune à tous les fichiers de diff produit.
	 * @throws SeleniumException en cas d'impossibilité d'accès ou de lecture d'un fichier ou d'un répertoire.
	 */
	public static String comparerPDF(File fichier1, File fichier2, int startPage, int endPage, boolean surligner, boolean transparence, boolean tolerance, int seuilTolerance) throws SeleniumException {
		String retour = new String();
		PDFUtil pdfutil = new PDFUtil();
		pdfutil.setCheminSauvegarde(".");
		pdfutil.setOptionComparaisonComplete(true);
		pdfutil.setOptionSurlignerDifferences(true);
		pdfutil.setModeDeComparaison(CompareMode.VISUAL_MODE);
		String cheminProduction = ".";
		boolean temp = false;
		
		// Si les repertoires existe on prépare le répertoire de sauvegarde
		cheminProduction = fichier1.getParent();
		if (cheminProduction != null) {
			//cheminProduction = cheminProduction.concat(File.separator + "diff");
			pdfutil.setCheminSauvegarde(cheminProduction);
		} else {
			throw new SeleniumException(Erreurs.E021, "Impossible de créer le répertoire de sortie.");
		}

		try {
			temp = pdfutil.convertirEnImageEtComparer(fichier1.getAbsolutePath(), fichier2.getAbsolutePath(), startPage, endPage, surligner, transparence, tolerance, seuilTolerance);
			
		} catch (/*IO*/Exception e) {
			throw new SeleniumException(Erreurs.E021, "Les fichiers PDF ne sont pas lisibles où le repertoire de sauvegarde n'est pas accessible.");
		}
		
		if (temp) {
			retour =  cheminProduction;
		}
		
		return retour;
	}
	
	/**
	 * Effectue une comparaison de masse entre deux répertoires contenant des PDF portant les mêmes noms.
	 * La comparaison à lieue par paire et produit un fichier de diff visuel sous forme d'image PNG pour les pages où des différences sont trouvées.
	 * @param repertoire1 le répertoire de révision (la "nouvelle version")
	 * @param repertoire2 le répertoire de référence (l'"ancienne version")
	 * @return une liste de boolean indiquant les différences des paires.
	 * @throws SeleniumException en cas d'impossibilité d'accès ou de lecture d'un fichier ou d'un répertoire.
	 */
	public static List<Boolean> comparerListePDF(File repertoire1, File repertoire2) throws SeleniumException {
		List<Boolean> retour = new LinkedList<Boolean>();
		boolean temp = false;
		PDFUtil pdfutil = new PDFUtil();
		pdfutil.setCheminSauvegarde(".");
		pdfutil.setOptionComparaisonComplete(true);
		pdfutil.setOptionSurlignerDifferences(true);
		pdfutil.setModeDeComparaison(CompareMode.VISUAL_MODE);
		String cheminProduction = ".";
		
		// On vérifie que l'on manipule bien des répertoires.
		if (repertoire1.isDirectory() && repertoire2.isDirectory()) {
			// Si les repertoires existe on prépare le répertoire de sauvegarde
			cheminProduction = repertoire1.getParent();
			if (cheminProduction != null) {
				cheminProduction = cheminProduction.concat(File.separator + "diff");
				pdfutil.setCheminSauvegarde(cheminProduction);
			} else {
				throw new SeleniumException(Erreurs.E021, "Impossible de créer le répertoire de sortie.");
			}
			// On va parcourir le premier repertoire et chercher dans le second repertoire tous les fichiers portant le même nom
			for (File fichierPDF : repertoire1.listFiles()) {
				if (fichierPDF.getName().toUpperCase().contains(".PDF")) {
					// On parcours le second répertoire
					for (File fichierPDFCible : repertoire2.listFiles()) {
						// On choisit le fichier dont le nom est le même
						if (fichierPDF.getName().toUpperCase().equals(fichierPDFCible.getName().toUpperCase())) {
							try {
								temp = pdfutil.compare(fichierPDF.getAbsolutePath(), fichierPDFCible.getAbsolutePath());
								//comparaisonTextuelleParBloc(fichierPDF.getAbsolutePath(), fichierPDFCible.getAbsolutePath());
								publierDiff(fichierPDF, fichierPDFCible, cheminProduction);
								retour.add(temp);
//								if (temp) {
//									retour.add(cheminProduction + File.separator + fichierPDF.getName() + "_diff.png");
//								}
								break;
							} catch (/*IO*/Exception e) {
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

	/**
	 * Fonction main de test, à supprimer.
	 * @param args les arguments (non utilisés).
	 */
	public static void main(String[] args) {
			try {
				//PDFOutils.comparerListePDF(new File("C:\\work\\TEST1"), new File("C:\\work\\TEST2"));
				PDFOutils.comparerListePDF(new File("C:\\work\\PDF V16.03"), new File("C:\\work\\PDF V15.11"));
//				List<String> retour = PDFOutils.lirePDFBalise("C:\\work\\PDF V16.03\\DEX.pdf", false);
				
//				try {
//					PDFOutils.ajouterMarges("C:\\work\\PDF V16.03\\DEX.pdf", "C:\\work\\PDF V16.03\\DEX2.pdf");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (DocumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				//List<String> retour = PDFOutils.lirePDFBalise("C:\\work\\PDF V16.03\\DEX.pdf", false);
				
//				for (String balise : retour) {
//					System.out.println(balise);
//				}
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
