package extensions.impl;

import com.jacob.com.Dispatch;

public class ALMDesignStepFactory {
	  private Dispatch test;

	  public ALMDesignStepFactory(Dispatch test)
	  {
	    this.test = test;
	    init();
	  }

	  private Dispatch init() {
	    Dispatch designStepFactory = Dispatch.get(this.test, "DesignStepFactory").toDispatch();

	    return designStepFactory;
	  }

	public Dispatch getTest() {
		return test;
	}

	public void setTest(Dispatch test) {
		this.test = test;
	}
	  
	  
}
