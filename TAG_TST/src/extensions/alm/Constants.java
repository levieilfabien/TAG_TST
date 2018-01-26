package extensions.alm;

/**
*
* Cette classe d�fini des constantes par d�faut lorsque celle ci ne sont pas valoris�es dans des fichier properties.
* Les valorisations dans ce fichier ne seront pas maintenues et ne doivent �tre utilis�es que pour des tests en interne du framework.
*/
public class Constants {

   public static final String HOST_ALM = "hpalm.intranatixis.com";
   public static final String HOST_CONFLUENCE = "https://confluence.eqsmut.intranatixis.com/rest/api"; //content/68944055";
   public static final String PORT = null;
   
   public static final String HOST_PROXY = "proxypartners.intranet";

   public static final String USERNAME = "";
   public static final String PASSWORD = "";

   public static final String DOMAIN = "NATIXIS_FINANCEMENT";
   public static final String PROJECT = "CREDIT_CONSOMMATION";

   /**
    * Constructeur par d�faut priv�.
    */
   private Constants() {
	   
   }


}