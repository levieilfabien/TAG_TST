package extensions.impl;

import com.jacob.com.Dispatch;

/**
 * Classe d'extension de Attachement
 * 
 * @author levieilfa
 * 
 */
public class ALMAttachement {

	private Dispatch currentAttachment;

	public ALMAttachement(Dispatch currentAttachment) {
		this.currentAttachment = currentAttachment;
	}

	public void setDescription(String attachmentDescription) {
		Dispatch.put(this.currentAttachment, "Description", attachmentDescription);
	}

	public void setFileName(String filePath) {
		System.out.println("FILE PATH" + filePath);
		Dispatch.put(this.currentAttachment, "FileName", filePath);
	}

	public void setType(String type) {
		Dispatch.put(this.currentAttachment, "Type", type);
	}

	public void post() {
		Dispatch.call(this.currentAttachment, "Post");
	}

	public ALMAttachementStorage getAttachmentStorage() {
		return new ALMAttachementStorage(this.currentAttachment);
	}

	public Dispatch getCurrentAttachment() {
		return currentAttachment;
	}

	public void setCurrentAttachment(Dispatch currentAttachment) {
		this.currentAttachment = currentAttachment;
	}

}
