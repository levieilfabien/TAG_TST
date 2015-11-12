package elements;

import java.lang.reflect.Field;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public class LinkedComboBox extends JComboBox {

	/**
	 * L'objet lien.
	 */
	private Object link;
	
	/**
	 * Le contenant du bouton.
	 */
	private JComponent contenant;
	
	public LinkedComboBox(Object lien, JComponent contenant, Object[] enumValues) {
		super(enumValues);
		this.setLink(lien);
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
	
	public Boolean setStringValue(String value) {
		String temp1 = "";
		String temp2 = "";
		
		for (int i = 0; i < this.getItemCount(); i++) {
			String item = this.getItemAt(i).toString();
			
			if(!"".equals(item) && item.contains(":")) {
				temp1 = item.split(":")[0].trim();
				temp2 = item.split(":")[1].trim();
			} else {
				temp1 = item;
			}
			
			if (temp1.equals(value) || temp2.equals(value)) {
				this.setSelectedIndex(i);
				return true;
			}
		}
		return false;
	}
	
	public String getStringValue() {
		String retour = this.getSelectedItem().toString();
		// On traite les deux cas possibles (liste de valeur séparée par ":" ou énumération)
		if(!"".equals(retour) && retour.contains(":")) {
			retour = retour.split(":")[0].trim();
		}
		
		return retour;
	}
	
	public void majChampInstance() {
		
		Object instance = ((Object[]) getLink())[1];
		Field champ = (Field) ((Object[]) getLink())[0];
		
		try {
			champ.set(instance, this.getStringValue());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Impossible de mettre à jour le champ");
		}
		
		//setLink(this.getStringValue());
	}
}
