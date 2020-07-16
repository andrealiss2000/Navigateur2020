/**
 * @author 	: Thibaut
 * @year	: 2013
 * @encode	: UTF-8
 */

package appliTraitement;

public class DonneesLigne {

	// tableau correspondant à une ligne du fichier excel
	private String[] donnees;

	public DonneesLigne() {
		donnees = new String[FenetreTraitement.tabNomColonnes.length-1];
		for (int i = 0; i < donnees.length; ++i)
			donnees[i] = " ";
	}

	/**
	 * Permet de donner une valeur à une case du fichier excel
	 * @param don		: La nouvelle donnée de la case
	 * @param indice	: L'indice de la case
	 */
	public void ajouter(String don, int indice) {
		donnees[indice] = don;
	}

	/**
	 * Permet de récupérer la donnée présente dans la case concernée
	 * @param i			: Indice de la case
	 * @return			: La valeur (string) de la case
	 */
	public String getDonnees(int i) {
		return donnees[i];
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "|";
		for (int i = 0; i < donnees.length; ++i)
			if (donnees[i] == " ")
				result += " null |";
			else
				result += " " + donnees[i] + " |";
		return result;
	}
}