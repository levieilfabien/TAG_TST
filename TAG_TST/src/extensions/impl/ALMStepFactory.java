package extensions.impl;

import com.jacob.com.Dispatch;

public class ALMStepFactory {
	  private Dispatch stepFactory;
	  private Dispatch run;

	  public ALMStepFactory(Dispatch run)
	  {
	    this.run = run;
	    this.stepFactory = init();
	  }

	  private Dispatch init() {
	    Dispatch stepFactory = Dispatch.get(this.run, "StepFactory").toDispatch();
	    return stepFactory;
	  }

	  public ALMStep addItem() {
	    Dispatch step = Dispatch.call(this.stepFactory, "AddItem", new Object[] { "Null" }).toDispatch();

	    return new ALMStep(step);
	  }

	public Dispatch getStepFactory() {
		return stepFactory;
	}

	public void setStepFactory(Dispatch stepFactory) {
		this.stepFactory = stepFactory;
	}

	public Dispatch getRun() {
		return run;
	}

	public void setRun(Dispatch run) {
		this.run = run;
	}
	  
	  
}
