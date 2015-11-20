package constantes;

/**
 * Listes des erreurs rencontr�es lors de l'execution des tests.
 * @author levieil_f
 *
 */
public enum Erreurs {
	E001("E001", "Erreur technique lors du chargement du cookie, pas de cookie disponible?", "Erreur cookie", true),
	E002("E002", "Erreur technique lors du chargement du cookie, le cookie est obsel�te !", "Erreur cookie", true),
	E003("E003", "Le motif de non distribution n'as pas �t� trouv�.", "Element non trouv�", false),
	E004("E004", "Impossible d'extraire l'identifiant unique de la tournee ou de l'objet.", "Element non trouv�", true),
	E005("E005", "Impossible de charger les donnees de test." ,"Erreur donnees", true),
	E006("E006", "Il faut regenerer le cas de test � j, les donnees sont obsel�tes." ,"Erreur donnees", true),
	E007("E007", "Impossible de charger les donnees du cas d'essai." ,"Erreur donnees", true),
	E008("E008", "Il faut regenerer le cas de test, les donnees sont obsel�tes." ,"Erreur donnees", true),
	E009("E009", "Un �l�ment cherch� n'as pas �t� trouv�, le cas d'essai est en echec.", "Element non trouv�", false),
	E010("E010", "Impossible de lire le fichier de propri�t�es associ� aux tests dans le m�me r�pertoire.", "Erreur de propri�t�es", true),
	E011("E011", "Impossible de generer l'excel en export (Le fichier de template est il dans le repertoire?).", "Erreur de generation excel", true),
	E012("E012", "La page charg�e n'est pas la bonne", "Erreur lors de l'attente d'une page", true),
	E013("E013", "Un texte pr�sent sur la page ne devrais pas s'afficher.", "Erreur lors d'une l'attente", true),
	E014("E014", "L'analyse d'un contenu xml � �chou�", "Erreur lors de l'analyse d'un contenu xml", true),
	E015("E015", "Impossible d'int�rragir physiquement avec un �l�ment demand�.", "Erreur lors de l'interraction physique avec l'�cran", true),
	E016("E016", "Un �l�ment cherch� n'as pas �t� trouv� car il n'est pas visible, le cas d'essai est en echec.", "Element non trouv�", false),
	E017("E017", "Un �l�ment attendu n'est pas pr�sent, le cas d'essai est en echec.", "Element non trouv�", false),
	E018("E018", "R�gle de gestion non respect�e.", "Regle de gestion", false),
	E019("E019", "Une popup bloque la continuit�e du test.", "Popup inatendue", false),
	E020("E020", "Impossible d'acceder � un fichier.", "Probl�me de fichier", true),
	E021("E021", "On � acceder � un fichier dont le format est invalide.", "Probl�me de fichier", true),
	E022("E022", "Saisie impossible.", "Impossible de saisir dans le champ", false),
	E023("E023", "Un �l�ment attendu n'est plus pr�sent, la page � chang�e ou � �t� rafraichie.", "Element non trouv�", true),
	E024("E024", "Il est impossible d'afficher le fichier SVG.", "Fichier SVG incorrect", true),
	E025("E025", "Impossible d'ouvrir un fichier, d'y �crire ?", "Erreur de flux", true),
	E026("E026", "Un texte devrait s'afficher mais ne s'affiche pas", "Erreur lors d'une l'attente", true),
	E027("E027", "Impossible de pars� une date", "Date non valide", true),
	E029("E029", "Une information dans le fichier XML est incorrecte", "Balise incorrecte ou inexistante", true),
	E030("E030", "Une action n'as pas pu s'execut�e", "Action impossible", false),
	E031("E031", "Un �l�ment sur la page ne devrait pas s'afficher", "El�ment inattendu", true),
	E032("E032", "Impossible de communiquer correctement avec ALM", "Erreur ALM", true),
	E033("E033", "Element mal configur� pour une communication avec ALM", "Erreur ALM", true);
	
	/**
	 * Le code associ� a l'erreur.
	 */
	private final String code;
	
	/**
	 * Le commentaire associ� � l'erreur.
	 */
	private final String commentaire;
	
	/**
	 * Le libelle associ� � l'erreur.
	 */
	private final String libelle;
	
	/**
	 * Indique si il s'agit d'une erreur technique.
	 */
	private final Boolean technique;
	
	/**
	 * Constructeur priv�.
	 */
	private Erreurs(String code, String commentaire, String libelle, Boolean technique) {
		this.code = code;
		this.commentaire = commentaire;
		this.libelle = libelle;
		this.technique = technique;
	}

	public String getCode() {
		return code;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public String getLibelle() {
		return libelle;
	}

	public Boolean getTechnique() {
		return technique;
	}
	
	@Override
	public String toString() {
		String retour = code + " " + libelle + " :\n" + commentaire;
		if (getTechnique()) {
			retour = "Erreur technique " + retour;
		}
		return retour;
	}
}
