package constantes;

/**
 * Enumération des différentes possibilité d'identification des élements.
 * @author levieil_f
 *
 */
public enum Clefs {
	/**
	 * Recherche un élément dont le texte est identique au critère de recherche.
	 */
	TEXTE_COMPLET(0),
	/**
	 * Recherche complexe permettant de chercher un élément à partir de tout ou partie de son texte distinctif.
	 */
	TEXTE_PARTIEL(1),
	/**
	 * Recherche par la valeur d'un attribut de l'élément.
	 */
	VALEUR(2),
	/**
	 * Permet une recherche via l'id de l'élément.
	 */
	ID(3),
	/**
	 * Permet une recherche via le nom de l'élément.
	 */
	NAME(4),
	/**
	 * Permet une recherche parmi les liens de la page.
	 */
	LIEN(5),
	/**
	 * Permet de faire une recherche sur tout ou partie du texte d'un lien sur la page.
	 */
	LIENPARTIEL(6),
	/**
	 * Permet de faire une recherche basé sur l'attribut de classe de l'élément.
	 */
	CLASSE(7),
	/**
	 * Permet une recherche via un texte contenu dans le TAG d'un élément.
	 */
	TEXTE_TAG(8),
	/**
	 * Permet d'obtenir l'élément englobant le texte rechercher dans les mêmes critères que le texte partiel.
	 */
	PARENT_TEXTE_PARTIEL(9),
	/**
	 * Permet de faire une recherche via une expression XPATH valide.
	 */
	XPATH(10),
	/**
	 * Permet d'effectuer une selection en utilisant un selecteur CSS (ex : div[class='vacancy'])
	 */
	CSSSELECTOR(11),
	/**
	 * Permet une recherche via des criteres libres formulés par un critère général (ex : input) et des paires de critères (ex : id, monID).
	 * Les critères libres associent plusieurs condition pour un même objet.
	 * Exemple : "//input[@name='Enregistrer' and @value='Valider' and @type='submit']"
	 */
	CRITERES_LIBRES(12),
	/**
	 * Une recherche par paire de valeurs séparé éventuellement (td, class=classe, id=IDENTIFIANT, pere, td[2])
	 * Exemple : (*, id=popup, /*, value=valider) => //*[@id="popup"]//*[@value="Valider"]
	 */
	CRITERES_ITERATIF(13);
	
	/**
	 * Le code associé au type d'identification de l'element.
	 */
	private final int code;
	
	/**
	 * Constructeur privé.
	 * @param code le code associé au type d'identification.
	 */
	private Clefs(int code) {
		this.code = code;
	}

	/**
	 * Permet d'obtenir le code associé à une clef.
	 * @return le code de la clef.
	 */
	public int getCode() {
		return code;
	}
}
