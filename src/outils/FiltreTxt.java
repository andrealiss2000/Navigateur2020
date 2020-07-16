/**
 * @author 	: frederic
 * @encode	: UTF-8
 */

package outils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FiltreTxt extends FileFilter {

	/**
	 * 
	 */
	public boolean accept(File fichier) {
		if(fichier.isDirectory())
			return true;

		//on teste si l'extension du fichier est .xls
		String nomFichier = fichier.getName();
		int i = nomFichier.lastIndexOf('.');

		if (i > 0 && i < nomFichier.length() - 1) {
			String extension = nomFichier.substring(i+1).toLowerCase();
			if(extension.equals("txt"))
				return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return "Fichiers texte (.txt)";
	}
}