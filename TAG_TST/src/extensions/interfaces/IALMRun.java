package extensions.interfaces;

import com.jacob.com.Dispatch;

import extensions.impl.ALMStepFactory;

public interface IALMRun extends ALMHasAttachement {

	public abstract ALMStepFactory getStepFactory();
	
	public Dispatch getRun();
}
