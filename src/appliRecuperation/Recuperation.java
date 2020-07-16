/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliRecuperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import jxl.Sheet;
import jxl.Workbook;

public class Recuperation implements Runnable{

	/**************************************************************************
						Attributs
	 *************************************************************************/

	private Workbook workbookEntree;
	private Sheet feuilleEntree;

	private String bDebut, bFin, fichTXT, cheminExcel;
	private boolean rec, trouv;

	private ArrayList<String> listeUrls;
	
	private Boolean tmp = false;
	
	private static Integer CPT_ERR = 0;
	
	private int min ,max;

	// liste des balises � remplacer par un numero identifiable
	private String[] balises = {
			"<br/>", "<div>", "</div>", "<strong>", "</strong>", "<tr>", "</tr>", 
			"<td>", "</td>", "<th>", "</th>", "<a>", "</a>", "<em>", "</em>", 
			"<mark>", "</mark>", "<hr/>", "<p>", "</p>", "<li>", "</li>", "<ul>", 
			"</ul>", "<ol>", "</ol>", "<table>", "</table>", "<span>", "</span>",
			"<h1>", "</h1>", "<h2>", "</h2>", "<h3>", "</h3>", "<h4>", "</h4>", 
			"<h5>", "</h5>", "<h6>", "</h6>", "<u>", "</u>"
	};

	private int indice;
	private String ficOut;

	// Permet d'ajouter une fin de ligne pour l'�criture dans un fichier
	private final String NEWLINE = System.getProperty("line.separator");

	private Writer writer = null, logError = null;

	/**************************************************************************
						M�thodes publiques
	 *************************************************************************/

	public Recuperation(String deb, String fin, String txt, String excel, String mn , String mx, String fO) {
		if(!mn.equals("")) min =Integer.valueOf(mn);
		else min = 0;
		if(!mx.equals("")) max = Integer.valueOf(mx);
		else max = 0;
		bDebut = deb;
		bFin = fin;
		fichTXT = txt;
		ficOut = fO;
		cheminExcel = excel;

		listeUrls = new ArrayList<String>();

		rec = false;
		trouv = false;
		indice = 1;
	}

	/**
	 * Permet de lancer le traitement
	 */
	@Override
	public void run() {
		try {
			workbookEntree = Workbook.getWorkbook(new File(cheminExcel));
			feuilleEntree = workbookEntree.getSheet(0);

			for (int i = 0;i < feuilleEntree.getRows(); ++i){
				listeUrls.add(feuilleEntree.getCell(0, i).getContents());
			}
				
			workbookEntree.close();
		} 
		catch (Exception e) {
			System.err.println("Erreur avec le fichier Excel de sortie");
		}

		String url;
		System.out.println("---Initialisation du programme---");

		// Permet d'�crire les erreurs dans un fichier texte � part
		try {
			logError = new FileWriter("./Annonce/ErreurRecuperation.txt");
		} catch (IOException e1) {
			System.err.println("Erreur dans l'ouverture du fichier d'erreur");
			System.err.println("Annulation de la r�cup�ration.");
			return;
		}

		// Permet d'�crire un r�mum� de l'op�ration
		FileWriter logResume = null;
		try {
			logResume = new FileWriter("./Annonce/ResumeRecuperation.txt", true);
		} catch (IOException e2) {
			System.err.println("Erreur dans l'ouverture du fichier de r�sum�");
			System.err.println("Annulation de la r�cup�ration.");
			return;
		}
		try {
			Scanner sc = new Scanner(new File("./Annonce/ResumeRecuperation.txt"));
			String txt="";
			while(sc.hasNextLine()) {
				txt+= sc.nextLine();
				
			}
			int count = StringUtils.countMatches(txt, ficOut);
			if(count>0) ficOut += count; 
		} catch (FileNotFoundException e3) {
			
		} 
		
	

		Date d = new Date();

		System.out.println("Nombre d'url : " + listeUrls.size());
		System.out.println("---D�but de la r�cup�ration---");
		try {
			logResume.write("--------D�but d'une nouvelle r�cup�ration---------" + NEWLINE);
			logResume.write("Nom du projet      : " + ficOut + NEWLINE);
			logResume.write("Date 				: " + d.toString() + NEWLINE);
			logResume.write("Balise de deb		: " + bDebut + NEWLINE);
			logResume.write("Balise de fin		: " + bFin + NEWLINE);
			logResume.write("Temps minimum      : " + min + NEWLINE);
			logResume.write("Temps maximum      : " + max + NEWLINE);
			logResume.write("Nombre d'URL 		: " + listeUrls.size() + NEWLINE);
			logResume.write("Fichier source		: " + cheminExcel + NEWLINE);
			logResume.write("Fichier sortie		: " + fichTXT + NEWLINE);
		} catch (IOException e2) {
			System.err.println("Erreur �criture fichier de r�sum� !!");
		}

		boolean enableScript = true, enableActivX = true;
		int init = 0;

		try {
			// affectation au writer d'un fichier et param�tre pour �crire � la suite de ce fichier
			writer = new FileWriter(fichTXT, true);

			WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60);
			
			int cpt = 0;
			boolean b = false;
			for (int h = 0; h < listeUrls.size(); ++h) {
				synchronized(CPT_ERR) {
					while(CPT_ERR >= 3) {
						b = true;
						CPT_ERR.wait();
					}
				}
					
			
			
				if(b) {
					h -= 3;
					b=false;
				}
				
				//Choisir au hasard le nombre de temps a attendre
				int rand = (int)(Math.random()*max + min);
				
				url = listeUrls.get(h);
				int i = h+1;

				if (init != h) {
					enableScript = true;
					enableActivX = true;
				}
				cpt = 0;

				// lancement de la r�cup�ration du code source de l'url
				if (recupererSource(url, webClient, enableScript, enableActivX))
					System.out.println("Fichier source n" + i + " r�cup�r�");
				else {
					// La r�cup�ration peut �tre en echec suite � une erreur de script ou d'objet dans le site cible
					// D�sactivation des scripts de la cible
					if (enableScript && enableActivX) {
						enableScript = false;
						// enregistrement de la ligne contenant l'URL pour laquelle on d�sactive les scripts
						init = h;
						// On retente la r�cup�ration de cette URL
						--h;
						logError.write("Tentative de r�cup�rer URL n�" + i + " sans scripts" + NEWLINE);
					}
					// D�sactivation des objets de la cible
					else if (!enableScript && enableActivX) {
						enableActivX = false;
						// enregistrement de la ligne contenant l'URL pour laquelle on d�sactive les objets
						init = h;
						// On retente la r�cup�ration de cette URL
						--h;
						logError.write("Tentative de r�cup�rer URL n�" + i + " sans scripts ni objets" + NEWLINE);
					}
					// R�cup�ration impossible suite � une erreur avec l'URL concern�e ou une erreur de g�n�ration de la page
					else if (!enableScript && !enableActivX) {
						System.err.println("Fichier source n" + i + " non r�cup�r�");
						CPT_ERR++;
						logError.write("Fichier source n" + i + " non r�cup�r�" + NEWLINE);
						logError.write("URL => " + url + NEWLINE);

						// arr�t de la r�cup�ration si premi�re adresse en erreur
						if (h == 0) {
							logError.write("Probl�me avec la premi�re URL, v�rifier " +
									"votre connexion Internet et vos URL." + NEWLINE 
									+ " Arr�t de la r�cup�ration...");
							logResume.write("R�cup�ration interrompue" + NEWLINE);
							System.err.println("Arr�t de la r�cup�ration ...");
							break;
						}
					}
				}
				
				// attendre avant de recommencer
				try {
					TimeUnit.MILLISECONDS.sleep(rand);
				}catch(InterruptedException e){
					System.err.println("Interrupted Exception");
					
				}
				
			}

			webClient.close();
			writer.close();
			logError.close();
			logResume.write("--------------------------------------------------" + NEWLINE);
			logResume.close();
			System.out.println("---Fin de la r�cup�ration---");
		} catch (Exception e) {
			try {
				logError.write(e.toString() + NEWLINE);
				logError.write(e.getMessage() + NEWLINE);
			} catch (IOException e1) {
				System.err.println("ERREUR dans l'�criture du fichier d'erreur !");
				e1.printStackTrace();
			}
		}
		
	}
	

	/**************************************************************************
						M�thodes priv�es
	 *************************************************************************/

	/**
	 * Permet de r�cup�rer le fichier code source de la page concern�e
	 * @param url		: L'adresse de la page
	 * @param writer	: Le fichier de sortie
	 * @return			: Si le fichier source a pu �tre r�cup�r�
	 */
	@SuppressWarnings("deprecation")
	private boolean recupererSource(String url, WebClient webClient, boolean script, boolean activX) {

		// Permet de supprimer tous les messages d'erreurs dans la console
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setActiveXNative(activX);

		// r�cup�ration du code source de l'url
		HtmlPage page;
		try {
			page = webClient.getPage(url);
		} catch (Exception e) {
			System.err.println("URL Invalide");
			System.err.println(e.getCause());
			System.err.println(e);
			System.err.println(e.getMessage());
			try  {
				logError.write(e.toString() + NEWLINE);
				logError.write(e.getMessage() + NEWLINE);
			} catch (Exception e1) {
				System.err.println("ERREUR FATALE: Ecriture fichier erreur Developpeur impossible !");
				e1.printStackTrace();
			}
			return false;
		}

		// convertion du code source r�cup�r� au format XML (stock� en m�moire pour le moment)
		String pageAsXml = page.asXml();
		String str;
		boolean ok = false;

		// Lecture pour traitement du code source r�cup�r�
		BufferedReader reader = new BufferedReader(new StringReader(pageAsXml));
		try {
			while ((str = reader.readLine()) != null) {
				str = trime(str);
				ok = recuperationBalise(str, writer);
			}
		} catch (Exception e) {
			System.out.println("Erreur d'�criture");
			return false;
		}
		return ok;
	}

	/**
	 * Permet de d�finir si la ligne lue doit �tre r�cup�r�e (�crite dans le fichier de sortie)
	 * @param segment		: La ligne lue
	 * @param writer		: Ecriture dans le fichier de sortie
	 * @return				: Si la balise de d�part � �t� trouv�e dans la ligne lue
	 */
	private boolean recuperationBalise(String segment, Writer writer) {
		String debut = "<DEBUTBALISE>" + NEWLINE;
		String fin = "<FINBALISE>" + NEWLINE;
		boolean except = false;

		if (segment.indexOf(bDebut) != -1 && !rec) {
			// commence l'enregistrement
			rec = true;
			// signale la pr�sence de la balise de d�part
			trouv = true;
			// signale que cette ligne ne doit pas �tre enregistr�e (c'est la balise de d�but)
			except = true;
			try {
				writer.write(debut);
			} catch (Exception e){
				System.err.println("Erreur d'�criture");
				return false;
			}
		}
		else if (segment.indexOf(bDebut) != -1 && rec) {
			// signale la pr�sence de la balise de d�part alors qu'une autre � d�j� �t� trouv�e
			// et qu'entre ces deux balises il n'y a pas de balise de fin
			trouv = true;
			try {
				// enregistrement d'une balise de fin puis d'une nouvelle de d�part
				// pour continuer l'enregistrement
				writer.write(fin);
				// r�initialisation du marqueur de balise;
				indice = 1;
				writer.write(debut);
			} catch (Exception e){
				System.err.println("Erreur d'�criture");
				return false;
			}
		}
		else if (segment.indexOf(bFin) != -1 && rec) {
			// on coupe l'enregistrement
			rec= false;
			// r�initialisation du marqueur de balise;
			indice = 1;
			// signale la pr�sence de la balise de fin
			trouv = true;
			try {
				writer.write(fin);
			} catch (Exception e){
				System.err.println("Erreur d'�criture");
				return false;
			}
		}

		try {
			// condition pour �crire la ligne (entre les balises de d�part et de fin)
			if (rec && !except) {
				if (notify(segment)) {
					writer.write("<" + indice + ">" + NEWLINE);
					++indice;
				} else
					writer.write(segment + NEWLINE);
			}
		} catch (Exception e) {
			System.err.println("Erreur lors de l'�criture dans le fichier");
			return false;
		}
		return trouv;
	}

	private boolean notify(String ligne) {
		for (int i = 0; i < balises.length; ++i)
			if (ligne.equalsIgnoreCase(balises[i]))
				return true;
		return false;
	}
	
	public  Integer getCptErr(){
		return CPT_ERR;
	}
	
	public  void setCptErr(int i){
		
		 synchronized (CPT_ERR) {
			 CPT_ERR = i;
			 CPT_ERR.notifyAll();
		}
		 
	}


	/**
	 * Permet de trimer � gauche et droite de la chaine
	 * @param ligne	: La chaine � trimer
	 * @return		: La chaine trim�e
	 */
	private String trime(String ligne) {
		String line = ligne;

		line = trimLeft(line);
		line = trimRight(line);

		return line;
	}

	/**
	 * Permet de supprimer tous les espaces � gauche de la chaine concern�e
	 * @param s		: La chaine � trimer
	 * @return		: La chaine trim�e
	 */
	private String trimLeft(String s) {
		return s.replaceAll("^\\s+", "");
	}

	/**
	 * Permet de supprimer tous les espaces � droite de la chaine concern�e
	 * @param s		: La chaine � trimer
	 * @return		: La chaine trim�e
	 */
	private String trimRight(String s) {
		return s.replaceAll("\\s+$", "");
	}


}