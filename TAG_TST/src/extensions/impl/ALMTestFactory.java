package extensions.impl;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class ALMTestFactory {

	  private ActiveXComponent almObject;
	  private Dispatch testFactory;

	  public ALMTestFactory(ActiveXComponent almObject)
	  {
	    this.almObject = almObject;
	    this.testFactory = init();
	  }

	  private Dispatch init() {
	    Dispatch bugFactory = Dispatch.call(this.almObject, "testFactory").toDispatch();

	    return bugFactory;
	  }

	  public ALMTest addItem() {
	    return new ALMTest(this.testFactory);
	  }
}
