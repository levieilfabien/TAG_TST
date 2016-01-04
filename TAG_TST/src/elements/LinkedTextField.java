package elements;

import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.List;

public class LinkedTextField extends JTextField {
	/**
	 * Id de serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * L'objet lien.
	 */
	private Object link;
	
	/**
	 * Le contenant du bouton.
	 */
	private JComponent contenant;
	
	/**
	 * Constructeur avec lien.
	 * @param link l'objet lié au champ.
	 * @param contenant le contenant du texte.
	 * @param valeurParDefaut la valeur par défaut.
	 */
	public LinkedTextField(Object link, JComponent contenant, String valeurParDefaut) {
		super(valeurParDefaut);
		this.setLink(link);
		this.setContenant(contenant);
	}

	public Object getLink() {
		return link;
	}

	public void setLink(Object link) {
		this.link = link;
	}

	public JComponent getContenant() {
		return contenant;
	}

	public void setContenant(JComponent contenant) {
		this.contenant = contenant;
	}
	
	
	public void majChampInstance() {
		
		Object[] tabLink = ((Object[]) getLink());
		
		Object instance = tabLink[1];
		Field champ = (Field) tabLink[0];
		Integer position = null;
		
		if (tabLink.length == 3) {
			position = (Integer) tabLink[2];
		}
		
		try {
			if (position == null) {
				champ.set(instance, this.getText());
			} else {
				List<String> tempList = (List<String>) champ.get(instance);
				tempList.set(position, this.getText());
				champ.set(instance, tempList);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Impossible de mettre à jour le champ");
		}
		
		//setLink(this.getText());
	}
	
	public void killInstance() {
		// Récupération des liens
		Object[] tabLink = ((Object[]) getLink());
		Object instance = tabLink[1];
		Field champ = (Field) tabLink[0];
		Integer position = null;
		
		// Si le nombre de lien le suggère on récupère la position
		if (tabLink.length >= 3) {
			position = (Integer) tabLink[2];
		}
		// Mise à vide du champ de l'instance.
		try {
			if (position == null) {
				champ.set(instance, null);
			} else {
				List<String> tempList = (List<String>) champ.get(instance);
				tempList.remove(position);
				champ.set(instance, tempList);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Impossible de supprimer le champ");
		}
		
		//setLink(this.getText());
	}
}
