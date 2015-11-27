package extensions.impl;

import atu.alm.wrapper.enums.StatusAs;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class ALMStep {
	private Dispatch step;

	public ALMStep(Dispatch step) {
		this.step = step;
	}

	public void setStepName(String stepName) {
		Dispatch.invoke(this.step, "Field", 4, new Object[] { "ST_STEP_NAME", new Variant(stepName) }, new int[1]);
	}

	public void setStatus(StatusAs as) {
		Dispatch.invoke(this.step, "Field", 4, new Object[] { "ST_STATUS", new Variant(as.getStatus()) }, new int[1]);
	}

	public void setDescription(String description) {
		Dispatch.invoke(this.step, "Field", 4, new Object[] { "ST_DESCRIPTION", new Variant(description) }, new int[1]);
	}

	public void setActual(String actual) {
		Dispatch.invoke(this.step, "Field", 4, new Object[] { "ST_ACTUAL", new Variant(actual) }, new int[1]);
	}

	public void setExpected(String expected) {
		Dispatch.invoke(this.step, "Field", 4, new Object[] { "ST_EXPECTED", new Variant(expected) }, new int[1]);
	}

	public void post() {
		Dispatch.call(this.step, "Post");
	}

	public Dispatch getStep() {
		return step;
	}

	public void setStep(Dispatch step) {
		this.step = step;
	}

}
