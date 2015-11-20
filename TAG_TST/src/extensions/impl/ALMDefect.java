package extensions.impl;

import atu.alm.wrapper.classes.AttachmentFactory;
import atu.alm.wrapper.classes.Bug;
import atu.alm.wrapper.enums.DefectPriority;
import atu.alm.wrapper.enums.DefectSeverity;
import atu.alm.wrapper.enums.DefectStatus;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import constantes.ALMDefectField;
import extensions.interfaces.IALMDefect;

public class ALMDefect implements IALMDefect {

	private Dispatch bugFactory;
	private Dispatch bug;

	// 1) The NULL parameter for "AddItem" method should be set as follows:
	// Variant paramVal = new Variant();
	// paramVal.putNull();
	//
	// Dispatch bug = Dispatch.call(bugFactory, "AddItem",
	// paramVall).getDispatch(); // Returns referecne to Bug Object in QC
	//
	// 2) 'Field' on Bug object is set as follows:
	// Variant v = Dispatch.invoke(bug, "Field", Dispatch.Put,
	// new Object {new Variant("BG_DETECTION_DATE"), new Variant(new
	// java.util.Date()) }, new int);
	//
	// Retrieve value of above 'Field' as below:
	// Variant v = Dispatch.invoke(bug, "Field", Dispatch.Get,
	// new Object {new Variant("BG_DETECTION_DATE") }, new int);

	/**
	 * Constructeur de base à partir d'une factory (un dispatch).
	 * 
	 * @param bugFactory
	 */
	public ALMDefect(Dispatch bugFactory) {
		super();
		this.bugFactory = bugFactory;
		this.bug = init();
	}

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

	public void setAssignedTo(String assignedTo) {
		Dispatch.put(this.bug, "AssignedTo", assignedTo);
	}

	public void setSeverity(DefectSeverity severity) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_SEVERITY", new Variant(severity.getSeverity()) }, new int[1]);
	}
	
	public void setSeverity(String severity) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { "BG_SEVERITY", new Variant(severity) }, new int[1]);
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
	
	public void setPriority(String priority) {
		Dispatch.put(this.bug, "Priority", priority);
	}

	public void setSummary(String summary) {
		Dispatch.put(this.bug, "Summary", summary);
	}

	public void setDetectedBy(String detectedBy) {
		Dispatch.put(this.bug, "DetectedBy", detectedBy);
	}

	public void save() {
		Dispatch.call(this.bug, "Post");
	}

	  public AttachmentFactory getAttachments() {
		    return new AttachmentFactory(this.bug);
		  }

	@Override
	public void setEtat(String paramString) {
		Dispatch.invoke(this.bug, "Field", 4, new Object[] { ALMDefectField.STATUT, new Variant(paramString) }, new int[1]);
	}

}
