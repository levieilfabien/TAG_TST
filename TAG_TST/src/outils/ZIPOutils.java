package outils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Classe d'outil contenant les fonctions pour la manipulation de fichiers zip.
 * @author levieil_f
 *
 */
public class ZIPOutils {
	
	/**
     * Extrait le contenu d'un fichier zip dans un repertoire choisi.
     * @param zipFile le fichier zip à fouiller (chemin complet).
     * @param output la destination pour le fichier zip
     * @param fichierAExtraire le fichier extraire au repertoire père
     */
    public static File unZip(String zipFile, String outputFolder, String fichierAExtraire){
    	File retour = null;
    	byte[] buffer = new byte[1024];

    	try{

    		//create output directory is not exists
    		File folder = new File(outputFolder);
    		if(!folder.exists()){
    			folder.mkdir();
    			retour = folder;
    		}

    		//get the zip file content
    		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    		//get the zipped file list entry
    		ZipEntry ze = zis.getNextEntry();

    		while(ze != null) {
    			String fileName = ze.getName();

    			// Si on à spécifié un fichier spécifique à extraire, on n'extrait que celui ci , sinon on extrait tout.
    			if (fichierAExtraire == null || fileName.equals(fichierAExtraire)) {

    				File newFile = new File(outputFolder + File.separator + fileName);

    				//System.out.println("Extraction du fichier : "+ newFile.getAbsoluteFile());
    				
    				// si le fichier existe déjà, impossible de l'extraire
    				if (!newFile.exists()) {
	    				//create all non exists folders
	    				//else you will hit FileNotFoundException for compressed folder
	    				new File(newFile.getParent()).mkdirs();
	
	    				FileOutputStream fos = new FileOutputStream(newFile);
	
	    				int len;
	    				while ((len = zis.read(buffer)) > 0) {
	    					fos.write(buffer, 0, len);
	    				}
	
	    				fos.close();	
    				}
    				
    				if (fileName.equals(fichierAExtraire)) {
    					retour = newFile;
    					break;
    				}
    			}
				ze = zis.getNextEntry();
    		}
    		zis.closeEntry();
    		zis.close();

    	}catch(IOException ex){
    		ex.printStackTrace();
    	}
    	return retour;
   }

	/**
	 * Permet de creer un fichier zip.
	 * @param chemin le repertoire ou le fichier zip doit être creer.
	 * @param nomFichierZip le nom du fichier zip à creer.
	 * @param fichiers les fichiers à mettre dans le zip.
	 */
	public static void creerZip(String chemin, String nomFichierZip, List<String> fichiers) {
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		try {
		    // Create the ZIP file
		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(chemin + File.separator + nomFichierZip));

		    // Compress the files
		    for (String fichier : fichiers) {
		        FileInputStream in = new FileInputStream(fichier);

		        // Add ZIP entry to output stream.
		        out.putNextEntry(new ZipEntry(new File(fichier).getName()));

		        // Transfer bytes from the file to the ZIP file
		        int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }

		        // Complete the entry
		        out.closeEntry();
		        in.close();
		    }

		    // Complete the ZIP file
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
