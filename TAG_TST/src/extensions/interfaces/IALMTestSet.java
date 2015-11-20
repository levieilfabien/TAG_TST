package extensions.interfaces;

import atu.alm.wrapper.ITestSet;
import atu.alm.wrapper.classes.AttachmentFactory;
import constantes.ALMTestSetField;

/**
 * Classe d'interface étendant le modèle ALMWrapper pour indexer plus de champs.
 * 
 * @author levieilfa
 * 
 */
public interface IALMTestSet extends ITestSet {
	
	public abstract void setChampSimple(ALMTestSetField champ, String paramString);

	public abstract void setChampObjet(ALMTestSetField champ, String paramString);

	public abstract AttachmentFactory getAttachments();
}
