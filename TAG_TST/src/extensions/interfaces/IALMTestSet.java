package extensions.interfaces;

import constantes.ALMTestSetField;
import extensions.impl.ALMAttachementFactory;

/**
 * Classe d'interface étendant le modèle ALMWrapper pour indexer plus de champs.
 * 
 * @author levieilfa
 * 
 */
public interface IALMTestSet extends ALMHasAttachement {
	
	public abstract void setChampSimple(ALMTestSetField champ, String paramString);

	public abstract void setChampObjet(ALMTestSetField champ, String paramString);

	public abstract ALMAttachementFactory getAttachments();
}
