/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliPrincipale;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import appliRecuperation.FenetreRecuperation;
import appliTraitement.FenetreTraitement;

public class Menu extends JFrame implements ActionListener{

	/**************************************************************************
						Attributs
	 *************************************************************************/

	private static final long serialVersionUID = 1L;
	private JButton bQuitter, bTraitement, bRecup;

	/**************************************************************************
						Classe intégrée spéciale (pour la fenêtre)
	 *************************************************************************/

	class MyWindowListener extends WindowAdapter {

		final Menu thiss;

		public MyWindowListener() {
			super();
			thiss = Menu.this;
		}

		public void windowClosing(WindowEvent evt) {
			System.out.println("Application fermee");
			System.exit(0);
		}
	}

	/**************************************************************************
						Méthodes principales
	 *************************************************************************/

	public Menu() {
		super("Menu");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int w = 800;
		int h = 300;
		defDimensions(w, h);
		init();
		addWindowListener(new MyWindowListener());
		setVisible(true);
	}

	/**
	 * Permet le lancement de l'application
	 */
	public static void main(String[] args) {
		new Menu();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent clic) {
		String action = clic.getActionCommand();

		if (action.equalsIgnoreCase("Quitter")) {
			System.out.println("Application fermee");
			System.exit(0);
		}

		if (action.equalsIgnoreCase("Nouvelle recuperation")) {
			setVisible(false);
			new FenetreRecuperation(this);
		}

		if (action.equalsIgnoreCase("Nouveau traitement")) {
			setVisible(false);
			new FenetreTraitement(this);
		}
	}

	/**************************************************************************
						Méthodes secondaires
	 *************************************************************************/

	/**
	 * Définition des dimensions de la fenêtre
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
		panel.setLayout(new GridLayout(7, 2, 10, 10));

		for (int i = 0; i < 2; ++i)
			panel.add(new JLabel());

		bRecup = new JButton("Nouvelle recuperation");
		bRecup.addActionListener(this);
		panel.add(bRecup);

		panel.add(new JLabel("=> Recuperation des sources d'URL dans un fichier texte."));

		for (int i = 0; i < 2; ++i)
			panel.add(new JLabel());

		bTraitement = new JButton("Nouveau traitement");

		bTraitement.addActionListener(this);
		panel.add(bTraitement);
		panel.add(new JLabel("=> Extraction des informations du fichier texte dans un fichier Excel."));

		for (int i = 0; i < 2; ++i)
			panel.add(new JLabel());

		bQuitter = new JButton("Quitter");
		bQuitter.addActionListener(this);
		panel.add(bQuitter);

		panel.add(new JLabel("=> Arret de l'application"));

		for (int i = 0; i < 2; ++i)
			panel.add(new JLabel());

		return panel;
	}
}