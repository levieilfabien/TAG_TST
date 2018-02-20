package exceptions;

import org.openqa.selenium.WebElement;

import constantes.Erreurs;

/**
 * Classe générique d'exception pour le selenium driver.
 * Cette classe intervient lors d'une erreur au cours d'un test.
 * @author levieil_f
 *
 */
public class SeleniumException extends Exception {

	/**
	 * Id de serialisation.
	 */
	private static final long serialVersionUID = 1352908630051136169L;
	
	/**
	 * Commentaire associé à l'erreur.
	 */
	private Erreurs informations;
	
	/**
	 * Indique si il s'agit d'une erreur technique.
	 * Si à faux, alors l'erreur est fonctionnelle.
	 */
	private Boolean erreurTechnique = false;
	
	/**
	 * Complément d'information.
	 */
	private String informationComplement = "";
	
	/**
	 * Element discriminant.
	 */
	private WebElement elementEnCause = null;
	

	public SeleniumException(Erreurs erreur) {
		super(erreur.toString());
		this.informations = erreur;
	}
	
	public SeleniumException(Erreurs erreur, String complement) {
		super(erreur.toString() + "(" + complement + ")");
		this.informations = erreur;
		this.informationComplement = complement;
	}
	
	public SeleniumException(SeleniumException ex, String complement) {
		super(ex.getInformations().toString() + "(" + ex.getInformationComplement() + " " + complement + ")");
		this.informations = ex.getInformations();
		this.informationComplement = ex.getInformationComplement() + complement;
	}
	
	public String toString() {
		String retour = "";
		
		if (informations != null) {
			retour = informations.toString();
		}
		if (informationComplement != null && !"".equals(informationComplement)) {
			retour = retour.concat(informationComplement);
		}
		if (elementEnCause != null) {
			retour = retour.concat(" (Cause : " + elementEnCause +")");
		}
		
		return retour;
	}

	////////////////////////////////////////////////////////////////
	
	public Erreurs getInformations() {
		return informations;
	}

	public void setInformations(Erreurs informations) {
		this.informations = informations;
	}

	public Boolean getErreurTechnique() {
		return erreurTechnique;
	}

	public void setErreurTechnique(Boolean erreurTechnique) {
		this.erreurTechnique = erreurTechnique;
	}

	public String getInformationComplement() {
		return informationComplement;
	}

	public void setInformationComplement(String informationComplement) {
		this.informationComplement = informationComplement;
	}

	public WebElement getElementEnCause() {
		return elementEnCause;
	}

	public void setElementEnCause(WebElement elementEnCause) {
		this.elementEnCause = elementEnCause;
	}
	
	////////////////////////////////////////////////////////////////
}
