package extensions.impl;

import com.jacob.com.Dispatch;

public class ALMRunFactory {

	private Dispatch runFactory;
	private Dispatch tsTest;

	public ALMRunFactory(Dispatch tsTest) {
		this.tsTest = tsTest;
		this.runFactory = init();
	}

	private Dispatch init() {
		Dispatch runFactory = Dispatch.get(this.tsTest, "RunFactory").toDispatch();
		return runFactory;
	}

	public ALMRun addItem() {
		Dispatch run = Dispatch.call(this.runFactory, "AddItem", new Object[] { "Null" }).toDispatch();

		return new ALMRun(run);
	}

	public Dispatch getRunFactory() {
		return runFactory;
	}

	public void setRunFactory(Dispatch runFactory) {
		this.runFactory = runFactory;
	}

	public Dispatch getTsTest() {
		return tsTest;
	}

	public void setTsTest(Dispatch tsTest) {
		this.tsTest = tsTest;
	}

}
