package elements;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * Element bouton permettant de conserver la référence d'un objet qui sera ensuite manipuler.
 * @author levieilfa
 *
 */
public class LinkedButton extends JButton {

	/**
	 * L'objet lien.
	 */
	private Object link;
	
	/**
	 * Le contenant du bouton.
	 */
	private JComponent contenant;
	
	/**
	 * Constructeur permettant de créer le bouton et d'y affecter une action.
	 * @param link l'objet lien
	 * @param action l'action
	 */
	public LinkedButton(Object link, JComponent contenant, AbstractAction action) {
		super(action);
		this.setLink(link);
		this.contenant = contenant;
	}
	
	/**
	 * Permet d'obtenir le component qui contient le bouton.
	 * @return le component.
	 */
	public JComponent getContenant() {
		return contenant;
	}

	public Object getLink() {
		return link;
	}

	public void setLink(Object link) {
		this.link = link;
	}

}
