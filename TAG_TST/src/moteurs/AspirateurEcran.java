package moteurs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import outils.XLSOutils;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.Tag;
import beans.CibleBean;
import beans.EcranBean;
import constantes.Clefs;
import constantes.Erreurs;
import exceptions.SeleniumException;

/**
 * Cette classe a pour objectif d'effectuer une extraction des donn�es et de la structure de la page.
 * Une fois les donn�es extraites, elle sont pr�sent�es � l'utilisateur pour la r�daction de ses tests.
 * @author Fabien Levieil
 *
 */
public class AspirateurEcran {
	
	/**
	 * Indique une aspiration limit�e aux �l�ments les plus courament utilis�s.
	 */
	public static final int ASPIRATION_SIMPLE = 1;
	
	/**
	 * Indique une aspiration compl�te de la page.
	 */
	public static final int ASPIRATION_COMPLETE = 2;
	
	/**
	 * Indique le niveau d'aspiration attendue de l'aspirateur d'�cran.
	 */
	private int niveauAspiration = ASPIRATION_SIMPLE;

	
	/**
	 * Permet d'aspirer un contenu. Si plusieurs contenu, alors chaque contenu correspond � un frame qu'il faut d�crire.
	 * @param url sagit t'il d'une url?
	 * @param contenus les contenus. Si plusieurs, on cherche les frame.
	 * @return le bean ecran renseign�.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public EcranBean aspirerPage(List<String> contenus) throws SeleniumException {
		if (contenus.size() > 0) {
			
			String[] contenusTab = new String[contenus.size()];
			int compteur = 0;
			for (String contenu : contenus) {
				contenusTab[compteur] = contenu;
				compteur++;
			}
			
			Source source = new Source(contenusTab[0]);
			return aspirerPage(false, contenusTab, obtenirListeIdFrame(source));
		}
		return null;
	}
	
	/**
	 * Permet d'aspirer une page dont on passe en param�tre le contenu sous forme de texte, ou l'url.
	 * Si l'url pointe sur un fichier local et non vers une page web, on pr�fixe avec "file:".
	 * @param contenu le contenu du fichier html ou l'url vers la page.
	 * @param url indique si il s'agit d'une url ou d'un contenu.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public EcranBean aspirerPage(boolean url, String[] contenus, String[] frames) throws SeleniumException {
		
		// On initialise une page 
		EcranBean ecran = new EcranBean();
		Long compteur = (new Date()).getTime();
		String contenu = "";
		
		if (contenus.length > 0) {
			
			
			// Si le contenu n'est pas vide on commence l'aspiration.
			try {
				
				for (int i = 0; i < contenus.length; i++) {
				
					contenu = contenus[i];
				
					if (contenu != null && !"".equals(contenu)) {
						
						// On creer l'�l�ment de source � manipuler � partir du contenu de la page web.
						Source source;
						if (url != true) {
							// On creer la source � partir du contenu param�tre.
							source = new Source(contenu);
						} else {
							// On v�rifie si il s'agit d'un fichier.
							if (contenu.indexOf(':') == -1) {
								contenu = "file:" + contenu;
							}
							// On cr�er la source � partir de l'url.
							source = new Source(new URL(contenu));
						}
						// On lit tous les �l�ments de la page pour pouvoir les utiliser plus tard.
						Tag[] allTags = source.fullSequentialParse();
						
						// On indique � l'�cran la source utilis�e
						ecran.setSource(source);
						
						// On cherche l'�l�ment repr�sentant le titre de la page, il permettra de stocker les donn�es plus tard.
						if (ecran.getTitrePage() == null || "".equals(ecran.getTitrePage().trim())) {
							String tempTitre = obtenirTexteBrut(source.getFirstElement(HTMLElementName.TITLE));
							ecran.setTitrePage(tempTitre);
							// TODO Trouver un identifiant plus discriminant.
							ecran.setIdentifiant(tempTitre);
						}
						
						if (getNiveauAspiration() == ASPIRATION_SIMPLE) {
							
							String frameId = "";
							if (frames.length > (i - 1) && i > 0) {
								frameId = frames[i - 1];
							}
							
							// On r�cup�re tous les input de l'�cran et on les transforment en cibles pour le stockage
							compteur = renseignerEcranBean(HTMLElementName.INPUT, ecran, source, compteur, frameId);
							
							// On r�cup�re tous les liens de l'�cran et on les transforment en cibles pour le stockage
							compteur = renseignerEcranBean(HTMLElementName.A, ecran, source, compteur, frameId);
							
							// On r�cup�re tous les select de l'�cran et on les transforment en cibles pour le stockage
							compteur = renseignerEcranBean(HTMLElementName.SELECT, ecran, source, compteur, frameId);
							
						} else if (getNiveauAspiration() == ASPIRATION_COMPLETE) {
							
							// On r�cup�re tous les element de l'�cran et on les transforment en cibles pour le stockage
							List<Element> all = source.getAllElements();
							
							for (Element element : all) {	
								CibleBean temp = obtenirCibleCriteresDiscriminants(element);
								
								// On aspire le XPATH qui cela est absolument necessaire.
								if (getNiveauAspiration() == ASPIRATION_COMPLETE || temp.getClef() == Clefs.CRITERES_LIBRES) {	
									temp.setXpath(obtenirXPath(element));
								}
								
//								List<Element> frames = source.getAllElements(HTMLElementName.FRAME);
//								if (frames.size() > 0) {
//									for (Element frame : frames) {
//										if (isElementParent(element, frame)) {
//											temp.setFrame(frame.getAttributeValue("id"));
//										}
//									}
//								}

								if (frames.length > (i - 1) && i > 0) {
									temp.setFrame(frames[i - 1]);
								}
								
								ecran.getCibles().put(element.getName() + (compteur++), temp);
							}
						}
						

					}
				}
				System.out.println("FIN");
				return ecran;
			} catch (MalformedURLException e) {
				throw new SeleniumException(Erreurs.E025, "L'url n'est pas valide.");
			} catch (IOException e) {
				throw new SeleniumException(Erreurs.E025, "Le fichier n'existe pas.");
			}
		} else {
			throw new SeleniumException(Erreurs.E025, "Impossible d'extraire une page vide.");
		}
		
	}
	
	/**
	 * Permet d'obtenir la liste des frame dans un �cran.
	 * @param source la source � analyser.
	 * @return la liste des id de frame.s
	 */
	public String[] obtenirListeIdFrame(Source source) {
		// On r�cup�re tous les elements de l'�cran et on les transforment en cibles pour le stockage
		List<Element> elements = source.getAllElements(HTMLElementName.FRAME);
		String[] retour = new String[elements.size()];
		int compteur = 0;
		
		for (Element element : elements) {	
			if (element != null) {
				retour[compteur] = element.getAttributeValue("id");
				compteur++;
			}
		}
		
		return retour;
	}
	
	/**
	 * Permet d'obtenir la liste des frame dans un �cran.
	 * @param source la source � analyser.
	 * @return la liste des id de frame.s
	 */
	public String[] obtenirListeIdFrame(String source) {
		if (source != null) {
			Source sourceHtml = new Source(source);
			sourceHtml.fullSequentialParse();
			return obtenirListeIdFrame(sourceHtml);
		}
		return new String[]{""};
	}
	
	/**
	 * Renseigne un bean ecran pour un type d'�l�ment donn�.
	 * @param elementName le type d'�l�ment concern�.
	 * @param ecran l'�cran � renseigner.
	 * @param source la source des donn�es.
	 * @param compteur le compteur pour tracage.
	 */
	public Long renseignerEcranBean(String elementName, EcranBean ecran, Source source, Long compteur, String frame) {
		// On r�cup�re tous les elements de l'�cran et on les transforment en cibles pour le stockage
		List<Element> elements = source.getAllElements(elementName);
//		List<Element> frames = source.getAllElements(HTMLElementName.FRAME);
		
		for (Element element : elements) {
			
			CibleBean temp = obtenirCibleCriteresDiscriminants(element);
			
			if (temp != null) {
				if (getNiveauAspiration() == ASPIRATION_COMPLETE || temp.getClef() == Clefs.CRITERES_LIBRES) {	
					temp.setXpath(obtenirXPath(element));
				}
				
				if (frame != null && !"".equals(frame)) {
					temp.setFrame(frame);
				}
				
				ecran.getCibles().put(element.getName() + (compteur++), temp);
			}
		}
		
		return compteur;
	}
	
	/**
	 * Permet d'obtenir une expression XPath pour acceder � l'�l�ment param�tre.
	 * @param element l'�l�ment dont on souhaites obtenir le XPath.
	 * @return l'expression XPath pour l'�l�ment param�tre.
	 */
	public String obtenirXPath(Element element) {	
		Integer compteur = obtenirPlacementDansLesFreres(element);
		String retour = "";
		// On regarde le placement de l'�l�ment param�tre parmi ces fr�res et on initialise le XPath en fonction.
		if (compteur > 0) {
			retour = (element.getName() + "["+ compteur + "]");
		} else {
			retour = element.getName();
		}
		compteur = 0;
		// On s'occupe de trouver les �l�ments p�res de l'�l�ment param�tre, et pour chacun on alimente le XPath.
		if (element != null) {
			Element tempParent = element.getParentElement();
			// On boucle de parent en parent afin de remonter � la racine du document et avoir le chemin complet.
			while (tempParent != null) {
				// On essai d'obtenir l'index de l'objet si il en existe plusieurs.	
				compteur = obtenirPlacementDansLesFreres(tempParent);
				if (compteur > 0) {
					retour = (tempParent.getName() + "["+ compteur + "]/").concat(retour);
				} else {
					retour = (tempParent.getName() + "/").concat(retour);
				}
				// On remet le compteur � zero pour le prochain �l�ment de la chaine.
				compteur = 0;
				tempParent = tempParent.getParentElement();
			}
		}
		return retour;
	}
	
	/**
	 * Permet d'obtenir le nombres de noeuds fr�res de l'�l�ment param�tre.
	 * C'est � dire les �l�ments au m�me niveau dont le nom est le m�me. 
	 * @param element l'�l�ment dont on recherche les fr�res.
	 * @return le nombres d'�l�ments fr�res de l'�l�ment.
	 */
	public Integer obtenirNombresDeFreres(Element element) {
		Element tempParent = element.getParentElement();
		Integer retour = 0;
		// On cherche � obtenir les enfants du p�re de l'�l�ment param�tre.
		if (tempParent != null) {
			// On parcours les fils du p�re de l'�l�ment param�tres.
			for (Element tempElement : tempParent.getChildElements()) {
				// Si l'�l�ment parcouru � le m�me nom que l'�l�ment param�tre et le m�me p�re, alors il est un fr�re.
				if (tempElement != null && (tempElement.getName().toUpperCase().equals(element.getName().toUpperCase()))) {
					retour++;
				}
			}
			// On retire l'�l�ment lui-m�me du compteur si celui-ci � �t� initialis�.
			if (retour > 0) {
				retour --;
			}
		}
		return retour;
	}
	
	/**
	 * Permet d'obtenir le placement parmi ses �l�ments fr�res d'un �l�ments.
	 * Un fr�re est un �l�ment ayant le m�me p�re et possedant le m�me nom (ex : des TR)
	 * @param element l'�l�ment dont on souhaites connaitre le placement dans les fr�res.
	 * @return le placement parmi les fr�res de l'�l�ment.
	 */
	public Integer obtenirPlacementDansLesFreres(Element element) {
		Element tempParent = element.getParentElement();
		Integer retour = 0;
		List<Element> tempListeFils = new LinkedList<Element>();
		boolean aumoinsunfrere = false;
		boolean elementcourantparcouru = false;
		
		if (tempParent != null) {
			// On r�cup�re la liste des fils du p�re de l'�l�ment
			tempListeFils = tempParent.getChildElements();
			// Si le nombre de fils est sup�rieur � 1 : Donc si l'�l�ment � des fr�res potentiels
			if (tempListeFils.size() > 1) {
				// On parcours les fils et on cherche la position de l'�l�ment parmis les fils.
				for (Element tempElement : tempListeFils) {
					if (tempElement != null) {
						// Si le fils parcourue est l'�l�ment alors on � la position ! 
						if (tempElement == element) {
							retour++;
							elementcourantparcouru = true;
						} else if (tempElement.getName().toUpperCase().equals(element.getName().toUpperCase())) {
							// Sinon on augmente le compteur et on continue le parcours
							aumoinsunfrere = true;
							if (!elementcourantparcouru) {
								retour++;
							}
						}
					}
				}
			}
		}
		// Si l'�l�ment n'as pas de fr�res alors le compteur est forcement � 0
		if (!aumoinsunfrere) {
			return 0;
		}
		// Sinon on renvoie le compteur incr�menter lors du parcours
		return retour;
	}
	
	/**
	 * Teste l'�l�ment param�tre est fils du p�re param�tre.
	 * @param fils l'�l�ment dont on souhaites savoir si il est le fils.
	 * @param pere l'�l�ment dont on souhaites savoir si il est le p�re de l'autre �l�ment param�tre.
	 * @return true si il y a lien de parent�, false sinon.
	 */
	public boolean isElementParent(Element fils, Element pere) {
		// On parcours les parents de l'�l�ment param�tre "fils".
		Element tempParent = fils.getParentElement();
		while (tempParent != null) {
			// Si lors du parcours on rencontre l'�l�ment param�tre "pere" alors on retourne vrai.
			if (tempParent == pere)  {
				return true;
			} else {
				// Sinon on continue le parcours jusqu'� la racine du document.
				tempParent = tempParent.getParentElement();
			}
		}
		// Si lors du parcours on � pas rencontr� l'�l�ment "pere" alors on retourne faux.
		return false;
	}
	
//	/**
//	 * Obtiens la liste des crit�res libres pour l'identification d'un element dans la page.
//	 * @param element l'�l�ment dont on cherche un crit�re discriminant.
//	 * @return la liste des crit�res libres pour l'identification de l'objet.
//	 */
//	public CibleBean obtenirCibleCriteresDiscriminants(Element element) {
//		String tempValue = "";
//		Clefs clefs = Clefs.ID;
//		String contenu = obtenirTexteBrut(element);
//
//		if (element.getAttributeValue("id") != null) {
//			clefs = Clefs.ID;
//			tempValue = element.getAttributeValue("id");
//		} else if (element.getAttributeValue("name") != null) {
//			clefs = Clefs.NAME;
//			tempValue = element.getAttributeValue("name");
//		} else if (contenu != null && !"".equals(contenu) && !contenu.contains("<")) {
//			clefs = Clefs.TEXTE_COMPLET;
//			tempValue = contenu;
//		} else {
//			return obtenirCibleCriteresLibres(element);
//		}	
//		return new CibleBean(clefs, tempValue);
//	}
	
	
	/**
	 * Obtiens la liste des crit�res libres pour l'identification d'un element dans la page.
	 * @param element l'�l�ment dont on cherche un crit�re discriminant.
	 * @return la liste des crit�res libres pour l'identification de l'objet.
	 */
	public CibleBean obtenirCibleCriteresDiscriminants(Element element) {
		List<String> tempValues = new LinkedList<String>();
		Integer criteres = 0;
		String contenu = obtenirTexteBrut(element);
		tempValues.add(element.getName());
		Clefs clefs = Clefs.CRITERES_ITERATIF;
		// On parcours les crit�res classique d'identification.
		if (element.getAttributeValue("id") != null) {
			tempValues.add("id=" + element.getAttributeValue("id"));
			criteres++;
		} else if (element.getAttributeValue("name") != null) {
			tempValues.add("name=" + element.getAttributeValue("name"));
			criteres++;
		} else if (contenu != null && !"".equals(contenu) && !contenu.contains("<")) {
			tempValues.add(CibleBean.CRITERE_TEXTE + "=" + contenu);
			criteres++;
		} 
		// Si cela n'est pas sufisant on utilise les crit�res libres
		if (criteres == 0) {
			return obtenirCibleCriteresLibres(element);
		}	
		return new CibleBean(clefs, tempValues);
	}
	
	/**
	 * Obtiens la liste des crit�res libres pour l'identification d'un element dans la page.
	 * @param element l'�l�ment dont on cherche un crit�re large.
	 * @return la liste des crit�res libres pour l'identification de l'objet.
	 */
	public CibleBean obtenirCibleCriteresLibres(Element element) {
		List<String> retour = new LinkedList<String>();
		Attributes attributes = element.getAttributes();	
		
		if (element.getName() != null && attributes != null && attributes.size() > 0) {
			// On ajoute le nom du tag comme premier element de ciblage
			retour.add(element.getName());
						
			Iterator<Attribute> iterator = attributes.iterator();
			Attribute temp = null;
			
			// On it�re sur les attributs de l'�l�ment, et on stocke leurs valeurs.
			while (iterator.hasNext()) {
				temp = iterator.next();
				
				// On limite le nombre de crit�res libres
				if (temp.getValue().length() < 50 && !temp.getValue().contains("'")) {
					retour.add(temp.getName());
					retour.add(temp.getValue());
				}
			}
			
		}
		// On renvoie par d�faut une cible avec tous les attributs possible.		
		return new CibleBean(Clefs.CRITERES_LIBRES, retour);
	}
	
	/**
	 * Permet d'obtenir le contenu texte � l'�tat brut d'un �l�ment.
	 * @param element l'�l�ment.
	 * @return le texte en l'�tat dans le tag de l'�l�ment.
	 */
	public String obtenirTexteBrut(Element element) {
		return CharacterReference.decode(element.getContent()); //CharacterReference.decodeCollapseWhiteSpace(element.getContent());
	}
	
	/**
	 * Permet d'obtenir le contenu texte � l'�tat brut d'un �l�ment.
	 * @param element l'�l�ment.
	 * @return le texte en l'�tat dans le tag de l'�l�ment.
	 */
	public String obtenirTexte(Element element) {
		return CharacterReference.decodeCollapseWhiteSpace(element.getContent());
	}
	
	/**
	 * Genere un fichier � partir du templace pour un �cran donner.
	 * @param ecran l'�cran sous forme de bean pour le renseignement du fichier XLS.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public void genererExcelEcran(EcranBean ecran) throws SeleniumException {
		Map map = new HashMap();
		map.put("ecran", ecran);
		
		XLSOutils.generationExcel("ecranTemplate.xls", ecran.getTitrePage() + "export.xls", map);
	}
	
	///////////////////////////// CONSTRUCTEURS ////////////////////////////////////////////
	
	/**
	 * Constructeur de l'aspiration pr�sicant le niveau d'aspiration.
	 * @param niveauAspiration le niveau d'aspiration souhait�.
	 */
	public AspirateurEcran(Integer niveauAspiration) {
		super();
		this.niveauAspiration = niveauAspiration;
	}
	 
	/**
	 * Constructeur par d�faut permettant une aspiration simple.
	 */
	public AspirateurEcran() {
		this(ASPIRATION_SIMPLE);
	}
	
	///////////////////////////// GETTERS & SETTERS ////////////////////////////////////////////
	
	public int getNiveauAspiration() {
		return niveauAspiration;
	}

	public void setNiveauAspiration(int niveauAspiration) {
		this.niveauAspiration = niveauAspiration;
	}

	
	///////////////////////////// TODO A SUPPRIMER ////////////////////////////////////////////
	
	public static void main(String varg[]) {
		AspirateurEcran asp = new AspirateurEcran();
		try {
			asp.genererExcelEcran(asp.aspirerPage(true, new String[]{"data/testTraceo.html"},  new String[]{""}));
		} catch (SeleniumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
