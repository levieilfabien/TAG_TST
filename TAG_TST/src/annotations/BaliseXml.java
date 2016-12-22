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
	 * Le champ existe t'il en multiple exemplaire. Dans ce cas cela doit être une liste.
	 * @return true ou false
	 */
	boolean multiple() default false;

	/**
	 * Le préfixe si il y a lieu.
	 * @return le préfixe.
	 */
	String prefixe() default "";

	/**
	 * S'agit il d'un objet complexe ou d'un champ ?
	 * @return true ou false.
	 */
	boolean complexe() default false;

	/**
	 * Liste des valeurs possibles pour cette balise. L'objet doit être une énumération dont le toString montre le libellé.
	 * @return la class de la liste des valeurs sous forme d'énumération
	 */
	Class<?> enumeration() default Object.class;
	
	/**
	 * Classe contenue dans une liste
	 * @return la class des objets contenus dans la liste
	 */
	Class<?> contenu() default Object.class;
	
	/**
	 * Liste de valeurs simples en "dur" pour un champ.
	 * @return un tableau de chaine de caractère décrivant les différentes valeurs possibles.
	 */
	String[] listeValeur() default { };
	
	/**
	 * Un libelle descriptif facultatif n'apparaissant pas dans le flux xml.
	 * @return le libelle explicatif.
	 */
	String libelle() default "";
	//enumeration listeValeur() default null;
	
	/**
	 * Un entete ne subissant aucune analyse qui sera concaténer en tant que préfixe au reste du flux.
	 * @return un entete pour le flux XML.
	 */
	String entete() default "";
	
	/**
	 * Un enqueue ne subissant aucune analyse qui sera concaténer en tant que suffixe au reste du flux.
	 * @return un enqueue pour le flux XML.
	 */
	String enqueue() default "";

	/**
	 * Définie un namespace associé au préfixe de la chaine de caractère si il y a lieu.
	 * @return le namespace associé au préfixe.
	 */
	String nameSpace() default "";
	
	/**
	 * Indique l'objet annoté doit faire l'objet de la génération d'une balise dédié ou nom.
	 * @return oui si une balise est associée à l'objet, non sinon.
	 */
	boolean balisable() default true;
}
