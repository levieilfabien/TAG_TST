package outils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import atu.alm.wrapper.ALMServiceWrapper;
import atu.alm.wrapper.ITestCase;
import atu.alm.wrapper.ITestCaseRun;
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
import extensions.impl.ALMDefect;
import extensions.interfaces.IALMDefect;

//regsvr32.exe C:\Users\levieilfa\AppData\Local\HP\ALM-Client\qc\OTAClient.dll

/**
 * Classe optionnelle d'outils permettant d'intégrer la connexion à ALM.
 * Si on utilise cet outil on pourra enregistrer et lire des tests saisies dans ALM.
 * @author levieilfa
 *
 */
public class ALMOutils {
	
	private static final String JACOB_X86 = "resources\\jacob-1.18-x86.dll";
	private static final String JACOB_X64 = "resources\\jacob-1.18-x64.dll";
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
	private static String getJacobDll(Boolean bit32) {
		// On souhaites utiliser l'implémentation 32bit ou 64bit ?
		String cheminDll = bit32?JACOB_X86:JACOB_X64;
		// On récupère le dll correspondant dans le répertoire "resources".
		String retour = PropertiesOutil.class.getClassLoader().getResource(cheminDll).getFile().substring(1);
		try {
			retour = URLDecoder.decode(retour, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			retour = URLDecoder.decode(retour);
		}
		return retour;
	}
	
	/**
	 * Création d'une instance de ALMWrapper à partir des informations de connexion contenue dans le singleton ALMBean.
	 * Ne fonctionne que si les paramètres sont définie dans "seleniumProperties" et que l'execution est en 32bit.
	 * @return une instance de ALMWrapper.
	 * @throws ALMServiceException
	 */
	public static ALMServiceWrapper connectionALMWrapper() throws ALMServiceException {
		return connectionALMWrapper(ALMBean.LOGIN_ALM, ALMBean.PASSWORD_ALM, ALMBean.DOMAIN_ALM, ALMBean.PROJECT_ALM, ALMBean.URL_ALM);
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
	public static ALMServiceWrapper connectionALMWrapper(String login, String password, String domain, String project, String url) throws ALMServiceException {
		// Instanciation du wrapper.
		ALMServiceWrapper wrapper = new ALMServiceWrapper(url);
		// Connection avec les identifiants.
		wrapper.connect(login, password, domain, project);
		return wrapper;
	}
	

	/**
	 * Création d'un defect de base (fonction exemple)
	 * @param wrapper le wrapper initialisé précédement.
	 * @throws ALMServiceException en cas d'impossibilité.
	 */
	public static void createDefect(ALMServiceWrapper wrapper) throws ALMServiceException {

		Dispatch bugFactory = Dispatch.call(wrapper.getAlmObj().getAlmObject(), "BugFactory").toDispatch();
		IALMDefect defect = new ALMDefect(bugFactory);
		defect.isReproducible(true);
		defect.setAssignedTo("levieilfa");
		defect.setDetectedBy("levieilfa");
		defect.setSummary("[POC] Defect automatisé");
		defect.setDetectionDate("13/11/2015");
		defect.setPriority("1-Basse");
		defect.setSeverity("Mineure");
		defect.setEtat("10-Nouveau");
		defect.setDescription("Ceci est un faux defect à supprimer");
		defect.setCycle("961"); // POC
		defect.setRelease("209"); // POC
		defect.setVersion("v15.11");
		defect.setApplication("ALM");
		defect.setEnvironement("Développement");
		defect.setProjetEvolution("N/A");
		defect.setEntiteResponsable("Equipe T&R");
		defect.setNiveauDeTest("03 - Tests de Vérification");
		defect.setEmetteur("Equipe T&R");
		defect.setType("Anomalie");
		System.out.println(defect.getDefectID());
		//wrapper.newAttachment(getJacobDll(true), "Attachement test", defect);
		
		defect.save();
		System.out.println(defect.getDefectID());
		wrapper.close();
	}
	
	/**
	 * Fonction permettant la mise à jour dans ALM du cas de test à partir des inforations saisies.
	 * Cette fonction suppose que le cas d'essai dans TESTLAB n'adresse qu'un scénario dans TESTPLAN (à mettre à jour)
	 * @param wrapper le connecteur à ALM
	 * @param casEssai le cas d'essai contenant les informations obligatoire.
	 * @param etat l'état final du cas de test.
	 * @throws SeleniumException en cas d'erreur.
	 */
	public static void miseAJourTest(CasEssaiBean casEssai, boolean etat) throws SeleniumException {
		boolean valide = false;
		// On vérifie les données pour ALM.
		if (casEssai.getAlm()) {
			if (casEssai.getNomTestLab() != null && casEssai.getCheminTestLab() != null && casEssai.getIdUniqueTestLab() != 0 && casEssai.getNomTestPlan() != null) {
				valide = true;
			}
		}
		// On ne fait l'injection dans l'ALM que si le cas d'essai est correcttement paramètré.
		if (valide) {
			try {
				// TODO Faire une version paramètrable
				ALMServiceWrapper wrapper = connectionALMWrapper();
				// Mise à jour de l'état général du cas de test.
				ITestCase execution = wrapper.updateResult(casEssai.getCheminTestLab(), casEssai.getNomTestLab(), casEssai.getIdUniqueTestLab(), casEssai.getNomTestPlan(), etat?StatusAs.PASSED:StatusAs.FAILED);
				// Mise à jour des steps via l'ajout d'une execution
				ITestCaseRun execution_run = wrapper.createNewRun(execution, "RUN AUTO " + casEssai.getTime(), etat?StatusAs.PASSED:StatusAs.FAILED);
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
			throw new SeleniumException(Erreurs.E033, "Impossible de mettre à jour l'état du cas de test dans ALM.");
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
	
	public static void main(String argv[]) throws ALMServiceException {
		//ALMOutils.connectionALM("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//Dispatch connection = ALMOutils.connectionALMJacob("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//Dispatch scenario3 = ALMOutils.obtenirTest(ALMOutils.obtenirListeElement("Root\\POC Selenium\\IZIVENTE", connection), "SC03 - Souscription distributeur TRAVAUX CE");
	
		//ALMServiceWrapper wrapper = connectionALMWrapper("levieilfa", "Sombros99", "NATIXIS_FINANCEMENT", "CREDIT_CONSOMMATION");
		//createDefect(wrapper);
		
		//miseAJourTest(wrapper, "POC Selenium\\IZIVENTE", "SC03 - Souscription distributeur TRAVAUX CE", "SC03 - IZIVENTE_Distributeur_Travaux", 49375, StatusAs.PASSED);
		//wrapper.close();
	}
}
