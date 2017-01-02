package ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import exceptions.SeleniumException;
import outils.IMGOutils;
import outils.PDFOutils;


/**
 * Classe décrivant une IHM permettant la comparaison entre deux fichiers PDF et l'affichage du résultat de la comparaison.
 * @author levieilfa
 *
 */
public class IHMComparaisonPDF {

	/**
	 * Permet le choix du fichier représentant la nouvelle version du fichier.
	 */
	JFileChooser nouveau = new JFileChooser();
	
	/**
	 * Permet le choix du fichier qui décrit l'ancienne version du document à comparer.
	 */
	JFileChooser ancien = new JFileChooser();
	
	/**
	 * Active ou désactive la transparence sur les données communes.
	 */
	JCheckBox optionTransparence = new JCheckBox("Transparence", true);
	
	/**
	 * Active ou désactive la tolérance sur les données communes.
	 */
	JCheckBox optionTolerance = new JCheckBox("Tolérance", true);
	
	/**
	 * Détaille le seuil de tolérance.
	 */
	JTextField seuilTolerance = new JTextField("50");
	
	
	public static void main(String[] argv) {
		
		final IHMComparaisonPDF ihm =  new IHMComparaisonPDF();
		// Initialisation
		JFrame fenetre = new JFrame("Comparaison de PDF");
		fenetre.setLayout(new BorderLayout());
		JPanel saisie = new JPanel(new FlowLayout());
		JPanel fichiers = new JPanel(new FlowLayout());
		JPanel options = new JPanel(new FlowLayout());
		JPanel bouton = new JPanel(new FlowLayout());
		
		// Obliger l'utilisation de fichier PDF
		FileFilter pdfFilter = new FileNameExtensionFilter("PDF", "pdf", "PDF");
		ihm.nouveau.setFileFilter(pdfFilter);
		ihm.ancien.setFileFilter(pdfFilter);
		
		// Champs de saisie
		fichiers.add(ihm.nouveau);
		fichiers.add(ihm.ancien);
		options.add(ihm.optionTransparence);
		options.add(ihm.optionTolerance);
		options.add(ihm.seuilTolerance);
		
		//TODO : Permettre le paramètrage des options
		ihm.seuilTolerance.setEnabled(false);
		ihm.optionTolerance.setEnabled(false);
		ihm.optionTransparence.setEnabled(false);
		
		saisie.add(fichiers);
		//saisie.add(options);
		
		// Boutons
		JButton comparer = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton source = (JButton) e.getSource();
				
				try {
					int seuil = -1;
					if (ihm.optionTolerance.getSelectedObjects() != null) {
						seuil =  Integer.parseInt(ihm.seuilTolerance.getText());
					}
					PDFOutils.comparerPDF(ihm.nouveau.getSelectedFile(), ihm.ancien.getSelectedFile(), -1, -1, true, ihm.optionTransparence.getSelectedObjects() != null, ihm.optionTolerance.getSelectedObjects() != null, seuil);

					File repertoire = ihm.nouveau.getSelectedFile().getParentFile();
					System.out.println(repertoire.getAbsolutePath());
					if (repertoire.isDirectory()) {
						for(File file : repertoire.listFiles()) {
							if (file.isFile()) {
								System.out.println(file.getAbsolutePath());
								String temp = file.getAbsolutePath();
								if (temp.endsWith("_diff.png")) {
									IMGOutils.afficherFichierPNG(file.getAbsolutePath());
								}
							}
						}
					}
				
					ihm.nouveau.setBackground(Color.GREEN);
					ihm.ancien.setBackground(Color.GREEN);
				} catch (SeleniumException e1) {
					ihm.nouveau.setBackground(Color.RED);
					ihm.ancien.setBackground(Color.RED);
				} catch (NumberFormatException e2) {
					ihm.nouveau.setBackground(Color.RED);
					ihm.ancien.setBackground(Color.RED);
					ihm.seuilTolerance.setBackground(Color.RED);
				} 
			}
		});
		comparer.setText("Comparaison");
		bouton.add(comparer);
		
		// Mise en place
		fenetre.add(saisie, BorderLayout.NORTH);
		fenetre.add(options, BorderLayout.CENTER);
		fenetre.add(bouton, BorderLayout.SOUTH);
		
		// Affichage
		fenetre.pack();
		fenetre.setVisible(true);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
