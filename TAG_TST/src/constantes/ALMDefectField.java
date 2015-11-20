package constantes;

public enum ALMDefectField {

	AFFECTE_A("BG_RESPONSIBLE", "Defect", "Affecté à"),  
	APPLICATION("BG_PROJECT", "Defect", "Application"),  
	BASELINE_ME("BG_USER_37", "Defect", "Baseline ME"),  
	ATTACHEMENT("BG_ATTACHMENT", "Defect", "Attachment"),  
	BASELINE_PJ("BG_USER_39", "Defect", "Baseline PJ"),  
	CODE_BANQUE("BG_USER_03", "Defect", "Code Banque"),  
	COMMENTAIRES("BG_DEV_COMMENTS", "Defect", "Commentaires"),  
	COMPTEUR("BG_USER_TEMPLATE_12", "Defect", "Compteur"),  
	CORRIGE_SUR("BG_USER_16", "Defect", "Corrigé sur"),  
	CYCLE_CORRECTION("BG_TARGET_RCYC", "Defect", "Cycle de correction"),  
	CYCLE_ID("BG_CYCLE_ID", "Defect", "Cycle ID"),  
	DATE_CORRECTION_REELLE("BG_USER_TEMPLATE_08", "Defect", "Date de correction réelle"),  
	DATE_FERMETURE("BG_CLOSING_DATE", "Defect", "Date de fermeture"),  
	DATE_LIVRAISON_RECETTE("BG_USER_14", "Defect", "Date de livraison en recette"),  
	DATE_LIVRAISON_INTEGRATION("BG_USER_05", "Defect", "Date de livraison en intégration"),  
	DEBUT_PROCESS_CORRECTION("BG_USER_TEMPLATE_16", "Defect", "Début process correction"),  
	DELAI_CORRECTION_ESTIME("BG_ESTIMATED_FIX_TIME", "Defect", "Délai de correction estimé"),  
	DELAI_CORRECTION_REEL("BG_ACTUAL_FIX_TIME", "Defect", "Délai de correction réel"),  
	DESCRIPTION("BG_DESCRIPTION", "Defect", "Description"),  
	DESCRIPTION_RESOLUTION("BG_USER_TEMPLATE_14", "Defect", "Description résolution"),  
	DETECTION_RELEASE("BG_DETECTED_IN_REL", "Defect", "Détecté dans la Release"),  
	DETECTION_VERSION("BG_DETECTION_VERSION", "Defect", "Détecté dans la version"),  
	CYCLE_DETECTION("BG_DETECTED_IN_RCYC", "Defect", "Détecté dans le Cycle"),  
	DATE_DETECTION("BG_DETECTION_DATE", "Defect", "Détecté le"),  
	DETECTE_PAR("BG_DETECTED_BY", "Defect", "Détecté par"),  
	DOMF("BG_USER_12", "Defect", "DomF"),  
	EMETTEUR("BG_USER_TEMPLATE_04", "Defect", "Emetteur"),  
	ENTITE_CORRECTRICE("BG_USER_TEMPLATE_09", "Defect", "Entité correctrice"),  
	ENTITE_RESPONSABLE("BG_USER_04", "Defect", "Entité Responsable"),  
	ENVIRONEMENT("BG_USER_TEMPLATE_03", "Defect", "Environnement"),  
	REFERENCE_ETENDUE("BG_EXTENDED_REFERENCE", "Defect", "Extended Reference"),  
	A_CHANGE("BG_HAS_CHANGE", "Defect", "Has Change"),  
	ID_ANOMALIE("BG_BUG_ID", "Defect", "ID Anomalie"),  
	EST_MODIFIE("BG_USER_32", "Defect", "IS_MODIFIED"),  
	DERNIERE_SYNCHRONISATION("BG_USER_33", "Defect", "LAST_SYNC"),  
	LIBELLE("BG_SUMMARY", "Defect", "Libellé"),  
	MODELE_EDITIQUE("BG_USER_02", "Defect", "Modèle éditique"),  
	MOTIF("BG_USER_TEMPLATE_15", "Defect", "Motif"),  
	MODIFIE_LE("BG_VTS", "Defect", "Modifié le"),  
	NOMBRE_LIVRAISON("BG_USER_TEMPLATE_11", "Defect", "Nb de livraisons"),  
	NIVEAU_TEST("BG_USER_TEMPLATE_02", "Defect", "Niveau de tests"),  
	NIVEAU("BG_USER_28", "Defect", "Niveau"),  
	NIVEAU_TEST_ATTENDU("BG_USER_TEMPLATE_10", "Defect", "Niveau de tests attendu"),  
	PPM_RESQUEST_ID("BG_REQUEST_ID", "Defect", "PPM Request Id"),  
	PPM_REQUEST_NOTE("BG_REQUEST_NOTE", "Defect", "PPM Request Note"),  
	PPM_REQUEST_TYPE("BG_REQUEST_TYPE", "Defect", "PPM Request Type"),  
	PPM_SERVER_URL("BG_REQUEST_SERVER", "Defect", "PPM Server URL"),  
	PRIORITE("BG_PRIORITY", "Defect", "Priorité"),  
	PROJET_EVOLUTION("BG_USER_TEMPLATE_01", "Defect", "Projet / Evolution"),  
	REF_ANO_EXTERNE("BG_USER_06", "Defect", "Référence ano. externe"),  
	REF_ANO_EXTERNE_UP("BG_USER_30", "Defect", "Référence externe UP"),  
	RELEASE_COMPOSANT("BG_USER_15", "Defect", "Release composant"),  
	RELEASE_CORRECTION("BG_TARGET_REL", "Defect", "Release de correction"),  
	REPRODUCTIBLE("BG_USER_TEMPLATE_06", "Defect", "Reproductible"),  
	RESOLU("BG_USER_TEMPLATE_13", "Defect", "Résolu"),  
	REFERENCE_RUN("BG_RUN_REFERENCE", "Defect", "Run Reference"),  
	SEVERITE("BG_SEVERITY", "Defect", "Sévérité"),  
	STATUT("BG_STATUS", "Defect", "Statut"),  
	REFERENCE_STEP("BG_STEP_REFERENCE", "Defect", "Step Reference"),  
	SUJET("BG_SUBJECT", "Defect", "Sujet"),  
	SYNCHRONISATION_FLAG("BG_USER_38", "Defect", "SYNC_FLAG"),  
	SYNCHRONISATION_STATUS("BG_USER_31", "Defect", "SYNC_STATUS"),  
	SYNCHRONISATION_TECH("BG_USER_34", "Defect", "SYNC_TECH"),  
	REFERENCE_TEST("BG_TEST_REFERENCE", "Defect", "Test Reference"),  
	MAIL("BG_TO_MAIL", "Defect", "To Mail"),  
	REFERENCE_TESTSET("BG_CYCLE_REFERENCE", "Defect", "TestSet Reference"),  
	TYPE("BG_USER_TEMPLATE_05", "Defect", "Type"),  
	VERSION_CORRECTION("BG_CLOSING_VERSION", "Defect", "Version de correction"),  
	VERSION_CORRECTION_PLANIFIEE("BG_PLANNED_CLOSING_VER", "Defect", "Version de correction planifiée"),  
	VERSION_CORRECTION_SOUHAITEE("BG_USER_TEMPLATE_07", "Defect", "Version de correction souhaitée"),  
	VERSION_DETECTION("BG_USER_01", "Defect", "Version de détection"),  
	VERSION_SOUHAITEE("BG_USER_07", "Defect", "Version souhaitée"),  
	VERSION_STAMP("BG_BUG_VER_STAMP", "Defect", "Version Stamp"),  
	VTS_CIBLE("BG_USER_36", "Defect", "VTS_CIBLE"),  
	VTS_SOURCE("BG_USER_35", "Defect", "VTS_SOURCE"),  
	XX_REPRODUCTIBLE("BG_REPRODUCIBLE", "Defect", "XX_Reproducible");
	             
	private String categorie = "";
	private String description = "";
	private String code = "";
	
	/**
	 * Constructeur de champ pour un defect ALM.
	 * @param categorie la cagétorie (defect).
	 * @param description la description.
	 */
	private ALMDefectField(String code, String categorie, String description) {
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
