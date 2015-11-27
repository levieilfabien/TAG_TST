package extensions.interfaces;

import extensions.impl.ALMRunFactory;

public interface IALMTestCase extends ALMHasAttachement {
	  public abstract ALMRunFactory getRunFactory();
}
