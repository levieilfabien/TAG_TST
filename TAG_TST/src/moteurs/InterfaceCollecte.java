package moteurs;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.firefox.FirefoxProfile;

import outils.SeleniumOutils;
import beans.CibleBean;
import beans.EcranBean;
import exceptions.SeleniumException;

/**
 * L'interface de collecte destinée à l'utilisateur pour faciliter la déclaration de cas de test.
 * @author Fabien Levieil
 *
 */
public class InterfaceCollecte extends JFrame {

	/**
	 * Id de sérialisation.
	 */
	private static final long serialVersionUID = 3667579268240726234L;

	/**
	 * Le driver en cours de manipulation.
	 */
	private GenericDriver driver; 
	
	/**
	 * Le champ contenant l'info discriminante du nom de l'écran.
	 */
	private JTextField champInfo = new JTextField(30); 
	
	/**
	 * Select pour le choix de la cible courante.
	 */
	private JComboBox choixCible = new JComboBox();
	
//	/**
//	 * Le champ contenant le code source de la page.
//	 */
//	private JTextArea sourceCode = new JTextArea(); 
	
	/**
	 * Le champ contenant le code source du script.
	 */
	private JTextArea script = new JTextArea(); 
	
	/**
	 * Le bouton de l'interface pour le rafraichissement des données affichées.
	 */
	private JButton refresh = new JButton("Actualiser");
	
	/**
	 * Map permettant de stockée de les informations des différents écrans parcourues.
	 */
	private HashMap<String, EcranBean> ecrans = new LinkedHashMap<String, EcranBean>();
	
	/**
	 * Fonction de rafraichissement de l'interface de collecte.
	 * Si des données sont disponibles pour l'écran en cours de consultation, on présente les infos à l'utilisateur.
	 */
	public void refresh() {
		if (driver != null) {
			champInfo.setText(driver.getTitle());
			choixCible.removeAllItems();
			EcranBean tempEcran = ecrans.get(driver.getTitle());
			if (tempEcran != null && tempEcran.getCibles() != null) {
				for (String clef : tempEcran.getCibles().keySet()) {
					choixCible.addItem(tempEcran.getCibles().get(clef));
				}
			}
			SeleniumOutils outil = new SeleniumOutils(driver);
			try {
				outil.detecterClicParJavascript();
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			champInfo.setText("VIDE");
		}
		pack();
	}
	
	public void voirDernierClic() {
		SeleniumOutils outil = new SeleniumOutils(driver);
		script.setText(script.getText() + outil.dernierClicParJavascript());
		
	}
	
	/**
	 * Gère la mise en évidence d'un élément de la page, et l'affichage des infos.
	 * @param cible cible qui à été selectionnée.
	 */
	public void selectionCible(CibleBean cible) {	
		if (cible != null && driver != null) {
			SeleniumOutils outil = new SeleniumOutils(driver);	
			try {
				outil.surlignerElement(outil.obtenirElement(cible));
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gère la mise en évidence d'un élément de la page, et l'affichage des infos.
	 * @param cible cible qui à été déselectionnée.
	 */
	public void deSelectionCible(CibleBean cible) {	
		if (cible != null && driver != null) {
			SeleniumOutils outil = new SeleniumOutils(driver);	
			try {
				outil.deSurlignerElement(outil.obtenirElement(cible));
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gère le focus sur l'objet selectionné.
	 * @param cible cible qui à été selectionnée.
	 */
	public void focusSurCible(CibleBean cible) {	
		if (cible != null && driver != null) {
			SeleniumOutils outil = new SeleniumOutils(driver);	
			try {
				outil.focusElement(outil.obtenirElement(cible));
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Permet d'aspirer la page courante et d'afficher les données collectées.
	 * @param aspirateur l'instance d'aspirateur à utiliser.
	 */
	public void aspirerPageCourante(AspirateurEcran aspirateur) {
		try {	
			SeleniumOutils outil = new SeleniumOutils(driver);		
			EcranBean temp = aspirateur.aspirerPage(outil.sourceParFrame(aspirateur.obtenirListeIdFrame(driver.getPageSource())));
			//outil.capturesEcrans(temp);
			aspirateur.genererExcelEcran(temp);
			ecrans.put(temp.getIdentifiant(), temp);
		} catch (SeleniumException e) {
			e.printStackTrace();
			champInfo.setText("L'aspiration à échouée");
		}
	}
	
	
	/**
	 * Constructeur de l'interface de collecte.
	 */
	public InterfaceCollecte(final GenericDriver driverCible) {
		super("Interface de collecte");
		
		//Toolkit.getDefaultToolkit().addAWTEventListener(new Espion(), AWTEvent.MOUSE_EVENT_MASK);
		
		this.driver = driverCible;
		final SeleniumOutils outil = new SeleniumOutils(driver);
		final AspirateurEcran aspirateur = new AspirateurEcran(AspirateurEcran.ASPIRATION_SIMPLE);
		
		outil.ajouterListener();
		
		JPanel contenuBandeau = new JPanel();
		JPanel contenuAction = new JPanel();
		JPanel contenuScript = new JPanel();
		
		// Bouton pour rafraichir le titre de page.
		refresh = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (driver != null) {
					refresh();
				}
			}
		});
		refresh.setText("Actualiser");
		refresh.setEnabled(true);
		
//		JButton absorb = new JButton(new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if (driver != null) {
//					sourceCode.setText(driver.getPageSource());
//					pack();
//				} else {
//					sourceCode.setText("VIDE");
//				}
//			}
//		});
//		absorb.setText("Absorber");
//		absorb.setEnabled(true);
		
		// Bouton pour aspirer la page.
		JButton aspirer = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (driver != null) {
					aspirerPageCourante(aspirateur);
					refresh();
				}
			}
		});
		aspirer.setText("Aspirer");
		aspirer.setEnabled(true);
		
		// Bouton pour fermer le driver
		JButton quit = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (driver != null) {
					driver.quit();
				}
			}
		});
		quit.setText("Fermer le navigateur");
		quit.setEnabled(true);
		
		// Ajout d'une écoute de la selection d'une valeur dans la liste
		choixCible.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent event) {
		       if (event.getStateChange() == ItemEvent.SELECTED) {
		    	   selectionCible((CibleBean) event.getItem());
		       }
		       if (event.getStateChange() == ItemEvent.DESELECTED) {
		    	   deSelectionCible((CibleBean) event.getItem());
		        }
		    }       
		});
		
		// Paramètrage du bouton pour le focus.
		JButton focus = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (driver != null && choixCible.getSelectedItem() != null) {
					focusSurCible((CibleBean) choixCible.getSelectedItem());
				}
			}
		});
		focus.setText("Focus");
		focus.setEnabled(true);
		
		// Paramètrage du bouton pour le clic.
		JButton clic = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (driver != null && choixCible.getSelectedItem() != null) {
					try {
						outil.cliquer((CibleBean) choixCible.getSelectedItem());
						script.setText(script.getText().concat("\nOn clique sur " + choixCible.getSelectedItem()));
					} catch (SeleniumException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		clic.setText("Clic sur cible");
		clic.setEnabled(true);
		
		// Paramètrage du bouton pour le clic.
		JButton attendrePage = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				script.setText(script.getText().concat("\nOn attend " + driver.getTitle()));
			}
		});
		attendrePage.setText("Attente Page");
		attendrePage.setEnabled(true);
		
		// Paramètrage du bouton pour le clic.
		JButton changerPage = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				outil.changerDeFenetre();
				script.setText(script.getText().concat("\nOn change de fenetre pour " + driver.getTitle()));
			}
		});
		changerPage.setText("Changer Page");
		changerPage.setEnabled(true);
		
		// Paramètrage du bouton pour le clic.
		JButton dernierClic = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				script.setText(script.getText() + "\n" + outil.dernierClicParJavascript());
			}
		});
		dernierClic.setText("Dernier clic");
		dernierClic.setEnabled(true);
		
		contenuBandeau.add(champInfo);
		contenuBandeau.add(refresh);
		contenuBandeau.add(aspirer);
		contenuBandeau.add(quit);
		
		JPanel contenuMilieu = new JPanel();
		contenuMilieu.setLayout(new BorderLayout());
		contenuMilieu.add(choixCible, BorderLayout.NORTH);
		contenuMilieu.add(contenuAction, BorderLayout.CENTER);
		contenuAction.add(focus);
		contenuAction.add(clic);
		contenuAction.add(attendrePage);
		contenuAction.add(changerPage);
		contenuAction.add(dernierClic);
		
		contenuScript.add(script);
		
		this.setLayout(new BorderLayout());
		this.add(contenuBandeau, BorderLayout.NORTH);
		this.add(contenuMilieu, BorderLayout.CENTER);
		this.add(contenuScript, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
	
	////////////////////////////// TODO A SUPPRIMER /////////////////////////////////////////////////
	
	
	public static void main(String args[]) throws SeleniumException {
		//SeleniumDriver driver = new SeleniumDriver("http://proxypac.log.intra.laposte.fr/proxyie.pac");
		
		//profile.setPreference("general.config.filename", new File("Test").getAbsolutePath() + File.separator + "test.cfg");
		FirefoxImpl driver = new FirefoxImpl(configurerProfil());
		 
		//driver.ecouter();
		SeleniumOutils outil = new SeleniumOutils(driver);
		//SeleniumDriver driver = new SeleniumDriver("http://proxypac.log.intra.laposte.fr/proxyie.pac");
		
	    //Connection à l'intranet
	    driver.get("http://www.wac.courrier.intra.laposte.fr/");
	    
	    InterfaceCollecte interfaceCollecte = new InterfaceCollecte(driver);
	}
	
	
	
	
	public static FirefoxProfile configurerProfil() {
		FirefoxProfile profile = new FirefoxProfile();
		Proxy proxy = new Proxy();
		proxy = proxy.setAutodetect(false);
		proxy = proxy.setProxyType(ProxyType.MANUAL);
		//TODO : à remplacer
		//profile.setProxyPreferences(proxy);
		profile.setPreference("network.http.phishy-userpass-length", 255);
		profile.setPreference("network.proxy.autoconfig_url", "http://proxypac.log.intra.laposte.fr/proxyie.pac");
		profile.setPreference("network.proxy.no_proxies_on", "*,*.intra.laposte.fr,localhost,127.0.0.1,93.93*,187.0.22.240,187.0.22.238,79.125.41.167,.spt.net2-courrier.extra.laposte.fr,.refprod.net-courrier.extra.laposte.fr,.refprod.pprd.net2-courrier.extra.laposte.fr,rao.net-courrier.extra.laposte.fr,gestri.net-courrier.extra.laposte.fr,refprod.net-courrier.extra.laposte.fr,.reftournees.pprd.net2-courrier.extra.laposte.fr,.gestri.pprd.net2-courrier.extra.laposte.fr");
		profile.setPreference("network.proxy.ftp", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.http", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.ssl", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.socks", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.gopher", "web.pandore.log.intra.laposte.fr");
		profile.setPreference("network.proxy.gopher_port", 8080);
		profile.setPreference("network.proxy.ftp_port", 8080);
		profile.setPreference("network.proxy.socks_port", 8080);
		profile.setPreference("network.proxy.http_port", 8080);
		profile.setPreference("network.proxy.ssl_port", 8080);
		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.share_proxy_settings", Boolean.TRUE);
		//profile.setPreference("network.negotiate-auth.allow-proxies", Boolean.FALSE);
		//profile.setPreference("network.automatic-ntlm-auth.allow-proxies", Boolean.FALSE);
		profile.setPreference("network.auth.use-sspi", Boolean.FALSE);
		profile.setPreference("capability.policy.strict.Window.alert", "noAccess");
		profile.setPreference("network.negotiate-auth.trusted-uris", "http://www.wac.courrier.intra.laposte.fr/,http://idp.si-tri.com/,https://gestri.recdtc.dip.courrier.intra.laposte.fr/gestri/");
		profile.setPreference("network.automatic-ntlm-auth.trusted-uris", "http://www.wac.courrier.intra.laposte.fr/,http://idp.si-tri.com/,gestri.assemblage.net3-courrier.extra.laposte.fr,https://gestri.recdtc.dip.courrier.intra.laposte.fr/gestri/");
		return profile;
	}
	
	   private static class Espion implements AWTEventListener {
	        public void eventDispatched(AWTEvent event) {
	        	if (event.getID() == MouseEvent.MOUSE_CLICKED) {
	        		System.out.print(MouseInfo.getPointerInfo().getLocation() + " | ");
	        		System.out.println(event);
	        	}
	        }
	    }
}
