package extensions.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import constantes.ALMTestSetField;
import extensions.interfaces.IALMTestSet;

public class ALMTestSet implements IALMTestSet {

	private ActiveXComponent almObject;
	private Dispatch testSet;

	public ALMTestSet(ActiveXComponent almObject, Dispatch testSet) {
		this.almObject = almObject;
		this.testSet = testSet;
	}

	public ALMTSTestFactory getTSTestFactory() {
		return new ALMTSTestFactory(this.almObject, this.testSet);
	}

	public String getName() {
		String name = Dispatch.call(this.testSet, "Name").getString();
		return name;
	}

	public ALMAttachementFactory getAttachments() {
		return new ALMAttachementFactory(this.testSet);
	}

	@Override
	public void setChampSimple(ALMTestSetField champ, String paramString) {
		Dispatch.put(this.testSet, champ.getCode(), paramString);
	}

	@Override
	public void setChampObjet(ALMTestSetField champ, String paramString) {
		Dispatch.invoke(this.testSet, "Field", 4, new Object[] { champ.getCode(), new Variant(paramString) }, new int[1]);
	}

	public ActiveXComponent getAlmObject() {
		return almObject;
	}

	public void setAlmObject(ActiveXComponent almObject) {
		this.almObject = almObject;
	}

	public Dispatch getTestSet() {
		return testSet;
	}

	public void setTestSet(Dispatch testSet) {
		this.testSet = testSet;
	}
}
