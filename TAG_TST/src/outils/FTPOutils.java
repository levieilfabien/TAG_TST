package outils;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

/**
 * Outils destinés aux transferts FPT, FTPS et SFTP.
 * @author levieil_f
 *
 */
public class FTPOutils {

	
	
	/// FONCTION POUR LE TRANSFERT
	
	/**
	 * Creer une adresse de connection pour les serveur SFTP lors d'un interraction avec un fichier.
	 * @param hostName l'hôte.
	 * @param username le login.
	 * @param password le mot de passe.
	 * @param remoteFilePath le chemin vers le fichier distant.
	 * @return la chaine complète représentant l'URL vers le fichier distant.
	 */
	public static String createConnectionString(String hostName, String username, String password, String remoteFilePath) {
	    // result: "sftp://user:123456@domainname.com/resume.pdf
		System.out.println("sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath);
	    return "sftp://" + username + ":" + password + "@" + hostName + "/" + remoteFilePath;
	}
	
	/**
	 * Creer un jeux d'options par défaut pour une connection.
	 * @return les options systeme pour la connection.
	 * @throws FileSystemException en cas d'erreur lors de la configuration.
	 */
	public static FileSystemOptions createDefaultOptions()
	        throws FileSystemException {
	    // Create SFTP options
	    FileSystemOptions opts = new FileSystemOptions();
	    // SSH Key checking
	    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
	    // Root directory set to user home
	    SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
	    // Timeout is count by Milliseconds
	    SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
	    return opts;
	}
	
	/**
	 * Permet de positionner un fichier (pas un dossier) dans un repertoire distant via un protocole SFTP.
	 * @param hostName l'hôte.
	 * @param username l'utilisateur.
	 * @param password le mot de passe.
	 * @param localFilePath le chemin vers le fichier à upload.
	 * @param remoteFilePath le chemin où uploader (nom du fichier compris).
	 */
	public static void upload(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
	 
	    File tempFile = new File(localFilePath);
	    if (!tempFile.exists())
	        throw new RuntimeException("Error. Local file not found");
	 
	    StandardFileSystemManager manager = new StandardFileSystemManager();
	 
	    try {
	        manager.init();
	 
	        // Create local file object
	        FileObject localFile = manager.resolveFile(tempFile.getAbsolutePath());
	        // Create remote file object
	        FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions());
	        // Copy local file to sftp server
	        System.out.println("Le fichier local est il repertoire : " + tempFile.isDirectory() + " , le type du fichier distant : " + remoteFile.getType());
	        if (!tempFile.isDirectory() && (remoteFile.getType() == FileType.FILE || remoteFile.getType() == FileType.IMAGINARY)) { 
	        	// SELECT_SELF?
	        	if (remoteFile.exists() && remoteFile.getType() != localFile.getType()) {
	        		System.out.println("File upload fail : RISQUE DE SUPPRESSION");
	        	} else {
	        		remoteFile.copyFrom(localFile, Selectors.SELECT_FILES);
	    	        System.out.println("File upload success : Le fichier est copier");
	        	}
	        } else {
	        	System.out.println("File upload fail : Pas de copie de repertoire");
	        }

	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
	        manager.close();
	    }
	}
	
	/**
	 * Permet de télécharger un fichier à partir d'un repertoire distant.
	 * @param hostName l'hôte.
	 * @param username le login.
	 * @param password le mot de passe.
	 * @param localFilePath le chemin vers le fichier local qui va accueillir le resultat du download.
	 * @param remoteFilePath le chemin vers le fichier à télécharger.
	 */
	public static void download(String hostName, String username, String password, String localFilePath, String remoteFilePath) {
	 
	    StandardFileSystemManager manager = new StandardFileSystemManager();
	 
	    try {
	        manager.init();
	 
	        String downloadFilePath = localFilePath.substring(0,
	                localFilePath.lastIndexOf("."))
	                + "_downlaod_from_sftp"
	                + localFilePath.substring(localFilePath.lastIndexOf("."),
	                        localFilePath.length());
	 
	        // Create local file object
	        FileObject localFile = manager.resolveFile(downloadFilePath);
	 
	        // Create remote file object
	        FileObject remoteFile = manager.resolveFile(
	                createConnectionString(hostName, username, password,
	                        remoteFilePath), createDefaultOptions());
	 
	        // Copy local file to sftp server
	        localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
	 
	        System.out.println("File download success");
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
	        manager.close();
	    }
	}
}
