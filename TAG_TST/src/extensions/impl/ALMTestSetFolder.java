package extensions.impl;

import atu.alm.wrapper.exceptions.ALMServiceException;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;

public class ALMTestSetFolder {

	private ActiveXComponent almObject;
	private Dispatch testSetFolder;

	public ALMTestSetFolder(ActiveXComponent almObject, Dispatch testSetFolder) {
		this.almObject = almObject;
		this.testSetFolder = testSetFolder;
	}

	public ALMTestSet findTestSet(String testSetName, int searchTestSetID) throws ALMServiceException {
		Dispatch listOfTestSet = Dispatch.call(this.testSetFolder, "FindTestSets", new Object[] { testSetName, Boolean.valueOf(true), null })
				.toDispatch();

		Dispatch testSet = null;
		try {
			int count = Dispatch.call(listOfTestSet, "Count").getInt();

			for (int i = 1; i <= count; ++i) {
				testSet = Dispatch.call(listOfTestSet, "Item", new Object[] { Integer.valueOf(i) }).toDispatch();
				int testSetID = Dispatch.call(testSet, "ID").getInt();
				if (searchTestSetID == testSetID) {
					return new ALMTestSet(this.almObject, testSet);
				}
			}
			throw new ALMServiceException("The Given Test Set Name \"" + testSetName + "\" Not Found");
		} catch (NullPointerException e) {
			throw new ALMServiceException("The Given Test Set Name \"" + testSetName + "\" Not Found ");
		}
	}

	public int getCount() {
		int count = Dispatch.call(this.testSetFolder, "Count").getInt();
		return count;
	}

	public String getName() {
		String name = Dispatch.call(this.testSetFolder, "Name").getString();
		return name;
	}

	public String getPath() {
		String path = Dispatch.call(this.testSetFolder, "Path").getString();
		return path;
	}
}
