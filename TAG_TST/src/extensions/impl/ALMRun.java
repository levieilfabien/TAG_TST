package extensions.impl;

import atu.alm.wrapper.enums.StatusAs;

import com.jacob.com.Dispatch;

import extensions.interfaces.IALMRun;

public class ALMRun implements IALMRun {
	
	private Dispatch run;

	public ALMRun(Dispatch run) {
		super();
		this.run = run;
	}
	
	public Dispatch getRun() {
		return run;
	}

	public void setStatus(StatusAs as) {
		Dispatch.put(this.run, "Status", as.getStatus());
	}

	public void setName(String runName) {
		Dispatch.put(this.run, "Name", runName);
	}

	public void post() {
		Dispatch.call(this.run, "Post");
	}

	public int getID() {
		return Dispatch.call(this.run, "ID").getInt();
	}

	public ALMStepFactory getStepFactory() {
		return new ALMStepFactory(this.run);
	}

	public ALMAttachementFactory getAttachments() {
		return new ALMAttachementFactory(this.run);
	}

}
