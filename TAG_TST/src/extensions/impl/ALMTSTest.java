package extensions.impl;

import atu.alm.wrapper.enums.StatusAs;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

import extensions.interfaces.IALMTestCase;

public class ALMTSTest implements IALMTestCase {

	private Dispatch test;
	private Dispatch tsTest;

	public ALMTSTest(ActiveXComponent almObject, Dispatch test) {
		this.test = test;
		this.tsTest = init();
	}

	public ALMTSTest(Dispatch tsTest) {
		this.tsTest = tsTest;
	}

	private Dispatch init() {
		Dispatch tsTest = Dispatch.call(this.test, "Item", new Object[] { Integer.valueOf(1) }).toDispatch();
		return tsTest;
	}

	public String getName() {
		String name = Dispatch.call(this.tsTest, "Name").getString();
		return name;
	}

	public String getTestName() {
		String testName = Dispatch.call(this.tsTest, "TestName").getString();
		return testName;
	}

	public void putStatus(StatusAs as) {
		Dispatch.put(this.tsTest, "Status", as.getStatus().trim());
	}

	public ALMRunFactory getRunFactory() {
		return new ALMRunFactory(this.tsTest);
	}

	public void post() {
		Dispatch.call(this.tsTest, "Post");
	}

	public ALMAttachementFactory getAttachments() {
		return new ALMAttachementFactory(this.tsTest);
	}
}
