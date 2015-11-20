package extensions.impl;

import atu.alm.wrapper.classes.TestSet;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import constantes.ALMTestSetField;
import extensions.interfaces.IALMTestSet;

public class ALMTestSet extends TestSet implements IALMTestSet {

	private ActiveXComponent almObject;
	private Dispatch testSet;

	public ALMTestSet(ActiveXComponent almObject, Dispatch testSet) {
		super(almObject, testSet);
		this.almObject = almObject;
		this.testSet = testSet;
	}

	@Override
	public void setChampSimple(ALMTestSetField champ, String paramString) {
		Dispatch.put(this.testSet, champ.getCode(), paramString);
	}

	@Override
	public void setChampObjet(ALMTestSetField champ, String paramString) {
		Dispatch.invoke(this.testSet, "Field", 4, new Object[] { champ.getCode(), new Variant(paramString) }, new int[1]);
	}
}
