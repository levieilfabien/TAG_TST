package extensions.interfaces;

import atu.alm.wrapper.IDefect;
import atu.alm.wrapper.classes.AttachmentFactory;
import atu.alm.wrapper.enums.DefectPriority;
import atu.alm.wrapper.enums.DefectSeverity;
import atu.alm.wrapper.enums.DefectStatus;

/**
 * Classe d'interface étendant le modèle ALMWrapper pour indexer plus de champs.
 * 
 * @author levieilfa
 * 
 */
public interface IALMDefect extends IDefect {

	// Nouvelles fonction

	public abstract void setCycle(String paramString);

	public abstract void setRelease(String paramString);

	public abstract void setEnvironement(String paramString);

	public abstract void setEntiteResponsable(String paramString);

	public abstract void setApplication(String paramString);

	public abstract void setVersion(String paramString);
	
	public abstract void setProjetEvolution(String string);

	public abstract void setNiveauDeTest(String string);

	public abstract void setEmetteur(String paramString);

	public abstract void setType(String paramString);
	
	public abstract void setEtat(String paramString);

	// Anciennes fonction

	public abstract void setDetectedBy(String paramString);

	public abstract void setAssignedTo(String paramString);

	public abstract void setPriority(DefectPriority paramDefectPriority);

	public abstract void setSeverity(DefectSeverity paramDefectSeverity);
	
	public abstract void setPriority(String paramDefectPriority);

	public abstract void setSeverity(String paramDefectSeverity);

	public abstract void setStatus(DefectStatus paramDefectStatus);

	public abstract void setSummary(String paramString);

	public abstract void setDetectionDate(String paramString);

	public abstract void setDescription(String paramString);

	public abstract void isReproducible(boolean paramBoolean);

	public abstract void setProject(String paramString);

	public abstract void save();

	public abstract int getDefectID();

	public abstract AttachmentFactory getAttachments();
}
