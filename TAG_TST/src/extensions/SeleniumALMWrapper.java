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
	 * Le logger des diff�rents action du Wrapper.
	 */
	static Logger logger = Logger.getLogger(SeleniumALMWrapper.class);
	
	/**
	 * Les informations sur le serveur.
	 */
	private ServerDetails serverDetails = null;
	
	/**
	 * La connection � ALM.
	 */
	private ALMTDConnection almObj = null;

	/**
	 * Constructuer par d�faut du Wrapper.
	 * @param url l'url vers ALM.
	 */
	public SeleniumALMWrapper(String url) {
		this.serverDetails = new ServerDetails();
		this.serverDetails.setUrl(url);
	}
	
	/**
	 * Permet de cr�er un nouveau Run pour un cas de test.
	 * @param tsTest le cas de test
	 * @param runName le nom a donner au run.
	 * @param as l'�tat du run � la cr�ation.
	 * @return le run � manipuler.
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
	 * Permet de mettre � jour l'�tat d'un cas de test appartenant � un "Test Set".
	 * @param testSetFolderPath l'emplacement du test set dans le test lab.
	 * @param testSetName le test set dans le test lab.
	 * @param testSetID l'identifiant unique du test set dans le test lab.
	 * @param tcName le test du test set � mettre � jour (test plan)
	 * @param as l'�tat
	 * @return le cas de test mis � jour
	 * @throws ALMServiceException en cas d'erreur.
	 */
	public IALMTestCase miseAJourTest(String testSetFolderPath, String testSetName, int testSetID, String tcName, StatusAs as)
			throws ALMServiceException {
		// R�cup�ration du r�pertoire dans ALM "Test Lab"
		ALMTestSetTreeManager testSetTreeManager = getAlmObj().getTestSetTreeManager();
		ALMTestSetFolder testSetFolder = testSetTreeManager.getNodeByPath(testSetFolderPath);
		// R�cup�ration de l'instance du sc�nario dans le "Test Lab"
		ALMTestSet testSet = testSetFolder.findTestSet(testSetName, testSetID);
		// Cr�ation d'une instance de manipulation du "Test Set" du sc�nario.
		ALMTSTestFactory tsTestFactory = testSet.getTSTestFactory();
		// On parcours des cas de tests (issus du "Test Plan") associ� au "Test Set".
		ListWrapper<ALMTSTest> listWrapper = tsTestFactory.getNewList();
		// On cherche dans les cas de test celui qui correspond au nom pass� en param�tre et on modifie son statut.
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
	 * Cette m�thode permet d'ajouter une pi�ce jointe � un �l�ment quel qu'il soit (ex : Step, test ...)
	 * On consid�re ceci comme un acte "facultatif" vis � vis de la sauvegarde dans ALM. Une erreur n'est donc pas bloquante.
	 * @param attachmentPath le chemin vers le fichier
	 * @param objTest l'�lement qui re�oit le fichier
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
			logger.info("Ajout d'une pi�ce jointe dans QC");
			Dispatch.call(ExStrg, "Save", attachmentFileName, "True");
			attachFileStatus = true;
			logger.debug("Le fichier : " + attachmentFileName + " est attacher depuis le chemin : " + attachmentFilePath + " a "
					+ Dispatch.call(objTest, "Name").toString());

		} catch (ComFailException e) {
			logger.error("Impossible d'uploader le fichier :" + attachmentFileName + "  depuis le chemin : " + attachmentFilePath
					+ " Merci de v�rifier si le fichier existe ::" + e.getMessage());
		} catch (ComException e) {
			logger.error("Impossible d'uploader le fichier :" + attachmentFileName + "  depuis le chemin : " + attachmentFilePath
					+ " Merci de v�rifier si le fichier existe ::" + e.getMessage());
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
	 * Lib�re la connection COM avec OTAClient.
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
	 * @throws ALMServiceException si la connection est impossible (DLL non enregistr�?)
	 */
	private void connectToOTA() throws ALMServiceException {
		try {
			//Rustine pour forcer la lecture d'une version de Jacob
			//System.setProperty("jacob.dll.path", "C:\\Windows\\System32\\jacob-1.18-x64.dll");
			
			//TODO Mettre en place r�f�rence dynamique
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
			throw new ALMServiceException("Il faut enregistrer OTAClient.dll et utiliser l'impl�mentation adapt�e l'ALM cible.");
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
	 * Ajoute un "step" � un "Run" dans ALM.
	 * @param run le run qu'on modifie.
	 * @param stepName le nom du step � ajouter
	 * @param as l'�tat du step que l'on ajoute.
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
	 * @param attachment l'attachement � ajouter (chemin vers le fichier)
	 * @param description la description de l'attachement � ajouter.
	 * @param hasAttachment l'objet qui re�oit la nouvelle pi�ce jointe.
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
			throw new ALMServiceException("Le fichier � attach� n'existe pas");
		}
	}

	/**
	 * Permet d'initialis� un nouveau Defect "vide".
	 * @return le nouveau defect "vide" cr�er.
	 */
	public IALMDefect newDefect() {
		IALMDefect defect = getAlmObj().getBugFactory().addItem();
		return defect;
	}

	/**
	 * Cr�er un Defect aliment� par des informations de base.
	 * @param detectedBy l'identifiant de la personne qui d�tecte.
	 * @param assignedTo l'identifiant de la personne qui re�oit le defect.
	 * @param priority la priorit� du defect
	 * @param severity la severit� du defect
	 * @param status l'�tat du defect
	 * @param summary le r�sum� du defect
	 * @param detectedDate la date de d�tection de l'anomalie
	 * @param description la description de l'anomalie.
	 * @param isReproducible le top "reproductible" de l'anomalie.
	 * @param project le projet de rattachement (ou l'application)
	 * @param attachment l'�ventuelle pi�ce jointe (chemin vers le fichier)
	 * @return un nouveau defect manipulable (qui � �t� cr�er).
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
