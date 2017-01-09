package outils;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;

import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.CIELab;


/**
 * Classe pour la manipulation des fichiers images comme le SVG.
 * Regroupe aussi les fonctions de manipulation de données visuelles comme les couleurs.
 * @author levieil_f
 *
 */
public class IMGOutils {
	
	static Logger logger = Logger.getLogger(IMGOutils.class.getName());

	/**
	 * Affiche une fenetre contenant l'image SVG à afficher.
	 * @param nomFichier le nom du fichier (où le chemin vers le fichier).
	 * @return la fenêtre obtenue.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static JFrame afficherFichierSVG(String nomFichier) throws SeleniumException {
        // Creation de la fenetre et de son contenu.
        final JFrame frame = new JFrame("Affichage SVG : " + nomFichier);
        final JPanel panel = new JPanel(new BorderLayout());
        final JSVGCanvas svgCanvas = new JSVGCanvas();
        
        // On tente d'afficher l'image.
        try {
			svgCanvas.setURI(new File(nomFichier).toURL().toString());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			throw new SeleniumException(Erreurs.E024, nomFichier);
		}
        
        // Mise en place des evenement d'affichage du fichier SVG.
        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                frame.pack();
            }
        });

        // On posisitionne les éléments en fonction des Layout.
        panel.add("Center", svgCanvas);

        frame.setContentPane(panel);
        
        // Affichage de la fenetre et de son contenu.
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
        
        return frame;
	}
	
	/**
	 * Affiche une fenetre contenant l'image PNG à afficher.
	 * @param nomFichier le nom du fichier (où le chemin vers le fichier).
	 * @return la fenêtre obtenue.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static JFrame afficherFichierPNG(String nomFichier) throws SeleniumException {
        // Creation de la fenetre et de son contenu.
        final JFrame frame = new JFrame("Affichage PNG : " + nomFichier);
        final JPanel panel = new JPanel(new BorderLayout());
        final BufferedImage image;
		try {
			image = ImageIO.read(new File(nomFichier));
	        
	        final Canvas canvas = new Canvas() {
	        	// Cette fonction sera appellée à chaque redimensionnement du canvas.
	        	public void paint(Graphics g)  
	            {  
	        		Dimension dim = super.getSize();
	        		int hauteur = dim.height;
	        		int largeur = dim.width;
	        		
	        		// On récupère la taille de l'écran
	        		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	        		int largeurEcran = gd.getDisplayMode().getWidth();
	        		int hauteurEcran = gd.getDisplayMode().getHeight();
	        		
	        		// On calcule le rapport entre la hauteur et la largeur de l'image afin de respecter les proportions.
	        		int rapport = hauteur/largeur;
	        		if (hauteur > hauteurEcran) {
	        			hauteur = hauteurEcran - 50;
	        			largeur = hauteur * rapport;
	        		} else if (largeur > largeurEcran) {
	        			largeur = largeurEcran - 50;
	        			hauteur = largeur * rapport;
	        		}
	        		
	                super.paint(g);  
	                Graphics2D g2 = (Graphics2D) g;  
	                g2.drawImage(image, 0, 0, largeur, hauteur, null);  
	            }  
	        	
			};       
	
	        // On posisitionne les éléments en fonction des Layout.
	        panel.add("Center", canvas);
	
	        frame.setContentPane(panel);
	        
	        // Affichage de la fenetre et de son contenu.
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		int hauteur = image.getHeight();
    		int largeur = image.getWidth();
    		
    		// On récupère les dimension des l'écran
    		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    		int largeurEcran = gd.getDisplayMode().getWidth();
    		int hauteurEcran = gd.getDisplayMode().getHeight();
    		
    		// On calcule le rapport entre la hauteur et la largeur de l'image afin de respecter les proportions.
    		int rapport = hauteur/largeur;
    		// Quoi qu'il arrive on s'assure que l'image ne dépasse pas la taille de l'écran.
    		if (hauteur > hauteurEcran) {
    			hauteur = hauteurEcran - 50;
    			largeur = hauteur * rapport;
    		} else if (largeur > largeurEcran) {
    			largeur = largeurEcran - 50;
    			hauteur = largeur * rapport;
    		}
    		
	        frame.setPreferredSize(new Dimension(largeur, hauteur));
	        frame.setVisible(true);
	        
	        frame.pack();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E024, nomFichier);
		}
        
        return frame;
	}
	
	/**
	 * Effectue une comparaison entre deux images et effectue une surbrillance sur les différences. Les ajout apparaissent en vert, les suppression en rouge et les autres différences apparaissent en Magenta.
	 * @param img1 la première image (base de comparaison)
	 * @param img2 la seconde image
	 * @param fileName le chemin vers le fichier de sauvegarde des différences (au format PNG)
	 * @param highlight à vrai si on souhaites une surbrillance (et la production du diff), à faux sinon.
	 * @return true si les documents sont identiques, false sinon.
	 * @throws IOException en cas d'erreur de manipulation des fichiers.
	 */
	public static boolean compareAndHighlight(final BufferedImage img1, final BufferedImage img2, String fileName, boolean highlight) throws IOException {
		return compareAndHighlight(img1, img2, fileName, highlight, true, true, 50);
	}
	
	/**
	 * Effectue une comparaison entre deux images et effectue une surbrillance sur les différences. Les ajout apparaissent en vert, les suppression en rouge et les autres différences apparaissent en Magenta.
	 * @param img1 la première image (base de comparaison)
	 * @param img2 la seconde image
	 * @param fileName le chemin vers le fichier de sauvegarde des différences (au format PNG)
	 * @param highlight à vrai si on souhaites une surbrillance (et la production du diff), à faux sinon.
	 * @param transparence à vrai si on souhaites que les pixels identiques soit légèrement transparent.
	 * @param tolerance à vrai si on souhaites que si un certain seuil n'est pas dépassé dans les distance, alors on ne colorise pas.
	 * @param seuilTolerance le seuil de distance de tolérane à respecter si tolerance est à vrai.
	 * @return true si les documents sont identiques, false sinon.
	 * @throws IOException en cas d'erreur de manipulation des fichiers.
	 */
	public static boolean compareAndHighlight(final BufferedImage img1, final BufferedImage img2, String fileName, boolean highlight, boolean transparence, boolean tolerance, int seuilTolerance) throws IOException {

		// On récupère les informations de tailles pour les images.
	    final int w = img1.getWidth();
	    final int h = img1.getHeight();
	    // On récupère l'ensemble des pixels composant chacune des images
	    final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
	    final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);
	    // P1 Va servir à stocker le merge des fichiers, ajout stockera uniquement les ajouts, suppression stockera uniquement les suppressions
	    final int[] ajout = img1.getRGB(0, 0, w, h, null, 0, w);
	    final int[] suppression = img2.getRGB(0, 0, w, h, null, 0, w);

	    // Si les deux tableaux sont différents c'est qu'il existe au moins un pixel qui n'est pas identique.
	    if(!(java.util.Arrays.equals(p1, p2))){
	    	logger.warning("Image comparée - Les images ne sont pas identiques");
	    	// On ne parcours tous les pixels que si on cherche à produire un diff
	    	if(highlight){
	    	    for (int i = 0; i < p1.length; i++) {
	    	        if (p1[i] != p2[i]){
	    	        	if (p2[i] == Color.WHITE.getRGB()) {
	    	        		////// AJOUT //////
	    	        		// Si P2 est blanc c'est que P1 est "en plus" par rapport à P2
	    	        		p1[i] = Color.GREEN.getRGB();
	    	        		ajout[i] = Color.GREEN.getRGB();
	    	        	} else if (p1[i] == Color.WHITE.getRGB()) {
	    	        		////// SUPPRESSION //////
	    	        		// Si P1 est blanc c'est que P1 est "en moins" par rapport à P2
	    	        		p1[i] = Color.RED.getRGB();
	    	        		suppression[i] = Color.RED.getRGB();
	    	        	} else {
	    	        		////// REMPLACEMENT /////
	    	        		// Si P1 & P2 ne sont pas blanc alors P1 est remplacé par P2 (on supperpose les deux)
	    	        		// On tolère une proximité des pixels de manière à éviter les faux positifs (décalages d'un pixel par exemple)
	    	        		if(!tolerance || (getDistance(p1[i], p2[i]) > seuilTolerance)) {
    	        				p1[i] = Color.MAGENTA.getRGB();
    	        				ajout[i] = Color.MAGENTA.getRGB();
    	        				suppression[i] = Color.MAGENTA.getRGB();
	    	        		}	
	    	        	}
	    	        } 
	    	        // Rendre transparent les zones communes entre les deux fichiers ?
	    	        else if (p1[i] != Color.WHITE.getRGB() && transparence) {
	    	        	p1[i] = rendreTransparent(p1[i], 0.75f);
	    	        	//p1[i] |= (0.1 & 0xff);
	    	        }
	    	    }
	    	    // Création de l'image à partir des nouvelles colorisations de P1.
//	    	    final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//	    	    out.setRGB(0, 0, w, h, p1, 0, w);
//	    	    saveImage(out, fileName);
//	    	    // Création de l'image d'ajout des nouvelles colorisations de ajout.
//	    	    final BufferedImage imgAjout = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//	    	    imgAjout.setRGB(0, 0, w, h, ajout, 0, w);
//	    	    saveImage(out, fileName);
//	    	    // Création de l'image de suppression des nouvelles colorisations de suppression.
//	    	    final BufferedImage imgSupp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//	    	    imgSupp.setRGB(0, 0, w, h, suppression, 0, w);
//	    	    saveImage(out, fileName);
	    	    
	    	    // On produit une image contenant deux sous images, les ajouts et les suppressions.
	    	    final BufferedImage multi = new BufferedImage(w*3, h, BufferedImage.TYPE_INT_ARGB);
	    	    multi.setRGB(0, 0, w, h, p1, 0, w);
	    	    multi.setRGB(w, 0, w, h, ajout, 0, w);
	    	    multi.setRGB(2*w, 0, w, h, suppression, 0, w);
	    	    
//	    	    saveImage(imgAjout, fileName.replace(".png", "ajout.png"));
//	    	    saveImage(imgSupp, fileName.replace(".png", "supp.png"));
	    	    saveImage(multi, fileName.replace(".png", "multi.png"));
	    	}
	    	return false;
	    }
	    return true;
	}
	
	/**
	 * Premet de rendre transparente suivant un facteur spécifié la couleur.
	 * @param color la couleur paramètre au format RGB.
	 * @param factor le facteur de transparence (ex : 0.5f pour semi transparence).
	 * @return la nouvelle valeur RGB de la couleur en tenant compte de la transparence.
	 */
	public static int rendreTransparent(int color, float factor) {
		Color temp = new Color(color);
	    int alpha = Math.round(temp.getAlpha() * factor);
	    int red = temp.getRed();
	    int green = temp.getGreen();
	    int blue = temp.getBlue();
	    return new Color(red, green, blue, alpha).getRGB();
	}
	
	/**
	 * Permet de calculer la distance entre deux couleurs au format RGB.
	 * Cette distance ne tiens pas compte de la transparence et s'effectue via une conversion au format CIE (norme).
	 * @param colorA la première couleur.
	 * @param colorB la seconde couleur.
	 * @return la différence entre les deux couleurs exprimée sous forme de double (ex : différence entre rouge et bleu = 175)
	 */
	public static double getDistance(int colorA, int colorB) {
		//Conversion en couleur RGB
		Color couleur1 = new Color(colorA);
		Color couleur2 = new Color(colorB);
		
		//Conversion en couleur CIE - Le calcul de la distance entre deux couleurs ce fait dans cet espace (convention)
		CIELab lab = CIELab.getInstance();
		float[] couleur3 = lab.fromRGB(new float[] {couleur1.getRed(), couleur1.getGreen(), couleur1.getBlue()});
		float[] couleur4 = lab.fromRGB(new float[] {couleur2.getRed(), couleur2.getGreen(), couleur2.getBlue()});
		
		// Calcul des distances entre les couleurs (L, a et b)
		double l = couleur3[0] - couleur4[0];
		double a = couleur3[1] - couleur4[1];
		double b = couleur3[2] - couleur4[2];
		
		// On calcule le deltaE, la distance entre deux couleurs
		double deltaE = Math.sqrt(Math.pow(l, 2) + Math.pow(a, 2) + Math.pow(b, 2));
		
		return deltaE;
	}


	/**
	 * Sauvegarde de l'image sur le disque.
	 * @param image l'image.
	 * @param file le fichier de destination.
	 */
	public static void saveImage(BufferedImage image, String file){
		try{
			File outputfile = new File(file);
			ImageIO.write(image, "png", outputfile);	
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	
//	public static void main(String args[]) throws SeleniumException {
//		IMGOutils.afficherFichierPNG("C:\\work\\PDF V15.11\\BAD_1_diff.png");
//	}
}
