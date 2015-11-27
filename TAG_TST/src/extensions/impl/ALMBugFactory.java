package extensions.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class ALMBugFactory {
	private ActiveXComponent almObject;
	private Dispatch bugFactory;

	public ALMBugFactory(ActiveXComponent almObject) {
		this.almObject = almObject;
		this.bugFactory = init();
	}

	private Dispatch init() {
		Dispatch bugFactory = Dispatch.call(this.almObject, "BugFactory").toDispatch();

		return bugFactory;
	}

	public ALMBug addItem() {
		return new ALMBug(this.bugFactory);
	}
}
