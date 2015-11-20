package constantes;

public enum ALMTestSetField {

	CYCLE_CONFIG("CY_CYCLE_CONFIG" , "TestSet", "CY_CYCLE_CONFIG"),
	CYCLE_STAMP("CY_CYCLE_VER_STAMP" , "TestSet", "CY_CYCLE_VER_STAMP"),
	ATTACHEMENT("CY_ATTACHMENT" , "TestSet", "Attachment"),
	BASELINE("CY_PINNED_BASELINE" , "TestSet", "Baseline"),
	CONFIGURATION("CY_OS_CONFIG" , "TestSet", "Configuration"),
	CREER_LE("CY_OPEN_DATE" , "TestSet", "Créé le"),
	CYCLE_TEST("CY_ASSIGN_RCYC" , "TestSet", "Cycle"),
	DESCRIPTION("CY_COMMENT" , "TestSet", "Description"),
	EXECUTION_FLOW("CY_DESCRIPTION" , "TestSet", "Execution Flow"),
	FERMER_LE("CY_CLOSE_DATE" , "TestSet", "Fermé le"),
	MODIFIER_LE("CY_VTS" , "TestSet", "Modifié le"),
	PARAMETRES_EN_CAS_ECHEC("CY_EXEC_EVENT_HANDLE" , "TestSet", "'On Failure' Settings"),
	PARAMETRES_RAPPORT("CY_REPORT_SETTINGS" , "TestSet", "Report Settings"),
	ID_REQUEST("CY_REQUEST_ID" , "TestSet", "PPM Request Id"),
	ETAT("CY_STATUS" , "TestSet", "Statut"),
	CYCLE("CY_CYCLE" , "TestSet", "TestSet"),
	ID_REPERTOIRE_TEST("CY_FOLDER_ID" , "TestSet", "Test Set Folder"),
	ID_TEST_SET("CY_CYCLE_ID" , "TestSet", "Test Set ID"),
	PARAMETRES_NOTIFICATION_MAIL("CY_MAIL_SETTINGS" , "TestSet", "TestSet Notifications Settings"),
	TYPE_TEST_SET("CY_SUBTYPE_ID" , "TestSet", "Type");
	
	private String categorie = "";
	private String description = "";
	private String code = "";
	
	/**
	 * Constructeur de champ pour un defect ALM.
	 * @param categorie la cagétorie (defect).
	 * @param description la description.
	 */
	private ALMTestSetField(String code, String categorie, String description) {
		this.categorie = categorie;
		this.description = description;
		this.setCode(code);
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String nom() {
		return name();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
