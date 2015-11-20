package extensions;

import atu.alm.wrapper.ALMServiceWrapper;
import extensions.impl.ALMDefect;
import extensions.interfaces.IALMDefect;

public class SeleniumALMWrapper extends ALMServiceWrapper {

	public SeleniumALMWrapper(String url) {
		super(url);
	}

//	public IALMDefect newDefect() {
//		IALMDefect defect = (ALMDefect) getAlmObj().getBugFactory().addItem();
//		return defect;
//	}
}
