package constantes;

public enum ALMTestStepField {
	
	ATTACHEMENT("ST_ATTACHMENT" , "TestStep", "Attachment"),
	ATTENDU("ST_EXPECTED" , "TestStep", "Attendu"), // L'attendu
	COMPOSANT_DONNEE("ST_COMPONENT_DATA" , "TestStep", "Component Step Data"),
	CONDITION("ST_BPTA_CONDITION" , "TestStep", "Condition"),
	DESCRIPTION("ST_DESCRIPTION" , "TestStep", "Description"), // description de l'étape
	ID_DESIGNSTEP("ST_DESSTEP_ID" , "TestStep", "DesignStep ID"), // ????
	EXECUTE_A("ST_EXECUTION_TIME" , "TestStep", "Exécuté à"), // Heure execution
	EXECUTE_LE("ST_EXECUTION_DATE" , "TestStep", "Exécuté le"), // Date execution
	REFERENCE_ETENDUE("ST_EXTENDED_REFERENCE" , "TestStep", "Extended Reference"),
	NIVEAU("ST_LEVEL" , "TestStep", "Level"),
	NIVEAU_IDENTIFIANT_OBJET("ST_OBJ_ID" , "TestStep", "Level"),
	NUMERO_LIGNE("ST_LINE_NO" , "TestStep", "Line_no"),
	NOM_STEP("ST_STEP_NAME" , "TestStep", "Nom étape"), // 
	CHEMIN("ST_PATH" , "TestStep", "Path"),
	OBTENU("ST_ACTUAL" , "TestStep", "Réel"), // L'obtenu
	ID_OBJET_RELIE("ST_REL_OBJ_ID" , "TestStep", "Related Object Id"),
	ID_RUN("ST_RUN_ID" , "TestStep", "Run ID"),
	ETAT_STEP("ST_STATUS" , "TestStep", "Statut"), // Passed
	ID_STEP("ST_ID" , "TestStep", "Step ID"), // L'id unique
	ORDRE_STEP("ST_STEP_ORDER" , "TestStep", "Step Order"), // L'ordre du step
	ID_PARENT_STEP("ST_PARENT_ID" , "TestStep", "Step Parent ID"),
	SOURCE_TEST("ST_TEST_ID" , "TestStep", "Test source"), // L'id unique du test en cours
	NOM_FLUX("ST_USER_04" , "TestStep", "To Delete - Nom du Flux");

	private String categorie = "";
	private String description = "";
	private String code = "";
	
	/**
	 * Constructeur de champ pour un defect ALM.
	 * @param categorie la cagétorie (defect).
	 * @param description la description.
	 */
	private ALMTestStepField(String code, String categorie, String description) {
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
