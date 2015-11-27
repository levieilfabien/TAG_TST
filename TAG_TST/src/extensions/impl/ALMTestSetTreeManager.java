package extensions.impl;

import atu.alm.wrapper.exceptions.ALMServiceException;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComFailException;
import com.jacob.com.Dispatch;

public class ALMTestSetTreeManager {

	private ActiveXComponent almObject;
	private Dispatch testSetTreeManager;
	private static final String ROOT = "Root";

	public ALMTestSetTreeManager(ActiveXComponent almObject) {
		this.almObject = almObject;
		this.testSetTreeManager = init();
	}

	private Dispatch init() {
		Dispatch testSetTreeManager = Dispatch.get(this.almObject, "TestSetTreeManager").toDispatch();

		return testSetTreeManager;
	}

	public ALMTestSetFolder getNodeByPath(String testSetFolderPath) throws ALMServiceException {
		Dispatch testSetFolder;
		try {
			testSetFolder = Dispatch.call(this.testSetTreeManager, "NodeByPath", new Object[] { "Root\\" + testSetFolderPath }).toDispatch();
		} catch (ComFailException e) {
			throw new ALMServiceException("The Given Test Set Folder Path \"" + testSetFolderPath + "\" Not Found");
		}

		return new ALMTestSetFolder(this.almObject, testSetFolder);
	}
}
