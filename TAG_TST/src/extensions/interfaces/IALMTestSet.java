package extensions.interfaces;

import constantes.ALMTestSetField;
import extensions.impl.ALMAttachementFactory;

/**
 * Classe d'interface �tendant le mod�le ALMWrapper pour indexer plus de champs.
 * 
 * @author levieilfa
 * 
 */
public interface IALMTestSet extends ALMHasAttachement {
	
	public abstract void setChampSimple(ALMTestSetField champ, String paramString);

	public abstract void setChampObjet(ALMTestSetField champ, String paramString);

	public abstract ALMAttachementFactory getAttachments();
}
