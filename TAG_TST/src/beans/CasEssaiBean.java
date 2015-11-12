package beans;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import moteurs.GenericDriver;

import org.openqa.selenium.WebDriver;

import outils.SeleniumOutils;

/**
 * Classe générique de cas d'essai.
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
	 * Descriptif du cas de test.
	 */
	private String descriptif = "";
	
	/**
	 * Commentaire sur l'execution à indiquer sur le rapport.
	 */
	private String commentaire = "";
	
	/**
	 * Liste des objectifs à atteindre pour la réalisation complète du cas d'essai.
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

	public String getTime() {
		return String.valueOf(getDateCreation().getTime());
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
	public void ajouterEcran(GenericDriver driver,String clef, ObjectifBean objectif) {
		if (driver != null) {
			objectif.setClefUnique(clef);
			new SeleniumOutils(driver).captureEcran(clef);
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
		if (getObjectifs().get(clef) != null) {
			getObjectifs().get(clef).setEtat(etat);
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
			new SeleniumOutils(driver).captureEcran(clef, getNomCasEssai());
		}
	}
	
	/**
	 * Permet de changer l'état d'un objectif dont le clef contient la date de création (si celui-ci existe).
	 * @param clef la clef de l'objectif sans la date de création.
	 * @param etat l'état souhaité de l'objectif.
	 */
	public void validerObjectifDate(GenericDriver driver, String clef, Boolean etat) {
		validerObjectif(clef + getTime(), etat);
		if (driver != null) {
			new SeleniumOutils(driver).captureEcran(clef + getTime(), getNomCasEssai());
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
			retour.append(objectif.getDescriptif() + ", Etat : [" + (objectif.getEtat() ? "OK" : "NOK") +"]\n");
		}
		if (getReglesDeGestions().size() > 0) {
			retour.append("Règles de gestion à valider : \n");
			for (String clef : getReglesDeGestions().keySet()) {
				RGMBean objectif = getReglesDeGestions().get(clef);
				retour.append(objectif.getDescriptif() + ", Etat : [" + (objectif.getEtat() ? "OK" : "NOK") +"]\n");
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
		if (registreExecution.length() > 30000) {
			return registreExecution.substring(30001, registreExecution.length());
		}
		return "";
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
	
	////////////////////////////////////////////////////////////////
}
