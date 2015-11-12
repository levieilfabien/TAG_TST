package outils;

import interfaces.WrapLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import annotations.BaliseXml;
import elements.LinkedButton;
import elements.LinkedComboBox;
import elements.LinkedTextField;

public class IHMOutils {

	// Liste des sous panneau à masquer/afficher
	public List<JComponent> subPans = new LinkedList<JComponent>();
	
	public List<JComponent> listeSaisies = new LinkedList<JComponent>();
	
	public Object[] instanceReference;
	
	public JFrame ihmGeneree;
	
	private void genererTabulation(Object instanceAnnotee, JComponent contenant, String nom) {
		JTabbedPane sous_Tabulation = new JTabbedPane();
		sous_Tabulation.setName(nom);
		sous_Tabulation.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		JPanel nouveau_panel_englobant = new JPanel();
		nouveau_panel_englobant.setLayout(new WrapLayout());
		
		genererPanneau(instanceAnnotee, nouveau_panel_englobant, instanceAnnotee.getClass().getSimpleName());
		
		sous_Tabulation.addTab(nom, nouveau_panel_englobant);
		contenant.add(sous_Tabulation);
	}
	
	/**
	 * Génère un panneau à partir de l'instance annotée et l'ajoute au contenant si l'objet est simple.
	 * Si l'objet est complexe, on propose un sous panneau.
	 * @param instanceAnnotee l'objet qui permet de généré le panneau.
	 * @param contenant le contenant qui reçoit le sous panneau.
	 * @return le sous panneau si l'objet ajouté est complexe.
	 */
	private JTabbedPane genererPanneau(Object instanceAnnotee, JComponent contenant, String nom) {
		//System.out.println("On génère un panneau pour : " + instanceAnnotee.getClass().getCanonicalName());
		
		//Tester si on est dans une classe annotée. Si c'est le cas et que le champ est simple, le contenant n'est peu être pas encore un onglet !
		
		JTabbedPane sous_Tabulation = new JTabbedPane();
		try {			
			// On récupère les champs de la classe annotée
			for (Field champ : instanceAnnotee.getClass().getDeclaredFields()) {
				
				// On cherche l'annotation "Balise XML".
				BaliseXml annotation = champ.getAnnotation(BaliseXml.class);
				
				// On effectue la transformation.
				if (annotation != null) {
					// Si le champ est complexe on visite le sous objet
					if (annotation.complexe()) {
						// On prépare une sous tabulation au cas ou il y aurais des objets complexe
						sous_Tabulation.setName(nom);
						sous_Tabulation.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
						// pour chaque objet complexe on creer un panel englobant pour mettre au centre les données et au sud les boutons
						JPanel nouveau_panel_englobant = new JPanel();
						nouveau_panel_englobant.setLayout(new BorderLayout());
						nouveau_panel_englobant.setName(annotation.libelle());
						// On initie le nouveau panneau central contenant les données
						JPanel nouveau_panel = new JPanel();
						nouveau_panel.setLayout(new WrapLayout());
						//nouveau_panel.setLayout(new GridLayout(0,1));
						//JScrollPane nouveau_panel = new JScrollPane(new JPanel(),  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						nouveau_panel.setPreferredSize(new Dimension(900, 1500));
						nouveau_panel.setName(annotation.libelle());
						
						// Si multiple il faudras prévoir un bouton ajouter/retirer
						if (!annotation.multiple()) {
							// Si non multiple on alimente le contenu du nouveau panneau directement
							genererPanneau(champ.get(instanceAnnotee), nouveau_panel, annotation.libelle());
							// On positionne le nouveau panneau au centre du panneau englobannt
							nouveau_panel_englobant.add(nouveau_panel, BorderLayout.CENTER);
							// On positionne le panneau englobant comme une tabulation
							sous_Tabulation.addTab(annotation.libelle(), nouveau_panel_englobant);
							contenant.add(sous_Tabulation);
						} else {
							int i = 0;
							boolean contientComplexe = false;
							List tempList = ((List) champ.get(instanceAnnotee));
							// Si la liste est vide, on ajoute un élément
							if (tempList.size() <= 0 && !annotation.contenu().equals(Object.class)) {
								tempList.add(annotation.contenu().newInstance());
							}
							// On alimente le panneau pour chaque élément de la liste
							for(Object objet : (List) champ.get(instanceAnnotee)) {
								nouveau_panel.add(new JLabel(champ.getName()));
								nouveau_panel.add(new JLabel("N°" + i));
								JTabbedPane sousTab = genererPanneau(objet, nouveau_panel, annotation.libelle());
								if (sousTab.getName() != null && !"".equals(sousTab.getName())) {
									contientComplexe = true;
								}
								i++;
							}
							// Le sous panneau est utilisé pour les boutons Ajouter et retirer dans les cas de liste
							JPanel sousPanel = new JPanel();
							// Préparation d'un bouton d'ajout (permet une occurence supplémentaires de l'objet)
							LinkedButton ajouter = new LinkedButton(new Object[] {champ, instanceAnnotee}, contientComplexe?contenant:nouveau_panel, new AbstractAction() {
								@Override
								public void actionPerformed(ActionEvent e) {
									LinkedButton leBouton = (LinkedButton) e.getSource();
									Field champ = ((Field) ((Object[]) leBouton.getLink())[0]);
									BaliseXml annotation = champ.getAnnotation(BaliseXml.class);
									//((List) champ.get(instanceAnnotee)).add(annotation.contenu().newInstance());
									JPanel micro_panel = new JPanel();
									micro_panel.setLayout(new GridLayout(0,2));
									micro_panel.add(new JLabel(champ.getName()));
									micro_panel.add(new JLabel("Suppl"));
									leBouton.getContenant().add(micro_panel);
									try {
										Object nouvelleInstance = annotation.contenu().newInstance();
										((List) champ.get(((Object[]) leBouton.getLink())[1])).add(nouvelleInstance);
										genererPanneau(nouvelleInstance, leBouton.getContenant(), annotation.libelle());
									} catch (InstantiationException e1) {
										System.out.println("Impossible d'ajouter un élément");
									} catch (IllegalAccessException e1) {
										System.out.println("L'objet n'est pas correctement balisé");
									}
								}
							});
							ajouter.setText("Ajouter");
							ajouter.setSize(100, 50);
							sousPanel.add(ajouter);
							// Préparation du bouton retirer (ne fonctionne pas pour le moment)
							sousPanel.add(new JButton("Retirer"));
							// Positionnement des panneaux et sous panneau ensemble dans le contenant.
							nouveau_panel_englobant.add(nouveau_panel, BorderLayout.CENTER);
							nouveau_panel_englobant.add(sousPanel, BorderLayout.NORTH);
							sous_Tabulation.addTab(annotation.libelle(), nouveau_panel_englobant);
							contenant.add(sous_Tabulation);
						}
					} else {
						// Sinon on récupère la valeur telle quelle comme contenu de balise
						// Si l'annotation est simple, on ne créer par de sous panneau, on renseigne le contenant.
						if (!annotation.multiple()) {
							JPanel micro_panel = new JPanel();
							micro_panel.setLayout(new GridLayout(0,2));
							micro_panel.add(new JLabel(annotation.libelle()));
							if (annotation.enumeration().isEnum()) {
								LinkedComboBox zone = new LinkedComboBox(new Object[] {champ, instanceAnnotee}, contenant, annotation.enumeration().getEnumConstants());
								micro_panel.add(zone);
								zone.setStringValue(champ.get(instanceAnnotee).toString());
								zone.setToolTipText(annotation.libelle());
								listeSaisies.add(zone);
							} else if (annotation.listeValeur().length > 0) {
								LinkedComboBox zone = new LinkedComboBox(new Object[] {champ, instanceAnnotee}, contenant, annotation.listeValeur());
								zone.setStringValue(champ.get(instanceAnnotee).toString());
								micro_panel.add(zone);
								zone.setToolTipText(annotation.libelle());
								listeSaisies.add(zone);
							} else {
								LinkedTextField zone = new LinkedTextField(new Object[] {champ, instanceAnnotee}, contenant, champ.get(instanceAnnotee).toString());
								zone.setPreferredSize(new Dimension(400, 40));
								micro_panel.add(zone);
								zone.setToolTipText(annotation.libelle());
								listeSaisies.add(zone);
							}
							// On différencie l'affichage des champs obligatoire
							if (annotation.obligatoire()) {
								micro_panel.setBackground(Color.orange);
							} else {
								micro_panel.setBackground(Color.LIGHT_GRAY);
							}
							// Si on est directement dans un panel 
//							if (JTabbedPane.class == contenant.getClass()) {
//								//sous_Tabulation.addTab(annotation.libelle(), micro_panel);
//								getLastPane(((JTabbedPane) contenant)).add(micro_panel);
//							} else {
								contenant.add(micro_panel);
							//}
						} else {
							List<Object> liste = (List) champ.get(instanceAnnotee);
							for(int i = 0; i < liste.size(); i++) {
							//for(Object objet : (List) champ.get(instanceAnnotee)) {
								Object objet = liste.get(i);
								contenant.add(new JLabel(annotation.libelle()));
//								if (annotation.enumeration().isEnum()) {
//									contenant.add(new JComboBox(annotation.enumeration().getEnumConstants()));
//								} else if (annotation.listeValeur().length > 0) {
//									contenant.add(new JComboBox(annotation.listeValeur()));
//								} else {
									LinkedTextField zone = new LinkedTextField(new Object[] {champ, instanceAnnotee, i}, contenant, objet.toString());
									zone.setPreferredSize(new Dimension(400, 40));
									contenant.add(zone);
									listeSaisies.add(zone);
//								}
							}
						}
					}
				}
				
			}
			// Si une sous tabulation à été nécessaire elle est renvoyée
		//} catch (NoSuchMethodException e1, SecurityException e2, IllegalAccessException e3, IllegalArgumentException e4, InvocationTargetException e5, NoSuchFieldException e6) {
		} catch (Exception erreur) {
			erreur.printStackTrace();
			System.out.println("L'objet" + instanceAnnotee.getClass() + "n'est pas correctement balisé");
		}
		if (sous_Tabulation != null && !sous_Tabulation.equals(new JTabbedPane())) {
			subPans.add(sous_Tabulation);
		}
		
		return sous_Tabulation;
	}
	
	/**
	 * Génère une interface à partir d'un objet anoté XML.
	 * @param instanceAnnotee l'instance annotée.
	 * @return une fenêtre pour la saisie ou la lecture d'un objet anoté.
	 */
	public JFrame genererInterfaceXML(Object... instanceAnnotee) {
		JFrame retour = new JFrame("Interface générée");
		retour.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		retour.setLayout(new BorderLayout());
		
		instanceReference = instanceAnnotee;
		
		// Panneau du contenu (en une colonne)
		JTabbedPane contenu = new JTabbedPane();
		contenu.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		//contenu.setLayout(new GridLayout(0,1));
		
		// Pour chaque instance on génère un onglet spécifique.
		for (Object instance : instanceAnnotee) {
		
			JPanel northOnlyPanel = new JPanel();
			northOnlyPanel.setLayout(new BorderLayout());
			
			genererTabulation(instance, contenu, instance.getClass().getName());
			//genererPanneau(instance, contenu, instance.getClass().getName());
			
			// On place les contenant dans la fenêtre principale
			northOnlyPanel.add(contenu, BorderLayout.NORTH);
			JScrollPane scroll = new JScrollPane(northOnlyPanel,  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			retour.add(scroll, BorderLayout.CENTER);
		
		}
		
		// On prépare le panel des bouton
		JPanel bouton = new JPanel();
		
		// On ajoute le bouton pour sauvegarder les changements
		JButton sauvegarder = new JButton(new AbstractAction() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				sauvegarder();
			}
		});
		sauvegarder.setText("Sauvegarder");
		// On ajoute un bouton qui rafraichie l'IHM
		JButton rafraichir = new JButton(new AbstractAction() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				ihmGeneree.setVisible(false);
				ihmGeneree.pack();
				ihmGeneree.setVisible(true);
			}
		});
		rafraichir.setText("Rafraichir");
		
		bouton.add(sauvegarder);
		bouton.add(rafraichir);
		retour.add(bouton, BorderLayout.NORTH);
		
		ihmGeneree = retour;
		
		return retour;
	}
	
	/**
	 * Fonction de sauvegarde des saisies dans l'objet instancié.
	 */
	public void sauvegarder() {
		// Sauvegarde des instances à partir des saisies
		for (JComponent composant : listeSaisies) {
			if (composant.getClass().equals(LinkedTextField.class)) {
				((LinkedTextField) composant).majChampInstance();
			} else if (composant.getClass().equals(LinkedComboBox.class)) {
				((LinkedComboBox) composant).majChampInstance();
			}
		}
		// Affichage du contenu des XML obtenus
		for (Object instance : instanceReference) {
			JFrame affichage = new JFrame(instance.getClass().getCanonicalName());
			affichage.setSize(400, 700);
			affichage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JTextArea texte = new JTextArea(XMLOutils.toXml(instance), 400, 700);
			JScrollPane scroll = new JScrollPane(texte,  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			affichage.add(scroll);
			affichage.pack();
			affichage.setVisible(true);
		}
	}
	

	public JComponent getSelectedPane(JTabbedPane tabPane) {
	    // Get the index of the currently selected tab
	    //int selIndex = tabPane.getSelectedIndex();
	    // Select the last tab
	    //selIndex = tabPane.getTabCount() - 1;
	    //tabPane.setSelectedIndex(selIndex);
	   return  (JComponent) tabPane.getSelectedComponent();
	}
	
	/**
	 * Permet de connaitre la dernière tabulation du panneau.
	 * @param tabPane le panneau.
	 * @return la dernière tabulation.
	 */
	public JComponent getLastPane(JTabbedPane tabPane) {
		if (tabPane.getTabCount() > 0) {
			return  (JComponent) tabPane.getComponentAt(tabPane.getTabCount() - 1);
		} else {
			return  tabPane;
		}
	}
}
