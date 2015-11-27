package extensions.interfaces;

import extensions.impl.ALMStepFactory;

public interface IALMTestCaseRun extends ALMHasAttachement {
	  public abstract ALMStepFactory getStepFactory();
}
