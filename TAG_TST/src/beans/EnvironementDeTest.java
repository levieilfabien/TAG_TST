package beans;

/**
 * Interface "chapeau" pour la d�finition d'environement de test.
 * @author levieil_f
 *
 */
public interface EnvironementDeTest {

	/**
	 * R�cup�raton du code unique de l'environement.
	 * @return le code unique de l'envrioneement.
	 */
	public String getCode();
	
	/**
	 * R�cup�ration du libelle de l'environement.
	 * @return le libelle.
	 */
	public String getEnvironement();
	
	/**
	 * Le code unique de la base associ�e � l'environement.
	 * @return le code de la base.
	 */
	public String getCodeBase();
	
	/**
	 * Le code associ� au login pour l'environement.
	 * @return le code pour extraction du properties du login .
	 */
	public String getCodeLogin();
	
	/**
	 * Le code associ� au password pour l'environement.
	 * @return le code pour extraction du properties du password .
	 */
	public String getCodePassword();
	
	/**
	 * Le code associ� au login pour la base.
	 * @return le code pour extraction du properties du login .
	 */
	public String getCodeBaseLogin();
	
	/**
	 * Le code associ� au password pour la base.
	 * @return le code pour extraction du properties du password .
	 */
	public String getCodeBasePassword();
	
	/**
	 * La mise sous forme de chaine de l'environement pour l'affichage.
	 * @return la chaine repr�sentant l'environement.
	 */
	public String toString();
}
