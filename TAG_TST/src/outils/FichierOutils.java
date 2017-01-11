package outils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import constantes.Erreurs;
import exceptions.SeleniumException;

public class FichierOutils {

	/**
	 * Effectue le remplacement dans un fichier d'une chaine de caractère par une autre.
	 * @param fichier le fichier dans lequel on va remplacer une chaine de caractère.
	 * @param original la chaine de caractère à remplacée.
	 * @param remplacement la chaine de caractère remplacante.
	 * @return true si un remplacement à eue lieu, false sinon.
	 * @throws SeleniumException en cas d'erreur lors de la manipulation du fichier.
	 */
	public static Boolean remplacement(File fichier, String original, String remplacement) throws SeleniumException {
		Boolean retour = false;
	    try {
	        // On lit le fichier
	        BufferedReader file = new BufferedReader(new FileReader(fichier));
	        StringBuilder input = new StringBuilder();
	        String line;
	        //String input = "";

	        // Pour chaque ligne lue , on stock son contenu.
	        while ((line = file.readLine()) != null) {
	        	input.append(line + '\n');
	        }
	        file.close();

	        // On remplace la chaine original par la chaine remplacement
	        String inputString = input.toString();
	        if (inputString.contains(original)) {
	        	inputString = inputString.replace(original, remplacement); 
	        	retour = true;
	        }     

	        // write the new String with the replaced line OVER the same file
	        FileOutputStream fileOut = new FileOutputStream(fichier);
	        fileOut.write(inputString.getBytes());
	        fileOut.close();

	    } catch (Exception e) {
	    	throw new SeleniumException(Erreurs.E020, "Impossible de lire ou d'écrire dans le fichier " + fichier.getAbsolutePath() + " pour un remplacement.");
	    }
	    return retour;
	}
	
	/**
	 * Remplace une ligne de fichier contenant une souschaine par une nouvelle ligne.
	 * @param fichier le fichier où le remplacement doit avoir lieu.
	 * @param sousChaine la sous chaine que l'on cherche.
	 * @param nouvelleChaine la nouvelle chaine qui remplacement la ligne entière contenant la sous chaine.
	 * @return true si un remplacement à eue lieu, false sinon.
	 * @throws SeleniumException en cas d'erreur d'accès au fichier.
	 */
	public Boolean remplacementLigneContenant(File fichier, String sousChaine, String nouvelleChaine) throws SeleniumException {
		Boolean retour = false;
	    try {
	        // On lit le fichier
	        BufferedReader file = new BufferedReader(new FileReader(fichier));
	        List<String> contenu = new LinkedList<String>();
	        String line;
	        StringBuilder nouveauContenu = new StringBuilder();

	        // Pour chaque ligne lue , on stock son contenu.
	        while ((line = file.readLine()) != null) {
	        	contenu.add(line + '\n');
	        }
	        file.close();

	        // On remplace la chaine original par la chaine remplacement
	        for(String ligneContenu : contenu) {
		        if (ligneContenu.contains(sousChaine)) {
		        	nouveauContenu.append(nouvelleChaine); 
		        	retour = true;
		        } else {
		        	nouveauContenu.append(ligneContenu);
		        }
	        }
	        // write the new String with the replaced line OVER the same file
	        FileOutputStream fileOut = new FileOutputStream(fichier);
	        fileOut.write(nouveauContenu.toString().getBytes());
	        fileOut.close();

	    } catch (Exception e) {
	    	throw new SeleniumException(Erreurs.E020, "Impossible de lire ou d'écrire dans le fichier " + fichier.getAbsolutePath() + " pour un remplacement.");
	    }
	    return retour;
	}
	
}
