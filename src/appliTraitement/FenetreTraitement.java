/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliTraitement;

import java.awt.Component;

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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.synth.SynthSpinnerUI;

import outils.FiltreTxt;

public class FenetreTraitement extends JFrame implements ActionListener {

	/**************************************************************************
						Attributs
	 *************************************************************************/

	private static final long serialVersionUID = 1L;

	private JFrame menu;

	private File dirEntree, dirSortie;
	
	private JComboBox settings;
	private List<String> settingsList;

	private JPanel panel;
	private JTextField jDeb, jFin, jcheminTXT, jNomExcel;
	private JLabel lDeb, lFin, lList;
	private JButton bParcourir, bRetirerChamps, bAjouterChamps, bLancer, bRetour, bQuitter;

	private int nbChamps = 0, h = 350, w = 800;

	private String nomExcel, cheminExcel;
	
	private File f  =new File("./Annonce/ResumeTraitement.txt");

	private ArrayList<JComponent> balise = new ArrayList<JComponent>();

	public static final String[] tabNomColonnes =
	{
		"","SEQ","TITRE 1","TITRE 2","PRENOM","NOM","AGE_MIN","AGE_MAX","NOM DU GERANT","ENSEIGNE 1","ENSEIGNE 2","commentaire dédoublonnage",
		"N° de rue","ADRESSE","ADRESSE VITRINE","CODE","VILLE","DEP","REGION","N°G","N°T","S","CO","EX","MB","LS","LUX","GARANTIE","T","G",
		"Tél","Fax","SIRET","salarié","salarié extérieur","Nombre de lots en gérance","Nombre de lots en copropriété","Nombre d'immeubles",
		"Responsable C","Comptabilité","Responsable G","Chiffre d'Affaires","CA COPRO","CA GERANCE","Expert-Comptable","LOGICIEL","CAPITAL",
		"BANQUE","Commentaires","SYNDICAT","LOYERS + CHARGES","CHARGES DE COPROPRIETE","NPAI MOIS"," MoisAnnée","FT","INTERNET","Mail","Test"

	};

	/**************************************************************************
						Classe intégrée spéciale (pour la fenêtre)
	 *************************************************************************/

	class MyWindowListener extends WindowAdapter {

		final FenetreTraitement thiss;

		public MyWindowListener() {
			super();
			thiss = FenetreTraitement.this;
		}

		public void windowClosing(WindowEvent evt) {
			System.out.println("Application fermée");
			System.exit(0);
		}
	}

	/**************************************************************************
						Méthodes publiques
	 *************************************************************************/

	public FenetreTraitement(JFrame menu) {
		super("Traitement");

		this.menu = menu;

		defDimensions(w, h);
		init();
		addWindowListener(new MyWindowListener());
		setVisible(true);

		setFiles();

		jDeb.setText("<DEBUTBALISE>");
		jFin.setText("<FINBALISE>");

	}
	
	public void setSettingsList() {
		settingsList = new ArrayList<String>();
		Scanner sc;
		try {
			sc = new Scanner(f);
			while(sc.hasNextLine()) {
				String str = sc.nextLine(); 
				if(str.contains("Nom du projet      : ")) {
					settingsList.add(str.replace("Nom du projet      : ", "").trim());
				}
					
			}
		} catch (FileNotFoundException e) {
			System.err.println("Fichier de traitement non trouv�");
		} 
		
		
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent clic) {
		String action = clic.getActionCommand();

		if (action.equalsIgnoreCase("Retour")) {
			System.out.println("Fin de traitement");
			// fermeture de cette fenêtre
			dispose();
			// Ouverture de la fenêtre du menu
			menu.setVisible(true);
		}

		if(action.equalsIgnoreCase("Quitter")) {
			System.out.println("Application fermée");
			// Arrêt de l'application entière
			System.exit(0);
		}

		if (action.equalsIgnoreCase("Parcourir")) {
			if (!dirEntree.exists())
				dirEntree.mkdir();

			JFileChooser chooser = new JFileChooser(dirEntree);
			chooser.setFileFilter(new FiltreTxt());
			int valRetour = chooser.showOpenDialog(null);
			if (valRetour == JFileChooser.APPROVE_OPTION) {
				jcheminTXT.setText(chooser.getSelectedFile().getPath());
				jNomExcel.setText(chooser.getSelectedFile().getName().substring(0,
						chooser.getSelectedFile().getName().length() - 4)
						+ "_traité");
			}
		}

		if (action.equals("Ajouter un champ")) {
			if (nbChamps >= tabNomColonnes.length) {
				JOptionPane.showMessageDialog(null,"Limite depassée","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			ajouterNouvelleLigne();
		}

		if (action.equals("Retirer un champs")) {
			if (nbChamps == 1) {
				JOptionPane.showMessageDialog(null, "Nombre de champs minimal atteint", "Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			supprimerLigne();
		}

		if (action.equals("Lancer")) {

			if (jcheminTXT.getText().equals("")) {
				JOptionPane.showMessageDialog(null,"Veuillez sélectionner un fichier en entrée","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (jNomExcel.getText().equals(""))
				nomExcel = "traitement";
			else
				nomExcel = jNomExcel.getText();
			cheminExcel = (new StringBuilder()).append(dirSortie).append("/").append(nomExcel).append(".xls").toString();

			if (jDeb.getText().equals("")) {
				JOptionPane.showMessageDialog(null,"Veuillez sélectionner une chaine d'entré (sur la même ligne)","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			if (jFin.getText().equals("")) {
				JOptionPane.showMessageDialog(null,"Veuillez sélectionner une chaine de fin (sur la même ligne)","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			for(JComponent t : balise) {
				if (t.getClass().equals(JTextField.class)) {
					JTextField j = (JTextField) t;
					if(j.getText().equals("")) {
						JOptionPane.showMessageDialog(null,"Veuillez remplir TOUT les champs","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				} else {
					JComboBox c = (JComboBox) t;
					if (c.getSelectedItem().toString().equals("")) {
						JOptionPane.showMessageDialog(null,"Veuillez remplir TOUT les champs","Message d'alerte", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			}

			new Traitement(cheminExcel, jcheminTXT.getText(), balise, jDeb.getText(), jFin.getText(), jNomExcel.getText()).traitement();
		}
	}

	/**************************************************************************
						Méthodes privées
	 *************************************************************************/

	/**
	 * Définition des dimensions de la fenêtre
	 */
	private void defDimensions(int w, int h) {
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
	private void init() {
		Container c = getContentPane();

		JPanel panel = creerPanel();
		c.add(panel, "North");
	}

	/**
	 * Permet de définir tous les éléments à afficher dans la fenêtre
	 * @return		: Le contenu de la fenêtre
	 */
	private JPanel creerPanel() {

		panel = new JPanel();
		// 9 cases verticales, 3 horizontales avec les espaces de 10 entre les cases
		panel.setLayout(new GridLayout(9, 3, 10, 10));

		bRetour = new JButton("Retour");
		bRetour.addActionListener(this);
		panel.add(bRetour);
		
		
        setSettingsList();
		
		settings = new JComboBox();
		settings.addItem("");
		for(String s : settingsList) 
			settings.addItem(s);
		settings.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				String choice = (String) e.getItem();
				if(choice.equals("")) return;
				if(balise.size()>3) {
					while(balise.size()> 3) {
						supprimerLigne();
					}
					JTextField j =(JTextField) balise.get(0);
					JTextField jj =(JTextField) balise.get(1);
					JComboBox c = (JComboBox) balise.get(2);
					j.setText("");
					jj.setText("");
					c.setSelectedItem(tabNomColonnes[0]);
				}else {
					JTextField j =(JTextField) balise.get(0);
					JTextField jj =(JTextField) balise.get(1);
					JComboBox c = (JComboBox) balise.get(2);
					j.setText("");
					jj.setText("");
					c.setSelectedItem(tabNomColonnes[0]);
					
				}
				try {
					Scanner sc = new Scanner(f); 
					boolean b = false;
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
						    	txt.append(str);				    	
						    }
							
						}while(!str.contains("--------------------------------------------------"));
						
						
						//lancer traitement
						
					   
					    
					}
					processTxt(txt.toString()); 
					
					
					
				} catch (FileNotFoundException b) {
					// TODO Auto-generated catch block
					b.printStackTrace();
				} 
				
				
				
				
				
			}
			
		});
		
		panel.add(settings);

		//panel.add(new JLabel());

		bQuitter = new JButton("Quitter");
		bQuitter.addActionListener(this);
		panel.add(bQuitter);
		
		//panel.add(new JLabel()); 
		//panel.add(new JLabel());
		
       

		bParcourir = new JButton("Parcourir");
		bParcourir.addActionListener(this);
		panel.add(bParcourir);
		jcheminTXT = new JTextField();
		jcheminTXT.setEditable(false);
		panel.add(jcheminTXT);
		panel.add(new JLabel());

		JLabel lExcelDest = new JLabel("Nom du fichier final :");
		panel.add(lExcelDest);
		jNomExcel = new JTextField();
		panel.add(jNomExcel);

		panel.add(new JLabel());

		lExcelDest = new JLabel("Balise de Début et de Fin générale :");

		panel.add(lExcelDest);
		jDeb = new JTextField();
		panel.add(jDeb);
		jFin = new JTextField();
		panel.add(jFin);
		
		
		
	
		panel.add(new JLabel());
		panel.add(new JLabel());
		panel.add(new JLabel());

		lDeb = new JLabel("Balise de début :");
		panel.add(lDeb);
		lFin = new JLabel("Balise de fin :");
		panel.add(lFin);
		lList = new JLabel("Nom du champs :");
		panel.add(lList);

		for (int i = 0 ;i < 2; ++i) {
			balise.add(new JTextField());
			panel.add(balise.get(balise.size()-1));
		}
		balise.add( new JComboBox(tabNomColonnes) );
		panel.add(balise.get(balise.size()-1));
		nbChamps++;

		bRetirerChamps = new JButton("Retirer un champs");
		bRetirerChamps.addActionListener(this);
		panel.add(bRetirerChamps);

		bAjouterChamps = new JButton("Ajouter un champ");
		bAjouterChamps.addActionListener(this);
		panel.add(bAjouterChamps);

		bLancer = new JButton("Lancer");
		bLancer.addActionListener(this);
		panel.add(bLancer);
		
	

		return panel;
	}
	
	
	private void processTxt(String txt) {
		
		String strDebutBalise ="";
		String strFinBalise="";
		
		ArrayList<Integer> indexes = new ArrayList<>();
		ArrayList<String> champs = new ArrayList<String>();
		for(int i = 1; i < tabNomColonnes.length; i++){
			
			int index = txt.indexOf(tabNomColonnes[i]+" 	:"); 
			if(index!=-1) {
				indexes.add(index);
				champs.add(tabNomColonnes[i]);
			}
		}
		
		for(int i = 0; i < indexes.size(); i++) {
			String texte="";
			if(i == indexes.size() -1) {
				texte = txt.substring(indexes.get(i));
			}
			else texte = txt.substring(indexes.get(i), indexes.get(i+1));
			if(texte.contains("Balise de Debut 	=> ")){
				//System.out.println("BALISE DE DEBUT "+str);
				strDebutBalise = texte.replace("Balise de Debut 	=>", ""); 
				strDebutBalise = strDebutBalise.replace(champs.get(i)+" 	:", ""); 
				strDebutBalise = strDebutBalise.substring(0, strDebutBalise.indexOf("Balise de Fin"));
			}
			
			if(texte.contains("Balise de Fin")){
				texte = texte.substring(texte.indexOf("Balise de Fin"));

				strFinBalise = texte.replace("Balise de Fin 		=>",""); 
				if(i == indexes.size() -1) 
					strFinBalise = strFinBalise.substring(0, strFinBalise.indexOf("Fichier source"));
			}
			if (balise.get(0).getClass().equals(JTextField.class) && balise.get(1).getClass().equals(JTextField.class)) {
				
				JTextField j = (JTextField) balise.get(0) ;
				JTextField jj = (JTextField) balise.get(1) ;
				
				if(j.getText().equals("") && jj.getText().equals("")) {
					j.setText(strDebutBalise.trim());
					jj.setText(strFinBalise.trim());
					
					if(balise.get(2).getClass().equals(JComboBox.class)) {
						JComboBox combo = (JComboBox) balise.get(2);
						combo.setSelectedItem(champs.get(i));
						//combo.addItem(champs.get(i));
						
					}
				}else {
					ajouterNouvelleLigne(strDebutBalise,strFinBalise, champs.get(i));
					
				}
				
				
			}
			
			
		}
		
		
		
		

	}

	/**
	 * Permet de construire et définir les chemins des fichiers d'entrée et de sortie
	 */
	private void setFiles() {
		File dirAgence = new File("./Annonce");
		if (!dirAgence.exists())
			dirAgence.mkdir();

		dirEntree = new File("./Annonce/Fichiers Textes");
		if (!dirEntree.exists())
			dirEntree.mkdir();

		dirSortie = new File("./Annonce/Fichiers Finaux");
		if (!dirSortie.exists())
			dirSortie.mkdir();
	}
	
	/**
	 * Permet d'ajouter une ligne de saisie de balises
	 */
	private void ajouterNouvelleLigne(String bDeb, String bFin, String champs) {
		panel.remove(bRetirerChamps);
		panel.remove(bAjouterChamps);
		panel.remove(bLancer);
		
		JTextField field1 = new JTextField();
		field1.setText(bDeb);
		balise.add(field1);
		panel.add(balise.get(balise.size()-1));
		
		JTextField field2 = new JTextField();
		field2.setText(bFin);
		balise.add(field2);
		panel.add(balise.get(balise.size()-1));
		
		JComboBox combo = new JComboBox(); 
		combo.addItem(champs);

		balise.add(combo);
		panel.add(balise.get(balise.size()-1));

		++nbChamps;

		panel.add(bRetirerChamps);
		panel.add(bAjouterChamps);
		panel.add(bLancer);

		panel.setLayout(new GridLayout(7+nbChamps, 3, 10, 10));

		h = h+balise.get(0).getHeight() + 10;
		defDimensions(w,h);
		setVisible(true);
	}

	/**
	 * Permet d'ajouter une ligne de saisie de balises
	 */
	private void ajouterNouvelleLigne() {
		panel.remove(bRetirerChamps);
		panel.remove(bAjouterChamps);
		panel.remove(bLancer);

		for (int i = 0 ;i < 2; ++i) {
			balise.add(new JTextField());
			panel.add(balise.get(balise.size()-1));
		}

		balise.add( new JComboBox(tabNomColonnes) );
		panel.add(balise.get(balise.size()-1));

		++nbChamps;

		panel.add(bRetirerChamps);
		panel.add(bAjouterChamps);
		panel.add(bLancer);

		panel.setLayout(new GridLayout(7+nbChamps, 3, 10, 10));

		h = h+balise.get(0).getHeight() + 10;
		defDimensions(w,h);
		setVisible(true);
	}

	/**
	 * Permet de supprimer une ligne de saisie de balises
	 */
	private void supprimerLigne() {
		panel.remove(bRetirerChamps);
		panel.remove(bAjouterChamps);
		panel.remove(bLancer);

		Component comp = balise.get(balise.size()-1);
		panel.remove(comp);
		balise.remove(balise.size()-1);

		for (int i = 0 ;i < 2; ++i) {
			comp = balise.get(balise.size()-1);
			panel.remove(balise.get(balise.size()-1));
			balise.remove(balise.size()-1);
		}

		comp = null;

		--nbChamps;

		panel.add(bRetirerChamps);
		panel.add(bAjouterChamps);
		panel.add(bLancer);

		panel.setLayout(new GridLayout(7+nbChamps, 3, 10, 10));

		h = h-balise.get(0).getHeight() - 10;
		defDimensions(w,h);
		setVisible(true);
	}
}