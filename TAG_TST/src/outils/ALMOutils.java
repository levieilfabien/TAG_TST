package outils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import atu.alm.wrapper.enums.StatusAs;
import atu.alm.wrapper.exceptions.ALMServiceException;
import beans.ALMBean;
import beans.CasEssaiBean;
import beans.ObjectifBean;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.LibraryLoader;

import constantes.Erreurs;
import exceptions.SeleniumException;
import extensions.NativeLibraries;
import extensions.SeleniumALMWrapper;
import extensions.interfaces.IALMRun;
import extensions.interfaces.IALMTestCase;

//regsvr32.exe C:\Users\levieilfa\AppData\Local\HP\ALM-Client\qc\OTAClient.dll

/**
 * Classe optionnelle d'outils permettant d'intégrer la connexion à ALM.
 * Si on utilise cet outil on pourra enregistrer et lire des tests saisies dans ALM.
 * @author levieilfa
 *
 */
public class ALMOutils {
	
	static Logger logger = Logger.getLogger(ALMOutils.class);
	
	private static final String RESOURCES = "resources\\";
	private static final String JACOB_X86 = "jacob-1.18-x86.dll";
	private static final String JACOB_X64 = "jacob-1.18-x64.dll";
	private static final String URL_ALM = "https://qc.intranatixis.com/qcbin";
	
	/**
	 * Connection à ALM via JACOB.
	 * @param login le login utilisateur ALM.
	 * @param password le mot de passe utilisateur ALM.
	 * @param domain le domaine de test.
	 * @param project le projet de test.
	 * @return le dispatch vers ALM.
	 */
	public static Dispatch connectionALMJacob(String login, String password, String domain, String project) {
		try{
			System.setProperty("jacob.dll.path", getJacobDll(true));
			LibraryLoader.loadJacobLibrary();
			
		    ActiveXComponent axc = new ActiveXComponent("TDApiOle80.TDConnection");
	        Dispatch disp = axc.getObject();
	        Dispatch.call(disp, "InitConnectionEx", URL_ALM);
	        Dispatch.call(disp, "Login", login, password);
	        Dispatch.call(disp, "Connect", domain, project);

		    System.out.println("Connection success!!!");		    
		    return disp;
		}
		catch (Exception e) {
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		    return null;
		}
	}
	
	/**
	 * Permet d'ajouter la librairie Jabob 32 bit dans le system path.
	 */
	public static void ajouterJacobSystem() {
		String path = getJacobDll(true);
		
		// On vérifie que la librairie n'est pas déjà chargée
//		NativeLibraries recuperateur;
//		try {
//			recuperateur = new NativeLibraries();
//			List<String> librairies = recuperateur.getLoadedLibraries();
//			for (String librairie : librairies) {
//				System.out.println(librairie);
//			}
//		} catch (NoSuchFieldException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
//		try {
			// On essai d'acceder au dll avec l'extension
			System.setProperty("jacob.dll.path", path);
			LibraryLoader.loadJacobLibrary();
//		} catch (UnsatisfiedLinkError ex) {
//			// On essai d'acceder au dll sans l'extension
//			//path = path.replaceAll(".dll", "");
//			System.setProperty("jacob.dll.path", path);
//			System.load(path);
//		}
	}
	
//	private static EnumVariant obtenirListeElement(String cheminTestLab, Dispatch disp)
//	{
//	    Dispatch treeManager = Dispatch.get(disp, "TestSetTreeManager").toDispatch();
//	    Dispatch testLabFolder = Dispatch.call(treeManager, "NodeByPath", cheminTestLab).toDispatch();
//	    Dispatch testSets = Dispatch.call(testLabFolder, "FindTestSets", "").getDispatch();
//	    EnumVariant testSetsList = new EnumVariant(testSets);
//
//	    return testSetsList;
//	}
//	
//	/**
//	 * Permet de retirer un test set à partir d'une liste de test set.
//	 * @param listeElement la liste de test set
//	 * @param nom le nom du test à obtenir
//	 * @return le test si il est trouvé, sinon null.
//	 */
//	private static Dispatch obtenirTest(EnumVariant listeElement, String nom) {
//	    while (listeElement.hasMoreElements())
//	    {
//	        Dispatch testSet = listeElement.nextElement().getDispatch();
//	        String nomElement = Dispatch.get(testSet, "Name").getString();
//	        
//	        if (nom.equals(nomElement)) {
//	        	return testSet;
//	        }
//	    }
//	    return null;
//	}

	/**
	 * Permet d'obtenir le chemin vers le fichier de dll de Jacob.
	 * @return le chemin vers le fichier de properties.
	 */
	@SuppressWarnings("deprecation")
	public static String getJacobDll(Boolean bit32) {
		// On souhaites utiliser l'implémentation 32bit ou 64bit ?
		String cheminDll = bit32?RESOURCES + JACOB_X86:RESOURCES + JACOB_X64;
		// On récupère le dll correspondant dans le répertoire "resources".
		String retour = PropertiesOutil.class.getClassLoader().getResource(cheminDll).getFile().substring(1);
		try {
			retour = URLDecoder.decode(retour, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			retour = URLDecoder.decode(retour);
		}
		while (!retour.endsWith("dll")) {
			retour = retour.substring(0, retour.length() - 2);
		}
		retour = retour.replaceAll("\\\\", Matcher.quoteReplacement(File.separator));
		retour = retour.replaceAll(Matcher.quoteReplacement("/"), Matcher.quoteReplacement(File.separator));
		return retour;
	}
	
	@SuppressWarnings("deprecation")
	private static String getDllDir(Boolean bit32) {
		// On souhaites utiliser l'implémentation 32bit ou 64bit ?
		String cheminDll = bit32?RESOURCES + JACOB_X86:RESOURCES + JACOB_X64;
		// On récupère le dll correspondant dans le répertoire "resources".
		String retour = PropertiesOutil.class.getClassLoader().getResource(cheminDll).getFile().substring(1);
		try {
			retour = URLDecoder.decode(retour, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			retour = URLDecoder.decode(retour);
		}
		retour = retour.replaceAll(JACOB_X86, "");
		retour = retour.replaceAll(JACOB_X64, "");
		retour = retour.replaceAll("\\\\", Matcher.quoteReplacement(File.separator));
		retour = retour.replaceAll(Matcher.quoteReplacement("/"), Matcher.quoteReplacement(File.separator));
		return retour;
	}
	
	/**
	 * Création d'une instance de ALMWrapper à partir des informations de connexion contenue dans le singleton ALMBean.
	 * Ne fonctionne que si les paramètres sont définie dans "seleniumProperties" et que l'execution est en 32bit.
	 * @return une instance de ALMWrapper.
	 * @throws ALMServiceException
	 */
	public static SeleniumALMWrapper myConnectionALMWrapper() throws ALMServiceException {
		return myConnectionALMWrapper(ALMBean.LOGIN_ALM, ALMBean.PASSWORD_ALM, ALMBean.DOMAIN_ALM, ALMBean.PROJECT_ALM, ALMBean.URL_ALM);
	}
	
	/**
	 * Création d'une instance de ALMWrapper à partir des informations de connexion.
	 * @param login le login utilisateur.
	 * @param password le mot de passe utilisateur.
	 * @param domain le domaine de test.
	 * @param project le projet de test.
	 * @param url url d'ALM.
	 * @return une instance de ALMWrapper.
	 * @throws ALMServiceException en cas d'erreur.
	 */
	private static SeleniumALMWrapper myConnectionALMWrapper(String login, String password, String domain, String project, String url) throws ALMServiceException {
		// Instanciation du wrapper.
		SeleniumALMWrapper wrapper = new SeleniumALMWrapper(url);
		// Connection avec les identifiants.
		wrapper.connect(login, password, domain, project);
		return wrapper;
	}
	

//	/**
//	 * Création d'un defect de base (fonction exemple)
//	 * @param wrapper le wrapper initialisé précédement.
//	 * @throws ALMServiceException en cas d'impossibilité.
//	 */
//	public static void createDefect(ALMServiceWrapper wrapper) throws ALMServiceException {
//
//		Dispatch bugFactory = Dispatch.call(wrapper.getAlmObj().getAlmObject(), "BugFactory").toDispatch();
//		IALMDefect defect = new ALMDefect(bugFactory);
//		defect.isReproducible(true);
//		defect.setAssignedTo("levieilfa");
//		defect.setDetectedBy("levieilfa");
//		defect.setSummary("[POC] Defect automatisé");
//		defect.setDetectionDate("13/11/2015");
//		defect.setPriority("1-Basse");
//		defect.setSeverity("Mineure");
//		defect.setEtat("10-Nouveau");
//		defect.setDescription("Ceci est un faux defect à supprimer");
//		defect.setCycle("961"); // POC
//		defect.setRelease("209"); // POC
//		defect.setVersion("v15.11");
//		defect.setApplication("ALM");
//		defect.setEnvironement("Développement");
//		defect.setProjetEvolution("N/A");
//		defect.setEntiteResponsable("Equipe T&R");
//		defect.setNiveauDeTest("03 - Tests de Vérification");
//		defect.setEmetteur("Equipe T&R");
//		defect.setType("Anomalie");
//		System.out.println(defect.getDefectID());
//		//wrapper.newAttachment(getJacobDll(true), "Attachement test", defect);
//		
//		defect.save();
//		System.out.println(defect.getDefectID());
//		wrapper.close();
//	}
	
	/**
	 * Fonction permettant la mise à jour dans ALM du cas de test à partir des inforations saisies.
	 * Cette fonction suppose que le cas d'essai dans TESTLAB n'adresse qu'un scénario dans TESTPLAN (à mettre à jour)
	 * @param wrapper le connecteur à ALM
	 * @param casEssai le cas d'essai contenant les informations obligatoire.
	 * @param etat l'état final du cas de test.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static void miseAJourTestSet(CasEssaiBean casEssai, boolean etat) throws SeleniumException {
		boolean valide = false;
		boolean casUnique = false;
		// On vérifie les données pour ALM.
		if (casEssai.getAlm()) {
			// Si le test plan n'est pas renseigné c'est qu'on est sur un scénario. Mais les autres informations sont requises
			System.out.println("Mise à jour dans ALM de " + casEssai.getNomTestLab() + ":" + casEssai.getNomTestPlan() + ":" + casEssai.getIdUniqueTestLab());
			if (casEssai.getNomTestLab() != null && casEssai.getCheminTestLab() != null && casEssai.getIdUniqueTestLab() != 0) {
				valide = true;
				System.out.println("Mise à jour dans ALM de " + casEssai.getNomTestLab() + ":" + casEssai.getNomTestPlan() + ":" + casEssai.getIdUniqueTestLab());
			}
		}
		// On vérifie si le cas d'essai est unique ou pas. Le test plan n'est renseigné que pour un "Cas de Test" pas un "Scénario".
		if (casEssai.getNomTestPlan() != null) {
			casUnique = true;
		}
		// On ne fait l'injection dans l'ALM que si le cas d'essai est correcttement paramètré.
		if (valide) {
			//System.out.println("Mise à jour dans ALM de " + casEssai.getNomTestLab() + ":" + casEssai.getNomTestPlan());
			// Si le cas est unique on met à jour le "Cas de test" (Test) dans le "scénario" (Test Set) en lui ajoutant un "Run".
			if (casUnique) {
				try {
					// TODO Faire une version paramètrable
					SeleniumALMWrapper wrapper = myConnectionALMWrapper();
					// Mise à jour de l'état général du cas de test.
					IALMTestCase execution = wrapper.miseAJourTest(casEssai.getCheminTestLab(), casEssai.getNomTestLab(), casEssai.getIdUniqueTestLab(), casEssai.getNomTestPlan(), etat?StatusAs.PASSED:StatusAs.FAILED);
					// Mise à jour des steps via l'ajout d'une execution
					IALMRun execution_run = wrapper.creationNouveauRun(execution, "RUN AUTO " + casEssai.getTime(), etat?StatusAs.PASSED:StatusAs.FAILED);
					// Si le cas d'essai est associé à un repertoire on cherche à obtenir les pièces jointes.
					if (casEssai.getRepertoireTelechargement() != null) {
						File repertoire = new File(casEssai.getRepertoireTelechargement());
						// On vérifie qu'il s'agit bien d'un repertoire
						if (repertoire.isDirectory()) {
							// Pour chaque fichier non repertoire dans le repertoire, on met en pièce jointe.
							for (File file : repertoire.listFiles()) {
								if (!file.isDirectory()) {
									String chemin = file.getAbsolutePath();
									// On remplace les référence au repertoire local \.\ par un simple \
									chemin = chemin.replaceAll("\\\\.\\\\", "\\\\");
									SeleniumALMWrapper.ajouterPieceJointe(chemin, execution_run.getRun());
								}
							}
						}
					}
					// On met à jour les STEP (sous forme d'ajout)
					for (ObjectifBean step : casEssai.getObjectifs().values()) {
						if (step.isStep()) {
							wrapper.addStep(execution_run, step.getCode(), step.getEtat()?StatusAs.PASSED:StatusAs.FAILED, step.getDescriptif(), step.getAttendu(), step.getObtenu());
						}
					}
					// On ferme la connexion avec ALM.
					wrapper.close();
				} catch (ALMServiceException e) {
					e.printStackTrace();
					throw new SeleniumException(Erreurs.E032, "Impossible de mettre à jour l'état du cas de test dans ALM.");
				}
			} else {
				// Si le cas d'essai en contient d'autres, on boucle sur chaucun d'entre eux.
				for (CasEssaiBean sousCas : casEssai.getTests()) {
					System.out.println("=> Mise à jour dans ALM de " + sousCas.getNomTestLab() + ":" + sousCas.getNomTestPlan());
					miseAJourTestSet(sousCas, sousCas.getEtatFinal());
				}
			}
		} else {
			throw new SeleniumException(Erreurs.E033, "Impossible de mettre à jour l'état du cas de test dans ALM : Informations manquantes.");
		}
	}
	
	
//	public static void miseAJourTest(ALMServiceWrapper wrapper, String cheminTest, String nomTestSet, String nomTest, int id, StatusAs etat) throws ALMServiceException {
//		ITestCase execution = wrapper.updateResult(cheminTest, nomTestSet, id, nomTest, etat);
//		
//		ITestCaseRun execution_run = wrapper.createNewRun(execution, "RUN AUTO", StatusAs.PASSED);
//		wrapper.addStep(execution_run, "Step1", StatusAs.PASSED, "Description1", "excepted", "actual");
//		wrapper.addStep(execution_run, "Step2", StatusAs.PASSED, "Description2", "excepted", "actual");
//		
//		wrapper.close();
//	}
	
	public static void main(String argv[]) {
		//ALMOutils.connectionALM("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//Dispatch connection = ALMOutils.connectionALMJacob("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//Dispatch scenario3 = ALMOutils.obtenirTest(ALMOutils.obtenirListeElement("Root\\POC Selenium\\IZIVENTE", connection), "SC03 - Souscription distributeur TRAVAUX CE");
	
		//ALMServiceWrapper wrapper = connectionALMWrapper("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//createDefect(wrapper);
		
		//miseAJourTest(wrapper, "POC Selenium\\IZIVENTE", "SC03 - Souscription distributeur TRAVAUX CE", "SC03 - IZIVENTE_Distributeur_Travaux", 49375, StatusAs.PASSED);
		//wrapper.close();
		
	}
}
