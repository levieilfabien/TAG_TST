package beans;

import java.io.Serializable;

/**
 * Représente un objectif de cas de test, ainsi que son état.
 * Cet objet est utilisé das le cadre de l'objet cas d'essai.
 * @author levieil_f
 *
 */
public class ObjectifBean implements Serializable {

	/**
	 * Id de sérialisation.
	 */
	private static final long serialVersionUID = 8742626312119849806L;
	
	/**
	 * Descriptif de l'objectif.
	 */
	private String descriptif = "";
	
	/**
	 * Code de l'objectif.
	 */
	private String code = "";
	
	/**
	 * Clef unique de l'objectif.
	 */
	private String clefUnique = "";
	
	/**
	 * Etat de l'objectif, est il atteint?
	 */
	private Boolean etat = false;
	
	/**
	 * Capture d'écran associée à l'objectif.
	 */
	private String capture = "";
	
	/**
	 * Descriptif de l'attendu
	 */
	private String attendu = "";
	
	/**
	 * Descriptif de l'obtenu.
	 */
	private String obtenu = "";
	
	/**
	 * Indique si l'objectif est un step.
	 */
	private boolean step = false;

	/**
	 * Construit un objectif à partir de données de base.
	 * @param descriptif descriptif de l'objectif.
	 * @param code code de l'objectif.
	 * @param etat etat initial de l'objectif.
	 */
	public ObjectifBean(String descriptif, String code, Boolean etat) {
		super();
		this.descriptif = descriptif;
		this.code = code;
		this.etat = etat;
		this.clefUnique = code;
	}
	
	/**
	 * Construit un objectif à partir de données de base.
	 * @param descriptif descriptif de l'objectif.
	 * @param code code de l'objectif.
	 * @param clef la clef unique de l'objectif.
	 * @param etat etat initial de l'objectif.
	 */
	public ObjectifBean(String descriptif, String code, String clef, Boolean etat) {
		super();
		this.descriptif = descriptif;
		this.code = code;
		this.etat = etat;
		this.clefUnique = clef;
	}
	
	/**
	 * Construit un objectif à partir de données de base.
	 * @param descriptif descriptif de l'objectif.
	 * @param code code de l'objectif.
	 * @param clef la clef unique de l'objectif.
	 */
	public ObjectifBean(String descriptif, String code, String clef) {
		super();
		this.descriptif = descriptif;
		this.code = code;
		this.etat = false;
		this.clefUnique = clef;
	}
	
	/**
	 * Construit un objectif à partir de données de base.
	 * @param descriptif descriptif de l'objectif.
	 * @param code code de l'objectif.
	 */
	public ObjectifBean(String descriptif, String code) {
		super();
		this.descriptif = descriptif;
		this.code = code;
		this.etat = false;
		this.clefUnique = code;
	}
	
	/**
	 * Construit un objectif de catégorie step (pour une synchronisation ALM).
	 * @param descriptif descriptif de l'objectif.
	 * @param code code de l'objectif.
	 * @param clef la clef unique de l'objectif.
	 * @param attendu l'attendu de l'étape
	 * @param l'obtenu par défaut (à modifier ultérieurement).
	 */
	public ObjectifBean(String descriptif, String code,  String clef, String attendu, String obtenu) {
		super();
		this.descriptif = descriptif;
		this.code = code;
		this.etat = false;
		this.clefUnique = clef;
		this.step = true;
		this.attendu = attendu;
		this.obtenu = obtenu;
	}
	
	////////////////////////////////////////////////////////////////
	// GETTERS & SETTERS 										////
	////////////////////////////////////////////////////////////////
	
	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getEtat() {
		return etat;
	}

	public void setEtat(Boolean etat) {
		this.etat = etat;
	}

	public String getClefUnique() {
		return clefUnique;
	}

	public void setClefUnique(String clefUnique) {
		this.clefUnique = clefUnique;
	}

	public String getCapture() {
		return capture;
	}

	public void setCapture(String capture) {
		this.capture = capture;
	}
	
	public String getAttendu() {
		return attendu;
	}

	public void setAttendu(String attendu) {
		this.attendu = attendu;
	}

	public String getObtenu() {
		return obtenu;
	}

	public void setObtenu(String obtenu) {
		this.obtenu = obtenu;
	}

	public boolean isStep() {
		return step;
	}

	public void setStep(boolean step) {
		this.step = step;
	}

	/**
	 * Permet de creer un lien hypertexte pour un rapport excel à partir du chemin présumé de la capture d'écran.
	 * Cette fonction ne vérifie pas si une capture existe , elle creer juste le lien vers la capture.
	 * @return le chaine formule pour un lien vers la capture.
	 */
	public String getCaptureHyperLien() {
		if (this.capture != null && !"".equals(this.capture)) {
			return "=LIEN_HYPERTEXTE(\"" + this.capture + "\";\"Capture\")";
		} else {
			return "N/A";
		}
	}
	////////////////////////////////////////////////////////////////
}
