package ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import outils.PDFOutils;


/**
 * Classe d�crivant une IHM permettant la comparaison entre deux fichiers PDF et l'affichage du r�sultat de la comparaison.
 * @author levieilfa
 *
 */
public class IHMComparaisonPDF {

	/**
	 * Permet le choix du fichier repr�sentant la nouvelle version du fichier.
	 */
	JFileChooser nouveau = new JFileChooser();
	
	/**
	 * Permet le choix du fichier qui d�crit l'ancienne version du document � comparer.
	 */
	JFileChooser ancien = new JFileChooser();
	
	/**
	 * Active ou d�sactive la transparence sur les donn�es communes.
	 */
	JCheckBox optionTransparence = new JCheckBox("Transparence", true);
	
	/**
	 * Active ou d�sactive la tol�rance sur les donn�es communes.
	 */
	JCheckBox optionTolerance = new JCheckBox("Tol�rance", true);
	
	/**
	 * D�taille le seuil de tol�rance.
	 */
	JTextField seuilTolerance = new JTextField("50");
	
	/**
	 * Permet le choix de la taille du pr�fixe
	 */
	JTextField taillePrefixe = new JTextField("7");
	
	
	public static void main(String[] argv) {
		
		if (argv.length < 4) {		
			final IHMComparaisonPDF ihm =  new IHMComparaisonPDF();
			// Initialisation
			JFrame fenetre = new JFrame("Comparaison de PDF");
			fenetre.setLayout(new BorderLayout());
			JPanel libelle = new JPanel(new GridLayout(1, 2));
			JPanel saisie = new JPanel(new BorderLayout());
			JPanel fichiers = new JPanel(new FlowLayout());
			JPanel options = new JPanel(new FlowLayout());
			JPanel bouton = new JPanel(new FlowLayout());
			
			// Mise en forme
			ihm.taillePrefixe.setMinimumSize(new Dimension(30, 20));
			ihm.taillePrefixe.setPreferredSize(new Dimension(30, 20));
			
			// Obliger l'utilisation de fichier PDF
			FileFilter pdfFilter = new FileNameExtensionFilter("PDF", "pdf", "PDF");
			ihm.nouveau.setFileFilter(pdfFilter);
			ihm.ancien.setFileFilter(pdfFilter);
			ihm.nouveau.setAcceptAllFileFilterUsed(false);
			ihm.ancien.setAcceptAllFileFilterUsed(false);
	
			// On autorise la selection de plusieurs fichiers
			ihm.nouveau.setMultiSelectionEnabled(true);
			ihm.ancien.setMultiSelectionEnabled(true);
			ihm.nouveau.setControlButtonsAreShown(false);
			ihm.ancien.setControlButtonsAreShown(false);
			
			// Champs de saisie
			Font police = new Font("Serif", Font.BOLD, 20);
			JLabel nvoFichier = new JLabel("Nouveaux Fichiers", JLabel.CENTER);
			nvoFichier.setFont(police);
			nvoFichier.setForeground(Color.GREEN);
			JLabel ancFichier = new JLabel("Anciens Fichiers", JLabel.CENTER);
			ancFichier.setFont(police);
			ancFichier.setForeground(Color.RED);
			libelle.setBackground(Color.WHITE);
			libelle.add(nvoFichier);
			libelle.add(ancFichier);
			fichiers.add(ihm.nouveau);
			fichiers.add(ihm.ancien);
			options.add(ihm.optionTransparence);
			options.add(ihm.optionTolerance);
			options.add(new JLabel("Seuil :"));
			options.add(ihm.seuilTolerance);
			options.add(new JLabel("Taille du pr�fixe :"));
			options.add(ihm.taillePrefixe);
			
			//TODO : Permettre le param�trage des options
	//		ihm.seuilTolerance.setEnabled(false);
	//		ihm.optionTolerance.setEnabled(false);
	//		ihm.optionTransparence.setEnabled(false);
			
			saisie.add(libelle, BorderLayout.NORTH);
			saisie.add(fichiers, BorderLayout.CENTER);
			//saisie.add(options);
			
			// Initialisation du selecteur de r�pertoire de sortie.
			final JFileChooser choisirSortie = new JFileChooser();
			choisirSortie.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			choisirSortie.setDialogTitle("Choisir le repertoire de sortie");
			
			// On verifie que nous avons bien choisie un r�pertoire, sinon on quitte l'application.
			if (choisirSortie.showOpenDialog(options) != JFileChooser.APPROVE_OPTION) {
				System.exit(0);
			}
			
			// Boutons
			JButton comparer = new JButton(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//JButton source = (JButton) e.getSource();
					boolean differences = false;
					StringBuilder comparatif = new StringBuilder();
					HashMap<String, Boolean> resultats = new LinkedHashMap<String, Boolean>();
					try {
						int seuil = -1;
						if (ihm.optionTolerance.getSelectedObjects() != null) {
							seuil =  Integer.parseInt(ihm.seuilTolerance.getText());
						}
						int prefixe =  Integer.parseInt(ihm.taillePrefixe.getText());
						// R�cup�ration du r�pertoire de sortie
						File sortie = choisirSortie.getSelectedFile();					
						if (!sortie.isDirectory() || !sortie.canWrite()) {
							sortie = new File("C:\\Temp");
							JOptionPane.showMessageDialog((JButton) e.getSource(),"Repertoire de sortie choisie incorrect \n mise par d�faut � C:\\Temp","Repertoire incorrect",JOptionPane.ERROR_MESSAGE);
						}
						// Effectuer la comparaison.
						resultats = PDFOutils.comparerListePDF(ihm.nouveau.getSelectedFiles(), ihm.ancien.getSelectedFiles(), sortie, ihm.optionTransparence.getSelectedObjects() != null, ihm.optionTolerance.getSelectedObjects() != null, seuil, prefixe, true);
						
						// On analyse le retour.
						String temp = "";
						for (String clef : resultats.keySet()) {
							if (resultats.get(clef)) {
								temp = "Identiques\n";
							} else {
								temp = "Diff�rents\n";
								differences = true;
							}
							comparatif = comparatif.append(clef + ":\t\t" + temp);
						}
						
					} catch (Exception e1) {
						JOptionPane.showMessageDialog((JButton) e.getSource(),"L'erreur suivante est survenue : \n" + e1.getMessage(),"Exception",JOptionPane.ERROR_MESSAGE);
					} catch (OutOfMemoryError error) {
						error.printStackTrace();
						JOptionPane.showMessageDialog((JButton) e.getSource(),"La m�moire n'est pas suffisante pour effecuter la comparaison.\n Merci d'ajouter un crit�re de m�moire � votre JVM  \nVous pouvez aussi tenter de lancer depuis votre poste (Pas depuis le r�seau)\n" + error.getMessage(),"Manque de m�moire",JOptionPane.ERROR_MESSAGE);
					} finally {
						if (differences) {
							// On affiche cette popup que si il y a des diff�rences
							JOptionPane.showMessageDialog((JButton) e.getSource(),"Comparaison(s) Effectu�e(s) : \n" + comparatif.toString(),"Fin du traitement",JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog((JButton) e.getSource(),"Aucune diff�rences constat�es entre les documents \n Nb de comparaisons : " + resultats.keySet().size(),"Fin du traitement",JOptionPane.INFORMATION_MESSAGE);
						}
					}
	//				catch (NumberFormatException e2) {
	//					ihm.seuilTolerance.setBackground(Color.RED);
	//				} 
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
		} else {
			// On est en ligne de commande.
			System.out.println("Repertoire nouveau : " + argv[0]);
			System.out.println("Repertoire ancien : " + argv[1]);
			System.out.println("Repertoire sortie : " + argv[2]);
			System.out.println("Taille du pr�fixe : " + argv[3]);
			StringBuilder comparatif = new StringBuilder();
			Boolean difference = false;
			HashMap<String, Boolean> resultats = new LinkedHashMap<String, Boolean>();
			try {
				resultats = PDFOutils.comparerListePDF(new File(argv[0]), new File(argv[1]), new File(argv[2]), Integer.parseInt(argv[3]));
				// On analyse le retour.
				String temp = "";
				for (String clef : resultats.keySet()) {
					if (resultats.get(clef)) {
						temp = "Identiques\n";
					} else {
						temp = "Diff�rents\n";
						difference = true;
					}
					comparatif = comparatif.append(clef + ":\t\t" + temp);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("Une erreur est survenue lors de la comparaison : " + e1.getMessage());
			} finally {
				if (difference) {
					// On affiche cette popup que si il y a des diff�rences
					System.out.println("Comparaison(s) Effectu�e(s) : \n" + comparatif.toString());
				} else {
					System.out.println("Aucune diff�rences constat�es entre les documents \n Nb de comparaisons : " + resultats.keySet().size());
				}
			}
		}
	}
}
