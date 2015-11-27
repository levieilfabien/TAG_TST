package extensions.impl;


import com.jacob.com.Dispatch;

public class ALMTest {
	  private Dispatch tsTest;
	  private Dispatch test;

	  public ALMTest(Dispatch tsTest)
	  {
	    this.tsTest = tsTest;
	    this.test = init();
	  }

	  private Dispatch init() {
	    Dispatch test = Dispatch.get(this.tsTest, "Test").toDispatch();
	    return test;
	  }

	  public ALMDesignStepFactory getDesignStepFactory() {
	    return new ALMDesignStepFactory(this.test);
	  }
}
