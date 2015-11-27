package extensions.impl;

import atu.alm.wrapper.collection.ListWrapper;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class ALMTSTestFactory {

	private Dispatch testSet;
	private Dispatch tsTestFactory;

	public ALMTSTestFactory(ActiveXComponent almObject, Dispatch testSet) {
		this.testSet = testSet;
		this.tsTestFactory = init();
	}

	private Dispatch init() {
		Dispatch tsTestFactory = Dispatch.get(this.testSet, "TSTestFactory").toDispatch();

		return tsTestFactory;
	}

	public ListWrapper<ALMTSTest> getNewList() {
		Dispatch listOfTests = Dispatch.call(this.tsTestFactory, "NewList", new Object[] { "" }).toDispatch();

		int count = Dispatch.call(listOfTests, "Count").getInt();
		ListWrapper<ALMTSTest> listWrapper = new ListWrapper<ALMTSTest>();
		for (int i = 1; i <= count; ++i) {
			Dispatch dispatchTSTest = Dispatch.call(listOfTests, "Item", new Object[] { Integer.valueOf(i) }).toDispatch();

			ALMTSTest tsTest = new ALMTSTest(dispatchTSTest);
			listWrapper.add(tsTest);
		}
		return listWrapper;
	}

	public Dispatch getTestSet() {
		return testSet;
	}

	public void setTestSet(Dispatch testSet) {
		this.testSet = testSet;
	}

	public Dispatch getTsTestFactory() {
		return tsTestFactory;
	}

	public void setTsTestFactory(Dispatch tsTestFactory) {
		this.tsTestFactory = tsTestFactory;
	}

}
