package outils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Classe d'outil contenant les fonctions pour la manipulation de fichiers zip.
 * @author levieil_f
 *
 */
public class ZIPOutils {

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
