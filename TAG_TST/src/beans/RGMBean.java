package beans;

import java.io.Serializable;

/**
 * Un RGMBean repr�sente une r�gle de gestion � respecter.
 * Pour d�finir une r�gle de gestion on doit fournir des crit�res pr�cis.
 * 1) Quelle est la cible de cette r�gle de gestion (un champs, un libelle ...)
 * 2) Quel est le type de RGM (�galit�, pr�sence, absence ...)
 * 3) Quel est la valeur attendue si il y a lieu
 * @author levieil_f
 *
 */
public class RGMBean extends ObjectifBean implements Serializable {
	
	/**
	 * Id de s�rialisation.
	 */
	private static final long serialVersionUID = 8835693530399657133L;
	
	/**
	 * Types de comparaisons.
	 */
	public static final int EGALITE = 1;
	public static final int INFERIEUR = 2;
	public static final int SUPERIEUR = 3;
	public static final int PRESENCE = 4;
	public static final int ABSENCE = 5;
	public static final int POSSIBLE = 6;
	public static final int IMPOSSIBLE = 7;

	/**
	 * La cible de la r�gle de gestion. Une cible valide est :
	 * - Un �lement de la page (WebElement)
	 * - Un texte (String)
	 * - Une cible ( Clef + valeur)
	 */
	private Object cible;
	
	/**
	 * Type de comparaison (1,2,3,4,5,6,7).
	 */
	private Integer typeComparaison;
	
	/**
	 * Constructeur pour une r�gle de gestion de base.
	 * @param description la description de la RGM si il y a lieu.
	 * @param cible la cible de la RGM (un �l�ment ou un texte)
	 * @param typeComparaison le type de comparaison ou d'existence � r�aliser.
	 * @param valide l'�tat de d�part de la r�gle.
	 */
	public RGMBean(String code, String description, Object cible, Integer typeComparaison, boolean valide) {
		super(description, code, valide);
		this.cible = cible;
		this.typeComparaison = typeComparaison;
	}
	
	/**
	 * Constructeur par d�faut pour une cible donn�e.
	 * On teste la pr�sence de la cible.
	 * @param cible la cible de la recherche.
	 */
	public RGMBean(String code, Object cible) {
		super("", code);
		this.cible = cible;
		this.typeComparaison = PRESENCE;
	}

	/**
	 * Construit une RGM � partir d'un objectif.
	 * @param bean le beau objectif.
	 */
	public RGMBean(ObjectifBean bean) {
		super(bean.getDescriptif(), bean.getCode(), bean.getEtat());
		this.cible = null;
		this.typeComparaison = PRESENCE;
	}
	
	///////////////////// GETTERS & SETTERS /////////////////////////////////////

	public Object getCible() {
		return cible;
	}

	public void setCible(Object cible) {
		this.cible = cible;
	}

	public Integer getTypeComparaison() {
		return typeComparaison;
	}

	public void setTypeComparaison(Integer typeComparaison) {
		this.typeComparaison = typeComparaison;
	}
}
