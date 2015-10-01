package beans;

import java.io.Serializable;
import java.util.Date;

import org.openqa.selenium.Cookie;

/**
 * Classe de cookie permetant la serialisation.
 * Un objet cookie permet de se connecter directement à une application la poste.
 * @author levieil_f
 *
 */
public class CookieBean implements Serializable {

	/**
	 * Id de serialisation.
	 */
	private static final long serialVersionUID = -7631144533115866852L;
	
	/**
	 * Le nom.
	 */
	private String name;
	/**
	 * La valeur.
	 */
	private String value;
	/**
	 * Le domaine.
	 */
	private String domain;
	/**
	 * Le chemin.
	 */
	private String path;
	/**
	 * La date d'expiration.
	 */
	private Date expiry;
	/**
	 * La sécurité du cookie.
	 */
	private boolean isSecure;
	
	/**
	 * Constructeur par défaut.
	 */
	public CookieBean() {
		super();
	}
	
	/**
	 *  Constructeur avec arguments.
	 */
	public CookieBean(String name, String value, String domain, String path,
			Date expiry, boolean isSecure) {
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.path = path;
		this.expiry = expiry;
		this.isSecure = isSecure;
	}
	
	/**
	 * Transforme le bean cookie en cookie en omettant le domaine.
	 * Le domaine est mis à vide.
	 * @return le cookie.
	 */
	public Cookie toCookie() {
		return new Cookie(name, value, "", path, expiry, isSecure);
	}
	
	/**
	 * Transforme le bean cookie en cookie en valorisant le domaine.
	 * Le domaine est pris dans son entier.
	 * @return le cookie.
	 */
	public Cookie toDomainCookie() {
		return new Cookie(name, value, domain, path, expiry, isSecure);
	}
	
	/**
	 * Transforme le bean cookie en cookie (en utilisant les paramètres du htlmdriver).
	 * Pour compatibilité, on met le domaine à null.
	 * @return le cookie.
	 */
	public Cookie toCookieHtml() {
		return new Cookie(name, value, null, path, expiry, isSecure);
	}
	
	/**
	 * Clone un cookie pour le rendre serializable.
	 * @param cookie le cookie de reference à cloner.
	 */
	public CookieBean(Cookie cookie) {
		this(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpiry(), cookie.isSecure());
		//System.out.println(cookie.getName() + " " + cookie.getValue() + " " + cookie.getDomain());
	}

}
