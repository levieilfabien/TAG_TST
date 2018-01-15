package constantes;

/**
 * Enumération servant à stocker des actions commune.
 * La boite à outil Selenium saura identifier la méthode à associé à chaque Actions.
 * @author levieilfa
 *
 */
public enum Actions {
	/**
	 * Fonction qui attend la présence de la cible paramètre puis clique dessus. 
	 */
	CLIQUER(0, 1, "<Cible>"),
	/**
	 * Un simple clic gauche sur une cible.
	 */
	CLIQUER_GAUCHE(1, 1, "<Cible>"),
	/**
	 * Fonction de clic sur une cible qui se répète jusqu'à ce que la seconde cible apparaisse. 
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
	 * Effectue un clique si un texte est présent à l'écran.
	 */
	CLIQUER_SI_TEXTE(8, 2, "<Cible> ET <Texte>")
	;
	
	/**
	 * Le code associé au type d'action.
	 */
	private final int code;
	
	/**
	 * Le code associé au type d'action.
	 */
	private final int nbParam;

	/**
	 * La syntaxe
	 */
	private final String syntaxe;
	
	/**
	 * Constructeur privé.
	 * @param code le code associé au type d'action.
	 */
	private Actions(int code, int nbParam, String syntaxe) {
		this.code = code;
		this.nbParam = nbParam;
		this.syntaxe = syntaxe;
	}

	/**
	 * Permet d'obtenir le code associé à une action.
	 * @return le code de l' action.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Permet d'indiquer le nombre de paramètre nécessaire pour l'action.
	 * @return le nombre de paramètre.
	 */
	public int getNbParam() {
		return nbParam;
	}
	
	
}
