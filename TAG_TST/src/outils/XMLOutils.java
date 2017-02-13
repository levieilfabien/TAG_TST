package outils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlObject.Factory;
import org.apache.xmlbeans.XmlTokenSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import annotations.BaliseXml;
import constantes.Erreurs;
import exceptions.SeleniumException;


/**
 * Classe d'outils pour l'analyse de fichiers XML.
 * @author kcw293
 *
 */
public class XMLOutils {
	
	// NAMESPACES POUR L'ANALYSE XML
	
	/** Namespace associé au format atom. */
	public final static String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
	
	/** Namespace associé au format xhtml. */
	public final static String NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml";

	
	/**
	 * Transforme un contenu xml en objet XML
	 * @param contenuXml la chaine de caractère xml à transformer en objet xml pour manipulation.
	 * @return l'objet xml.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static XmlObject obtenirXml(String contenuXml) throws SeleniumException {
		try {
			return Factory.parse(contenuXml);
		} catch (XmlException e) {
			throw new SeleniumException(Erreurs.E014, "Impossible de lire le contenu xml.");
		}
	}
	
	
	/**
	 * Enlève la balise de fragmentxml autour d'une extraction xml.
	 * @param fragmentXml le fragment de fichier xml.
	 * @return la chaine de caractère comprise dans la balise de fragment (xml ou texte).
	 */
	public static String contenuTexte(XmlTokenSource fragmentXml) {
		if (fragmentXml != null) {
			return fragmentXml.xmlText().split(">", 2)[1].split("</xml-fragment")[0];
		} else {
			return null;
		}
	}
	
	/**
	 * Renvoie le contenu du texte de la première occurence de la balise recherchée.
	 * @param xmlComplet le xml dans lequel on cherche la balise.
	 * @param balise la balise à chercher.
	 * @return le contenu du texte de la balise.
	 */
	public static String contenuTexte(XmlObject xmlComplet, String balise) {
		if (xmlComplet != null && balise != null) {
			return contenuTexte(obtenirElementsXml(xmlComplet, balise, null, false)[0]);
		} else {
			return null;
		}
	}

	/**
	 * Récupère la liste d'objets xml répondant aux critères (dans un même namespace).
	 * @param xmlComplet le xml complet (ou l'objet xml) à analyser.
	 * @param prefixe le préfixe corespondant au namespace (ex : a dans a:entry).
	 * @param suffixe le suffixe sous forme element#souselement@positionDansElement (ex : entry#div#ul@1 signifie les ul de la première div de l'entry)
	 * @param nameSpace le namespace associé.
	 * @return les objets issus de la recherche.
	 */
	@Deprecated
	public static XmlObject[] obtenirElementsXml(XmlObject xmlComplet, String prefixe, String suffixe, String nameSpace) {
		return obtenirElementsXml(xmlComplet, prefixe, suffixe, nameSpace, false);
	}
	
	/**
	 * Récupère la liste d'objets xml répondant aux critères (dans un même namespace).
	 * @param xmlComplet le xml complet (ou l'objet xml) à analyser.
	 * @param prefixe le préfixe corespondant au namespace (ex : a dans a:entry).
	 * @param suffixe le suffixe sous forme element#souselement@positionDansElement (ex : entry#div#ul@1 signifie les ul de la première div de l'entry)
	 * @param nameSpace le namespace associé.
	 * @param all si vrai alors on veux tous les éléments associés. Si non juste le premier.
	 * @return les objets issus de la recherche.
	 */
	@Deprecated
	public static XmlObject[] obtenirElementsXml(XmlObject xmlComplet, String prefixe, String suffixe, String nameSpace, boolean all) {
		XmlObject[] temp = null;
		List<XmlObject> tempListe = new LinkedList<XmlObject>();
		// En absence de namespace on suppose qu'il n'y en a pas.
		if (nameSpace == null) {
			//TODO vérifier qu'est ce qui remplace !
			nameSpace = XMLConstants.XML_NS_URI;//.NULL_NS_URI;
		}
		if (prefixe == null) {
			prefixe = XMLConstants.DEFAULT_NS_PREFIX;
		}
		for (String suffixeUnitaire : suffixe.split("#")) {
			//System.out.println(suffixeUnitaire);
			// On crée le nom qualifié pour l'élément rechercher.
			QName cible;
			
			if (prefixe == XMLConstants.DEFAULT_NS_PREFIX) {
				cible = new QName(nameSpace, suffixeUnitaire.split("@")[0]);
			} else {
				cible = new QName(nameSpace, suffixeUnitaire.split("@")[0], prefixe);
			}
			
			if (temp == null) {
				temp = xmlComplet.selectChildren(cible);
			} else if (temp.length > 0){
				if (suffixeUnitaire.split("@").length > 1) {
					temp = temp[Integer.parseInt(suffixeUnitaire.split("@")[1])].selectChildren(cible);
				} else if (all == false) {
					// Si on ne doit pas parcourir tout alors on prend le premier fils.
					temp = temp[0].selectChildren(cible);
				} else {
					// On vide la liste.
					tempListe = new LinkedList<XmlObject>();
					// On parcours l'ensemble des composants XML compatible avec la cible.
					for (int compteur = 0; compteur < temp.length; compteur++) {
						for (XmlObject objet : temp[compteur].selectChildren(cible)) {
							tempListe.add(objet);
						}
					}
					temp = tempListe.toArray(new XmlObject[tempListe.size()]);
				}
				//System.out.println("Taille de temp : " + temp.length);
			}
		}
		
		return temp;
	}
	
	
	/**
	 * Récupère la liste d'objets xml répondant aux critères.
	 * @param xmlComplet le xml complet (ou l'objet xml) à analyser.
	 * @param cibleRecherche la cible sous forme prefixe:elemenprefixe:souselement@positionDansElement (ex : a:entry#b:div#ul@1 signifie les ul de la première div de l'entry)
	 * @return les objets issus de la recherche.
	 */
	public static XmlObject[] obtenirElementsXml(XmlObject xmlComplet, String cibleRecherche) {
		return obtenirElementsXml(xmlComplet, cibleRecherche, null, true);
	}
	
	/**
	 * Récupère la liste d'objets xml répondant aux critères.
	 * @param xmlComplet le xml complet (ou l'objet xml) à analyser.
	 * @param cibleRecherche la cible sous forme prefixe:elemenprefixe:souselement@positionDansElement (ex : a:entry#b:div#ul@1 signifie les ul de la première div de l'entry)
	 * @param nameSpace le ou les namespaces associé séparé par des # (ex :http://www.bpce.fr/xsd/vcc/foyer-3/response#http://www.bpce.fr/xsd/vcc/vcc-3). Si le namespace est null on essaye de le trouver automatiquement.
	 * @param all si vrai alors on veux tous les éléments associés. Si non juste le premier.
	 * @return les objets issus de la recherche.
	 */
	public static XmlObject[] obtenirElementsXml(XmlObject xmlComplet, String cibleRecherche, String nameSpace, boolean all) {
		
		//System.out.println("On cherche à obtenir " + cibleRecherche + " pour " + nameSpace);
		
		XmlObject[] temp = null;
		List<XmlObject> tempListe = new LinkedList<XmlObject>();

		// On découpe les listes de valeurs.
		String[] nameSpaces = nameSpace!=null?nameSpace.split("#"):null;
		String[] cibles = cibleRecherche.split("#");
		int compteurNs = -1;
		
		// On doit avoir une correspondance entre les nameSpaces et les cibles. Ou alors le namespace est communs aux cibles.
		// Si la valeur namespace est null, alors on garde à vide et on affectera dynamiquement la valeur.
		if (nameSpaces != null && nameSpaces.length != 1) {
			compteurNs = 0;
			if (nameSpaces.length != cibles.length) {
				//System.out.println("Les nombres de namespaces et de cibles ne sont pas cohérents");
				return temp; 
			}
		}
		
		for (String suffixeUnitaire : cibles) {
			//System.out.println(suffixeUnitaire);

			// Si un préfixe est spécifier alors on l'extrait, si non on prend l'entièreté du suffixe.
			String prefixe = XMLConstants.DEFAULT_NS_PREFIX;
			String[] composantes = suffixeUnitaire.split(":");
			if (composantes.length > 1) {
				prefixe = composantes[0];
				suffixeUnitaire = composantes[1];
			}
			// En absence de namespace on suppose qu'on prend celui par défaut.
			String nameSpaceUnitaire = nameSpace;
			// Si une liste à été définie on prend le namespace associé à cet élément. Si il n'en existe qu'un, on prend celui ci pour tous les éléments.
			if (compteurNs>=0) {
				nameSpaceUnitaire = nameSpaces[compteurNs];
				compteurNs++;
			}
			// En absence de namespace on suppose qu'on prend celui par défaut du DOM.
			if (nameSpaceUnitaire == null) {
					nameSpaceUnitaire = obtenirNamespacePrefixe(xmlComplet, prefixe);
			}
			
			// Si après avoir analyser les paramètres et avoir regarder le namespace par défaut on est toujours à vide, on met la valeur par défaut.
			if (nameSpaceUnitaire == null) {
				nameSpaceUnitaire = XMLConstants.DEFAULT_NS_PREFIX;//NULL_NS_URI;
			}

			//System.out.println("Unitaire " + nameSpaceUnitaire + " , " + prefixe + ":" + suffixeUnitaire);
			
			// On crée le nom qualifié pour l'élément rechercher.
			QName cible;
			if (prefixe == XMLConstants.DEFAULT_NS_PREFIX) {
				cible = new QName(nameSpaceUnitaire, suffixeUnitaire.split("@")[0]);
			} else {
				cible = new QName(nameSpaceUnitaire, suffixeUnitaire.split("@")[0], prefixe);
			}
			
			if (temp == null) {
				temp = xmlComplet.selectChildren(cible);
			} else if (temp.length > 0){
				if (suffixeUnitaire.split("@").length > 1) {
					temp = temp[Integer.parseInt(suffixeUnitaire.split("@")[1])].selectChildren(cible);
				} else if (all == false) {
					// Si on ne doit pas parcourir tout alors on prend le premier fils.
					temp = temp[0].selectChildren(cible);
				} else {
					// On vide la liste.
					tempListe = new LinkedList<XmlObject>();
					// On parcours l'ensemble des composants XML compatible avec la cible.
					for (int compteur = 0; compteur < temp.length; compteur++) {
						for (XmlObject objet : temp[compteur].selectChildren(cible)) {
							tempListe.add(objet);
						}
					}
					temp = tempListe.toArray(new XmlObject[tempListe.size()]);
				}
				//System.out.println("Taille de temp : " + temp.length);
			}
		}
		
		return temp;
	}
	
	/**
	 * Extrait la valeur associée à un attribut pour un objet xml donné;
	 * @param objet l'objet xml.
	 * @param attribut le nom de l'attribut.
	 * @return la valeur associée à l'attribut xml demandé.
	 */
	public static String obtenirAttribut(XmlObject objet, String attribut) {
		Node temp = objet.getDomNode().getAttributes().getNamedItem(attribut);
		if (temp != null) {
			return objet.getDomNode().getAttributes().getNamedItem(attribut).getNodeValue();
		} else {
			return "";
		}
	}
	
	/**
	 * Permet d'obtenir l'identifiant du champ XML associé à une balise XML.
	 * @param classe_annotation l'annotation.
	 * @return l'identifiant.
	 */
	public static String obtenirIdentifiant(BaliseXml classe_annotation) {
		String retour = "";	
		if(classe_annotation != null) {
			retour = classe_annotation.nom();
			if (classe_annotation.prefixe() != null && !"".equals(classe_annotation.prefixe())) {
				retour = classe_annotation.prefixe() + ":" + classe_annotation.nom();
			}	
		}
		return retour;
	}
	
	/**
	 * Permet de renseigner une instance annotée à partir du flux xml correspondant.
	 * @param instanceAnnotee l'instance à valorisée.
	 * @param xmlObj l'objet xml
	 * @return l'instance annotée après valorisation.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static Object toObject(Object instanceAnnotee, XmlObject xmlObj) throws SeleniumException {
		
		try {
			// On récupère l'annotation de la classe
			BaliseXml classe_annotation = instanceAnnotee.getClass().getAnnotation(BaliseXml.class);
		
			// On récupère les champs de la classe annotée
			for (Field champ : instanceAnnotee.getClass().getDeclaredFields()) {
	
				//System.out.println("On inspecte le champ : " + champ.getName());
				
				// On cherche l'annotation "Balise XML".
				BaliseXml annotation = champ.getAnnotation(BaliseXml.class);
				//String tempIdent = 
				
				// On effectue la transformation.
				if (annotation != null) {
					// Si le champ est complexe on visite le sous objet
					if (annotation.complexe()) {
						if (!annotation.multiple()) {
							//String tempIdent = classe_annotation!=null?obtenirIdentifiant(classe_annotation) + "#" + obtenirIdentifiant(annotation):obtenirIdentifiant(annotation);
							//System.out.println("Complexe " + obtenirIdentifiant(annotation) + " dans " + xmlObj.getDomNode().getNodeName());
							XmlObject[] tempObj = obtenirElementsXml(xmlObj, obtenirIdentifiant(annotation), null, false);
							if (tempObj.length > 0) {
								champ.set(instanceAnnotee, toObject(champ.get(instanceAnnotee), tempObj[0]));
							} else {
								champ.set(instanceAnnotee, null);
							}
						} else {
							// Si il s'agit d'une liste d'objets complexe, il faut extraire chacun d'entre eux et les ajouter à la liste.
							// On récupère les objets XML
							//String tempIdent = classe_annotation!=null?obtenirIdentifiant(classe_annotation) + "#" + obtenirIdentifiant(annotation):obtenirIdentifiant(annotation);
							//System.out.println("Multiple complexe " + obtenirIdentifiant(annotation) + " dans " + xmlObj.getDomNode().getNodeName());
							XmlObject[] tempListe = obtenirElementsXml(xmlObj, obtenirIdentifiant(annotation), null, true);
							// On récupère la liste "vide" et on s'assure qu'elle est effectivement vide
							List tempListeCible = (List) champ.get(instanceAnnotee);
							tempListeCible.clear();
							// Pour chacun objet XML on transforme en objet et on ajoute à la liste
							for(XmlObject objet : tempListe) {
								tempListeCible.add(toObject(((Class)((ParameterizedType)(champ.getGenericType())).getActualTypeArguments()[0]).newInstance(), objet));
							}
							champ.set(instanceAnnotee, tempListeCible);
						}			
					} else {
						// Sinon on récupère la valeur telle quelle comme contenu de balise
						if (!annotation.multiple()) {
							//String tempIdent = classe_annotation!=null?obtenirIdentifiant(classe_annotation) + "#" + obtenirIdentifiant(annotation):obtenirIdentifiant(annotation);
							//System.out.println("Simple " + obtenirIdentifiant(annotation) + " dans " + xmlObj.getDomNode().getNodeName());
							XmlObject[] tempObj = obtenirElementsXml(xmlObj, obtenirIdentifiant(annotation), null, false);
							if (tempObj.length > 0) {
								champ.set(instanceAnnotee, obtenirTexte(tempObj[0]));
							} else {
								champ.set(instanceAnnotee, null);
							}
						} else {
							//String tempIdent = classe_annotation!=null?obtenirIdentifiant(classe_annotation) + "#" + obtenirIdentifiant(annotation):obtenirIdentifiant(annotation);
							//System.out.println("Multiple simple " + obtenirIdentifiant(annotation) + " dans " + xmlObj.getDomNode().getNodeName());
							XmlObject[] tempListe = obtenirElementsXml(xmlObj, obtenirIdentifiant(annotation), null, true);
							List tempListeCible = (List) champ.get(instanceAnnotee);
							tempListeCible.clear();
							for(XmlObject objet : tempListe) {
								tempListeCible.add(obtenirTexte(objet));
							}
							champ.set(instanceAnnotee, tempListeCible);
						}
					}
				}
				
			}
			
		} catch (Exception erreur) {
			erreur.printStackTrace();
			System.out.println("L'objet n'est pas correctement balisé pour la lecture");
			System.exit(0);
		}
		return instanceAnnotee;
	}
	
	/**
	 * Permet de former le contenu d'un fichier XML à partir d'un objet portant des annotations BaliseXML.
	 * @param instanceAnnotee l'instance annotées avec BaliseXML
	 * @return le contenu du fichier XML généré.
	 */
		public static String toXml(Object instanceAnnotee) {
			String retour = new String();
			try {
				// On récupère l'annotation de la classe
				BaliseXml classe_annotation = instanceAnnotee.getClass().getAnnotation(BaliseXml.class);
				
				if (classe_annotation != null && classe_annotation.balisable()) {
					if (!"".equals(classe_annotation.entete())) {
						retour = retour.concat(classe_annotation.entete() + "\n");
					}
					retour = retour.concat("<" + classe_annotation.prefixe() + ":" + classe_annotation.nom() + ">\n");
				}
				
				// On récupère les champs de la classe annotée
				for (Field champ : instanceAnnotee.getClass().getDeclaredFields()) {
		
					//System.out.println("On inspecte le champ : " + champ.getName());
					
					// On cherche l'annotation "Balise XML".
					BaliseXml annotation = champ.getAnnotation(BaliseXml.class);
					
					// On effectue la transformation.
					if (annotation != null && annotation.balisable()) {
						// Si le champ est complexe on visite le sous objet
						if (annotation.complexe()) {
							if (!annotation.multiple()) {
								retour = retour.concat("<" + annotation.prefixe() + ":" + annotation.nom() + ">\n");
								retour = retour.concat(toXml(champ.get(instanceAnnotee)));
								retour = retour.concat("</" + annotation.prefixe() + ":" + annotation.nom() + ">\n");	
							} else {
								for(Object objet : (List) champ.get(instanceAnnotee)) {
									retour = retour.concat("<" + annotation.prefixe() + ":" + annotation.nom() + ">\n");
									retour = retour.concat(toXml(objet));
									retour = retour.concat("</" + annotation.prefixe() + ":" + annotation.nom() + ">\n");	
								}
							}			
						} else {
							// Sinon on récupère la valeur telle quelle comme contenu de balise
							if (!annotation.multiple()) {
								Object valeur = champ.get(instanceAnnotee);
								retour = retour.concat("<" + annotation.prefixe() + ":" + annotation.nom() + ">" + ((valeur == null)?"":valeur) + "</" + annotation.prefixe() + ":" + annotation.nom() +">\n");
							} else {
								for(Object objet : (List) champ.get(instanceAnnotee)) {
									retour = retour.concat(toXml(objet));
								}
							}
						}
					}
					
				}
				// Si la classe est annotée on ferme la balise principale
				if (classe_annotation != null && classe_annotation.balisable()) {
					retour = retour.concat("</" + classe_annotation.prefixe() + ":" + classe_annotation.nom() + ">\n");
					if (!"".equals(classe_annotation.enqueue())) {
						retour = retour.concat(classe_annotation.enqueue());
					}
				}
				
			//} catch (NoSuchMethodException e1, SecurityException e2, IllegalAccessException e3, IllegalArgumentException e4, InvocationTargetException e5, NoSuchFieldException e6) {
			} catch (Exception erreur) {
				System.out.println("L'objet n'est pas correctement balisé");
			}
			return retour;

		}
		
		 /**
		  * Permet d'objtenir un objet XML le namespace associé à un préfixe donné.
		  * @param xml l'objet XML à manipuler.
		  * @param prefixe le préfixe pour lequel on cherche le namespace.
		  * @return le namespace associé au préfixe demandé.
		  */
		private static String obtenirPrefixe(XmlObject xml, String suffixe) {
			NodeList liste = xml.getDomNode().getChildNodes();
			String retour = null;
			for (int i = 0; i < liste.getLength(); i++) {
				Node node = liste.item(i);
				if (node.getNodeType() != Node.TEXT_NODE) {
					retour = obtenirPrefixe((Element) node, suffixe);
				} 
				if (retour != null) {
					return retour;
				}
			}
			return retour;
		}
		
		 /**
		  * Permet d'objtenir un Element XML le namespace associé à un préfixe donné.
		  * @param element l'Element XML à manipuler.
		  * @param prefixe le préfixe pour lequel on cherche le namespace.
		  * @return le namespace associé au préfixe demandé.
		  */
		 public static String obtenirPrefixe(Element element, String suffixe) {
			 String retour = null;
			
			 // L'élement est peu être porteur du préfixe
			 if (suffixe.equals(element.getLocalName())) {
				 if (element.getNodeType() != Node.TEXT_NODE) {
					 return element.getPrefix();
				 }
			 }
			 // Sinon on évolue parmi les sous éléments
		    NodeList list = element.getChildNodes();
		    for(int i = 0; i < list.getLength(); i++) {
		      Node node = list.item(i);
		      if (suffixe.equals(node.getLocalName())) {
		    	  retour = node.getPrefix();
		      } else if (node.getNodeType() != Node.TEXT_NODE) {
		    	  retour = obtenirPrefixe((Element) node, suffixe);
		      }
		      
		      if (retour != null) {
		    	   break;
		      }
		    }
		    return retour;
		  }
		
		 /**
		  * Permet d'objtenir un objet XML le namespace associé à un préfixe donné.
		  * @param xml l'objet XML à manipuler.
		  * @param prefixe le préfixe pour lequel on cherche le namespace.
		  * @return le namespace associé au préfixe demandé.
		  */
		private static String obtenirNamespacePrefixe(XmlObject xml, String prefixe) {
			NodeList liste = xml.getDomNode().getChildNodes();
			String retour = null;
			for (int i = 0; i < liste.getLength(); i++) {
				Node node = liste.item(i);
				if (node.getNodeType() != Node.TEXT_NODE) {
					retour = obtenirNamespacePrefixe((Element) node, prefixe);
				} 
				// Si on à trouver le bon namespace associé au node, on le renvoie.
				if (retour != null) {
					return retour;
				}
			}
			return retour;
		}
		
		 /**
		  * Permet d'objtenir un Element XML le namespace associé à un préfixe donné.
		  * @param element l'Element XML à manipuler.
		  * @param prefixe le préfixe pour lequel on cherche le namespace.
		  * @return le namespace associé au préfixe demandé.
		  */
		 public static String obtenirNamespacePrefixe(Element element, String prefixe) {
			 String retour = null;
			
			 // L'élement est peu être porteur du préfixe
			 if (prefixe.equals(element.getPrefix())) {
				 if (element.getNodeType() != Node.TEXT_NODE) {
					 return element.getNamespaceURI();
				 }
			 }
			 // Sinon on évolue parmi les sous éléments
		    NodeList list = element.getChildNodes();
		    for(int i = 0; i < list.getLength(); i++) {
		      Node node = list.item(i);
		      short typeNode = node.getNodeType();
		      String prefixeNode = node.getPrefix();
		      //typeNode != Node.TEXT_NODE && 
		      if (prefixe.equals(prefixeNode)) {
//				      System.out.println("-->Type du node : " + typeNode);
//						System.out.println("-->Nom :" + node.getNodeName());
//						System.out.println("-->Préfixe :" + node.getPrefix());
//						System.out.println("-->Valeur : " + node.getNodeValue());
//			    	  System.out.println("-->Namespace : " + node.getNamespaceURI() + " pour " + prefixe);
		    	  retour = node.getNamespaceURI();
		      } else if (typeNode != Node.TEXT_NODE) {
		    	  retour = obtenirNamespacePrefixe((Element) node, prefixe);
		      }
		      
		      if (retour != null) {
		    	   break;
		      }
		    }
		    return retour;
		  }

		 /**
		  * Permet d'obtenir le texte contenu dans un objet XML.
		  * @param xml l'objet XML à manipuler.
		  * @return le texte contenu dans l'objet XML
		  */
		private static String obtenirTexte(XmlObject xml) {
			NodeList liste = xml.getDomNode().getChildNodes();
			String retour = "";
			for (int i = 0; i < liste.getLength(); i++) {
				Node node = liste.item(i);
				if(node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
					retour = node.getNodeValue();
				} else {
					retour = retour.concat(obtenirTexte((Element) node));
				}
				if (retour != null) {
					break;
				}
			}
			//System.out.println("TEXT : " + retour);
			return retour;
		}
		 
		/**
		 * Permet d'objet la chaine concaténée des éléments "fils" de type texte de l'élément paramètre. 
		 * @param element l'élément pour lequel on cherche le texte.
		 * @return la chaine concaténée des éléments "fils" de l'élément.
		 */
		 public static String obtenirTexte(Element element) {
		    StringBuilder builder = new StringBuilder();
		    NodeList list = element.getChildNodes();
		    for(int i = 0; i < list.getLength(); i++) {
		      Node node = list.item(i);
		      short type = node.getNodeType();
		      if(type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
		    	  builder = builder.append(node.getNodeValue());
		      }
		    }
		    //System.out.println("TEXT : " + builder.toString());
		    return builder.toString();
		  }

		 
		 
		public static void main(String[] argv) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
			//System.out.println(new Character((char)9702));	
			//System.out.println(XMLOutils.entityToHtml("&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;&lt;svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"600\" height=\"400\"&gt;&lt;title&gt;1A20121218006&lt;/title&gt;&lt;desc&gt;2012-12-18T14:49:11Z&lt;/desc&gt;&lt;rect width=\"600\" height=\"400\" x=\"0\" y=\"0\" fill=\"#F1E9D5\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M263.84793,253.21875 Q263.84793,253.21875 259.14377,250.52292 Q254.43959,247.82707 252.3,246.59166 Q250.16042,245.35625 247.80624,243.7854 Q245.45209,242.21457 243.31459,240.75415 Q241.1771,239.29375 238.61041,237.72292 Q236.04375,236.15208 233.47708,234.57916 Q230.91042,233.00626 228.12918,231.21042 Q225.34793,229.41458 222.78125,227.61874 Q220.21458,225.8229 217.64792,223.68958 Q215.08125,221.55624 213.15625,219.64792 Q211.23126,217.73958 209.51877,215.83124 Q207.80626,213.92291 205.45416,211.56458 Q203.10208,209.20625 200.96251,206.84793 Q198.82294,204.4896 197.32501,202.01875 Q195.82709,199.54791 194.54376,197.07916 Q193.26042,194.61041 192.40625,192.25208 Q191.5521,189.89375 190.48126,184.50418 Q189.41043,179.1146 188.98334,177.43126 Q188.55624,175.74792 188.34167,174.0625 Q188.12709,172.37708 188.34167,170.69374 Q188.55624,169.0104 188.77083,167.21458 Q188.98541,165.41876 189.62709,163.62292 Q190.26877,161.82709 191.33752,160.14166 Q192.40627,158.45624 193.475,156.8854 Q194.54376,155.31458 196.25626,153.85416 Q197.96875,152.39374 200.74792,151.15833 Q203.52708,149.92291 206.09584,149.025 Q208.6646,148.12708 211.01668,147.56667 Q213.36876,147.00626 215.72084,146.89375 Q218.07292,146.78125 220.4271,146.89375 Q222.78127,147.00626 225.13333,147.34167 Q227.48541,147.6771 229.1979,148.2396 Q230.91042,148.8021 232.40625,149.3625 Q233.9021,149.92291 234.75835,150.70833 Q235.6146,151.49374 236.47084,152.39374 Q237.32709,153.29375 237.96875,154.75208 Q238.61043,156.21042 238.82292,157.89583 Q239.03542,159.58124 239.03542,161.26457 Q239.03542,162.9479 238.82292,164.63124 Q238.61043,166.31458 238.61043,167.775 Q238.61043,169.23541 238.39584,170.47083 Q238.18126,171.70625 238.18126,172.71667 Q238.18126,173.72708 238.18126,174.5125 Q238.18126,175.29791 238.39584,176.30833 Q238.61043,177.31876 239.25209,177.20625 Q239.89375,177.09375 240.74792,176.64583 Q241.60208,176.1979 242.45834,175.52292 Q243.31459,174.84792 244.17084,174.0625 Q245.02708,173.27708 245.88126,172.37918 Q246.73543,171.48126 247.59167,170.35834 Q248.44792,169.23541 249.73126,168.0 Q251.01459,166.76459 252.29791,165.41667 Q253.58125,164.06876 255.50626,162.49792 Q257.43127,160.9271 258.9292,159.57918 Q260.4271,158.23126 261.925,156.88542 Q263.4229,155.53958 265.3479,154.30417 Q267.27292,153.06876 268.98334,152.05833 Q270.69376,151.04791 272.61877,150.15 Q274.54376,149.25208 276.89792,148.91458 Q279.25208,148.57709 281.3896,148.46458 Q283.5271,148.35208 286.30835,148.57709 Q289.0896,148.8021 291.44376,149.3625 Q293.7979,149.92291 296.15,150.82083 Q298.50208,151.71875 300.64166,153.06667 Q302.78125,154.41458 304.91876,155.9875 Q307.05624,157.56042 308.76874,159.35626 Q310.48126,161.15208 311.55002,162.83542 Q312.61877,164.51875 313.475,166.4271 Q314.33124,168.33542 314.54584,170.13333 Q314.7604,171.93124 314.54584,173.72708 Q314.33124,175.52292 314.11667,177.20625 Q313.9021,178.88959 313.0479,180.575 Q312.19376,182.2604 311.33752,183.71875 Q310.48126,185.1771 309.19794,186.63751 Q307.91458,188.09792 306.4167,189.44583 Q304.91876,190.79376 303.42084,192.02708 Q301.9229,193.2604 299.7854,194.60832 Q297.6479,195.95624 295.50833,197.30417 Q293.36877,198.65208 291.2292,200.0 Q289.0896,201.34792 287.1646,202.69376 Q285.2396,204.03958 283.31458,205.1625 Q281.3896,206.28542 279.67917,207.40833 Q277.96875,208.53125 276.04376,209.65417 Q274.11877,210.77708 272.40625,212.0125 Q270.69376,213.24791 269.4104,214.48334 Q268.12708,215.71875 266.84375,216.84167 Q265.56042,217.96458 264.49167,219.08542 Q263.4229,220.20625 262.56665,221.21875 Q261.71042,222.23125 261.28333,223.24167 Q260.85626,224.25208 260.4271,225.15 Q259.99792,226.04791 259.78543,226.94583 Q259.57294,227.84375 259.35834,228.74167 Q259.14374,229.63957 259.14374,230.53749 Q259.14374,231.43541 259.14374,232.22083 Q259.14374,233.00626 259.14374,233.79376 Q259.14374,234.58125 259.35834,235.25417 Q259.57294,235.92708 259.78543,236.6 Q259.99792,237.27292 260.6396,238.39584 Q261.28125,239.51875 261.71042,240.75417 Q262.1396,241.98958 262.35208,242.66249 Q262.56458,243.3354 262.99374,244.34583 Q263.4229,245.35625 263.63544,246.03125 Q263.84793,246.70625 263.84793,247.60417 Q263.84793,248.50209 263.84793,249.4 Q263.84793,250.29791 263.84793,250.29791\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M289.94794,235.25208 Q289.94794,235.25208 289.73334,236.375 Q289.51874,237.49792 289.73334,238.84584 Q289.94794,240.19376 290.16043,241.31667 Q290.37292,242.43958 290.37292,243.67499 Q290.37292,244.91042 290.37292,245.92084 Q290.37292,246.93124 290.37292,247.71667 Q290.37292,248.50209 290.58752,250.86041 Q290.8021,253.21875 290.8021,253.21875\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M298.93124,228.96458 Q298.93124,228.96458 297.64792,228.96458 Q296.3646,228.96458 294.8667,229.30208 Q293.36877,229.63957 291.87085,229.86458 Q290.37292,230.08958 288.6625,230.5375 Q286.9521,230.98541 285.2396,231.54791 Q283.5271,232.11041 281.6021,232.78333 Q279.6771,233.45625 277.96667,234.12917 Q276.25626,234.80208 274.75836,235.36458 Q273.26044,235.92708 272.40625,236.37708 Q271.5521,236.82707 270.9104,237.1625 Q270.26874,237.49792 269.8396,238.39584 Q269.41043,239.29375 269.41043,239.29375\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M301.06876,232.56041 Q301.06876,232.56041 301.925,233.79373 Q302.78125,235.02707 302.99374,236.15 Q303.20624,237.27292 303.20624,237.9479 Q303.20624,238.62291 303.20624,239.63333 Q303.20624,240.64375 302.35208,241.09167 Q301.49792,241.53958 300.85626,240.75417 Q300.2146,239.96875 300.64166,238.62083 Q301.06876,237.27292 301.49585,236.15 Q301.9229,235.02707 302.56458,234.01666 Q303.20624,233.00626 303.8479,232.22083 Q304.4896,231.43541 305.34583,230.875 Q306.2021,230.31458 307.05835,229.97708 Q307.91458,229.63957 308.55627,229.52707 Q309.19794,229.41458 309.8396,229.41458 Q310.48126,229.41458 311.33752,229.97708 Q312.19376,230.53958 312.62085,231.66042 Q313.0479,232.78125 313.90417,233.56876 Q314.7604,234.35625 315.82916,235.14166 Q316.89792,235.92708 318.18127,236.7125 Q319.4646,237.49792 320.96252,238.28333 Q322.46045,239.06874 324.38544,241.42708 Q326.31042,243.78542 326.31042,243.78542\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M328.01874,230.7604 Q328.01874,230.7604 328.4479,233.34375 Q328.87708,235.92708 328.87708,236.6 Q328.87708,237.27292 328.87708,238.28333 Q328.87708,239.29375 328.0208,238.17084 Q327.16458,237.04791 327.37915,235.47708 Q327.59375,233.90625 327.59375,232.22292 Q327.59375,230.53958 327.80624,228.96667 Q328.01874,227.39375 328.23334,226.04584 Q328.44794,224.69792 328.8771,223.6875 Q329.30627,222.67708 329.94794,222.11667 Q330.5896,221.55624 331.44376,221.44374 Q332.2979,221.33124 333.36874,221.89166 Q334.43958,222.45207 335.50833,223.4625 Q336.5771,224.47292 337.64584,225.59583 Q338.7146,226.71875 339.78543,227.95416 Q340.85626,229.18959 341.49792,230.425 Q342.1396,231.66042 342.99374,232.78333 Q343.8479,233.90625 344.91873,237.3875 Q345.98956,240.86874 345.98956,240.86874\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M343.8479,238.62291 Q343.8479,238.62291 343.20624,238.5104 Q342.56458,238.39792 341.28125,238.62292 Q339.99792,238.84792 338.92917,239.07083 Q337.8604,239.29375 334.86667,239.74374 Q331.87292,240.19376 330.8021,240.41876 Q329.73126,240.64375 328.875,241.20416 Q328.01874,241.76459 328.01874,241.76459\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M355.4021,215.26875 Q355.4021,215.26875 354.11874,214.93124 Q352.8354,214.59375 351.97916,215.37917 Q351.12292,216.16458 350.48126,216.95209 Q349.8396,217.73958 349.4125,219.1979 Q348.9854,220.65625 349.4125,222.34167 Q349.8396,224.02708 350.2667,225.71042 Q350.6938,227.39375 351.33542,229.07709 Q351.97705,230.7604 353.0479,232.33333 Q354.11877,233.90625 355.8292,235.14166 Q357.5396,236.37709 359.0375,237.275 Q360.5354,238.17291 362.03125,238.73334 Q363.5271,239.29375 365.02502,239.63126 Q366.52295,239.96875 368.44794,240.75417 Q370.37292,241.53958 370.37292,241.53958\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M373.7979,221.33124 Q373.7979,221.33124 373.7979,222.22916 Q373.7979,223.12708 373.58334,223.9125 Q373.36877,224.69792 372.94168,225.93333 Q372.5146,227.16875 371.65833,229.86458 Q370.80206,232.56041 370.375,235.14166 Q369.94794,237.72292 370.375,239.51875 Q370.80206,241.31459 372.94165,241.87708 Q375.08124,242.43958 377.64792,242.10208 Q380.2146,241.76459 381.28336,241.53958 Q382.3521,241.31459 383.20834,241.09167 Q384.06458,240.86874 385.13333,239.85834 Q386.2021,238.84792 386.2021,238.84792\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M384.06458,232.33542 Q384.06458,232.33542 382.99374,232.55833 Q381.9229,232.78125 381.28125,232.78125 Q380.6396,232.78125 379.78543,232.78125 Q378.93127,232.78125 376.5771,232.33334 Q374.2229,231.88542 373.15414,231.1 Q372.0854,230.31458 372.0854,230.31458\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M376.78955,209.43124 Q376.78955,209.43124 376.3625,210.88959 Q375.93542,212.34792 374.6521,213.35834 Q373.36877,214.36874 372.72708,214.70624 Q372.0854,215.04375 371.22916,215.82916 Q370.37292,216.6146 370.37292,216.6146\" /&gt;&lt;path fill=\"none\" stroke=\"black\" stroke-width=\"3\" d=\"M391.7646,202.01875 Q391.7646,202.01875 392.62085,201.90625 Q393.4771,201.79376 394.76044,202.13126 Q396.04376,202.46875 400.10626,205.1625 Q404.16876,207.85625 407.37918,212.5729 Q410.5896,217.28958 411.23126,219.7604 Q411.87292,222.23125 410.8021,226.16042 Q409.73126,230.08958 406.09586,231.77292 Q402.46045,233.45625 399.89377,232.78333 Q397.3271,232.11041 395.6146,231.1 Q393.9021,230.08958 392.40625,228.29166 Q390.9104,226.49374 390.05417,224.1375 Q389.19794,221.78125 388.98334,219.53542 Q388.76874,217.28958 388.98334,215.38126 Q389.19794,213.47292 390.05417,212.125 Q390.9104,210.77708 391.97916,206.5104 Q393.0479,202.24374 393.0479,202.24374\" /&gt;&lt;/svg&gt;&lt;hash&gt;52823066668a695c5a76331a9807e3aa47e35f49&lt;/hash&gt;"));
		
			String contenu = "";
			try {
				InputStream flux=new FileInputStream("C:\\Temp\\test.xml"); 
				InputStreamReader lecture=new InputStreamReader(flux);
				BufferedReader buff=new BufferedReader(lecture);
				String ligne;
				while ((ligne=buff.readLine())!=null){
					contenu = contenu.concat(ligne);
				}
				buff.close(); 
				
				XmlObject xml = obtenirXml(contenu);
				
				System.out.println(obtenirNamespacePrefixe(xml, "_3_3"));
				System.out.println(obtenirPrefixe(xml, "IdntClntDistr"));

				//XmlObject temp = obtenirElementsXml(xml, "soapenv:Envelope#soapenv:Body#_1:obtenirFoyerAvecRessourcesEtChargesResponse#re:Foyer#_3_3:PersPhys", null, true)[0];
				//System.out.println(contenuTexte(temp));
				
//				XmlObject temp2 = obtenirElementsXml(temp, "_3_3:IdntClntDistr", null, false)[0];
//				System.out.println(contenuTexte(temp2));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SeleniumException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			
		}

		/**
		 * Utilise les code caractères pour remplacer les entités HTML du paramètre .
		 * @param string la chaine de caractères contenant ls entités HTML.
		 * @return la conversion de la chaine passée en paramètre.
		 */
		public static String entityToHtml(String string) {
			
			HashMap<Integer, String> entityTable = new LinkedHashMap<Integer, String>();
			
			entityTable.put(38, "&amp;");     // Quotation mark. Not required     
			entityTable.put(34, "&quot;");     // Quotation mark. Not required     
			entityTable.put(60, "&lt;");    // Less-than sign
			entityTable.put(62, "&gt;");     // Greater-than sign
//			  // 63, "&#63;");      // Question mark
//			  // 111, "&#111;");        // Latin small letter o
			entityTable.put(160, "&nbsp;");      // Non-breaking space
			entityTable.put(161, "&iexcl;");     // Inverted exclamation mark
			entityTable.put(162, "&cent;");      // Cent sign
			entityTable.put(163, "&pound;");     // Pound sign
			entityTable.put(164, "&curren;");    // Currency sign
			entityTable.put(165, "&yen;");       // Yen sign
			entityTable.put(166, "&brvbar;");    // Broken vertical bar
			entityTable.put(167, "&sect;");      // Section sign
			entityTable.put(168, "&uml;");       // Diaeresis
			entityTable.put(169, "&copy;");      // Copyright sign
			entityTable.put(170, "&ordf;");      // Feminine ordinal indicator
			entityTable.put(171, "&laquo;");     // Left-pointing double angle quotation mark
			entityTable.put(172, "&not;");       // Not sign
			entityTable.put(173, "&shy;");       // Soft hyphen
//			  174, "&reg;");       // Registered sign
			entityTable.put(175, "&macr;");      // Macron
			entityTable.put(176, "&deg;");       // Degree sign
			entityTable.put(177, "&plusmn;");    // Plus-minus sign
			entityTable.put(178, "&sup2;");      // Superscript two
			entityTable.put(179, "&sup3;");      // Superscript three
			entityTable.put(180, "&acute;");     // Acute accent
			entityTable.put(181, "&micro;");     // Micro sign
			entityTable.put(182, "&para;");      // Pilcrow sign
			entityTable.put(183, "&middot;");    // Middle dot
			entityTable.put(184, "&cedil;");     // Cedilla
			entityTable.put(185, "&sup1;");      // Superscript one
			entityTable.put(186, "&ordm;");      // Masculine ordinal indicator
			entityTable.put(187, "&raquo;");     // Right-pointing double angle quotation mark
			entityTable.put(188, "&frac14;");    // Vulgar fraction one-quarter
			entityTable.put(189, "&frac12;");    // Vulgar fraction one-half
			entityTable.put(190, "&frac34;");    // Vulgar fraction three-quarters
			entityTable.put(191, "&iquest;");    // Inverted question mark
			entityTable.put(192, "&Agrave;");    // A with grave
			entityTable.put(193, "&Aacute;");    // A with acute
			entityTable.put(194, "&Acirc;");     // A with circumflex
			entityTable.put(195, "&Atilde;");    // A with tilde
			entityTable.put(196, "&Auml;");      // A with diaeresis
			entityTable.put(197, "&Aring;");     // A with ring above
			entityTable.put(198, "&AElig;");     // AE
			entityTable.put(199, "&Ccedil;");    // C with cedilla
			entityTable.put(200, "&Egrave;");    // E with grave
			entityTable.put(201, "&Eacute;");    // E with acute
			entityTable.put(202, "&Ecirc;");     // E with circumflex
			entityTable.put(203, "&Euml;");      // E with diaeresis
			entityTable.put(204, "&Igrave;");    // I with grave
			entityTable.put(205, "&Iacute;");    // I with acute
			entityTable.put(206, "&Icirc;");     // I with circumflex
			entityTable.put(207, "&Iuml;");      // I with diaeresis
			entityTable.put(208, "&ETH;");       // Eth
			entityTable.put(209, "&Ntilde;");    // N with tilde
			entityTable.put(210, "&Ograve;");    // O with grave
			entityTable.put(211, "&Oacute;");    // O with acute
			entityTable.put(212, "&Ocirc;");     // O with circumflex
			entityTable.put(213, "&Otilde;");    // O with tilde
			entityTable.put(214, "&Ouml;");      // O with diaeresis
			entityTable.put(215, "&times;");     // Multiplication sign
			entityTable.put(216, "&Oslash;");    // O with stroke
			entityTable.put(217, "&Ugrave;");    // U with grave
			entityTable.put(218, "&Uacute;");    // U with acute
			entityTable.put(219, "&Ucirc;");     // U with circumflex
			entityTable.put(220, "&Uuml;");      // U with diaeresis
			entityTable.put(221, "&Yacute;");    // Y with acute
			entityTable.put(222, "&THORN;");     // Thorn
			entityTable.put(223, "&szlig;");     // Sharp s. Also known as ess-zed
			entityTable.put(224, "&agrave;");    // a with grave
			entityTable.put(225, "&aacute;");    // a with acute
			entityTable.put(226, "&acirc;");     // a with circumflex
			entityTable.put(227, "&atilde;");    // a with tilde
			entityTable.put(228, "&auml;");      // a with diaeresis
			entityTable.put(229, "&aring;");     // a with ring above
			entityTable.put(230, "&aelig;");     // ae. Also known as ligature ae
			entityTable.put(231, "&ccedil;");    // c with cedilla
			entityTable.put(232, "&egrave;");    // e with grave
			entityTable.put(233, "&eacute;");    // e with acute
			entityTable.put(234, "&ecirc;");     // e with circumflex
			entityTable.put(235, "&euml;");      // e with diaeresis
			entityTable.put(236, "&igrave;");    // i with grave
			entityTable.put(237, "&iacute;");    // i with acute
			entityTable.put(238, "&icirc;");     // i with circumflex
			entityTable.put(239, "&iuml;");      // i with diaeresis
			entityTable.put(240, "&eth;");       // eth
			entityTable.put(241, "&ntilde;");    // n with tilde
			entityTable.put(242, "&ograve;");    // o with grave
			entityTable.put(243, "&oacute;");    // o with acute
			entityTable.put(244, "&ocirc;");     // o with circumflex
			entityTable.put(245, "&otilde;");    // o with tilde
			entityTable.put(246, "&ouml;");      // o with diaeresis
			entityTable.put(247, "&divide;");    // Division sign
			entityTable.put(248, "&oslash;");    // o with stroke. Also known as o with slash
			entityTable.put(249, "&ugrave;");    // u with grave
			entityTable.put(250, "&uacute;");    // u with acute
			entityTable.put(251, "&ucirc;");     // u with circumflex
			entityTable.put(252, "&uuml;");      // u with diaeresis
			entityTable.put(253, "&yacute;");    // y with acute
			entityTable.put(254, "&thorn;");     // thorn
			entityTable.put(255, "&yuml;");      // y with diaeresis
			entityTable.put(264, "&#264;");      // Latin capital letter C with circumflex
			entityTable.put(265, "&#265;");      // Latin small letter c with circumflex
			entityTable.put(338, "&OElig;");     // Latin capital ligature OE
			entityTable.put(339, "&oelig;");     // Latin small ligature oe
			entityTable.put(352, "&Scaron;");    // Latin capital letter S with caron
			entityTable.put(353, "&scaron;");    // Latin small letter s with caron
			entityTable.put(372, "&#372;");      // Latin capital letter W with circumflex
			entityTable.put(373, "&#373;");      // Latin small letter w with circumflex
			entityTable.put(374, "&#374;");      // Latin capital letter Y with circumflex
			entityTable.put(375, "&#375;");      // Latin small letter y with circumflex
			entityTable.put(376, "&Yuml;");      // Latin capital letter Y with diaeresis
			entityTable.put(402, "&fnof;");      // Latin small f with hook, function, florin
			entityTable.put(710, "&circ;");      // Modifier letter circumflex accent
			entityTable.put(732, "&tilde;");     // Small tilde
			entityTable.put(913, "&Alpha;");     // Alpha
			entityTable.put(914, "&Beta;");      // Beta
			entityTable.put(915, "&Gamma;");     // Gamma
			entityTable.put(916, "&Delta;");     // Delta
			entityTable.put(917, "&Epsilon;");   // Epsilon
			entityTable.put(918, "&Zeta;");      // Zeta
			entityTable.put(919, "&Eta;");       // Eta
			entityTable.put(920, "&Theta;");     // Theta
			entityTable.put(921, "&Iota;");      // Iota
			entityTable.put(922, "&Kappa;");     // Kappa
			entityTable.put(923, "&Lambda;");    // Lambda
			entityTable.put(924, "&Mu;");        // Mu
			entityTable.put(925, "&Nu;");        // Nu
			entityTable.put(926, "&Xi;");        // Xi
			entityTable.put(927, "&Omicron;");   // Omicron
			entityTable.put(928, "&Pi;");        // Pi
			entityTable.put(929, "&Rho;");       // Rho
			entityTable.put(931, "&Sigma;");     // Sigma
			entityTable.put(932, "&Tau;");       // Tau
			entityTable.put(933, "&Upsilon;");   // Upsilon
			entityTable.put(934, "&Phi;");       // Phi
			entityTable.put(935, "&Chi;");       // Chi
			entityTable.put(936, "&Psi;");       // Psi
			entityTable.put(937, "&Omega;");     // Omega
			entityTable.put(945, "&alpha;");     // alpha
			entityTable.put(946, "&beta;");      // beta
			entityTable.put(947, "&gamma;");     // gamma
			entityTable.put(948, "&delta;");     // delta
			entityTable.put(949, "&epsilon;");   // epsilon
			entityTable.put(950, "&zeta;");      // zeta
			entityTable.put(951, "&eta;");       // eta
			entityTable.put(952, "&theta;");     // theta
			entityTable.put(953, "&iota;");      // iota
			entityTable.put(954, "&kappa;");     // kappa
			entityTable.put(955, "&lambda;");    // lambda
			entityTable.put(956, "&mu;");        // mu
			entityTable.put(957, "&nu;");        // nu
			entityTable.put(958, "&xi;");        // xi
			entityTable.put(959, "&omicron;");   // omicron
			entityTable.put(960, "&pi;");        // pi
			entityTable.put(961, "&rho;");       // rho
			entityTable.put(962, "&sigmaf;");    // sigmaf
			entityTable.put(963, "&sigma;");     // sigma
			entityTable.put(964, "&tau;");       // tau
			entityTable.put(965, "&upsilon;");   // upsilon
			entityTable.put(966, "&phi;");       // phi
			entityTable.put(967, "&chi;");       // chi
			entityTable.put(968, "&psi;");       // psi
			entityTable.put(969, "&omega;");     // omega
			entityTable.put(977, "&thetasym;");  // Theta symbol
			entityTable.put(978, "&upsih;");     // Greek upsilon with hook symbol
			entityTable.put(982, "&piv;");       // Pi symbol
			entityTable.put(8194, "&ensp;");     // En space
			entityTable.put(8195, "&emsp;");     // Em space
			entityTable.put(8201, "&thinsp;");   // Thin space
			entityTable.put(8204, "&zwnj;");     // Zero width non-joiner
			entityTable.put(8205, "&zwj;");      // Zero width joiner
			entityTable.put(8206, "&lrm;");      // Left-to-right mark
			entityTable.put(8207, "&rlm;");      // Right-to-left mark
			entityTable.put(8211, "&ndash;");    // En dash
			entityTable.put(8212, "&mdash;");    // Em dash
			entityTable.put(8216, "&lsquo;");    // Left single quotation mark
			entityTable.put(8217, "&rsquo;");    // Right single quotation mark
			entityTable.put(8218, "&sbquo;");    // Single low-9 quotation mark
			entityTable.put(8220, "&ldquo;");    // Left double quotation mark
			entityTable.put(8221, "&rdquo;");    // Right double quotation mark
			entityTable.put(8222, "&bdquo;");    // Double low-9 quotation mark
			entityTable.put(8224, "&dagger;");   // Dagger
			entityTable.put(8225, "&Dagger;");   // Double dagger
			entityTable.put(8226, "&bull;");     // Bullet
			entityTable.put(8230, "&hellip;");   // Horizontal ellipsis
			entityTable.put(8240, "&permil;");   // Per mille sign
			entityTable.put(8242, "&prime;");    // Prime
			entityTable.put(8243, "&Prime;");    // Double Prime
			entityTable.put(8249, "&lsaquo;");   // Single left-pointing angle quotation
			entityTable.put(8250, "&rsaquo;");   // Single right-pointing angle quotation
			entityTable.put(8254, "&oline;");    // Overline
			entityTable.put(8260, "&frasl;");    // Fraction Slash
			entityTable.put(8364, "&euro;");     // Euro sign
			entityTable.put(8472, "&weierp;");   // Script capital
			entityTable.put(8465, "&image;");    // Blackletter capital I
			entityTable.put(8476, "&real;");     // Blackletter capital R
			entityTable.put(8482, "&trade;");    // Trade mark sign
			entityTable.put(8501, "&alefsym;");  // Alef symbol
			entityTable.put(8592, "&larr;");     // Leftward arrow
			entityTable.put(8593, "&uarr;");     // Upward arrow
			entityTable.put(8594, "&rarr;");     // Rightward arrow
			entityTable.put(8595, "&darr;");     // Downward arrow
			entityTable.put(8596, "&harr;");     // Left right arrow
			entityTable.put(8629, "&crarr;");    // Downward arrow with corner leftward. Also known as carriage return
			entityTable.put(8656, "&lArr;");     // Leftward double arrow. ISO 10646 does not say that lArr is the same as the 'is implied by' arrow but also does not have any other character for that function. So ? lArr can be used for 'is implied by' as ISOtech suggests
			entityTable.put(8657, "&uArr;");     // Upward double arrow
			entityTable.put(8658, "&rArr;");     // Rightward double arrow. ISO 10646 does not say this is the 'implies' character but does not have another character with this function so ? rArr can be used for 'implies' as ISOtech suggests
			entityTable.put(8659, "&dArr;");     // Downward double arrow
			entityTable.put(8660, "&hArr;");     // Left-right double arrow
			// Mathematical Operators
			entityTable.put(8704, "&forall;");   // For all
			entityTable.put(8706, "&part;");     // Partial differential
			entityTable.put(8707, "&exist;");    // There exists
			entityTable.put(8709, "&empty;");    // Empty set. Also known as null set and diameter
			entityTable.put(8711, "&nabla;");    // Nabla. Also known as backward difference
			entityTable.put(8712, "&isin;");     // Element of
			entityTable.put(8713, "&notin;");    // Not an element of
			entityTable.put(8715, "&ni;");       // Contains as member
			entityTable.put(8719, "&prod;");     // N-ary product. Also known as product sign. Prod is not the same character as U+03A0 'greek capital letter pi' though the same glyph might be used for both
			entityTable.put(8721, "&sum;");      // N-ary summation. Sum is not the same character as U+03A3 'greek capital letter sigma' though the same glyph might be used for both
			entityTable.put(8722, "&minus;");    // Minus sign
			entityTable.put(8727, "&lowast;");   // Asterisk operator
			entityTable.put(8729, "&#8729;");    // Bullet operator
			entityTable.put(8730, "&radic;");    // Square root. Also known as radical sign
			entityTable.put(8733, "&prop;");     // Proportional to
			entityTable.put(8734, "&infin;");    // Infinity
			entityTable.put(8736, "&ang;");      // Angle
			entityTable.put(8743, "&and;");      // Logical and. Also known as wedge
			entityTable.put(8744, "&or;");       // Logical or. Also known as vee
			entityTable.put(8745, "&cap;");      // Intersection. Also known as cap
			entityTable.put(8746, "&cup;");      // Union. Also known as cup
			entityTable.put(8747, "&int;");      // Integral
			entityTable.put(8756, "&there4;");   // Therefore
			entityTable.put(8764, "&sim;");      // tilde operator. Also known as varies with and similar to. The tilde operator is not the same character as the tilde, U+007E, although the same glyph might be used to represent both
			entityTable.put(8773, "&cong;");     // Approximately equal to
			entityTable.put(8776, "&asymp;");    // Almost equal to. Also known as asymptotic to
			entityTable.put(8800, "&ne;");       // Not equal to
			entityTable.put(8801, "&equiv;");    // Identical to
			entityTable.put(8804, "&le;");       // Less-than or equal to
			entityTable.put(8805, "&ge;");       // Greater-than or equal to
			entityTable.put(8834, "&sub;");      // Subset of
			entityTable.put(8835, "&sup;");      // Superset of. Note that nsup, 'not a superset of, U+2283' is not covered by the Symbol font encoding and is not included.
			entityTable.put(8836, "&nsub;");     // Not a subset of
			entityTable.put(8838, "&sube;");     // Subset of or equal to
			entityTable.put(8839, "&supe;");     // Superset of or equal to
			entityTable.put(8853, "&oplus;");    // Circled plus. Also known as direct sum
			entityTable.put(8855, "&otimes;");   // Circled times. Also known as vector product
			entityTable.put(8869, "&perp;");     // Up tack. Also known as orthogonal to and perpendicular
			entityTable.put(8901, "&sdot;");     // Dot operator. The dot operator is not the same character as U+00B7 middle dot
			// Miscellaneous Technical
			entityTable.put(8968, "&lceil;");    // Left ceiling. Also known as an APL upstile
			entityTable.put(8969, "&rceil;");    // Right ceiling
			entityTable.put(8970, "&lfloor;");   // left floor. Also known as APL downstile
			entityTable.put(8971, "&rfloor;");   // Right floor
			entityTable.put(9001, "&lang;");     // Left-pointing angle bracket. Also known as bra. Lang is not the same character as U+003C 'less than'or U+2039 'single left-pointing angle quotation mark'
			entityTable.put(9002, "&rang;");     // Right-pointing angle bracket. Also known as ket. Rang is not the same character as U+003E 'greater than' or U+203A 'single right-pointing angle quotation mark'
			// Geometric Shapes
			entityTable.put(9642, "&#9642;");    // Black small square
			entityTable.put(9643, "&#9643;");    // White small square
			entityTable.put(9674, "&loz;");      // Lozenge
			// Miscellaneous Symbols
			entityTable.put(9702, "&#9702;");    // White bullet
			entityTable.put(9824, "&spades;");   // Black (filled) spade suit
			entityTable.put(9827, "&clubs;");    // Black (filled) club suit. Also known as shamrock
			entityTable.put(9829, "&hearts;");   // Black (filled) heart suit. Also known as shamrock
			entityTable.put(9830, "&diams;");   // Black (filled) diamond suit

			
			  for (Integer i : entityTable.keySet()) {
			     string = string.replaceAll(entityTable.get(i), "" + (new Character((char) i.intValue())));
			  }

			  return string;
			}
		
		
		/**
		 * Utilise les code caractères pour remplacer les caractères du paramètre en entité HTML .
		 * @param string la chaine de caractères contenant les caractères à encoder.
		 * @return la conversion de la chaine passée en paramètre.
		 */
		public static String HtmlToEntity(String string) {
			
			HashMap<Integer, String> entityTable = new LinkedHashMap<Integer, String>();
			
			entityTable.put(38, "&amp;");     // Quotation mark. Not required     
			entityTable.put(34, "&quot;");     // Quotation mark. Not required     
			entityTable.put(60, "&lt;");    // Less-than sign
			entityTable.put(62, "&gt;");     // Greater-than sign
//			  // 63, "&#63;");      // Question mark
//			  // 111, "&#111;");        // Latin small letter o
			entityTable.put(160, "&nbsp;");      // Non-breaking space
			entityTable.put(161, "&iexcl;");     // Inverted exclamation mark
			entityTable.put(162, "&cent;");      // Cent sign
			entityTable.put(163, "&pound;");     // Pound sign
			entityTable.put(164, "&curren;");    // Currency sign
			entityTable.put(165, "&yen;");       // Yen sign
			entityTable.put(166, "&brvbar;");    // Broken vertical bar
			entityTable.put(167, "&sect;");      // Section sign
			entityTable.put(168, "&uml;");       // Diaeresis
			entityTable.put(169, "&copy;");      // Copyright sign
			entityTable.put(170, "&ordf;");      // Feminine ordinal indicator
			entityTable.put(171, "&laquo;");     // Left-pointing double angle quotation mark
			entityTable.put(172, "&not;");       // Not sign
			entityTable.put(173, "&shy;");       // Soft hyphen
//			  174, "&reg;");       // Registered sign
			entityTable.put(175, "&macr;");      // Macron
			entityTable.put(176, "&deg;");       // Degree sign
			entityTable.put(177, "&plusmn;");    // Plus-minus sign
			entityTable.put(178, "&sup2;");      // Superscript two
			entityTable.put(179, "&sup3;");      // Superscript three
			entityTable.put(180, "&acute;");     // Acute accent
			entityTable.put(181, "&micro;");     // Micro sign
			entityTable.put(182, "&para;");      // Pilcrow sign
			entityTable.put(183, "&middot;");    // Middle dot
			entityTable.put(184, "&cedil;");     // Cedilla
			entityTable.put(185, "&sup1;");      // Superscript one
			entityTable.put(186, "&ordm;");      // Masculine ordinal indicator
			entityTable.put(187, "&raquo;");     // Right-pointing double angle quotation mark
			entityTable.put(188, "&frac14;");    // Vulgar fraction one-quarter
			entityTable.put(189, "&frac12;");    // Vulgar fraction one-half
			entityTable.put(190, "&frac34;");    // Vulgar fraction three-quarters
			entityTable.put(191, "&iquest;");    // Inverted question mark
			entityTable.put(192, "&Agrave;");    // A with grave
			entityTable.put(193, "&Aacute;");    // A with acute
			entityTable.put(194, "&Acirc;");     // A with circumflex
			entityTable.put(195, "&Atilde;");    // A with tilde
			entityTable.put(196, "&Auml;");      // A with diaeresis
			entityTable.put(197, "&Aring;");     // A with ring above
			entityTable.put(198, "&AElig;");     // AE
			entityTable.put(199, "&Ccedil;");    // C with cedilla
			entityTable.put(200, "&Egrave;");    // E with grave
			entityTable.put(201, "&Eacute;");    // E with acute
			entityTable.put(202, "&Ecirc;");     // E with circumflex
			entityTable.put(203, "&Euml;");      // E with diaeresis
			entityTable.put(204, "&Igrave;");    // I with grave
			entityTable.put(205, "&Iacute;");    // I with acute
			entityTable.put(206, "&Icirc;");     // I with circumflex
			entityTable.put(207, "&Iuml;");      // I with diaeresis
			entityTable.put(208, "&ETH;");       // Eth
			entityTable.put(209, "&Ntilde;");    // N with tilde
			entityTable.put(210, "&Ograve;");    // O with grave
			entityTable.put(211, "&Oacute;");    // O with acute
			entityTable.put(212, "&Ocirc;");     // O with circumflex
			entityTable.put(213, "&Otilde;");    // O with tilde
			entityTable.put(214, "&Ouml;");      // O with diaeresis
			entityTable.put(215, "&times;");     // Multiplication sign
			entityTable.put(216, "&Oslash;");    // O with stroke
			entityTable.put(217, "&Ugrave;");    // U with grave
			entityTable.put(218, "&Uacute;");    // U with acute
			entityTable.put(219, "&Ucirc;");     // U with circumflex
			entityTable.put(220, "&Uuml;");      // U with diaeresis
			entityTable.put(221, "&Yacute;");    // Y with acute
			entityTable.put(222, "&THORN;");     // Thorn
			entityTable.put(223, "&szlig;");     // Sharp s. Also known as ess-zed
			entityTable.put(224, "&agrave;");    // a with grave
			entityTable.put(225, "&aacute;");    // a with acute
			entityTable.put(226, "&acirc;");     // a with circumflex
			entityTable.put(227, "&atilde;");    // a with tilde
			entityTable.put(228, "&auml;");      // a with diaeresis
			entityTable.put(229, "&aring;");     // a with ring above
			entityTable.put(230, "&aelig;");     // ae. Also known as ligature ae
			entityTable.put(231, "&ccedil;");    // c with cedilla
			entityTable.put(232, "&egrave;");    // e with grave
			entityTable.put(233, "&eacute;");    // e with acute
			entityTable.put(234, "&ecirc;");     // e with circumflex
			entityTable.put(235, "&euml;");      // e with diaeresis
			entityTable.put(236, "&igrave;");    // i with grave
			entityTable.put(237, "&iacute;");    // i with acute
			entityTable.put(238, "&icirc;");     // i with circumflex
			entityTable.put(239, "&iuml;");      // i with diaeresis
			entityTable.put(240, "&eth;");       // eth
			entityTable.put(241, "&ntilde;");    // n with tilde
			entityTable.put(242, "&ograve;");    // o with grave
			entityTable.put(243, "&oacute;");    // o with acute
			entityTable.put(244, "&ocirc;");     // o with circumflex
			entityTable.put(245, "&otilde;");    // o with tilde
			entityTable.put(246, "&ouml;");      // o with diaeresis
			entityTable.put(247, "&divide;");    // Division sign
			entityTable.put(248, "&oslash;");    // o with stroke. Also known as o with slash
			entityTable.put(249, "&ugrave;");    // u with grave
			entityTable.put(250, "&uacute;");    // u with acute
			entityTable.put(251, "&ucirc;");     // u with circumflex
			entityTable.put(252, "&uuml;");      // u with diaeresis
			entityTable.put(253, "&yacute;");    // y with acute
			entityTable.put(254, "&thorn;");     // thorn
			entityTable.put(255, "&yuml;");      // y with diaeresis
			entityTable.put(264, "&#264;");      // Latin capital letter C with circumflex
			entityTable.put(265, "&#265;");      // Latin small letter c with circumflex
			entityTable.put(338, "&OElig;");     // Latin capital ligature OE
			entityTable.put(339, "&oelig;");     // Latin small ligature oe
			entityTable.put(352, "&Scaron;");    // Latin capital letter S with caron
			entityTable.put(353, "&scaron;");    // Latin small letter s with caron
			entityTable.put(372, "&#372;");      // Latin capital letter W with circumflex
			entityTable.put(373, "&#373;");      // Latin small letter w with circumflex
			entityTable.put(374, "&#374;");      // Latin capital letter Y with circumflex
			entityTable.put(375, "&#375;");      // Latin small letter y with circumflex
			entityTable.put(376, "&Yuml;");      // Latin capital letter Y with diaeresis
			entityTable.put(402, "&fnof;");      // Latin small f with hook, function, florin
			entityTable.put(710, "&circ;");      // Modifier letter circumflex accent
			entityTable.put(732, "&tilde;");     // Small tilde
			entityTable.put(913, "&Alpha;");     // Alpha
			entityTable.put(914, "&Beta;");      // Beta
			entityTable.put(915, "&Gamma;");     // Gamma
			entityTable.put(916, "&Delta;");     // Delta
			entityTable.put(917, "&Epsilon;");   // Epsilon
			entityTable.put(918, "&Zeta;");      // Zeta
			entityTable.put(919, "&Eta;");       // Eta
			entityTable.put(920, "&Theta;");     // Theta
			entityTable.put(921, "&Iota;");      // Iota
			entityTable.put(922, "&Kappa;");     // Kappa
			entityTable.put(923, "&Lambda;");    // Lambda
			entityTable.put(924, "&Mu;");        // Mu
			entityTable.put(925, "&Nu;");        // Nu
			entityTable.put(926, "&Xi;");        // Xi
			entityTable.put(927, "&Omicron;");   // Omicron
			entityTable.put(928, "&Pi;");        // Pi
			entityTable.put(929, "&Rho;");       // Rho
			entityTable.put(931, "&Sigma;");     // Sigma
			entityTable.put(932, "&Tau;");       // Tau
			entityTable.put(933, "&Upsilon;");   // Upsilon
			entityTable.put(934, "&Phi;");       // Phi
			entityTable.put(935, "&Chi;");       // Chi
			entityTable.put(936, "&Psi;");       // Psi
			entityTable.put(937, "&Omega;");     // Omega
			entityTable.put(945, "&alpha;");     // alpha
			entityTable.put(946, "&beta;");      // beta
			entityTable.put(947, "&gamma;");     // gamma
			entityTable.put(948, "&delta;");     // delta
			entityTable.put(949, "&epsilon;");   // epsilon
			entityTable.put(950, "&zeta;");      // zeta
			entityTable.put(951, "&eta;");       // eta
			entityTable.put(952, "&theta;");     // theta
			entityTable.put(953, "&iota;");      // iota
			entityTable.put(954, "&kappa;");     // kappa
			entityTable.put(955, "&lambda;");    // lambda
			entityTable.put(956, "&mu;");        // mu
			entityTable.put(957, "&nu;");        // nu
			entityTable.put(958, "&xi;");        // xi
			entityTable.put(959, "&omicron;");   // omicron
			entityTable.put(960, "&pi;");        // pi
			entityTable.put(961, "&rho;");       // rho
			entityTable.put(962, "&sigmaf;");    // sigmaf
			entityTable.put(963, "&sigma;");     // sigma
			entityTable.put(964, "&tau;");       // tau
			entityTable.put(965, "&upsilon;");   // upsilon
			entityTable.put(966, "&phi;");       // phi
			entityTable.put(967, "&chi;");       // chi
			entityTable.put(968, "&psi;");       // psi
			entityTable.put(969, "&omega;");     // omega
			entityTable.put(977, "&thetasym;");  // Theta symbol
			entityTable.put(978, "&upsih;");     // Greek upsilon with hook symbol
			entityTable.put(982, "&piv;");       // Pi symbol
			entityTable.put(8194, "&ensp;");     // En space
			entityTable.put(8195, "&emsp;");     // Em space
			entityTable.put(8201, "&thinsp;");   // Thin space
			entityTable.put(8204, "&zwnj;");     // Zero width non-joiner
			entityTable.put(8205, "&zwj;");      // Zero width joiner
			entityTable.put(8206, "&lrm;");      // Left-to-right mark
			entityTable.put(8207, "&rlm;");      // Right-to-left mark
			entityTable.put(8211, "&ndash;");    // En dash
			entityTable.put(8212, "&mdash;");    // Em dash
			entityTable.put(8216, "&lsquo;");    // Left single quotation mark
			entityTable.put(8217, "&rsquo;");    // Right single quotation mark
			entityTable.put(8218, "&sbquo;");    // Single low-9 quotation mark
			entityTable.put(8220, "&ldquo;");    // Left double quotation mark
			entityTable.put(8221, "&rdquo;");    // Right double quotation mark
			entityTable.put(8222, "&bdquo;");    // Double low-9 quotation mark
			entityTable.put(8224, "&dagger;");   // Dagger
			entityTable.put(8225, "&Dagger;");   // Double dagger
			entityTable.put(8226, "&bull;");     // Bullet
			entityTable.put(8230, "&hellip;");   // Horizontal ellipsis
			entityTable.put(8240, "&permil;");   // Per mille sign
			entityTable.put(8242, "&prime;");    // Prime
			entityTable.put(8243, "&Prime;");    // Double Prime
			entityTable.put(8249, "&lsaquo;");   // Single left-pointing angle quotation
			entityTable.put(8250, "&rsaquo;");   // Single right-pointing angle quotation
			entityTable.put(8254, "&oline;");    // Overline
			entityTable.put(8260, "&frasl;");    // Fraction Slash
			entityTable.put(8364, "&euro;");     // Euro sign
			entityTable.put(8472, "&weierp;");   // Script capital
			entityTable.put(8465, "&image;");    // Blackletter capital I
			entityTable.put(8476, "&real;");     // Blackletter capital R
			entityTable.put(8482, "&trade;");    // Trade mark sign
			entityTable.put(8501, "&alefsym;");  // Alef symbol
			entityTable.put(8592, "&larr;");     // Leftward arrow
			entityTable.put(8593, "&uarr;");     // Upward arrow
			entityTable.put(8594, "&rarr;");     // Rightward arrow
			entityTable.put(8595, "&darr;");     // Downward arrow
			entityTable.put(8596, "&harr;");     // Left right arrow
			entityTable.put(8629, "&crarr;");    // Downward arrow with corner leftward. Also known as carriage return
			entityTable.put(8656, "&lArr;");     // Leftward double arrow. ISO 10646 does not say that lArr is the same as the 'is implied by' arrow but also does not have any other character for that function. So ? lArr can be used for 'is implied by' as ISOtech suggests
			entityTable.put(8657, "&uArr;");     // Upward double arrow
			entityTable.put(8658, "&rArr;");     // Rightward double arrow. ISO 10646 does not say this is the 'implies' character but does not have another character with this function so ? rArr can be used for 'implies' as ISOtech suggests
			entityTable.put(8659, "&dArr;");     // Downward double arrow
			entityTable.put(8660, "&hArr;");     // Left-right double arrow
			// Mathematical Operators
			entityTable.put(8704, "&forall;");   // For all
			entityTable.put(8706, "&part;");     // Partial differential
			entityTable.put(8707, "&exist;");    // There exists
			entityTable.put(8709, "&empty;");    // Empty set. Also known as null set and diameter
			entityTable.put(8711, "&nabla;");    // Nabla. Also known as backward difference
			entityTable.put(8712, "&isin;");     // Element of
			entityTable.put(8713, "&notin;");    // Not an element of
			entityTable.put(8715, "&ni;");       // Contains as member
			entityTable.put(8719, "&prod;");     // N-ary product. Also known as product sign. Prod is not the same character as U+03A0 'greek capital letter pi' though the same glyph might be used for both
			entityTable.put(8721, "&sum;");      // N-ary summation. Sum is not the same character as U+03A3 'greek capital letter sigma' though the same glyph might be used for both
			entityTable.put(8722, "&minus;");    // Minus sign
			entityTable.put(8727, "&lowast;");   // Asterisk operator
			entityTable.put(8729, "&#8729;");    // Bullet operator
			entityTable.put(8730, "&radic;");    // Square root. Also known as radical sign
			entityTable.put(8733, "&prop;");     // Proportional to
			entityTable.put(8734, "&infin;");    // Infinity
			entityTable.put(8736, "&ang;");      // Angle
			entityTable.put(8743, "&and;");      // Logical and. Also known as wedge
			entityTable.put(8744, "&or;");       // Logical or. Also known as vee
			entityTable.put(8745, "&cap;");      // Intersection. Also known as cap
			entityTable.put(8746, "&cup;");      // Union. Also known as cup
			entityTable.put(8747, "&int;");      // Integral
			entityTable.put(8756, "&there4;");   // Therefore
			entityTable.put(8764, "&sim;");      // tilde operator. Also known as varies with and similar to. The tilde operator is not the same character as the tilde, U+007E, although the same glyph might be used to represent both
			entityTable.put(8773, "&cong;");     // Approximately equal to
			entityTable.put(8776, "&asymp;");    // Almost equal to. Also known as asymptotic to
			entityTable.put(8800, "&ne;");       // Not equal to
			entityTable.put(8801, "&equiv;");    // Identical to
			entityTable.put(8804, "&le;");       // Less-than or equal to
			entityTable.put(8805, "&ge;");       // Greater-than or equal to
			entityTable.put(8834, "&sub;");      // Subset of
			entityTable.put(8835, "&sup;");      // Superset of. Note that nsup, 'not a superset of, U+2283' is not covered by the Symbol font encoding and is not included.
			entityTable.put(8836, "&nsub;");     // Not a subset of
			entityTable.put(8838, "&sube;");     // Subset of or equal to
			entityTable.put(8839, "&supe;");     // Superset of or equal to
			entityTable.put(8853, "&oplus;");    // Circled plus. Also known as direct sum
			entityTable.put(8855, "&otimes;");   // Circled times. Also known as vector product
			entityTable.put(8869, "&perp;");     // Up tack. Also known as orthogonal to and perpendicular
			entityTable.put(8901, "&sdot;");     // Dot operator. The dot operator is not the same character as U+00B7 middle dot
			// Miscellaneous Technical
			entityTable.put(8968, "&lceil;");    // Left ceiling. Also known as an APL upstile
			entityTable.put(8969, "&rceil;");    // Right ceiling
			entityTable.put(8970, "&lfloor;");   // left floor. Also known as APL downstile
			entityTable.put(8971, "&rfloor;");   // Right floor
			entityTable.put(9001, "&lang;");     // Left-pointing angle bracket. Also known as bra. Lang is not the same character as U+003C 'less than'or U+2039 'single left-pointing angle quotation mark'
			entityTable.put(9002, "&rang;");     // Right-pointing angle bracket. Also known as ket. Rang is not the same character as U+003E 'greater than' or U+203A 'single right-pointing angle quotation mark'
			// Geometric Shapes
			entityTable.put(9642, "&#9642;");    // Black small square
			entityTable.put(9643, "&#9643;");    // White small square
			entityTable.put(9674, "&loz;");      // Lozenge
			// Miscellaneous Symbols
			entityTable.put(9702, "&#9702;");    // White bullet
			entityTable.put(9824, "&spades;");   // Black (filled) spade suit
			entityTable.put(9827, "&clubs;");    // Black (filled) club suit. Also known as shamrock
			entityTable.put(9829, "&hearts;");   // Black (filled) heart suit. Also known as shamrock
			entityTable.put(9830, "&diams;");   // Black (filled) diamond suit

			
			  for (Integer i : entityTable.keySet()) {
			     string = string.replaceAll("" + new Character((char) i.intValue()), entityTable.get(i));
			  }

			  return string;
			}

}
