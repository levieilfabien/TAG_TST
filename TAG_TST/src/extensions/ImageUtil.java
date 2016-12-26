package extensions;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

class ImageUtil {
	
	static Logger logger = Logger.getLogger(ImageUtil.class.getName());
	
	/**
	 * Effectue une comparaison entre deux images et effectue une surbrillance sur les diff�rences. Les ajout apparaissent en vert, les suppression en rouge et les autres diff�rences apparaissent dans la couleur choisie.
	 * @param img1 la premi�re image (base de comparaison)
	 * @param img2 la seconde image
	 * @param fileName le chemin vers le fichier de sauvegarde des diff�rences (au format PNG)
	 * @param highlight � vrai si on souhaites une surbrillance (et la production du diff), � faux sinon.
	 * @param colorCode le code couleur choisie pour les diff�rences autres qu'ajout et suppression.
	 * @return true si les documents sont identiques, false sinon.
	 * @throws IOException en cas d'erreur de manipulation des fichiers.
	 */
	static boolean compareAndHighlight(final BufferedImage img1, final BufferedImage img2, String fileName, boolean highlight, int colorCode) throws IOException {

		// On r�cup�re les informations de tailles pour les images.
	    final int w = img1.getWidth();
	    final int h = img1.getHeight();
	    // On r�cup�re l'ensemble des pixels composant chacune des images
	    final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
	    final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);

	    // Si les deux tableaux sont diff�rents c'est qu'il existe au moins un pixel qui n'est pas identique.
	    if(!(java.util.Arrays.equals(p1, p2))){
	    	logger.warning("Image compar�e - Les images ne sont pas identiques");
	    	// On ne parcours tous les pixels que si on cherche � produire un diff
	    	if(highlight){
	    	    for (int i = 0; i < p1.length; i++) {
	    	        if (p1[i] != p2[i]){
	    	        	if (p2[i] == Color.WHITE.getRGB()) {
	    	        		////// AJOUT //////
	    	        		// Si P2 est blanc c'est que P1 est "en plus" par rapport � P2
	    	        		p1[i] = Color.GREEN.getRGB();
	    	        	} else if (p1[i] == Color.WHITE.getRGB()) {
	    	        		////// SUPPRESSION //////
	    	        		// Si P1 est blanc c'est que P1 est "en moins" par rapport � P2
	    	        		p1[i] = Color.RED.getRGB();
	    	        	} else {
	    	        		////// REMPLACEMENT /////
	    	        		// Si P1 & P2 ne sont pas blanc alors P1 est remplac� par P2 (on supperpose les deux)
	    	        		p1[i] = colorCode;
	    	        	}
	    	        } 
	    	    }
	    	    // Cr�ation de l'image � partir des nouvelles colorisations de P1.
	    	    final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    	    out.setRGB(0, 0, w, h, p1, 0, w);
	    	    saveImage(out, fileName);
	    	}
	    	return false;
	    }
	    return true;
	}

	/**
	 * Sauvegarde de l'image sur le disque.
	 * @param image l'image.
	 * @param file le fichier de destination.
	 */
	static void saveImage(BufferedImage image, String file){
		try{
			File outputfile = new File(file);
			ImageIO.write(image, "png", outputfile);	
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
}
