package constantes;

public enum ALMTestField {

	TEST_VER_STAMP("TS_TEST_VER_STAMP", "Test", "TS_TEST_VER_STAMP"), // 13?
	TEXT_SYNC("TS_TEXT_SYNC", "Test", "TS_TEXT_SYNC"),
	TIMEOUT("TS_TIMEOUT", "Test", "TS_TIMEOUT"),
	ATTACHEMENT("TS_ATTACHMENT", "Test", "Attachment"),
	AUDIT_ID_START("TS_VC_START_AUDIT_ACTION_ID", "Test", "Audit ID for start point for this version"), // 3265516
	AUDIT_ID_END("TS_VC_END_AUDIT_ACTION_ID", "Test", "Audit ID that is a end point for this version of the test"), // 3265516
	ID_TEST_BASE("TS_BASE_TEST_ID", "Test", "Base Test ID"),
	CHEMIN_ACCES("TS_PATH", "Test", "Chemin accès"),
	COMMENTAIRES("TS_DEV_COMMENTS", "Test", "Commentaires"),
	COMPOSANT("TS_USER_05", "Test", "Composant"), // Ex : IZIVENTE
	CONCEPTEUR("TS_RESPONSIBLE", "Test", "Concepteur"), // Obligatoire
	CREER_LE("TS_CREATION_DATE", "Test", "Créé le"),  // Obligatoire
	DESCRIPTION("TS_DESCRIPTION", "Test", "Description"),
	DOMF("TS_USER_04", "Test", "DomF"),
	EMETTEUR("TS_USER_TEMPLATE_01", "Test", "Emetteur"), // Ex : Equipe T&R
	MODELE("TS_TEMPLATE", "Test", "Modèle"),
	MODIFIE_LE("TS_VTS", "Test", "Modifié le"), //
	NIVEAU_TEST("TS_USER_TEMPLATE_02", "Test", "Niveau de tests"),
	NOM_TEST("TS_NAME", "Test", "Nom du test"), // Le nom du test
	STATUT_TEST("TS_STATUS", "Test", "Statut"),
	STATUT_CHANGEMENT("TS_BPTA_CHANGE_DETECTED", "Test", "Statut de changement"),
	STATUT_EXECUTION("TS_EXEC_STATUS", "Test", "Statut exécution"),
	PARAMETRE_DE_STEP("TS_STEP_PARAM", "Test", "Step Param"), // 0?
	NOMBRES_STEPS("TS_STEPS", "Test", "Steps"), // En fait le nombre de steps !
	SUJET("TS_SUBJECT", "Test", "Sujet"), // 19037
	TEMPS_DEV_ESTIME("TS_ESTIMATE_DEVTIME", "Test", "Temps de développement estimé"),
	DONNEE_EXECUTION_TEST("TS_RUNTIME_DATA", "Test", "Test Runtime Data"),
	ID_TEST("TS_TEST_ID", "Test", "Test ID"), // 76408
	MODE_DE_TEST("TS_SERVICE_TEST_MODE", "Test", "Testing Mode"),
	TYPE_DE_PROTOCOLE("TS_PROTOCOL_TYPE", "Test", "Type de protocole"),
	TYPE_TEST("TS_TYPE", "Test", "Type"), // MANUAL
	TYPE_OFFRE("TS_USER_08", "Test", "Type offre"),
	VERSION("TS_USER_TEMPLATE_03", "Test", "Version"),
	VERSION_CHECK_COMMENTAIRE("TS_VC_CHECKIN_COMMENTS", "Test", "Version Check In Comments"),
	VERSION_CHECK_DATE("TS_VC_CHECKIN_DATE", "Test", "Version Check In Date"),
	VERSION_CHECK_TIME("TS_VC_CHECKIN_TIME", "Test", "Version Check In Time"),
	VERSION_CHECK_BY("TS_VC_CHECKIN_USER_NAME", "Test", "Version Checked In By"),
	VERSION_COMMENTAIRE("TS_VC_COMMENTS", "Test", "Version Comments"),
	VERSION_DATE("TS_VC_DATE", "Test", "Version Date"),
	VERSION_NUMERO("TS_VC_VERSION_NUMBER", "Test", "Version Number"),
	VERSION_POSSESSEUR("TS_VC_USER_NAME", "Test", "Version Owner"),
	VERSION_ETAT("TS_VC_STATUS", "Test", "Version Status"),
	VERSION_TEMPS("TS_VC_TIME", "Test", "Version Time"),
	MODE_DE_TRAVAIL("TS_WORKING_MODE", "Test", "Working Mode");
	
	private String categorie = "";
	private String description = "";
	private String code = "";
	
	/**
	 * Constructeur de champ pour un defect ALM.
	 * @param categorie la cagétorie (defect).
	 * @param description la description.
	 */
	private ALMTestField(String code, String categorie, String description) {
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
