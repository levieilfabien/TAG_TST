package outils;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;

import constantes.Erreurs;
import exceptions.SeleniumException;


/**
 * Classe pour la manipulation des fichiers SVG.
 * @author levieil_f
 *
 */
public class IMGOutils {

	/**
	 * Affiche une fenetre contenant l'image SVG à afficher.
	 * @param nomFichier le nom du fichier (où le chemin vers le fichier).
	 * @return la fenêtre obtenue.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static JFrame affichierFichierSVG(String nomFichier) throws SeleniumException {
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
	public static JFrame affichierFichierPNG(String nomFichier) throws SeleniumException {
        // Creation de la fenetre et de son contenu.
        final JFrame frame = new JFrame("Affichage PNG : " + nomFichier);
        final JPanel panel = new JPanel(new BorderLayout());
        final BufferedImage image;
		try {
			image = ImageIO.read(new File(nomFichier));
	        
	        final Canvas canvas = new Canvas() {
	        	public void paint(Graphics g)  
	            {  
	                super.paint(g);  
	                Graphics2D g2 = (Graphics2D) g;  
	                g2.drawImage(image, 0, 0, null);  
	            }  
	        	
			};       
	
	        // On posisitionne les éléments en fonction des Layout.
	        panel.add("Center", canvas);
	
	        frame.setContentPane(panel);
	        
	        // Affichage de la fenetre et de son contenu.
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	        frame.setVisible(true);
	        
	        frame.pack();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SeleniumException(Erreurs.E024, nomFichier);
		}
        
        return frame;
	}
	
	
	public static void main(String args[]) throws SeleniumException {
		IMGOutils.affichierFichierPNG("captures/TRACEO-Selectiondusite1362567866707.png");
	}
}
