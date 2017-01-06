package extensions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.commons.io.FileUtils;

import outils.IMGOutils;

/**
* Outil de manipulation de PDFpermettant notament d'obtenir les différences entre deux pdf sous forme d'image.
* Librement inspiré du code de "PDFUtil" de www.testautomationguru.com (2015-06-13)
* @author  levieilfa
* @since   2016-12-23
*/

public class PDFUtil {

	/**
	 * Logger.
	 */
	static Logger logger = Logger.getLogger(PDFUtil.class.getName());
	
	/**
	 * Chemin vers le repertoire d'écriture du fichier image de sortie. 
	 */
	private String cheminSauvegarde;
	/**
	 * Option permettant lors d'une comparaison textuelle d'ignorer les espaces.
	 */
	private boolean optionTrimEspaces;
	/**
	 * Option permettant lors de la comparaison par image de produire un diff sous forme d'image avec une surbrillance.
	 */
	private boolean optionSurlignerDifferences;
	/**
	 * Couleur à appliquée sur les différences.
	 */
	private Color couleurDiff;
	/**
	 * Couleur réservée pour indiquer les ajouts.
	 */
	private Color couleurAjout;
	/**
	 * Couleur réservée pour indiquer les suppression.
	 */
	private Color couleurSuppression;
	/**
	 * Option permettant la comparaison de toute les pages. Si à non, on ne compare que la première.
	 */
	private boolean optionComparaisonComplete;
	/**
	 * Indique le type de comparaison à effectuer (text ou image).
	 */
	private CompareMode modeDeComparaison;
	/**
	 * Le numéro de page où débute la comparaison.
	 */
	private int pageDebut = 1;
	/**
	 * Le numéro de page où finie la comparaison (par défaut -1 signifie jusqu'à la dernière page)
	 */
	private int pageFin = -1;
	
	/**
	 * Constructeur par défaut de l'outil.
	 */
	public PDFUtil(){
		this.optionTrimEspaces = true;
		this.optionSurlignerDifferences = false;
		this.couleurDiff = Color.MAGENTA;
		this.couleurAjout = Color.GREEN;
		this.couleurSuppression = Color.RED;
		this.optionComparaisonComplete = false;
		this.modeDeComparaison = CompareMode.TEXT_MODE;
		logger.setLevel(Level.OFF);
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
	}
	
   /**
   * This method is used to show log in the console. Level.INFO
   * It is set to Level.OFF by default.
   */
	public void enableLog(){
		logger.setLevel(Level.INFO);
	}

   /**
   * This method is used to change the level
   * @param level java.util.logging.Level 
   */
	public void setLogLevel(java.util.logging.Level level){
		logger.setLevel(level);
	}		
		
   /**
   * Get the page count of the document.
   * 
   * @param file Absolute file path
   * @return int No of pages in the document.
   * @throws java.io.IOException when file is not found.
   */	
	public int getPageCount(String file) throws IOException{
		logger.info("file :" + file);
		PDDocument doc = PDDocument.load(new File(file));
		int pageCount = doc.getNumberOfPages();
		logger.info("pageCount :" + pageCount);
		doc.close();
		return pageCount;
	}
				
   /**
   * Get the content of the document as plain text.
   *  
   * @param file Absolute file path
   * @return String document content in plain text.
   * @throws java.io.IOException when file is not found.
   */
	public String getText(String file) throws IOException{
		return this.getPDFText(file,-1, -1);
	}
	
   /**
   * Get the content of the document as plain text.
   *  
   * @param file Absolute file path
   * @param startPage Starting page number of the document
   * @return String document content in plain text.
   * @throws java.io.IOException when file is not found.
   */
	public String getText(String file, int startPage) throws IOException{
		return this.getPDFText(file,startPage, -1);
	}
	
   /**
   * Get the content of the document as plain text.
   *  
   * @param file Absolute file path
   * @param startPage Starting page number of the document
   * @param endPage Ending page number of the document
   * @return String document content in plain text.
   * @throws java.io.IOException when file is not found.
   */
	public String getText(String file, int startPage, int endPage) throws IOException{
		return this.getPDFText(file,startPage, endPage);
	}

   /**
   * This method returns the content of the document
   */
	private String getPDFText(String file, int startPage, int endPage) throws IOException{
		
		logger.info("file : " + file);
		logger.info("startPage : " + startPage);
		logger.info("endPage : " + endPage);
		
		PDDocument doc = PDDocument.load(new File(file));
		PDFTextStripper stripper = new PDFTextStripper();
		
		this.mettreAJourPagesDebutEtFin(file, startPage, endPage);
		stripper.setStartPage(this.pageDebut);
		stripper.setEndPage(this.pageFin);
		
		String txt = stripper.getText(doc);
		logger.info("PDF Text before trimming : " + txt);
		if(this.optionTrimEspaces){
			txt = txt.trim().replaceAll("\\s+", " ").trim();
			logger.info("PDF Text after  trimming : " + txt);	
		}
		
		doc.close();
		return txt;
	}
   /**
   * Compares two given pdf documents.
   * 
   * <b>Note :</b> <b>TEXT_MODE</b> : Compare 2 pdf documents contents with no formatting. 
   * 			   <b>VISUAL_MODE</b> : Compare 2 pdf documents pixel by pixel for the content and format.
   * @param file1 Absolute file path of the expected file
   * @param file2 Absolute file path of the actual file
   * @return boolean true if matches, false otherwise
   * @throws java.io.IOException when file is not found.
   */
	public boolean compare(String file1, String file2) throws IOException{
		return this.comparePdfFiles(file1, file2, -1, -1);
	}
	
   /**
   * Compares two given pdf documents.
   * 
   * <b>Note :</b> <b>TEXT_MODE</b> : Compare 2 pdf documents contents with no formatting. 
   * 			   <b>VISUAL_MODE</b> : Compare 2 pdf documents pixel by pixel for the content and format.
   * 
   * @param file1 Absolute file path of the expected file
   * @param file2 Absolute file path of the actual file
   * @param startPage Starting page number of the document
   * @param endPage Ending page number of the document
   * @return boolean true if matches, false otherwise
   * @throws java.io.IOException when file is not found.
   */
	public boolean compare(String file1, String file2, int startPage, int endPage) throws IOException{
		return this.comparePdfFiles(file1, file2, startPage, endPage);
	}
	
   /**
   * Compares two given pdf documents.
   * 
   * <b>Note :</b> <b>TEXT_MODE</b> : Compare 2 pdf documents contents with no formatting. 
   * 			   <b>VISUAL_MODE</b> : Compare 2 pdf documents pixel by pixel for the content and format.
   * 
   * @param file1 Absolute file path of the expected file
   * @param file2 Absolute file path of the actual file
   * @param startPage Starting page number of the document
   * @return boolean true if matches, false otherwise
   * @throws java.io.IOException when file is not found.
   */	
	public boolean compare(String file1, String file2, int startPage) throws IOException{
		return this.comparePdfFiles(file1, file2, startPage, -1);
	}
	
	/**
	 * Effectue la comparaison entre deux fichiers PDF suivant le mode choisie.
	 * @param file1 le fichier de référence.
	 * @param file2 le fichier "révisé".
	 * @param startPage la page de début de comparaison.
	 * @param endPage la page de fin de comparaison.
	 * @return true si les fichiers osnt identiques, false sinon.
	 * @throws IOException en cas de problèmes d'accès aux fichiers.
	 */
	private boolean comparePdfFiles(String file1, String file2, int startPage, int endPage)throws IOException{
		if(CompareMode.TEXT_MODE==this.modeDeComparaison)
			return comparepdfFilesWithTextMode(file1, file2, startPage, endPage);
		else
			return comparePdfByImage(file1, file2, startPage, endPage);
	}
	
	/**
	 * Effectue une comparaison entre deux fichier en mode "texte" uniquement.
	 * Attention ce mode de comparaison ne fournit pas le "diff" entre les fichiers, uniquement un résultat "vrai" ou "faux".
	 * @param file1 le premier fichier PDF
	 * @param file2 le second fichier PDF
	 * @param startPage la première page pour la comparaison
	 * @param endPage la dernière page pour la comparaison
	 * @return vrai si les deux fichiers sont identiques, faux sinon.
	 * @throws IOException en cas de problème d'accès ou de lecture des fichiers.
	 */
	private boolean comparepdfFilesWithTextMode(String file1, String file2, int startPage, int endPage) throws IOException{
		
		String file1Txt = this.getPDFText(file1, startPage, endPage).trim();
		String file2Txt = this.getPDFText(file2, startPage, endPage).trim();
		
		logger.info("File 1 Txt : " + file1Txt);
		logger.info("File 2 Txt : " + file2Txt);
		
		boolean result = file1Txt.equalsIgnoreCase(file2Txt);
		
		if(!result){
			logger.warning("PDF content does not match");
		}
		
		return result; 
	}
	
   /**
   * Save each page of the pdf as image
   * 
   * @param file Absolute file path of the file
   * @param startPage Starting page number of the document
   * @return List list of image file names with absolute path
   * @throws java.io.IOException when file is not found.
   */
	public List<String> savePdfAsImage(String file, int startPage) throws IOException{
		return this.saveAsImage(file, startPage, -1);	
	}
	
   /**
   * Save each page of the pdf as image
   * 
   * @param file Absolute file path of the file
   * @param startPage Starting page number of the document
   * @param endPage Ending page number of the document
   * @return List list of image file names with absolute path
   * @throws java.io.IOException when file is not found.
   */
	public List<String> savePdfAsImage(String file, int startPage, int endPage) throws IOException{
		return this.saveAsImage(file, startPage, endPage);	
	}
	
   /**
   * Save each page of the pdf as image
   * 
   * @param file Absolute file path of the file
   * @return List list of image file names with absolute path
   * @throws java.io.IOException when file is not found.
   */	
	public List<String> savePdfAsImage(String file) throws IOException{
		return this.saveAsImage(file, -1, -1);
	}
	
	/**
	 * Permet de sauvegarder sous forme d'image l'ensemble des pages du document PDF.
	 * @param file le fichier PDF (chemin absolue)
	 * @param startPage la première page
	 * @param endPage la dernière page
	 * @return une liste de nom de fichier (chemin absolue) représentant les images tirées du PDF
	 * @throws IOException en cas de problèmes lors de la lecture ou de l'écriture des fichiers.
	 */
	private List<String> saveAsImage(String file, int startPage, int endPage) throws IOException{
		
		logger.info("Fichier : " + file);
		logger.info("Première page : " + startPage);
		logger.info("Dernière page : " + endPage);
		
		ArrayList<String> imgNames = new ArrayList<String>();
		
		try {
			File sourceFile = new File(file);
			this.creerRepertoireDeSauvegarde(file);			
			this.mettreAJourPagesDebutEtFin(file, startPage, endPage);
			
			String fileName = sourceFile.getName().replace(".pdf", "");
			
			PDDocument document = PDDocument.load(sourceFile);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			for(int iPage=this.pageDebut-1;iPage<this.pageFin;iPage++){
				logger.info("Page No : " + (iPage+1));
				String fname = this.cheminSauvegarde + fileName + "_" + (iPage + 1) + ".png";
				BufferedImage image = pdfRenderer.renderImageWithDPI(iPage, 300, ImageType.RGB);
				ImageIOUtil.writeImage(image, fname , 300);
				imgNames.add(fname);
				logger.info("Page sauvegardée sous forme d'image : " + fname);
			}
			document.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return imgNames;  	
	}
		
   /**
   * Compare 2 pdf documents pixel by pixel for the content and format.
   * 
   * @param file1 Absolute file path of the expected file
   * @param file2 Absolute file path of the actual file
   * @param startPage Starting page number of the document
   * @param endPage Ending page number of the document   
   * @param highlightImageDifferences To highlight differences in the images
   * @param showAllDifferences To compare all the pages of the PDF (by default as soon as a mismatch is found in a page, this method exits)
   * @return boolean true if matches, false otherwise
   * @throws java.io.IOException when file is not found.
   */		
	public boolean compare(String file1, String file2,int startPage, int endPage, boolean highlightImageDifferences, boolean showAllDifferences) throws IOException{
		this.modeDeComparaison = CompareMode.VISUAL_MODE;
		this.optionSurlignerDifferences = highlightImageDifferences;
		this.optionComparaisonComplete = showAllDifferences;
		return this.comparePdfByImage(file1, file2, startPage, endPage);
	}		
	

	/**
	 * Compare deux fichiers PDF sous forme d'image.
	 * @param file1 le fichier nouveau
	 * @param file2 le fichier référence
	 * @param startPage la page de début, -1 si non connu.
	 * @param endPage la page de fin, -1 si non connu
	 * @return true si les documents sont identiques, false sinon.
	 * @throws IOException en cas d'erreur lors de l'accès aux fichiers ou de la conversion en image.
	 */
	private boolean comparePdfByImage(String file1, String file2, int startPage, int endPage) throws IOException{
		
		logger.info("file1 : " + file1);
		logger.info("file2 : " + file2);
		
		// Permet d'obtenir le nombre de page des deux documents.
		int pgCount1 = this.getPageCount(file1);
		int pgCount2 = this.getPageCount(file2);
		
		// Si le nombre de page n'est pas le même entre les deux documents, c'est impossible que la comparaison donne un résultat positif.
		if (pgCount1 != pgCount2) {
			logger.warning("Les deux fichiers ne comportent pas le même nombre de page à comparer - on renvoie faux");
			return false;
		}
		
		if(this.optionSurlignerDifferences) {
			creerRepertoireDeSauvegarde(file2);
		}
		
		mettreAJourPagesDebutEtFin(file1, startPage, endPage);		
		
		return convertToImageAndCompare(file1, file2, this.pageDebut, this.pageFin);
	}	
	
	/**
	 * Permet la conversion des fichiers PDF en image puis une comparaison pixel à pixel.
	 * Si l'option à été choisie les différences trouvées seront fournis sous forme de fichier image.
	 * @param file1 le fichier numéro un
	 * @param file2 le fichier numéro deux
	 * @param startPage la page de départ de la comparaison
	 * @param endPage la page de fin de comparaison
	 * @return vrai si les deux fichiers sont identique, faux sinon.
	 * @throws IOException en cas de problème d'accès aux fichiers.
	 */
	private boolean convertToImageAndCompare(String file1, String file2, int startPage, int endPage) throws IOException{
		
		boolean result = true;
		
		PDDocument doc1=null;
		PDDocument doc2=null;
		
		PDFRenderer pdfRenderer1 = null;
		PDFRenderer pdfRenderer2 = null;
		
		try {
			doc1 = PDDocument.load(new File(file1));
			doc2 = PDDocument.load(new File(file2));
		 
			pdfRenderer1 = new PDFRenderer(doc1);
			pdfRenderer2 = new PDFRenderer(doc2);

			for(int iPage=startPage-1;iPage<endPage;iPage++){
				String fileName = new File(file1).getName().replace(".pdf", "_") + (iPage + 1);
				fileName = this.getCheminSauvegarde() + "/" + fileName + "_diff.png";
				
				logger.info("Comparing Page No : " + (iPage+1));
				BufferedImage image1 = pdfRenderer1.renderImageWithDPI(iPage, 300, ImageType.RGB);
				BufferedImage image2 = pdfRenderer2.renderImageWithDPI(iPage, 300, ImageType.RGB);
				result = IMGOutils.compareAndHighlight(image1, image2, fileName, this.optionSurlignerDifferences) && result;
				if(!this.optionComparaisonComplete && !result){
					break;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			doc1.close();
			doc2.close();
		}
		return result;  	
	}
	
	/**
	 * Permet la conversion des fichiers PDF en image puis une comparaison pixel à pixel.
	 * Si l'option à été choisie les différences trouvées seront fournis sous forme de fichier image.
	 * @param file1 le fichier numéro un
	 * @param file2 le fichier numéro deux
	 * @param startPage la page de départ de la comparaison
	 * @param endPage la page de fin de comparaison
	 * @param surligner indique si on surligne les différence (et on produit une image)
	 * @param transparence indique si on met en transparence les objets communs aux deux fichiers
	 * @param tolerance indique si on tolère une différence de couleur entre deux pixels (evite les faux positif)
	 * @param seuilTolerance le seuil de tolérance de différence entreles pixels (50 pour un seuil logique).
	 * @return vrai si les deux fichiers sont identique, faux sinon.
	 * @throws IOException en cas de problème d'accès aux fichiers.
	 */
	public boolean convertirEnImageEtComparer(String file1, String file2, int startPage, int endPage, boolean surligner, boolean transparence, boolean tolerance, int seuilTolerance) throws IOException{
		
		boolean result = true;
		
		PDDocument doc1=null;
		PDDocument doc2=null;
		
		PDFRenderer pdfRenderer1 = null;
		PDFRenderer pdfRenderer2 = null;
		
		try {
			// On lit les 2 fichiers PDF
			doc1 = PDDocument.load(new File(file1));
			doc2 = PDDocument.load(new File(file2));
		 
			pdfRenderer1 = new PDFRenderer(doc1);
			pdfRenderer2 = new PDFRenderer(doc2);
			
			// Si les pages ne sont pas précisée on les mets à jour.
			if (startPage == -1 || endPage == -1) {
				this.mettreAJourPagesDebutEtFin(file1, startPage, endPage);
				//this.mettreAJourPagesDebutEtFin(file2, startPage, endPage);
			}
			

			// Pour chaque "paire" de page on effectue la comparaison
			for(int iPage=this.pageDebut-1;iPage<this.pageFin;iPage++){
				String fileName = new File(file1).getName().replace(".pdf", "_") + (iPage + 1);
				fileName = this.getCheminSauvegarde() + "/" + fileName + "_diff.png";
				
				// Conversion en image puis comparaison
				logger.info("Comparing Page No : " + (iPage+1));
				BufferedImage image1 = pdfRenderer1.renderImageWithDPI(iPage, 300, ImageType.RGB);
				BufferedImage image2 = pdfRenderer2.renderImageWithDPI(iPage, 300, ImageType.RGB);
				result = IMGOutils.compareAndHighlight(image1, image2, fileName, surligner, transparence, tolerance, seuilTolerance) && result;
				if(!this.optionComparaisonComplete && !result){
					break;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			doc1.close();
			doc2.close();
		}
		return result;  	
	}

	/**
	 * Cette méthode à pour objectif l'extraction des images présentes dans un fichier PDF.
	 * Les images sont ensuite sauvegarder sur le disque dans le répertoire spécifié dans "cheminSauvegarde".
	 * @param file le fichier PDF duquel on cherche à extraire les images.
	 * @param startPage la première page de l'analyse.
	 * @return une liste regroupant les noms (chemin absolue) des images extraites.
	 * @throws java.io.IOException si le fichier est introuvable
	 */		
	public List<String> extraireImages(String file, int startPage) throws IOException{
		return this.extractimages(file, startPage, -1);	
	}
	
	/**
	 * Cette méthode à pour objectif l'extraction des images présentes dans un fichier PDF.
	 * Les images sont ensuite sauvegarder sur le disque dans le répertoire spécifié dans "cheminSauvegarde".
	 * @param file le fichier PDF duquel on cherche à extraire les images.
	 * @param startPage la première page de l'analyse.
	 * @param endPage la dernière page de l'analyse.
	 * @return une liste regroupant les noms (chemin absolue) des images extraites.
	 * @throws java.io.IOException si le fichier est introuvable
	 */		
	public List<String> extraireImages(String file, int startPage, int endPage) throws IOException{
		return this.extractimages(file, startPage, endPage);	
	}
	
	/**
	 * Cette méthode à pour objectif l'extraction des images présentes dans un fichier PDF.
	 * Les images sont ensuite sauvegarder sur le disque dans le répertoire spécifié dans "cheminSauvegarde".
	 * @param file le fichier PDF duquel on cherche à extraire les images.
	 * @return une liste regroupant les noms (chemin absolue) des images extraites.
	 * @throws java.io.IOException si le fichier est introuvable
	 */		
	public List<String> extraireImages(String file) throws IOException{
		return this.extractimages(file, -1, -1);
	}
	
	/**
	 * Cette méthode à pour objectif l'extraction des images présentes dans un fichier PDF.
	 * Les images sont ensuite sauvegarder sur le disque dans le répertoire spécifié dans "cheminSauvegarde".
	 * @param file le fichier PDF duquel on cherche à extraire les images.
	 * @param startPage la première page de l'analyse.
	 * @param endPage la dernière page de l'analyse.
	 * @return une liste regroupant les noms (chemin absolue) des images extraites.
	 */ 
	private List<String> extractimages(String file, int startPage, int endPage){
		
		logger.info("file : " + file);
		logger.info("startPage : " + startPage);
		logger.info("endPage : " + endPage);
		
		ArrayList<String> imgNames = new ArrayList<String>();
		boolean bImageFound = false;
		try {

			this.creerRepertoireDeSauvegarde(file);
			String fileName = this.obtenirNomFichier(file).replace(".pdf", "_resource");
			
			PDDocument document = PDDocument.load(new File(file));
			PDPageTree list = document.getPages();
			
			this.mettreAJourPagesDebutEtFin(file, startPage, endPage);
			
			int totalImages = 1;
			for(int iPage=this.pageDebut-1;iPage<this.pageFin;iPage++){	
				logger.info("Page No : " + (iPage+1));
				PDResources pdResources = list.get(iPage).getResources();
				for (COSName c : pdResources.getXObjectNames()) {
		            PDXObject o = pdResources.getXObject(c);
		            if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
		            	bImageFound = true;
		            	String fname = this.cheminSauvegarde + "/" + fileName+ "_" + totalImages + ".png";
		                ImageIO.write(((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)o).getImage(), "png", new File(fname));
		                imgNames.add(fname);
		                totalImages++;
		            }
		        }
			}
			document.close();		
			if(bImageFound)
				logger.info("Images are saved @ " + this.cheminSauvegarde);
			else
				logger.info("No images were found in the PDF");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return imgNames;  	
	}

	/**
	 * Permet la création du répertoire destiné à accueillir les images extraires.
	 * Attention : Si le répertoire existe déjà il est supprimer avant de créer le nouveau.
	 * @param file le repertoire destination.
	 * @throws IOException en cas d'erreur.
	 */
	private void creerRepertoireDeSauvegarde(String file) throws IOException{
		if(null == this.cheminSauvegarde){
			File sourceFile = new File(file);
			String destinationDir = sourceFile.getParent() + "/temp/";
			this.cheminSauvegarde=destinationDir;
			this.creerRepertoire(destinationDir);
		}
	}
	
	/**
	 * Permet la création d'un répertoire. Si le répertoire existe déjà il est supprimer avant de créer le nouveau.
	 * @param file répertoire à créer.
	 * @return vrai si la création est fait, faux sinon.
	 * @throws IOException si il est impossible de supprimer l'ancien répertoire ou de créer le nouveau.
	 */
	private boolean creerRepertoire(String file) throws IOException{
		FileUtils.deleteDirectory(new File(file));
		return new File(file).mkdir();
	}
	
	/**
	 * Permet d'obtenir le nom du fichier ou du répertoire passé en paramètre.
	 * @param file le fichier ou le répertoire dont on souhaites le nom.
	 * @return le nom du fichier ou du répertoire paramètre.
	 */
	private String obtenirNomFichier(String file){
		return new File(file).getName();
	}
	
	/**
	 * Permet de mettre à jour les numéros de pages de références pour la comparaison entre les fichiers. 
	 * @param file le fichier concerné.
	 * @param start la page de début souhaitées (si mis à 0 ou moins, la première page est prise)
	 * @param end la page de fin souhaitées (si msi à 0 ou moins, la dernière page du document est prise)
	 * @throws IOException en cas de problème d'accès au fichier PDF.
	 */
	private void mettreAJourPagesDebutEtFin(String file, int start, int end) throws IOException{
		
		PDDocument document = PDDocument.load(new File(file));
		int pagecount = document.getNumberOfPages();
		logger.info("Page Count : " + pagecount);
		logger.info("Given start page:" + start);
		logger.info("Given end   page:" + end);
		
		if((start > 0 && start <= pagecount)){
			this.pageDebut = start;
		}else{
			this.pageDebut = 1;
		}
		if((end > 0 && end >= start && end <= pagecount)){
			this.pageFin = end;
		}else{
			this.pageFin = pagecount;
		}
		document.close();
		logger.info("Updated start page:" + this.pageDebut);
		logger.info("Updated end   page:" + this.pageFin);
	}
	
	public String getCheminSauvegarde() {
		return cheminSauvegarde;
	}

	public void setCheminSauvegarde(String cheminSauvegarde) {
		this.cheminSauvegarde = cheminSauvegarde;
	}

	public boolean isOptionTrimEspaces() {
		return optionTrimEspaces;
	}

	public void setOptionTrimEspaces(boolean optionTrimEspaces) {
		this.optionTrimEspaces = optionTrimEspaces;
	}

	public boolean isOptionSurlignerDifferences() {
		return optionSurlignerDifferences;
	}

	public void setOptionSurlignerDifferences(boolean optionSurlignerDifferences) {
		this.optionSurlignerDifferences = optionSurlignerDifferences;
	}

	public Color getCouleurDiff() {
		return couleurDiff;
	}

	public void setCouleurDiff(Color couleurDiff) {
		this.couleurDiff = couleurDiff;
	}

	public Color getCouleurAjout() {
		return couleurAjout;
	}

	public void setCouleurAjout(Color couleurAjout) {
		this.couleurAjout = couleurAjout;
	}

	public Color getCouleurSuppression() {
		return couleurSuppression;
	}

	public void setCouleurSuppression(Color couleurSuppression) {
		this.couleurSuppression = couleurSuppression;
	}

	public boolean isOptionComparaisonComplete() {
		return optionComparaisonComplete;
	}

	public void setOptionComparaisonComplete(boolean optionComparaisonComplete) {
		this.optionComparaisonComplete = optionComparaisonComplete;
	}

	public CompareMode getModeDeComparaison() {
		return modeDeComparaison;
	}

	public void setModeDeComparaison(CompareMode modeDeComparaison) {
		this.modeDeComparaison = modeDeComparaison;
	}

	public int getPageDebut() {
		return pageDebut;
	}

	public void setPageDebut(int pageDebut) {
		this.pageDebut = pageDebut;
	}

	public int getPageFin() {
		return pageFin;
	}

	public void setPageFin(int pageFin) {
		this.pageFin = pageFin;
	}

	public static void main(String[] args) throws IOException {
		
//		if(args.length<2){
//			showUsage();
//		}else{
			PDFUtil pdfutil = new PDFUtil();
			pdfutil.setCheminSauvegarde(".");
			pdfutil.optionSurlignerDifferences = true;
			pdfutil.optionComparaisonComplete = true;
			pdfutil.setModeDeComparaison(CompareMode.VISUAL_MODE);
			//pdfutil.compare("OPC1.pdf", "OPC2.pdf");
			pdfutil.compare("FIP1.pdf", "FIP2.pdf");
//		}
		
	}

	private static void showUsage(){
		System.out.println("Usage: java -jar pdf-util.jar file1.pdf file2.pdf");
	}
	
	
}