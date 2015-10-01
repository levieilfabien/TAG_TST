package beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import net.htmlparser.jericho.Source;

/**
 * Classe représentant un écran dans le processus de test.
 * @author levieil fabien
 *
 */
public class EcranBean implements Serializable {

	/**
	 * Id de sérialisation.
	 */
	private static final long serialVersionUID = -650234586755515488L;

	/**
	 * Map des cibles prioritaires pour la page.
	 */
	private HashMap<String, CibleBean> cibles = new LinkedHashMap<String, CibleBean>();
	
	/**
	 * La source de la Page (obtenue avec le parser).
	 */
	private Source source;
	
	/**
	 * Titre de la page.
	 */
	private String titrePage = "";
	
	/**
	 * Identifiant de la page.
	 */
	private String identifiant = "";
	
	
	public String listerCibles() {
		String retour = "";
		
		// On parcours les cibles et on les présente sous forme lisible.
		for (String clef : cibles.keySet()) {
			retour = retour.concat(clef).concat(" : ").concat(cibles.get(clef).lister()).concat(" " + cibles.get(clef).getXpath()).concat("\n");
		}
		
		return retour;
	}

	public HashMap<String, CibleBean> getCibles() {
		return cibles;
	}

	public void setCibles(HashMap<String, CibleBean> cibles) {
		this.cibles = cibles;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public String getTitrePage() {
		return titrePage;
	}

	public void setTitrePage(String titrePage) {
		this.titrePage = titrePage;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}
	
	
}
