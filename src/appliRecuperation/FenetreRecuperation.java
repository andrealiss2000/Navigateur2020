/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliRecuperation;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import outils.FiltreXls;

public class FenetreRecuperation extends JFrame implements ActionListener {

	/**************************************************************************
						Attributs
	 *************************************************************************/

	private static final long serialVersionUID = 1L;

	private JFrame menu;

	private JTextField nomSortie, cheminEntree, balDeb, balFin, tpsMin, tpsMax;
	private JButton bRetour, bQuitter, bLancer, bParcourir, bReprendre;
	private JComboBox settings;
	private List<String> settingsList;
	private File f  =new File("./Annonce/ResumeRecuperation.txt");
	private File dirEntree, dirSortie;

	/**************************************************************************
						Classe intégrée spéciale (pour la fenêtre)
	 *************************************************************************/

	class MyWindowListener extends WindowAdapter {

		final FenetreRecuperation thiss;

		public MyWindowListener() {
			super();
			thiss = FenetreRecuperation.this;
		}

		public void windowClosing(WindowEvent evt) {
			System.out.println("Application fermee");
			System.exit(0);
		}
	}

	/**************************************************************************
						Méthodes principales
	 *************************************************************************/

	public FenetreRecuperation(JFrame menu) {
		super("Recuperation des sources d'URL");

		this.menu = menu;

		int w = 800, h = 300;
		defDimensions(w, h);
		init();
		addWindowListener(new MyWindowListener());
		setVisible(true);
		setFiles();
		

	}
	
	
	public void setSettingsList() {
		settingsList = new ArrayList<String>();
		Scanner sc;
		try {
			sc = new Scanner(f);
			while(sc.hasNextLine()) {
				String str = sc.nextLine(); 
				if(str.contains("Nom du projet      : ")) 
					settingsList.add(str.replace("Nom du projet      : ", ""));
			}
		} catch (FileNotFoundException e) {
			System.err.println("Fichier de recup�ration non trouv�");
		} 
		
		
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent clic) {
		String action = clic.getActionCommand();
		
		

		if (action.equalsIgnoreCase("Retour")) {
			System.out.println("Fin de recuperation");
			dispose();
			menu.setVisible(true);
		}

		if(action.equalsIgnoreCase("Quitter")) {
			System.out.println("Application fermee");
			System.exit(0);
		}

		if (action.equalsIgnoreCase("Parcourir")) {
			if (!dirEntree.exists())
				dirEntree.mkdir();

			JFileChooser chooser = new JFileChooser(dirEntree);
			chooser.setFileFilter(new FiltreXls());
			int valRetour = chooser.showOpenDialog(null);
			if (valRetour == JFileChooser.APPROVE_OPTION)
				cheminEntree.setText(chooser.getSelectedFile().toString());
		}

		if (action.equalsIgnoreCase("Lancer")) {

			if (nomSortie.getText().equals("")) {
				JOptionPane.showMessageDialog(null,
						"Veuillez entrer un nom pour le fichier texte de sortie",
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (cheminEntree.getText().equals("")) {
				JOptionPane.showMessageDialog(null,
						"Veuillez selectionner un fichier en entree",
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balDeb.getText().equals("")) {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de debut '<..>'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balDeb.getText().charAt(0) != '<') {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de debut commencant par '<'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balDeb.getText().charAt(balDeb.getText().length()-1) != '>') {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de debut terminant par '>'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balFin.getText().equals("")) {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de fin '<..>'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balFin.getText().charAt(0) != '<') {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de fin commencant par '<'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balFin.getText().charAt(balFin.getText().length()-1) != '>') {
				JOptionPane.showMessageDialog(null, 
						"Veuillez entrer une balise de fin terminant par '>'", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (balFin.getText().equals(balDeb.getText())) {
				JOptionPane.showMessageDialog(null, 
						"Les deux balises doivent etre differentes", 
						"Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			String cheminSortie = (new StringBuilder()).append(dirSortie).append("/").append(nomSortie.getText()).append(".txt").toString();

			File sort = new File(cheminSortie);
			sort.delete();
			Recuperation r = new Recuperation(balDeb.getText(), balFin.getText(), cheminSortie, cheminEntree.getText(), tpsMin.getText(), tpsMax.getText(), nomSortie.getText());
			//r.traitement();
			new Thread(r).start();

			bReprendre.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
						r.setCptErr(0);
						
					
					
				}
				
			});
			if(action.equalsIgnoreCase("Reprendre")) {
				r.setCptErr(0);
				System.out.println("AVANT LE NOTIFY ");
				r.getCptErr().notify();
				
				
				
			}
		}
	}

	/**************************************************************************
						Méthodes secondaires
	 *************************************************************************/

	/**
	 * Définition des dimensions de la fenètre
	 */
	public void defDimensions(int w, int h) {
		Toolkit aTK = Toolkit.getDefaultToolkit();
		Dimension dim = aTK.getScreenSize();
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		setBounds(x, y, w, h);
		setSize(w, h);
	}

	/**
	 * Initialisation de la fenêtre
	 */
	public void init() {
		Container c = getContentPane();

		JPanel panel = creerPanel();
		c.add(panel, "North");
	}

	/**
	 * Permet de définir tous les éléments à afficher dans la fenêtre
	 * @return		: Le contenu de la fenêtre
	 */
	public JPanel creerPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 3, 10, 10));

		JLabel fichierTXT = new JLabel("Entrez le nom du fichier de sortie");
		panel.add(fichierTXT);
		

		nomSortie = new JTextField();
		panel.add(nomSortie);
		panel.add(new JLabel());
		panel.add(new JLabel());


		panel.add(new JLabel());

		JLabel lBalises = new JLabel("BALISE de debut et BALISE de fin");
		panel.add(lBalises);

		balDeb = new JTextField();
		panel.add(balDeb);

		balFin = new JTextField();
		panel.add(balFin);
		
	
		panel.add(new JLabel());
		panel.add(new JLabel());
		
		JLabel min = new JLabel("Temps minimum (ms)");
		panel.add(min);
		
		tpsMin = new JTextField(); 
		panel.add(tpsMin); 
		
		
		JLabel max = new JLabel("Temps maximum (ms)");
		panel.add(max);
		
		tpsMax = new JTextField(); 
		panel.add(tpsMax);
		
		panel.add(new JLabel());

		

		JLabel fichEntree = new JLabel("Selectionnez un fichier EXCEL");
		panel.add(fichEntree);
		

		bParcourir = new JButton("Parcourir");
		bParcourir.addActionListener(this);
		panel.add(bParcourir);
		
		

		cheminEntree = new JTextField();
		panel.add(cheminEntree);

		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());

		bLancer = new JButton("Lancer");
		bLancer.addActionListener(this);
		panel.add(bLancer);

		panel.add(new JLabel());

		bQuitter = new JButton("Quitter");
		bQuitter.addActionListener(this);
		panel.add(bQuitter);

		panel.add(new JLabel());

		bRetour = new JButton("Retour");
		bRetour.addActionListener(this);
		panel.add(bRetour);
		
		bReprendre = new JButton("Reprendre");
		bReprendre.addActionListener(this);
		panel.add(bReprendre);
		
		setSettingsList();
		panel.add(new JLabel()); 
	
		settings = new JComboBox();
		settings.addItem("");
		for(String s : settingsList) 
			settings.addItem(s);
		settings.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				String choice = (String) e.getItem();
				if(choice.equals("")) {
					return;
				}
				try {
					Scanner sc = new Scanner(f); 
					boolean b = false;
					String strMin ="";
					String strMax="";
					String strPath="";
					String strDebutBalise="";
					String strFinBalise="";
					StringBuilder txt = null; 
					while(sc.hasNextLine()){
						String str=""; 
						int cpt=0;
						do {
							str = sc.nextLine(); 
							str = str.replace("Nom du projet      :", "").trim();
							if(str.equals(choice.trim())) {
								txt = new StringBuilder(""); 
								str = sc.nextLine(); 
								txt.append(str); 
								cpt++;
								
							}
							if(cpt>0) {
								txt.append(" <endline> ");
								txt.append(str);
							}
						}while(!str.contains("-----------------------------------------------"));
						
					}
					
					processTxt(txt.toString());
						
						
				} catch (FileNotFoundException b) {
					// TODO Auto-generated catch block
					b.printStackTrace();
				} 
				
				
				
			}
			
		});
		panel.add(settings);

		return panel;
	}

	public void processTxt(String txt) {
		if(txt.contains("Temps minimum")){
			String txtTmp = txt.substring(txt.indexOf("Temps minimum"));
			String s =  txtTmp.substring(txtTmp.indexOf("Temps minimum"), txtTmp.indexOf("<endline>"));
			s = s.replace("Temps minimum      :", "");
			tpsMin.setText(s.trim()); 
			
		}
			
		if(txt.contains("Temps maximum")){
			String txtTmp = txt.substring(txt.indexOf("Temps maximum"));
			String s =  txtTmp.substring(txtTmp.indexOf("Temps maximum"), txtTmp.indexOf("<endline>"));
			s = s.replace("Temps maximum      :", "");
			tpsMax.setText(s.trim());
		}
		if(txt.contains("Fichier source")){
			String txtTmp = txt.substring(txt.indexOf("Fichier source"));
			String s =  txtTmp.substring(txtTmp.indexOf("Fichier source"), txtTmp.indexOf("<endline>"));
			s= s.replace("Fichier source		:", "");
			cheminEntree.setText(s.trim());
			
		}
		if(txt.contains("Balise de deb")){
			String txtTmp = txt.substring(txt.indexOf("Balise de deb"));
			String s =  txtTmp.substring(txtTmp.indexOf("Balise de deb"), txtTmp.indexOf("<endline>"));
			s = s.replace("Balise de deb		: ", "");	
			balDeb.setText(s.trim());
		}
		if(txt.contains("Balise de fin")){
			String txtTmp = txt.substring(txt.indexOf("Balise de fin"));
			String s =  txtTmp.substring(txtTmp.indexOf("Balise de fin"), txtTmp.indexOf("<endline>"));
			s = s.replace("Balise de fin		: ", "");	
			balFin.setText(s.trim());
		}
		
	}

	/**
	 * Permet de construire et définir les chemins des fichiers d'entrée et de sortie
	 */
	public void setFiles() {
		File dirAgence = new File("./Annonce");
		if (!dirAgence.exists())
			dirAgence.mkdir();

		dirEntree = new File("./Annonce/Fichiers URL");
		if (!dirEntree.exists())
			dirEntree.mkdir();

		dirSortie = new File("./Annonce/Fichiers Textes");
		if (!dirSortie.exists())
			dirSortie.mkdir();
	}
}