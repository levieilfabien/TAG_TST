package extensions.impl;

import atu.alm.wrapper.enums.DefectPriority;
import atu.alm.wrapper.enums.DefectSeverity;
import atu.alm.wrapper.enums.DefectStatus;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import constantes.ALMDefectField;
import extensions.interfaces.IALMDefect;

/**
 * Classe d'extension de la classe Bug.
 * Cette classe réprésente un defect dans ALM.
 * @author levieilfa
 *
 */
public class ALMBug implements IALMDefect {
	
	/**
	 * L'objet permettant la manipulation de nouveaux defect.
	 */
	private Dispatch bugFactory;
	
	/**
	 * L'objet defect en cours de manipulation.
	 */
	private Dispatch bug;

	/**
	 * Le constructeur permettant de créer un nouveau defect à partir de la factory.
	 * @param bugFactory
	 */
	public ALMBug(Dispatch bugFactory) {
		this.bugFactory = bugFactory;
		this.bug = init();
	}

	/**
	 * Permet de manipuler les pièces jointes du defect.
	 */
	public ALMAttachementFactory getAttachments() {
		return new ALMAttachementFactory(this.bug);
	}

	/**
	 * Constructeur privé pour la création d'un nouveau defect.
	 * @return un nouveau defect "coquille vide".
	 */
	private Dispatch init() {
		// On créer une coquille vide
		Variant paramVal = new Variant();
		paramVal.putNull();

		// Dispatch bug = Dispatch.call(this.bugFactory, "AddItem", new Object[]
		// { "" }).toDispatch();

		// On recupère un formulaire de bug
		Dispatch bug = Dispatch.call(this.bugFactory, "AddItem", paramVal).toDispatch();
		return bug;
	}
	
	/**
	 * Permet d'affecter un cycle de détection au defect.
	 * @param paramString le nom exact du cycle de detection.
	 */
	@Override
	public void setCycle(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.CYCLE_DETECTION.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.CYCLE_DETECTION.getCode(), new Variant(paramString) }, new int[1]);
	}

	@Override
	public void setRelease(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.DETECTION_RELEASE.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.CYCLE_DETECTION.getCode(), new Variant(paramString) }, new int[1]);

	}

	@Override
	public void setEnvironement(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.ENVIRONEMENT.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.ENVIRONEMENT.getCode(), new Variant(paramString) }, new int[1]);
	}

	@Override
	public void setEntiteResponsable(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.ENTITE_RESPONSABLE.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.ENTITE_RESPONSABLE.getCode(), new Variant(paramString) }, new int[1]);
	}
	
	@Override
	public void setEmetteur(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.ENTITE_RESPONSABLE.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.EMETTEUR.getCode(), new Variant(paramString) }, new int[1]);
	}
	
	@Override
	public void setType(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.ENTITE_RESPONSABLE.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.TYPE.getCode(), new Variant(paramString) }, new int[1]);
	}
	
	public void setProjetEvolution(String paramString) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.PROJET_EVOLUTION.getCode(), new Variant(paramString) }, new int[1]);
	}
	
	public void setNiveauDeTest(String paramString) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.NIVEAU_TEST.getCode(), new Variant(paramString) }, new int[1]);
		//Dispatch.put(this.bug, ALMDefectField.NIVEAU_TEST.getCode(), paramString);
	}

	@Override
	public void setApplication(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.APPLICATION.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.APPLICATION.getCode(), new Variant(paramString) }, new int[1]);
	}

	@Override
	public void setVersion(String paramString) {
		//Dispatch.put(this.bug, ALMDefectField.VERSION_DETECTION.getCode(), paramString);
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.VERSION_DETECTION.getCode(), new Variant(paramString) }, new int[1]);
	}

	@Override
	public void setEtat(String paramString) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.STATUT, new Variant(paramString) }, new int[1]);
	}

	public void setSeverity(String severity) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_SEVERITY", new Variant(severity) }, new int[1]);
	}

	public void setPriority(String priority) {
		Dispatch.put(this.bug, "Priority", priority);
	}

	public void setAssignedTo(String assignedTo) {
		Dispatch.put(this.bug, "AssignedTo", assignedTo);
	}

	public void setSeverity(DefectSeverity severity) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_SEVERITY", new Variant(severity.getSeverity()) }, new int[1]);
	}

	public void setDescription(String description) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_DESCRIPTION", new Variant(description) }, new int[1]);
	}

	public void setProject(String project) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_PROJECT", new Variant(project) }, new int[1]);
	}

	public void isReproducible(boolean isReproducible) {
		String reproducible;
		if (isReproducible)
			reproducible = "Y";
		else {
			reproducible = "N";
		}
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_REPRODUCIBLE", new Variant(reproducible) }, new int[1]);
	}

	public void setDetectionDate(String date) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_DETECTION_DATE", new Variant(date) }, new int[1]);
	}

	public int getDefectID() {
		int id = Dispatch.call(this.bug, "ID").getInt();
		return id;
	}

	public void setStatus(DefectStatus status) {
		Dispatch.put(this.bug, "Status", status.getStatus());
	}

	public void setPriority(DefectPriority priority) {
		Dispatch.put(this.bug, "Priority", priority.getPriority());
	}

	public void setSummary(String summary) {
		Dispatch.put(this.bug, "Summary", summary);
	}

	public void setDetectedBy(String detectedBy) {
		Dispatch.put(this.bug, "DetectedBy", detectedBy);
	}

	/**
	 * Permet de sauvegarder dans ALM (via une commande "Post") le defect en cours de manipulation.
	 */
	public void save() {
		Dispatch.call(this.bug, "Post");
	}

	public Dispatch getBugFactory() {
		return bugFactory;
	}

	public void setBugFactory(Dispatch bugFactory) {
		this.bugFactory = bugFactory;
	}

	public Dispatch getBug() {
		return bug;
	}

	public void setBug(Dispatch bug) {
		this.bug = bug;
	}
	
}
