package beans;

/**
 * Interface "chapeau" pour la définition d'environement de test.
 * @author levieil_f
 *
 */
public interface EnvironementDeTest {

	/**
	 * Récupératon du code unique de l'environement.
	 * @return le code unique de l'envrioneement.
	 */
	public String getCode();
	
	/**
	 * Récupération du libelle de l'environement.
	 * @return le libelle.
	 */
	public String getEnvironement();
	
	/**
	 * Le code unique de la base associée à l'environement.
	 * @return le code de la base.
	 */
	public String getCodeBase();
	
	/**
	 * Le code associé au login pour l'environement.
	 * @return le code pour extraction du properties du login .
	 */
	public String getCodeLogin();
	
	/**
	 * Le code associé au password pour l'environement.
	 * @return le code pour extraction du properties du password .
	 */
	public String getCodePassword();
	
	/**
	 * Le code associé au login pour la base.
	 * @return le code pour extraction du properties du login .
	 */
	public String getCodeBaseLogin();
	
	/**
	 * Le code associé au password pour la base.
	 * @return le code pour extraction du properties du password .
	 */
	public String getCodeBasePassword();
	
	/**
	 * La mise sous forme de chaine de l'environement pour l'affichage.
	 * @return la chaine représentant l'environement.
	 */
	public String toString();
}
