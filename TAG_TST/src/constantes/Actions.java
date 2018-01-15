package constantes;

/**
 * Enum�ration servant � stocker des actions commune.
 * La boite � outil Selenium saura identifier la m�thode � associ� � chaque Actions.
 * @author levieilfa
 *
 */
public enum Actions {
	/**
	 * Fonction qui attend la pr�sence de la cible param�tre puis clique dessus. 
	 */
	CLIQUER(0, 1, "<Cible>"),
	/**
	 * Un simple clic gauche sur une cible.
	 */
	CLIQUER_GAUCHE(1, 1, "<Cible>"),
	/**
	 * Fonction de clic sur une cible qui se r�p�te jusqu'� ce que la seconde cible apparaisse. 
	 */
	CLIQUER_JUSQUA(2, 2, "<Cible> ET <CibleAttente>"),
	/**
	 * Fonction de clic optionnel. Si le clique n'est pas possible il ne se passe rien.
	 */
	CLIQUER_OPTIONNEL(3, 1, "<Cible>"),
	/**
	 * Fonction d'attente (aussi bien pour un texte qu'une cible)
	 */
	ATTENDRE(4, 1, "<Cible> OU <Texte>"),
	/**
	 * Fonction d'attente de valorisation
	 */
	ATTENDRE_VALEUR(5, 2, "<Cible> ET <Texte>"),
	/**
	 * Fonction de saisie.
	 */
	VIDER_ET_SAISIR(6, 2, "<Cible> ET <Texte>"),
	/**
	 * Fonction de selection. 
	 */
	SELECTIONNER(7, 2, "<Cible> ET <Texte>"),
	/**
	 * Effectue un clique si un texte est pr�sent � l'�cran.
	 */
	CLIQUER_SI_TEXTE(8, 2, "<Cible> ET <Texte>")
	;
	
	/**
	 * Le code associ� au type d'action.
	 */
	private final int code;
	
	/**
	 * Le code associ� au type d'action.
	 */
	private final int nbParam;

	/**
	 * La syntaxe
	 */
	private final String syntaxe;
	
	/**
	 * Constructeur priv�.
	 * @param code le code associ� au type d'action.
	 */
	private Actions(int code, int nbParam, String syntaxe) {
		this.code = code;
		this.nbParam = nbParam;
		this.syntaxe = syntaxe;
	}

	/**
	 * Permet d'obtenir le code associ� � une action.
	 * @return le code de l' action.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Permet d'indiquer le nombre de param�tre n�cessaire pour l'action.
	 * @return le nombre de param�tre.
	 */
	public int getNbParam() {
		return nbParam;
	}
	
	
}
