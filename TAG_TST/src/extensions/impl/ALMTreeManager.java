package extensions.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class ALMTreeManager {

	  private ActiveXComponent almObject;
	  private Dispatch treeManager;

	  public ALMTreeManager(ActiveXComponent almObject)
	  {
	    this.almObject = almObject;
	    this.treeManager = init();
	  }

	  private Dispatch init() {
	    Dispatch bugFactory = Dispatch.call(this.almObject, "treeManager").toDispatch();

	    return bugFactory;
	  }
}
