package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaliseXml {
	
	/**
	 * Indique le nom du champ.
	 * @return le nom du champ.
	 */
	String nom() default "";

	/**
	 * Le champ est il obligatoire ?
	 * @return true ou false
	 */
	boolean obligatoire() default false;

	/**
	 * Le champ existe t'il en multiple exemplaire. Dans ce cas cela doit �tre une liste.
	 * @return true ou false
	 */
	boolean multiple() default false;

	/**
	 * Le pr�fixe si il y a lieu.
	 * @return le pr�fixe.
	 */
	String prefixe() default "";

	/**
	 * S'agit il d'un objet complexe ou d'un champ ?
	 * @return true ou false.
	 */
	boolean complexe() default false;

	/**
	 * Liste des valeurs possibles pour cette balise. L'objet doit �tre une �num�ration dont le toString montre le libell�.
	 * @return la class de la liste des valeurs sous forme d'�num�ration
	 */
	Class<?> enumeration() default Object.class;
	
	/**
	 * Classe contenue dans une liste
	 * @return la class des objets contenus dans la liste
	 */
	Class<?> contenu() default Object.class;
	
	/**
	 * Liste de valeurs simples en "dur" pour un champ.
	 * @return un tableau de chaine de caract�re d�crivant les diff�rentes valeurs possibles.
	 */
	String[] listeValeur() default { };
	
	/**
	 * Un libelle descriptif facultatif n'apparaissant pas dans le flux xml.
	 * @return le libelle explicatif.
	 */
	String libelle() default "";
	//enumeration listeValeur() default null;
	
	/**
	 * Un entete ne subissant aucune analyse qui sera concat�ner en tant que pr�fixe au reste du flux.
	 * @return un entete pour le flux XML.
	 */
	String entete() default "";
	
	/**
	 * Un enqueue ne subissant aucune analyse qui sera concat�ner en tant que suffixe au reste du flux.
	 * @return un enqueue pour le flux XML.
	 */
	String enqueue() default "";

	/**
	 * D�finie un namespace associ� au pr�fixe de la chaine de caract�re si il y a lieu.
	 * @return le namespace associ� au pr�fixe.
	 */
	String nameSpace() default "";
	
	/**
	 * Indique l'objet annot� doit faire l'objet de la g�n�ration d'une balise d�di� ou nom.
	 * @return oui si une balise est associ�e � l'objet, non sinon.
	 */
	boolean balisable() default true;
}
