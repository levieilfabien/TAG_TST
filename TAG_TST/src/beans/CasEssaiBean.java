package beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import moteurs.GenericDriver;
import outils.SeleniumOutils;

/**
 * Classe générique de cas d'essai. Représente aussi bien un scénario qu'un cas de test unitaire.
 * Cette classe est étendue dans les autres cas d'essai spécifique à chaque projet.
 * @author levieil_f
 *
 */
public class CasEssaiBean implements Serializable {

	/**
	 * Environement de test pour le cas d'essai.
	 */
	private EnvironementDeTest environement;
	
	/**
	 * Date de création du cas de test.
	 */
	private Date dateCreation = new Date();
	
	/**
	 * Id de serialisation.
	 */
	private static final long serialVersionUID = 2917647680800145572L;
	
	/**
	 * Nom du cas d'essai.
	 */
	private String nomCasEssai = "monCasEssai";
	
	/**
	 * Doit on s'attendre à ce que le cas d'essai soit finalisé?
	 */
	private Boolean finalise = true;
	
	/**
	 * Registre de l'execution du cas de test.
	 */
	private String registreExecution = "";
	
	/**
	 * Etat final du test.
	 */
	private Boolean etatFinal = false;
	
	/**
	 * Le cas de test existe t'il dans ALM.
	 */
	private Boolean alm = false;
	
	/**
	 * Indique l'id de la page confluence à mettre à jour.
	 * Si la valeur est à null, c'est qu'il n'y pas de mise à jour confluence.
	 */
	private String idConfluence = null;
	
	/**
	 * Repertoire de téléchargement.
	 */
	private String repertoireTelechargement = null;
	
	// DONNEES ALM
	/**
	 * Identifiant du cas de test dans TESTLAB
	 */
	private Integer idUniqueTestLab = -1;
	
	/**
	 * Identifiant du cas de test dans TESTPLAN
	 */
	private Integer idUniqueTestPlan = -1;
	
	/**
	 * Chemin dans le test LAB vers le cas de test sans inclure le root.
	 * EX : POC Selenium\\IZIVENTE
	 */
	private String cheminTestLab;
	
	/**
	 * Nom exact du cas de test dans test Lab
	 */
	private String nomTestLab;
	
	/**
	 * Nom exact du cas de test dans test plan (onglet execution_grid du testLab)
	 */
	private String nomTestPlan = null;
	
	/**
	 * Sous cas d'essai ayant leurs propres objectifs.
	 */
	private List<CasEssaiBean> tests = new LinkedList<CasEssaiBean>();
	
	/**
	 * Descriptif du cas de test.
	 */
	private String descriptif = "";
	
	/**
	 * Commentaire sur l'execution à indiquer sur le rapport.
	 */
	private String commentaire = "";
	
	/**
	 * Liste des objectifs à atteindre pour la réalisation complète du cas d'essai.
	 * Les objectifs peuvent être utilisés comme step dans des cas ALM.
	 */
	private HashMap<String, ObjectifBean> objectifs = new LinkedHashMap<String, ObjectifBean>();

	/**
	 * Liste des RGM atteintes lors de l'execution du cas de test.
	 */
	private HashMap<String, RGMBean> reglesDeGestions = new LinkedHashMap<String, RGMBean>();
	
	/**
	 * Liste des écrans atteints lors de l'execution du cas de test.
	 * Cette liste est mise à jour à chaque accès à un ecran. Il ne s'agit pas de validation de RGM associé à des écrans.
	 */
	private HashMap<String, ObjectifBean> parcours = new LinkedHashMap<String, ObjectifBean>();

	/**
	 * Log d'execution du test.
	 */
	public String logs = "";
	
	/**
	 * Permet d'obtenir le registre d'execution du test.
	 * @return le registre d'execution.
	 */
	public String getLogs() {
		return logs;
	}

	/**
	 * Permet d'obtenir le timestamp unique du cas de test.
	 * @return le timestamp unique du cas de test.
	 */
	public String getTime() {
		return String.valueOf(new SimpleDateFormat("yyyy_MM_dd").format(getDateCreation()) + "_" + getDateCreation().getTime());
	}

	/**
	 * Setter pour le registre d'execution.
	 * @param logs le nouveau registre d'execution.
	 */
	public void setLogs(String logs) {
		this.logs = logs;
	}
	
	/**
	 * Ajoute un objectif au cas d'essai
	 * @param objectif object à ajouter.
	 */
	public void ajouterObjectif(ObjectifBean objectif) {
		this.objectifs.put(objectif.getCode(), objectif);
	}
	
	/**
	 * Ajoute un objectif au cas d'essai
	 * @param objectif object à ajouter.
	 */
	public void ajouterStep(String descriptif, String code, String attendu) {
		ObjectifBean step = new ObjectifBean(descriptif, code, code + getTime(), attendu, "");
		// Par défaut un step est "not run", il est donc avec une valorisation de son état à "Null".
		step.setEtat(null);
		this.objectifs.put(code, step);
	}
	
	/**
	 * Constructeur par défaut.
	 */
	public CasEssaiBean() {
		super();
	}
	
	/**
	 * Construit un sous cas d'essai (donc un cas de test au niveau scénario) pour le cas d'essai passé en paramètre.
	 * @param reference le scénario ou cas d'essai de référence qui fournis les informations d'initialisation.
	 * @param prefixe le préfixe associé au nouveau cas d'essai (ou cas de test)
	 * @param idTestPlan l'identifiant associé au nouveau cas d'essai (l'id dans le test plan associé au cas de test)
	 */
	public CasEssaiBean(CasEssaiBean reference, String prefixe, Integer idTestPlan) {
		super();
		setAlm(reference.getAlm());
		setNomCasEssai(prefixe + reference.getTime());
		setIdUniqueTestLab(reference.getIdUniqueTestLab());
		setIdUniqueTestPlan(idTestPlan);
		setRepertoireTelechargement(reference.getRepertoireTelechargement());
		// Par défaut on ne connais pas l'état final de cette étape, elle sera déduite de la sommes de ses steps.
		setEtatFinal(null);
		reference.getTests().add(this);
	}
	
	/**
	 * Ajoute un objectif au cas d'essai
	 * @param objectif object à ajouter.
	 */
	public void ajouterEcran(GenericDriver driver,String clef, ObjectifBean objectif) {
		if (driver != null) {
			objectif.setClefUnique(clef);
			SeleniumOutils outil = new SeleniumOutils(driver);
			outil.setRepertoireRacine(getRepertoireTelechargement());
			outil.captureEcran(clef);
		}
		this.objectifs.put(clef, objectif);
	}
	
	/**
	 * Retire un objectif au cas d'essai
	 * @param objectif l'objectif à retirer.
	 */
	public void retirerObjectif(String objectif) {
		this.objectifs.remove(objectif);
	}

	/**
	 * Permet de changer l'état d'un objectif (si celui-ci existe).
	 * @param clef la clef de l'objectif.
	 * @param etat l'état souhaité de l'objectif.
	 */
	public void validerObjectif(String clef, Boolean etat) {
		ObjectifBean bean = getObjectifs().get(clef);
		if (bean != null) {
			getObjectifs().get(clef).setEtat(etat);
			if (bean.isStep() && etat) {
				getObjectifs().get(clef).setObtenu("Conforme à l'attendu");
			}
		}
		
		// Si toutes les sous étapes du cas de test sont valides (typiquement les steps) alors le cas de test est lui même valide.
		boolean tousValide = true;
		for (ObjectifBean objectif : getObjectifs().values()) {
			//System.out.println(objectif.getClefUnique() + " " + objectif.isEtat());
			tousValide = tousValide && (objectif.isEtat() == true);
		}
		if(tousValide) {
			setEtatFinal(tousValide);
		}
	}
	
	/**
	 * Permet de changer l'état d'un objectif (si celui-ci existe).
	 * @param clef la clef de l'objectif.
	 * @param etat l'état souhaité de l'objectif.
	 */
	public void validerObjectif(GenericDriver driver, String clef, Boolean etat) {
		validerObjectif(clef, etat);
		if (driver != null) {
			SeleniumOutils outil = new SeleniumOutils(driver);
			if (getRepertoireTelechargement() != null) {
				outil.setRepertoireRacine(getRepertoireTelechargement());
				outil.captureEcran(clef);
			} else {
				outil.captureEcran(clef, getNomCasEssai());
			}
			
		}
	}
	
	/**
	 * Valide un objectif dont la clef est connue.
	 * @param outil l'outil selenium.
	 * @param clef la clef concernée.
	 */
	public void valider(SeleniumOutils outil, String clef) {
		validerObjectif(outil.getDriver(), clef, true);
	}
	
	/**
	 * Invalide un objectif dont la clef est connue.
	 * @param outil l'outil selenium.
	 * @param clef la clef concernée.
	 */
	public void invalider(SeleniumOutils outil, String clef) {
		validerObjectif(outil.getDriver(), clef, false);
	}
	
	/**
	 * Permet de changer l'état d'un objectif dont le clef contient la date de création (si celui-ci existe).
	 * @param clef la clef de l'objectif sans la date de création.
	 * @param etat l'état souhaité de l'objectif.
	 */
	public void validerObjectifDate(GenericDriver driver, String clef, Boolean etat) {
		validerObjectif(clef + getTime(), etat);
		if (driver != null) {
			SeleniumOutils outil = new SeleniumOutils(driver);
			if (getRepertoireTelechargement() != null) {
				outil.setRepertoireRacine(getRepertoireTelechargement());
				outil.captureEcran(clef + getTime());
			} else {
				outil.captureEcran(clef + getTime(), getNomCasEssai());
			}
		}
	}
	
	/**
	 * Ajoute un objectif au cas d'essai
	 * @param objectif object à ajouter.
	 */
	public void ajouterRGM(RGMBean objectif) {
		this.reglesDeGestions.put(objectif.getCode(), objectif);
	}
	
	/**
	 * Ajoute un objectif au cas d'essai
	 * @param objectif object à ajouter.
	 */
	public void ajouterRGM(ObjectifBean objectif) {
		if (objectif != null) {
			this.reglesDeGestions.put(objectif.getCode(), new RGMBean(objectif));
		}
	}
	
	/**
	 * Retire un objectif au cas d'essai
	 * @param objectif l'objectif à retirer.
	 */
	public void retirerRGM(String objectif) {
		this.reglesDeGestions.remove(objectif);
	}

	/**
	 * Permet de changer l'état d'un objectif (si celui-ci existe).
	 * @param clef la clef de l'objectif.
	 * @param etat l'état souhaité de l'objectif.
	 */
	public void validerRGM(String clef, Boolean etat) {
		if (getReglesDeGestions().get(clef) != null) {
			getReglesDeGestions().get(clef).setEtat(etat);
		}
	}
	
	/**
	 * Permet d'extraire le registre d'execution du driver et de l'inserer dans le cas d'essai.
	 * Fusinonne les objectifs d'écrans par la même ocasion.
	 * @param driver le driver. 
	 */
	public void setRegistreExecution(GenericDriver driver) {
		if (driver != null && driver.getLog() != null) {
			registreExecution = registreExecution + driver.getLog().toString();
			if (driver.getObjectifsDriver() != null) {
				getParcours().putAll(driver.getObjectifsDriver());
			}
		} else {
			registreExecution = "";
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer retour = new StringBuffer("\nTest " + getNomCasEssai() + ", objectifs à atteindre : \n");
		for (String clef : getObjectifs().keySet()) {
			ObjectifBean objectif = getObjectifs().get(clef);
			retour.append(objectif.getDescriptif() + ", Etat : [" + (objectif.isEtat() == true ? "OK" : "NOK") +"]\n");
		}
		if (getReglesDeGestions().size() > 0) {
			retour.append("Règles de gestion à valider : \n");
			for (String clef : getReglesDeGestions().keySet()) {
				RGMBean objectif = getReglesDeGestions().get(clef);
				retour.append(objectif.getDescriptif() + ", Etat : [" + (objectif.isEtat() == true ? "OK" : "NOK") +"]\n");
			}
		}
		retour.append("Etat final attendu : " + (getFinalise() ? "Finalisé\n" : "Non Finalisé\n"));
		return retour.toString();
	}
	
	////////////////////////////////////////////////////////////////
	// GETTERS & SETTERS 										////
	////////////////////////////////////////////////////////////////
	
	public String getNomCasEssai() {
		return nomCasEssai;
	}

	public void setNomCasEssai(String nomCasEssai) {
		this.nomCasEssai = nomCasEssai;
	}
	
	public Boolean getFinalise() {
		return finalise;
	}

	public void setFinalise(Boolean finalise) {
		this.finalise = finalise;
	}

	public HashMap<String, ObjectifBean> getObjectifs() {
		return objectifs;
	}

	public void setObjectifs(HashMap<String, ObjectifBean> objectifs) {
		this.objectifs = objectifs;
	}
	
	public Boolean getEtatFinal() {
		return etatFinal;
	}

	public void setEtatFinal(Boolean etatFinal) {
		//System.out.println("Modification etat final " + this.getNomCasEssai() + " : " + etatFinal);
		this.etatFinal = etatFinal;
	}

	public HashMap<String, ObjectifBean> getParcours() {
		return parcours;
	}

	public void setParcours(HashMap<String, ObjectifBean> ecrans) {
		this.parcours = ecrans;
	}

	public HashMap<String, RGMBean> getReglesDeGestions() {
		return reglesDeGestions;
	}

	public void setReglesDeGestions(HashMap<String, RGMBean> reglesDeGestions) {
		this.reglesDeGestions = reglesDeGestions;
	}

	public String getRegistreExecution() {
		return registreExecution;
	}

	public void setRegistreExecution(String registreExecution) {
		this.registreExecution = registreExecution;
	}
	
	public String getRegistreExecutionTrim() {
		if (registreExecution.length() > 30000) {
			return registreExecution.substring(0, 30000);
		}
		return registreExecution;
	}

	public String getRegistreExecutionTail() {
		//TODO trouver une autre solution.
		String retour = "";
		if (registreExecution.length() > 30000) {
			retour = registreExecution.substring(30001, registreExecution.length());
		}
		if (retour.length() > 30000) {
			retour = "Logs trop importants pour être présentés ici";
		}
		return retour;
	}

	public EnvironementDeTest getEnvironement() {
		return environement;
	}

	public void setEnvironement(EnvironementDeTest environement) {
		this.environement = environement;
	}

	public String getDescriptif() {
		return descriptif;
	}

	public void setDescriptif(String descriptif) {
		this.descriptif = descriptif;
	}
	
	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public Boolean getAlm() {
		return alm;
	}

	public void setAlm(Boolean alm) {
		this.alm = alm;
	}

	public Integer getIdUniqueTestLab() {
		return idUniqueTestLab;
	}

	public void setIdUniqueTestLab(Integer idUniqueTestLab) {
		this.idUniqueTestLab = idUniqueTestLab;
	}

	public String getCheminTestLab() {
		return cheminTestLab;
	}

	public void setCheminTestLab(String cheminTestLab) {
		this.cheminTestLab = cheminTestLab;
	}

	public String getNomTestLab() {
		return nomTestLab;
	}

	public void setNomTestLab(String nomTestLab) {
		this.nomTestLab = nomTestLab;
	}

	public String getNomTestPlan() {
		return nomTestPlan;
	}

	public void setNomTestPlan(String nomTestPlan) {
		this.nomTestPlan = nomTestPlan;
	}

	public String getRepertoireTelechargement() {
		return repertoireTelechargement;
	}

	public void setRepertoireTelechargement(String repertoireTelechargement) {
		this.repertoireTelechargement = repertoireTelechargement;
	}

	public List<CasEssaiBean> getTests() {
		return tests;
	}

	public void setTests(List<CasEssaiBean> tests) {
		this.tests = tests;
	}

	public Integer getIdUniqueTestPlan() {
		return idUniqueTestPlan;
	}

	public void setIdUniqueTestPlan(Integer idUniqueTestPlan) {
		this.idUniqueTestPlan = idUniqueTestPlan;
	}

	public String getIdConfluence() {
		return idConfluence;
	}

	public void setIdConfluence(String idConfluence) {
		this.idConfluence = idConfluence;
	}
	
	////////////////////////////////////////////////////////////////
	
}
