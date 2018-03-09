package constantes;

/**
 * Enum�ration des diff�rentes possibilit� d'identification des �lements.
 * @author levieil_f
 *
 */
public enum Clefs {
	/**
	 * Recherche un �l�ment dont le texte est identique au crit�re de recherche.
	 */
	TEXTE_COMPLET(0),
	/**
	 * Recherche complexe permettant de chercher un �l�ment � partir de tout ou partie de son texte distinctif.
	 */
	TEXTE_PARTIEL(1),
	/**
	 * Recherche par la valeur d'un attribut de l'�l�ment.
	 */
	VALEUR(2),
	/**
	 * Permet une recherche via l'id de l'�l�ment.
	 */
	ID(3),
	/**
	 * Permet une recherche via le nom de l'�l�ment.
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
	 * Permet de faire une recherche bas� sur l'attribut de classe de l'�l�ment.
	 */
	CLASSE(7),
	/**
	 * Permet une recherche via un texte contenu dans le TAG d'un �l�ment.
	 */
	TEXTE_TAG(8),
	/**
	 * Permet d'obtenir l'�l�ment englobant le texte rechercher dans les m�mes crit�res que le texte partiel.
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
	 * Permet une recherche via des criteres libres formul�s par un crit�re g�n�ral (ex : input) et des paires de crit�res (ex : id, monID).
	 * Les crit�res libres associent plusieurs condition pour un m�me objet.
	 * Exemple : "//input[@name='Enregistrer' and @value='Valider' and @type='submit']"
	 */
	CRITERES_LIBRES(12),
	/**
	 * Une recherche par paire de valeurs s�par� �ventuellement (td, class=classe, id=IDENTIFIANT, pere, td[2])
	 * Exemple : (*, id=popup, /*, value=valider) => //*[@id="popup"]//*[@value="Valider"]
	 */
	CRITERES_ITERATIF(13);
	
	/**
	 * Le code associ� au type d'identification de l'element.
	 */
	private final int code;
	
	/**
	 * Constructeur priv�.
	 * @param code le code associ� au type d'identification.
	 */
	private Clefs(int code) {
		this.code = code;
	}

	/**
	 * Permet d'obtenir le code associ� � une clef.
	 * @return le code de la clef.
	 */
	public int getCode() {
		return code;
	}
}
