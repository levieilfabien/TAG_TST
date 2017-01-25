package extensions;

import java.io.File;

import org.apache.log4j.Logger;

import outils.ALMOutils;
import atu.alm.wrapper.bean.ServerDetails;
import atu.alm.wrapper.collection.ListWrapper;
import atu.alm.wrapper.enums.DefectPriority;
import atu.alm.wrapper.enums.DefectSeverity;
import atu.alm.wrapper.enums.DefectStatus;
import atu.alm.wrapper.enums.StatusAs;
import atu.alm.wrapper.exceptions.ALMServiceException;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComException;
import com.jacob.com.ComFailException;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

import extensions.impl.ALMAttachement;
import extensions.impl.ALMAttachementStorage;
import extensions.impl.ALMRun;
import extensions.impl.ALMRunFactory;
import extensions.impl.ALMStep;
import extensions.impl.ALMStepFactory;
import extensions.impl.ALMTDConnection;
import extensions.impl.ALMTSTest;
import extensions.impl.ALMTSTestFactory;
import extensions.impl.ALMTestSet;
import extensions.impl.ALMTestSetFolder;
import extensions.impl.ALMTestSetTreeManager;
import extensions.interfaces.ALMHasAttachement;
import extensions.interfaces.IALMDefect;
import extensions.interfaces.IALMRun;
import extensions.interfaces.IALMTestCase;
import extensions.interfaces.IALMTestCaseRun;
import extensions.interfaces.IALMTestSet;

/**
 * Classe permettant la manipulation du contexte ALM en ouvrant une communication de type "COM" avec le OTAClient.dll.
 * @author levieilfa
 *
 */
public class SeleniumALMWrapper {

	/**
	 * Le logger des différents action du Wrapper.
	 */
	static Logger logger = Logger.getLogger(SeleniumALMWrapper.class);
	
	/**
	 * Les informations sur le serveur.
	 */
	private ServerDetails serverDetails = null;
	
	/**
	 * La connection à ALM.
	 */
	private ALMTDConnection almObj = null;

	/**
	 * Constructuer par défaut du Wrapper.
	 * @param url l'url vers ALM.
	 */
	public SeleniumALMWrapper(String url) {
		this.serverDetails = new ServerDetails();
		this.serverDetails.setUrl(url);
	}
	
	/**
	 * Permet de créer un nouveau Run pour un cas de test.
	 * @param tsTest le cas de test
	 * @param runName le nom a donner au run.
	 * @param as l'état du run à la création.
	 * @return le run à manipuler.
	 */
	public IALMRun creationNouveauRun(IALMTestCase tsTest, String runName, StatusAs as) {
		// Dispatch.get(tsTest, "RunFactory").toDispatch()
		ALMRunFactory runFactory = tsTest.getRunFactory();
		ALMRun run = runFactory.addItem();
		run.setStatus(as);
		run.setName(runName);
		run.post();
		return run;
	}

	/**
	 * Permet de mettre à jour l'état d'un cas de test appartenant à un "Test Set".
	 * @param testSetFolderPath l'emplacement du test set dans le test lab.
	 * @param testSetName le test set dans le test lab.
	 * @param testSetID l'identifiant unique du test set dans le test lab.
	 * @param tcName le test du test set à mettre à jour (test plan)
	 * @param as l'état
	 * @return le cas de test mis à jour
	 * @throws ALMServiceException en cas d'erreur.
	 */
	public IALMTestCase miseAJourTest(String testSetFolderPath, String testSetName, int testSetID, String tcName, StatusAs as)
			throws ALMServiceException {
		// Récupération du répertoire dans ALM "Test Lab"
		ALMTestSetTreeManager testSetTreeManager = getAlmObj().getTestSetTreeManager();
		ALMTestSetFolder testSetFolder = testSetTreeManager.getNodeByPath(testSetFolderPath);
		// Récupération de l'instance du scénario dans le "Test Lab"
		ALMTestSet testSet = testSetFolder.findTestSet(testSetName, testSetID);
		// Création d'une instance de manipulation du "Test Set" du scénario.
		ALMTSTestFactory tsTestFactory = testSet.getTSTestFactory();
		// On parcours des cas de tests (issus du "Test Plan") associé au "Test Set".
		ListWrapper<ALMTSTest> listWrapper = tsTestFactory.getNewList();
		// On cherche dans les cas de test celui qui correspond au nom passé en paramètre et on modifie son statut.
		for (ALMTSTest tsTest : listWrapper) {
			if (tcName.equals(tsTest.getTestName())) {
				tsTest.putStatus(as);
				tsTest.post();
				return tsTest;
			}
			//TODO A supprimer
			System.out.println(tsTest.getTestName());
		}
		throw new ALMServiceException("Impossible de trouver le test portant le nom de \"" + tcName + "\"");
	}

	/**
	 * Cette méthode permet d'ajouter une pièce jointe à un élément quel qu'il soit (ex : Step, test ...)
	 * On considère ceci comme un acte "facultatif" vis à vis de la sauvegarde dans ALM. Une erreur n'est donc pas bloquante.
	 * @param attachmentPath le chemin vers le fichier
	 * @param objTest l'élement qui reçoit le fichier
	 * @return true si le fichier est joint, false sinon.
	 **/
	public static boolean ajouterPieceJointe(String attachmentPath, Dispatch objTest) {

		boolean attachFileStatus = false;
		// Attaching file to test instance

		String attachmentFileName = attachmentPath.substring(attachmentPath.lastIndexOf("\\") + 1, attachmentPath.length());
		String attachmentFilePath = attachmentPath.substring(0, attachmentPath.lastIndexOf("\\"));

//		System.out.println("attachment File Name:  " + attachmentFileName);
//		System.out.println("attachment File Path:  " + attachmentFilePath);

		try {
			// Use Attachments to get the attachment factory
			Dispatch attachFact = Dispatch.call(objTest, "attachments").toDispatch();
			// Add a new extended storage object,an attachment
			Dispatch attachObj = Dispatch.call(attachFact, "AddItem", attachmentFileName).toDispatch();
			// Modify the attachment description
			Dispatch.put(attachObj, "Description", "File Attachment");
			// Update the attachment record in the project database
			Dispatch.call(attachObj, "Post");
			// Get the attachment extended storage object
			Dispatch ExStrg = Dispatch.call(attachObj, "AttachmentStorage").toDispatch();
			// Specify the location of the file to upload.
			Dispatch.put(ExStrg, "ClientPath", attachmentFilePath);
			// Use IExtendedStorage.Save to upload the file
			logger.info("Ajout d'une pièce jointe dans QC");
			Dispatch.call(ExStrg, "Save", attachmentFileName, "True");
			attachFileStatus = true;
			logger.debug("Le fichier : " + attachmentFileName + " est attacher depuis le chemin : " + attachmentFilePath + " a "
					+ Dispatch.call(objTest, "Name").toString());

		} catch (ComFailException e) {
			logger.error("Impossible d'uploader le fichier :" + attachmentFileName + "  depuis le chemin : " + attachmentFilePath
					+ " Merci de vérifier si le fichier existe ::" + e.getMessage());
		} catch (ComException e) {
			logger.error("Impossible d'uploader le fichier :" + attachmentFileName + "  depuis le chemin : " + attachmentFilePath
					+ " Merci de vérifier si le fichier existe ::" + e.getMessage());
		}
		return attachFileStatus;
	}

	public boolean connect(String username, String password, String domain, String project) throws ALMServiceException {
		this.serverDetails.setUsername(username);
		this.serverDetails.setPassword(password);
		this.serverDetails.setDomain(domain);
		this.serverDetails.setProject(project);
		connectToOTA();
		return true;
	}

	/**
	 * Libère la connection COM avec OTAClient.
	 */
	private void releaseConnection() {
		try {
			if (getAlmObj().isConnected()) {
				getAlmObj().disconnect();
			}
			if (getAlmObj().isLoggedIn()) {
				getAlmObj().logout();
			}
			getAlmObj().releaseConnection();
		} catch (Exception e) {
		}
	}

	/**
	 * Connecte l'outil avec OTAClient
	 * @throws ALMServiceException si la connection est impossible (DLL non enregistré?)
	 */
	private void connectToOTA() throws ALMServiceException {
		try {
			//Rustine pour forcer la lecture d'une version de Jacob
			//System.setProperty("jacob.dll.path", "C:\\Windows\\System32\\jacob-1.18-x64.dll");
			
			//TODO Mettre en place référence dynamique
			//System.setProperty("jacob.dll.path", "C:\\Users\\levieilfa\\git\\Selenium\\TAG_TST\\src\\resources\\jacob-1.18-x86.dll");
			//System.setProperty("jacob.dll.path", "C:\\Users\\levieilfa\\git\\Selenium\\TAG_TST\\src\\resources\\jacob-1.18-x64.dll");
			//System.setProperty("jacob.dll.path", "C:\\Users\\levieilfa\\git\\Selenium\\TAG_TST\\target\\classes\\jacob-1.18-x86.dll");
			System.setProperty("jacob.dll.path", ALMOutils.getJacobDll(true));
			//System.setProperty("jacob.dll.path", "C:\\Users\\levieilfa\\git\\Selenium\\TAG_TST\\target\\classes\\jacob-1.18-x86.dll");
			
			ActiveXComponent activexObject = new ActiveXComponent("TDApiOle80.TDConnection");

			setAlmObj(new ALMTDConnection(activexObject, getServerDetails()));
			releaseConnection();
			getAlmObj().initConnectionEx(getServerDetails().getUrl());
			getAlmObj().login(getServerDetails().getUsername(), getServerDetails().getPassword());

			getAlmObj().connect(getServerDetails().getDomain(), getServerDetails().getProject());
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			throw new ALMServiceException("Il faut ajouter la librairie jacob-(version-bit-type).dll dans le System path");
		} catch (ComFailException e) {
			e.printStackTrace();
			throw new ALMServiceException("Il faut enregistrer OTAClient.dll et utiliser l'implémentation adaptée l'ALM cible.");
		}
	}

	/**
	 * Obtenir un set de test pour ensuite le manipuler.
	 * @param testSetFolderPath le repertoire du set de test.
	 * @param testSetName le nom du set de test.
	 * @param testSetID l'identifiant du sert de test.
	 * @return le set de test sous forme d'objet manipulable.
	 * @throws ALMServiceException en cas d'erreur.
	 */
	public IALMTestSet getTestSet(String testSetFolderPath, String testSetName, int testSetID) throws ALMServiceException {
		ALMTestSetTreeManager testSetTreeManager = getAlmObj().getTestSetTreeManager();

		ALMTestSetFolder testSetFolder = testSetTreeManager.getNodeByPath(testSetFolderPath);

		ALMTestSet testSet = testSetFolder.findTestSet(testSetName, testSetID);
		return testSet;
	}

	/**
	 * Ajoute un "step" à un "Run" dans ALM.
	 * @param run le run qu'on modifie.
	 * @param stepName le nom du step à ajouter
	 * @param as l'état du step que l'on ajoute.
	 * @param description la description du step.
	 * @param expected l'attendu pour ce step.
	 * @param actual l'obtenu pour ce step.
	 */
	public void addStep(IALMRun run, String stepName, StatusAs as, String description, String expected, String actual) {
		ALMStepFactory stepFactory = run.getStepFactory();
		ALMStep step = stepFactory.addItem();
		step.setStepName(stepName);
		step.setStatus(as);
		step.setDescription(description);
		step.setExpected(expected);
		step.setActual(actual);
		step.post();
	}

	/**
	 * Permet d'ajouter un nouvel attachemet dans l'objet cible.
	 * @param attachment l'attachement à ajouter (chemin vers le fichier)
	 * @param description la description de l'attachement à ajouter.
	 * @param hasAttachment l'objet qui reçoit la nouvelle pièce jointe.
	 * @throws ALMServiceException en cas d'erreur.
	 */
	public void newAttachment(String attachment, String description, ALMHasAttachement hasAttachment) throws ALMServiceException {
		File file = new File(attachment);
		if (file.exists()) {
			ALMAttachement attachmentFile = hasAttachment.getAttachments().addItem(file.getName());

			attachmentFile.setDescription(description);
			attachmentFile.post();
			ALMAttachementStorage as = attachmentFile.getAttachmentStorage();
			as.clientPath(file.getParent());
			as.save(file.getName());
		} else {
			throw new ALMServiceException("Le fichier à attaché n'existe pas");
		}
	}

	/**
	 * Permet d'initialisé un nouveau Defect "vide".
	 * @return le nouveau defect "vide" créer.
	 */
	public IALMDefect newDefect() {
		IALMDefect defect = getAlmObj().getBugFactory().addItem();
		return defect;
	}

	/**
	 * Créer un Defect alimenté par des informations de base.
	 * @param detectedBy l'identifiant de la personne qui détecte.
	 * @param assignedTo l'identifiant de la personne qui reçoit le defect.
	 * @param priority la priorité du defect
	 * @param severity la severité du defect
	 * @param status l'état du defect
	 * @param summary le résumé du defect
	 * @param detectedDate la date de détection de l'anomalie
	 * @param description la description de l'anomalie.
	 * @param isReproducible le top "reproductible" de l'anomalie.
	 * @param project le projet de rattachement (ou l'application)
	 * @param attachment l'éventuelle pièce jointe (chemin vers le fichier)
	 * @return un nouveau defect manipulable (qui à été créer).
	 * @throws ALMServiceException en cas d'erreur.
	 */
	public int newDefect(String detectedBy, String assignedTo, DefectPriority priority, DefectSeverity severity, DefectStatus status, String summary,
			String detectedDate, String description, boolean isReproducible, String project, String attachment) throws ALMServiceException {
		IALMDefect bug = getAlmObj().getBugFactory().addItem();
		if ((detectedBy != null) && (detectedBy.length() > 0)) {
			bug.setDetectedBy(detectedBy);
		}
		if ((assignedTo != null) && (assignedTo.length() > 0)) {
			bug.setAssignedTo(assignedTo);
		}
		if (priority != null) {
			bug.setPriority(priority);
		}
		if (severity != null) {
			bug.setSeverity(severity);
		}
		if (status != null) {
			bug.setStatus(status);
		}
		if ((summary != null) && (summary.length() > 0)) {
			bug.setSummary(summary);
		}
		if ((detectedDate != null) && (detectedDate.length() > 0)) {
			bug.setDetectionDate(detectedDate);
		}

		if ((description != null) && (description.length() > 0)) {
			bug.setDescription(description);
		}
		bug.isReproducible(isReproducible);
		if ((project != null) && (project.length() > 0)) {
			bug.setProject(project);
		}

		File file = new File(attachment);
		if (file.exists()) {
			ALMAttachement attachmentFile = bug.getAttachments().addItem(file.getName());

			attachmentFile.setDescription("Sample Attchment Desc");
			attachmentFile.post();
			ALMAttachementStorage as = attachmentFile.getAttachmentStorage();
			as.clientPath(file.getParent());
			as.save(file.getName());
		} else {
			throw new ALMServiceException("The Specified Attachment file does not exist");

		}

		bug.save();
		return bug.getDefectID();
	}

	/**
	 * Permet de fermer la connection COM.
	 */
	public void close() {
		try {
			releaseConnection();
		} catch (Exception e) {
		} finally {
			try {
				ComThread.Release();
			} catch (Exception e) {
			}
		}
	}

	public ServerDetails getServerDetails() {
		return this.serverDetails;
	}

	public ALMTDConnection getAlmObj() {
		return this.almObj;
	}

	public void setAlmObj(ALMTDConnection almObj) {
		this.almObj = almObj;
	}

	// public String getTestSetFolder() {
	// return this.testSetFolder;
	// }
	//
	// public void setTestSetFolder(String testSetFolder) {
	// this.testSetFolder = testSetFolder;
	// }
	//
	// public String getTestSetName() {
	// return this.testSetName;
	// }
	//
	// public void setTestSetName(String testSetName) {
	// this.testSetName = testSetName;
	// }
}
