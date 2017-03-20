package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Step {
	
	/**
	 * Indique le nom du step.
	 * @return le nom du step.
	 */
	String nom() default "";

	/**
	 * La méthode renvoie une valeur.
	 * @return true ou false
	 */
	boolean retour() default false;
}
