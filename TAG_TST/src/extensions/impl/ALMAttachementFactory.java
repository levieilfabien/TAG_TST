package extensions.impl;

import com.jacob.com.Dispatch;

/**
 * Classe d'extention du atachement factory.
 * @author levieilfa
 *
 */
public class ALMAttachementFactory {

	private Dispatch attachmentFactory;
	private Dispatch bug;

	public ALMAttachementFactory(Dispatch bug) {
		this.bug = bug;
		this.attachmentFactory = init();
	}

	private Dispatch init() {
		Dispatch attachmentFactory = Dispatch.call(this.bug, "Attachments").toDispatch();

		return attachmentFactory;
	}

	public ALMAttachement addItem(String fileName) {
		Dispatch attachment = Dispatch.call(this.attachmentFactory, "AddItem", new Object[] { fileName }).toDispatch();

		return new ALMAttachement(attachment);
	}

	public Dispatch getAttachmentFactory() {
		return attachmentFactory;
	}

	public void setAttachmentFactory(Dispatch attachmentFactory) {
		this.attachmentFactory = attachmentFactory;
	}

	public Dispatch getBug() {
		return bug;
	}

	public void setBug(Dispatch bug) {
		this.bug = bug;
	}

}
