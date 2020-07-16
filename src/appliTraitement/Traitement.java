/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliTraitement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Traitement {

	/**************************************************************************
						Attributs
	 *************************************************************************/

	private WritableWorkbook workbookFichierSortie;
	private WritableSheet feuilleFichierSortie;

	private String cheminTxt, cheminExcel;

	private ArrayList<DonneesLigne> result;

	private ArrayList<String> balise = new ArrayList<String>();
	private String bDeb, bFin;
	
	private String ficOut;
	
	// Permet d'ajouter une fin de ligne pour l'écriture dans un fichier
	private final String NEWLINE = System.getProperty("line.separator");

	/**************************************************************************
						Méthodes publiques
	 *************************************************************************/

	public Traitement(String cheminExcel, String cheminTxt, ArrayList<JComponent> balise, String bDeb, String bFin, String fO) {

		this.bDeb = bDeb;
		this.bFin = bFin;

		for(int i = 0 ; i < balise.size(); ++i) {
			if (balise.get(i).getClass().equals(JTextField.class)) {
				JTextField j = (JTextField) balise.get(i);
				this.balise.add(j.getText());
			} else {
				JComboBox c = (JComboBox) balise.get(i);
				this.balise.add(c.getSelectedItem().toString());
			}
		}

		try {
			workbookFichierSortie = Workbook.createWorkbook(new File(cheminExcel));
			feuilleFichierSortie = workbookFichierSortie.createSheet("Donnees", 0);
			creationEntete(feuilleFichierSortie);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.cheminTxt = cheminTxt;
		this.cheminExcel = cheminExcel;
		ficOut = fO;
	}

	/**
	 * Permet d'afficher le contenu total des lignes en consoles avant leurs
	 * écriture en Excel
	 */
	public void afficherResult() {
		for (int i = 0; i < FenetreTraitement.tabNomColonnes.length-1; ++i)
			System.out.print(FenetreTraitement.tabNomColonnes[i+1] + " ");
		System.out.println();
		for (int i = 0; i < result.size(); ++i)
			System.out.println(result.get(i).toString());
		System.out.println("FIN");
	}

	/**
	 * Permet de lancer le traitement
	 */
	public void traitement() {
		
		FileWriter writer;
		try {
			writer = new FileWriter("./Annonce/ResumeTraitement.txt", true);
			
			Date d = new Date();
			writer.write("--------Début d'un nouveau traitement---------" + NEWLINE);
			writer.write("Nom du projet      : " + ficOut + NEWLINE);
			writer.write("Date 		: " + d.toString() + NEWLINE);
			writer.write("Balises	: " + NEWLINE);
			
			for (int i = 0; i < balise.size(); i+=3) {
				writer.write("	" + balise.get(i+2) + " 	:" + NEWLINE);
				writer.write("		Balise de Debut 	=> " + balise.get(i) + NEWLINE);
				writer.write("		Balise de Fin 		=> " + balise.get(i+1) + NEWLINE);
			}
			
			writer.write(NEWLINE + "Fichier source	: " + cheminTxt + NEWLINE);
			writer.write("Fichier sortie	: " + cheminExcel + NEWLINE);
			writer.write("--------------------------------------------------" + NEWLINE);
			writer.close();
			
		} catch (IOException e) {
			System.err.println("Erreur d'Entrée/Sortie avec le fichier Log.txt");
			System.err.println("Annulation du traitement");
			return;
		}
		
		System.out.println("---Début du traitement---");
		int indice = 1;
		try {
			InputStream ips = new FileInputStream(cheminTxt);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);

			boolean rec = false;

			String ligne;
			String ajout = "";
			String[] tabBalise;

			int i = 0;
			int compteur = 0;

			tabBalise = nuvTraitement(i);
			result = new ArrayList<DonneesLigne>();
			DonneesLigne DON = new DonneesLigne();

			while ((ligne = br.readLine()) != null) {

				ligne = trime(ligne);

				if (ligne.indexOf(bDeb) != -1 && rec) {
					rec = false;
					ajout += ligne.replace(bDeb, "");
					DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
					i+=3;
					if (i >= balise.size()) {
						i = 0;
						result.add(DON);
						DON = new DonneesLigne();
						++compteur;
					}
					tabBalise = nuvTraitement(i);
					System.out.println("Ligne " + indice + " coupe l'enregistrement.");
					continue;
				}

				if (ligne.indexOf(bFin) != -1 && rec) {
					rec = false;
					ajout += ligne.replace(bFin, "");
					DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
					i+=3;
					if (i >= balise.size()) {
						i = 0;
						result.add(DON);
						DON = new DonneesLigne();
						++compteur;
					}
					tabBalise = nuvTraitement(i);
					System.out.println("Ligne " + indice + " coupe l'enregistrement.");
					continue;
				}

				if (ligne.indexOf(tabBalise[0]) != -1 && ligne.indexOf(tabBalise[1]) != -1) {
					if (rec) {
						System.err.println("ERREUR : Deux balises de début rencontrées successivement");
						ajout += ligne.replace(tabBalise[0], "");
						DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
						i+=3;
						if (i >= balise.size()) {
							i = 0;
							result.add(DON);
							DON = new DonneesLigne();
							++compteur;
						}
						tabBalise = nuvTraitement(i);
						System.out.println("Ligne " + indice + " coupe l'enregistrement.");
					} else {
						ajout = ligne.replace(tabBalise[0], "");
						ajout = ajout.replace(tabBalise[1], "");
						DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
						i+=3;
						if (i >= balise.size()) {
							i = 0;
							result.add(DON);
							DON = new DonneesLigne();
							++compteur;
							System.out.println("Nb de ligne enregistrées : " + compteur);
						}
						tabBalise = nuvTraitement(i);
						System.out.println("Ligne " + indice + " enregistrée.");
					}
				}

				if (ligne.indexOf(tabBalise[0]) != -1 && ligne.indexOf(tabBalise[1]) == -1) {
					if (rec) {
						System.err.println("ERREUR : Deux balises de début rencontrées successivement");
						ajout += ligne.replace(tabBalise[0], "");
						DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
						i+=3;
						if (i >= balise.size()) {
							i = 0;
							result.add(DON);
							DON = new DonneesLigne();
							++compteur;
						}
						tabBalise = nuvTraitement(i);
						System.out.println("Ligne " + indice + " coupe l'enregistrement.");
					} else {
						rec = true;
						ajout = ligne.replace(tabBalise[0], "");
						System.out.println("Ligne " + indice + " débute l'enregistrement.");
					}
				}

				if (ligne.indexOf(tabBalise[0]) == -1 && ligne.indexOf(tabBalise[1]) != -1) {
					if (rec) {
						rec = false;
						ajout += ligne.replace(tabBalise[1], "");
						DON.ajouter(ajout, recupIndice(tabBalise[2])-1);
						i+=3;
						if (i >= balise.size()) {
							i = 0;
							result.add(DON);
							DON = new DonneesLigne();
							++compteur;
						}
						tabBalise = nuvTraitement(i);
						System.out.println("Ligne " + indice + " coupe l'enregistrement.");
					} else
						System.err.println("ERREUR : Deux balises de fin rencontrées successivement");
				}

				if (ligne.indexOf(tabBalise[0]) == -1 && ligne.indexOf(tabBalise[1]) == -1) {
					if (rec) {
						ajout += ligne;
						System.out.println("Ligne " + indice + " ajoutée pour enregistrement.");
					} else {
						System.out.println("Ligne " + indice + " ignorée");
					}
				}
				++indice;
			}
			decouperAdresse();
			integrerInfos();
		} catch (Exception e) {
			System.err.println("Ligne " + indice + " en erreur!");
		}
		try {
			workbookFichierSortie.write();
			workbookFichierSortie.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("---Fin du traitement---");
	}

	/**************************************************************************
						Méthodes privées
	 *************************************************************************/

	/**
	 * Méthode qui crée les entêtes du fichier excel en sortie
	 * @param sheet feuille excel en sortie
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	private void creationEntete(WritableSheet sheet) throws RowsExceededException, WriteException {
		// Création du format des entêtes des cellules
		WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 10,WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK, ScriptStyle.NORMAL_SCRIPT);
		WritableCellFormat arial10format = new WritableCellFormat(arial10font);
		try 
		{
			arial10format.setBackground(Colour.GRAY_25);
			arial10format.setAlignment(Alignment.CENTRE);
			arial10format.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
			arial10format.setVerticalAlignment(VerticalAlignment.BOTTOM);
		} 
		catch (WriteException e) 
		{
			e.printStackTrace();
		}

		// Création des entêtes du fichier excel en sortie
		// Ajout des cellules d'entêtes
		for (int i = 0; i < FenetreTraitement.tabNomColonnes.length-1; ++i) {
			Label lab = new Label(i, 0, FenetreTraitement.tabNomColonnes[i+1], arial10format);
			sheet.addCell(lab);
		}
	}

	/**
	 * Permet de traiter les abréviations avant enregistrement dans la base
	 * @param chaine		: La chaine à analyser
	 * @return				: La chaine à intégrer dans la base
	 */
	private String traduireAbreviationRue(String chaine) {

		String abrev[] = {
				" r ", " rue ", "r ", " av ", " avenue ", "av ", " pl ", " place ", "pl ", " rte ", 
				" route ", "rte ", " bld ", " boulevard ", "bld ", " bd ", "bd ", "fbg", "faubourg", "fbg", 
				"chem", "che ", "chemin ", "chemin ", " pl ", "pl ", " all ", "all ", " all\351e ", " mar\351chal "," marechal ", "marechal ", 
				"mar\351chal ", " g\351n\351ral ", "g\351n\351ral ", " general ", "general ", " commandant ", "commandant ", " mar "
		};

		String expReg[] = {
				" r ", " rue ", "^r ", " av ", " avenue ", "^av ", " pl ", " place ", "^pl ", " rte ", 
				" route ", "^rte ", " bld ", " boulevard ", "^bld ", " bd ", "^bd ", "fbg", "faubourg", "^fbg", 
				"chem", "che ", "chemin ", "^chemin ", " pl ", "^pl ", " all ", "^all ", " all\351e ", " mar\351chal ", " marechal ", "^marechal ",
				"^mar\351chal ", " g\351n\351ral ", "^g\351n\351ral ", " general ", "^general ", " commandant ", "^commandant ", " mar "
		};

		String trad[] = {
				" Rue ", " Rue ", "Rue ", " Avenue ", " Avenue ", "Avenue ", " Place ", " Place ", "Place ", " Route ", 
				" Route ", "Route ", " Bd ", " Bd ", "Bd ", " Bd ", "Bd ", "FBG", "FBG", "FBG", 
				"Chemin", " Chemin ", " Chemin ", "Chemin ", " Place ", "Place ", " All\351e ", "All\351e ", " All\351e ", " Mal ", " Mal ", " Mal ", 
				"Mal ", " Gal ", "Gal ", "Gal ", "Gal ", " Cdt ", "Cdt ", " Mal "
		};

		for(int i = 0; i < abrev.length; i++) {
			Pattern motif = Pattern.compile(expReg[i]);
			Matcher m = motif.matcher(chaine);
			if(m.find()) {
				StringBuffer temp = new StringBuffer(chaine);
				temp.replace(temp.indexOf(abrev[i]), temp.indexOf(abrev[i]) + abrev[i].length(), trad[i]);
				chaine = temp.toString();
			}
		}
		return chaine;
	}

	/**
	 * Permet de définir un tableau contenant la balise de départ à rechercher pour ce passage,
	 * ainsi que la balise de fin et la case où ces données doivent être enregistrées
	 * @param i		: Le passage
	 * @return		: Le tableau des balises à rechercher pour ce passage
	 */
	private String[] nuvTraitement(int i) {
		String[] tabBal = new String[3];
		tabBal[0] = balise.get(i);
		tabBal[1] = balise.get(i+1);
		tabBal[2] = balise.get(i+2);
		return tabBal;
	}

	/**
	 * Permet de connaitre l'indice de la colonne Excel correspondant à la chaine indiquée
	 * @param nom		: La chaine
	 * @return			: L'indice de la colonne
	 */
	private int recupIndice(String nom) {
		for (int i = 0; i < FenetreTraitement.tabNomColonnes.length; ++i)
			if (nom.equalsIgnoreCase(FenetreTraitement.tabNomColonnes[i]))
				return i;
		return -1;
	}

	/**
	 * Permet de trimer à gauche et droite de la chaine
	 * @param ligne	: La chaine à trimer
	 * @return		: La chaine trimée
	 */
	private String trime(String ligne) {
		String line = ligne;

		line = trimLeft(line);
		line = trimRight(line);

		return line;
	}

	/**
	 * Permet de supprimer tous les espaces à gauche de la chaine concernée
	 * @param s		: La chaine à trimer
	 * @return		: La chaine trimée
	 */
	private String trimLeft(String s) {
		return s.replaceAll("^\\s+", "");
	}

	/**
	 * Permet de supprimer tous les espaces à droite de la chaine concernée
	 * @param s		: La chaine à trimer
	 * @return		: La chaine trimée
	 */
	private String trimRight(String s) {
		return s.replaceAll("\\s+$", "");
	}

	/**
	 * Permet d'extraire le code postal, la ville et le numéro de rue présent dans l'adresse
	 */
	private void decouperAdresse() {
		for (int i = 0; i < result.size(); ++i) {

			String info = result.get(i).getDonnees(recupIndice("ADRESSE"));

			String codePostal = "";
			String ville = "";
			String nRue = "";

			//séparation codePostal avec le nom de la ville
			Pattern patternCodePostal = Pattern.compile("[0-9]{5} [a-zA-Z -]+");
			Matcher matcherCodePostal = patternCodePostal.matcher(info);
			String ligneTrouvee = "";
			if(matcherCodePostal.find()) {
				//code postal
				ligneTrouvee = matcherCodePostal.group().trim();
				codePostal = ligneTrouvee.substring(0, 5);
				result.get(i).ajouter(codePostal, recupIndice("CODE"));

				//on enleve le codePostal de l'adresse
				ville = ligneTrouvee.replace(codePostal, "").trim();

				//ville
				ville = ville.toUpperCase();
				result.get(i).ajouter(ville, recupIndice("VILLE"));
			}

			info = info.replace(ligneTrouvee, "");

			//séparation numéro de rue avec l'adresse
			Pattern patternNRue = Pattern.compile("[0-9][-|/]*[0-9]*");
			Matcher matcherNRue = patternNRue.matcher(info);

			//numéro de rue trouvé
			if(matcherNRue.find()) {       	
				nRue = matcherNRue.group().trim();
				info = info.replace(nRue, "");
				info = traduireAbreviationRue(info.toLowerCase()).trim();
				info = info.replace(",", "").trim();
				if(info.contains("bis") || info.contains("Bis")) {
					info = info.replace("bis", "").trim();
					nRue = nRue+" B";
					result.get(i).ajouter(nRue, recupIndice("N° de rue"));
				}
				info = info.trim();
			}
			else {
				info = traduireAbreviationRue(info.toLowerCase()).trim();
				info = info.replace("n'hésitez pas à nous consulter", "").trim();
				result.get(i).ajouter(info, recupIndice("ADRESSE"));
			}
		}
	}

	/**
	 * Permet de recopier chaque ligne dans la ligne Excel qui lui est associée
	 */
	private void integrerInfos() {

		for (int i = 0; i < result.size(); ++i) { // nb de lignes
			for (int j = 0; j < FenetreTraitement.tabNomColonnes.length-1; ++j) { // nb de colonnes
				try {
					String info = result.get(i).getDonnees(j);
					feuilleFichierSortie.addCell(new Label(j, i+1, info));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}