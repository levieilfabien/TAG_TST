package beans;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;

import constantes.Clefs;

/**
 * Classe repr�sentant une cible lors de recherche dans le cadre de l'utilisation du driver S�l�nium.
 * Cette cible consiste principalement en une clef (Voir classe Clefs) et d'une s�rie de crit�res de recherche.
 * C'est une classe interne elle n'a pas vocation � �tre utilis� de l'ext�rieur du package.
 * @author kcw293
 *
 */
public class CibleBean implements Serializable {

	/**
	 * Id de s�rialisation.
	 */
	private static final long serialVersionUID = 3255061268993071061L;

	/**
	 * Clefs d'identification de l'objet dans l'interface.
	 */
	private Clefs clef = Clefs.ID;
	
	/**
	 * Ensemble des crit�res d'identification via la clef de l'objet recherch�.
	 * Ces crit�res sont d�finis dans les fonctions de recherche des driver et de d�finition des clefs.
	 */
	private String[] criteres;
	
	/**
	 * Crit�re optionnel de frame pour le recherche d'un �l�ment.
	 * La plupart du temps la valeur est � null ce qui signifie que l'�l�ment ne fait pas partie d'une frame ou d'une iframe.
	 */
	private String frame = null;
	
	/**
	 * Xpath vers l'objet si il est renseign�.
	 */
	private String xpath = "";
	
	/**
	 * Indicateur de recherche pere.
	 */
	public final static String PERE = "pere";
	
	/**
	 * Indicateur de recherche � inserer entre deux �l�ments imbriqu�s dont on ignore la distance l'un de l'autre.
	 * EX : .//*[@id='popupSyntheseContentTable']//*[@value='Valider']
	 */
	public final static String RECHERCHE = "/*";
	
	/**
	 * Indicateur de recherche par texte.
	 */
	public final static String CRITERE_TEXTE = "text";

	/**
	 * Capture d'�cran pour la cible si elle existe.
	 */
	private String capture = "capture";
	
	/**
	 * Permet de lister les �l�ments crit�re d'une recherche.
	 * @return la chaine composite des crit�res.
	 */
	public String lister() {
		String retour = "";
		for (String temp : criteres) {
			retour = retour.concat(temp + " ");
		}
		return retour;
	}
	
	///////////////////////////////////////// GETTERS & SETTERS //////////////////////////////////////////////
	
	public Clefs getClef() {
		return clef;
	}

	public void setClef(Clefs clef) {
		this.clef = clef;
	}

	public String[] getCriteres() {
		return criteres;
	}

	public void setCriteres(String[] criteres) {
		this.criteres = criteres;
	}

	public String getFrame() {
		return frame;
	}

	public void setFrame(String frame) {
		this.frame = frame;
	}
	
	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	
	public String getCapture() {
		return capture;
	}

	public void setCapture(String capture) {
		this.capture = capture;
	}
	
	/**
	 * Creer le by correspondant � une cible d�finie.
	 * @param cible la cible du by.
	 * @return le by creer � partir de la cible.
	 */
	private By creerBy(CibleBean cible) {
		return creerBy(cible.getClef(), cible.getCriteres());
	}
	
//	/**
//	 * Permet d'obtenir un by via des crit�res..
//	 * @param identification par quoi identifie t'on l'�l�ment.
//	 * @param valeur quelle valeur pour identifier.
//	 */
//	private By creerBy(Clefs identification, String valeur) {
//		By by = By.id(valeur);
//		if (Clefs.XPATH == identification) {
//			by = By.xpath(valeur);
//		} else if (Clefs.ID == identification) {
//			by = By.id(valeur);
//		} else if (Clefs.NAME == identification) {
//			by = By.name(valeur);
//		} else if (Clefs.LIEN == identification) {
//			by = By.linkText(valeur);
//		} else if (Clefs.LIENPARTIEL == identification) {
//			by = By.partialLinkText(valeur);
//		} else if (Clefs.CLASSE == identification) {
//			by = By.className(valeur);
//		} else if (Clefs.TEXTE_TAG == identification) {
//			by = By.xpath(".//*[normalize-space()='" + valeur + "']");
//		} else if (Clefs.TEXTE_COMPLET == identification) {
//			by = By.xpath(".//*[text()='" + valeur + "']");
//		} else if (Clefs.TEXTE_PARTIEL == identification) {
//			by = By.xpath(".//*[contains(text(),'" + valeur + "')]");
//		} else if (Clefs.PARENT_TEXTE_PARTIEL == identification) {
//			by = By.xpath(".//*[contains(text(),'" + valeur + "')]/..");
//		} else if (Clefs.VALEUR == identification) {
//			by = By.xpath(".//*[@*='" + valeur + "']");
//		}
//		//System.out.println(by);
//		return by;
//	}
	
	/**
	 * Permet d'obtenir un by via des crit�res multiples..
	 * Ex : //input[@name='Enregistrer' and @value='Valider' and @type='submit']
	 * @param identification par quoi identifie t'on l'�l�ment.
	 * @param valeur quelle valeurs servent � identifier.
	 * @return le by correspondant aux criteres.
	 */
	private By creerBy(Clefs identification, String...valeur) {
		By by = creerBy(identification, valeur[0]);
		
		if (Clefs.CRITERES_LIBRES == identification) {
			// Exemple : "//input[@name='Enregistrer' and @value='Valider' and @type='submit']"
			String chaineXpath = "//" + valeur[0] + "[";
			Integer pere = 0;
			boolean pair = false;
			boolean fonction = false;
			for (String critere : valeur) {
				if (!valeur[0].equals(critere)) {
					if (CRITERE_TEXTE.equals(critere)) {
						// On effectue une recherche par crit�re de texte, on fait appel � une fonction xpath.
						chaineXpath = chaineXpath.concat("contains(text(),");
						fonction = true;
						pair = !pair;
					} else if (PERE.equals(critere)) {	
						// Si PERE, alors on prend en compte le fait que l'on doit acceder � l'�l�ment p�re du r�sultat de la recherche.
						pere = pere + 1;
					} else {
						if (!pair) {
							// Si non pair, alors il s'agit du nom d'un attribut
							if (!valeur[1].equals(critere)) {
								chaineXpath = chaineXpath.concat(" and ");
							}
							chaineXpath = chaineXpath.concat("@" + critere + "=");
						} else {
							// Si pair, alors c'est une valeur associ�e � un attribut
							chaineXpath = chaineXpath.concat("\"" + critere + "\"");
							if (fonction) {
								// Si on est dans le cadre d'une fonction du xpath, alors on la cloture.
								chaineXpath = chaineXpath.concat(")");
								fonction = false;
							}
						}
						pair = !pair;
					}
				}
			}
			chaineXpath = chaineXpath.concat("]");
			// Pour chaque mention du p�re, on utilise .. pour remonter l'arborescence.
			for (int i = 0; i < pere; i++) {
				chaineXpath = chaineXpath.concat("/..");
			}
			// On creer le by correspondant au XPATH construit.
			//System.out.println(chaineXpath);
			by = By.xpath(chaineXpath);
			
		} else if (Clefs.CRITERES_ITERATIF == identification) {
			// Exemple : "//input[@name='Enregistrer' and @value='Valider' and @type='submit']"
			String chaineXpath = "/";
			boolean conditionsOuverte = false;
			
			for (String critere : valeur) {
				if (critere.contains("=")) {
					// Si le crit�re est une paire de valeur
					if (conditionsOuverte) {
						chaineXpath = chaineXpath.concat(" and ");
					} else {
						chaineXpath = chaineXpath.concat("[");
						conditionsOuverte = true;	
					}
					if (CRITERE_TEXTE.equals(critere.split("=")[0])) {
						// Cas particulier sur le text.
						chaineXpath = chaineXpath.concat("contains(text(),\"" + critere.split("=")[1] + "\")");
					} else {
						// Si le crit�re est une paire de valeur
						chaineXpath = chaineXpath.concat("@" + critere.split("=")[0] + "=\"" + critere.split("=")[1] + "\"");
					}
				} else {
					// Si le crit�re est seul
					if (conditionsOuverte) {
						chaineXpath = chaineXpath.concat("]");
						conditionsOuverte = false;
					}
					if (PERE.equals(critere)) {	
						// Si le crit�re est p�re
						chaineXpath = chaineXpath.concat("/..");
					} else {
						// Sinon
						chaineXpath = chaineXpath.concat("/" + critere);
					}
				}
			}
			// Si � la fin la condition est toujours ouverte, on la ferme.
			if (conditionsOuverte) {
				chaineXpath = chaineXpath.concat("]");
			}
			// On creer le by correspondant au XPATH construit.
			//System.out.println(chaineXpath);
			by = By.xpath(chaineXpath);
			System.out.println(chaineXpath);
		}
		return by;
	}
	
	/**
	 * Creer le by correspondant � une cible d�finie.
	 * @param cible la cible du by.
	 * @return le by creer � partir de la cible.
	 */
	public By creerBy() {
		if (getCriteres() == null || getCriteres().length < 1) {
			if (getXpath() != null && !"".equals(getXpath())) {
				return creerBy(Clefs.XPATH, getXpath());
			} else {
				return null;
			}
		}	
		return creerBy(getClef(), getCriteres());
	}
	
	/**
	 * Permet d'obtenir un by via des crit�res..
	 * @param identification par quoi identifie t'on l'�l�ment.
	 * @param valeur quelle valeur pour identifier.
	 */
	private By creerBy(Clefs identification, String valeur) {
		By by = By.id(valeur);
		String temp = "'";
		if (Clefs.XPATH == identification) {
			by = By.xpath(valeur);
		} else if (Clefs.ID == identification) {
			by = By.id(valeur);
		} else if (Clefs.NAME == identification) {
			by = By.name(valeur);
		} else if (Clefs.LIEN == identification) {
			by = By.linkText(valeur);
		} else if (Clefs.LIENPARTIEL == identification) {
			by = By.partialLinkText(valeur);
		} else if (Clefs.CLASSE == identification) {
			by = By.className(valeur);
		} else {
			// On remplace les simple quote par des doubles.
			if (valeur.contains("'")) {
				temp = "\"";
			} 
			if (Clefs.TEXTE_TAG == identification) {
				by = By.xpath(".//*[normalize-space()=" + temp + valeur + temp + "]");
			} else if (Clefs.TEXTE_COMPLET == identification) {
				by = By.xpath(".//*[text()=" + temp + valeur + temp + "]");
			} else if (Clefs.TEXTE_PARTIEL == identification) {
				by = By.xpath(".//*[contains(text()," + temp + valeur + temp + ")]");
			} else if (Clefs.PARENT_TEXTE_PARTIEL == identification) {
				by = By.xpath(".//*[contains(text()," + temp + valeur + temp + ")]/..");
			} else if (Clefs.VALEUR == identification) {
				by = By.xpath(".//*[@*=" + temp + valeur + temp + "]");
			}

		}
		//System.out.println(by);
		return by;
	}
	
//	/**
//	 * Permet d'obtenir un by via des crit�res multiples..
//	 * Ex : //input[@name='Enregistrer' and @value='Valider' and @type='submit']
//	 * @param identification par quoi identifie t'on l'�l�ment.
//	 * @param valeur quelle valeurs servent � identifier.
//	 * @return le by correspondant aux criteres.
//	 */
//	private By creerBy(Clefs identification, String...valeur) {
//		By by = creerBy(identification, valeur[0]);
//		
//		if (Clefs.CRITERES_LIBRES == identification) {
//			// Exemple : "//input[@name='Enregistrer' and @value='Valider' and @type='submit']"
//			String chaineXpath = "//" + valeur[0] + "[";
//			Integer pere = 0;
//			boolean pair = false;
//			boolean fonction = false;
//			for (String critere : valeur) {
//				if (!valeur[0].equals(critere)) {
//					if (CRITERE_TEXTE.equals(critere)) {
//						chaineXpath = chaineXpath.concat("contains(text(),");
//						fonction = true;
//						pair = !pair;
//					} else if (PERE.equals(critere)) {	
//						pere = pere + 1;
//					} else {
//						if (!pair) {
//							if (!valeur[1].equals(critere)) {
//								chaineXpath = chaineXpath.concat(" and ");
//							}
//							chaineXpath = chaineXpath.concat("@" + critere + "=");
//						} else {
//							if (critere.contains("'")) {
//								chaineXpath = chaineXpath.concat("\"" + critere + "\"");
//							} else {
//								chaineXpath = chaineXpath.concat("'" + critere + "'");
//							}
//							if (fonction) {
//								chaineXpath = chaineXpath.concat(")");
//								fonction = false;
//							}
//						}
//						pair = !pair;
//					}
//				}
//			}
//			chaineXpath = chaineXpath.concat("]");
//			for (int i = 0; i < pere; i++) {
//				chaineXpath = chaineXpath.concat("/..");
//			}
//			//System.out.println(chaineXpath);
//			by = By.xpath(chaineXpath);
//		}
//		
//		return by;
//	}
	
	///////////////////////////////////////// CONSTRUCTEURS //////////////////////////////////////////////

//	/**
//	 * Constructeur param�tr� pour une cible.
//	 * @param frame la frame ou ce situe la cible (null si pas de frame).
//	 * @param clef la clef d'identification de la cible.
//	 * @param criteres les crit�res d'identification.
//	 */
//	public CibleBean(String frame, Clefs clef, String[] criteres) {
//		super();
//		this.clef = clef;
//		this.criteres = criteres;
//		this.frame = frame;
//	}

	/**
	 * Constructeur param�tr� pour une cible.
	 * @param frame la frame ou ce situe la cible (null si pas de frame).
	 * @param clef la clef d'identification de la cible.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(String frame, Clefs clef, String... criteres) {
		super();
		this.clef = clef;
		this.criteres = criteres;
		this.frame = frame;
	}
	
	/**
	 * Constructeur param�tr� pour une cible sans frame.
	 * @param clef la clef d'identification de la cible.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(Clefs clef, String... criteres) {
		super();
		this.clef = clef;
		this.criteres = criteres;
		this.frame = null;
	}
	
	/**
	 * Constructeur param�tr� pour une cible sans frame.
	 * @param clef la clef d'identification de la cible.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(Clefs clef, List<String> criteres) {
		super();
		
		String[] criteresTab = new String[criteres.size()];
		for (int i = 0; i < criteres.size(); i++) {
			criteresTab[i] = criteres.get(i);
		}
		
		this.clef = clef;
		this.criteres = criteresTab;
		this.frame = null;
	}

	/**
	 * Constructeur param�tr� pour une cible sans frame identifi�e par son ID.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(String... criteres) {
		super();
		this.clef = Clefs.ID;
		this.criteres = criteres;
		this.frame = null;
	}
	
	/**
	 * Constructeur param�tr� pour une cible sans frame identifi�e par son ID.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(String criteres) {
		super();
		this.clef = Clefs.ID;
		this.criteres = new String[] {criteres};
		this.frame = null;
	}
	
	/**
	 * Constructeur param�tr� pour ajouter des crit�res suppl�mentaire � un cible disposant d�j� de crit�re.
	 * @param reference la cible de r�f�rence.
	 * @param criteres les crit�res d'identification.
	 */
	public CibleBean(CibleBean reference, String... criteres) {
		super();
		
		String[] criteresComplet = new String[reference.getCriteres().length + criteres.length];
		criteresComplet = ArrayUtils.addAll(reference.getCriteres(), criteres);
		
		this.clef = reference.getClef();
		this.criteres = criteresComplet;
		this.frame = reference.getFrame();
	}
	
	/**
	 * Renvoie la repr�sentation sous forme de chaine de la cible.
	 * @return la chaine repr�sentant la cible.
	 */
	@Override
	public String toString() {
		return this.getClef() + " " + lister(); 
	}
	
	/**
	 * Constructeur param�tr� pour une cible dependant d'une autre autre et partageant la m�me clefs.
	 * Ce constructeur est con�ue pour les crit�res it�ratifs.
	 * @param frame l'information de frame si il y a lieu.
	 * @param base la cible servant de base pour les autres recherches.
	 * @param criteres criteres suppl�mentaires pour la recherche.
	 */
	public CibleBean(String frame, CibleBean base, String... criteres) {
		super();
		this.clef = base.getClef();
		if (criteres != null && criteres.length > 0) {
			String[] criteresConcat = new String[base.getCriteres().length + criteres.length];
			int j = 0;
			for (int i = 0; i < criteresConcat.length; i++) {
				if (i < base.getCriteres().length) {
					criteresConcat[i] = base.getCriteres()[i];
				} else {
					criteresConcat[i] = criteres[j];
					j++;
				}
			}
			this.criteres = criteresConcat;
		} else {
			this.criteres = base.getCriteres();
		}
		this.frame = frame;
	}
}
