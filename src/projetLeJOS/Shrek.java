package projetLeJOS;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Shrek {

	private final static int DEBUT = 0;
	private final static int CHERCHETOURNE = 1;
	private final static int VERSPALET = 2;
	private final static int REPOPALET = 3;
	private final static int GOCAMPADVERSE = 4;
	private final static int APRESPOINT = 5;

	private final static int PALET = 0;
	private final static int REPO = 1;
	private final static int MUR = 2;
	private final static int RIEN = -1;

	// Déclaration des ports qu'on va utiliser
	private static final String PORTUS = "S1";
	private static final String PORTOUCH = "S2";
	private static final String PORTCOLOR = "S3";

	// Variables d'angle déterminées selon le robot
	private static final int ANGLE360 = 300;
	private static final int ANGLE180 = 150;
	private static final int ANGLE120 = 100;
	private static final int ANGLE90 = 75;
	private static final int ANGLE60 = 50;
	private static final int ANGLE45 = 37;

	// Objets qui vont nous servir tout le long
	private Senseurs sensor;
	private Actions act;

	// Etat qui va diriger le switch
	private static int ETAT;

	public Shrek() {

		// instanciation d'un objet qui contrôle les senseurs
		Port us = LocalEV3.get().getPort(PORTUS);
		Port touch = LocalEV3.get().getPort(PORTOUCH);
		Port color = LocalEV3.get().getPort(PORTCOLOR);
		sensor = new Senseurs(us, touch, color);

		// Instanciation d'un objet qui contrôle les moteurs
		act = new Actions();

		// Déclaration de l'état initial, état DEBUT
		ETAT = DEBUT;

	}

	public static void main(String[] args) {
		Shrek shrek = new Shrek();

		System.out.println("En attente de lancement");
		Button.waitForAnyPress();

		// pour stopper le robot on appuie sur le bouton de retour
		while (Button.ESCAPE.isUp()) {

			switch (ETAT) {

			// Tout premier cas, on code un comportement pour attraper le premier palet et
			// le ramener
			case DEBUT:
				System.out.println("Début de partie, lancement de la première action en dur");

				ETAT = shrek.premierPaletPositionADroite();
				break;

			// Cas où le robot va tourner pour trouver un angle vers lequel aller
			case CHERCHETOURNE:
				System.out.println("Recherche d'un palet par rotation");
				ETAT = shrek.rotationInformation360();
				break;

			// Cas où le robot va aller vers un palet en vérifiant que tout se passe bien
			case VERSPALET:
				System.out.println("En direction d'un élément proche");
				ETAT = shrek.avancerVers();

				break;
			// Cas où le robot doit se repositionner en face du palet
			case REPOPALET:
				System.out.println("Repositionnement nécessaire");
				ETAT = shrek.rotationInformation90();
				break;

			// Cas où le robot vient de choper un palet pour l'amener dans le camp en face
			case GOCAMPADVERSE:
				System.out.println("Go vers chez les adversaires");
				ETAT = shrek.allerVersCampAdverse();
				break;
				
			// Cas où le robot vient de franchir une ligne blanche du camp adverse, dépôt du
			// palet
			case 5:
				System.out.println("On vient de gagner un point, on cherche");
				ETAT = shrek.rotationInformation120();
				break;
			}
		}
	}

	/**
	 * Méthode appelée au tout début de la partie, récupère en dur deux palets
	 * 
	 * @return l'état vers lequel aller ensuite
	 */
	private int premierPaletPositionADroite() {
		this.act.mouvement(400, false);
		this.act.choperPalet(); // il attrape le 1er palet

		// il évite le palet
		this.act.tourne(-45, false);
		this.act.mouvement(400, false);
		this.act.tourne(46, false);

		// Il va vers le camp adverse
		this.act.mouvement(1560, false);
		this.act.ouvrirPinces(false);
		this.act.mouvement(-200, false);
		this.act.fermerPinces(false);

		this.act.tourne(120, false); // Tour au moment de retourner chercher le 2 palet
		this.act.ouvrirPinces(false);
		this.act.mouvement(300, false);
		this.act.fermerPinces(false); // A chopé le 2e palet

		this.act.tourne(-135, false);
		this.act.mouvement(430, false); // Va vers la ligne adverse
		this.act.lacherPalet();

		this.act.mouvement(620, false);
		this.act.choperPalet();
		this.act.tourne(175, false);
		this.act.mouvement(1015, false);
		this.act.lacherPalet();
		this.act.mouvement(650, false);

		return CHERCHETOURNE; // Doit retourner 1 normalement
	}

	/**
	 * Tourne sur lui-même à 360 degrés et récupère des infos puis se tourne vers l'élément le
	 * plus proche
	 * 
	 * @return VERSPALET
	 */
	private int rotationInformation360() {
		this.act.tourneMesures(ANGLE360, true);
		int angle = this.sensor.anglePosition360(ANGLE360, this.sensor.prendreMesures(act));
		this.act.tourne(angle, false);
		return VERSPALET;
	}

	
	/** Méthode qui sert seulement quand on a mis un palet chez l'adversaire, qui cherche des palets sur 120 degrés
	 * 
	 * @return VERSPALET quand il a trouvé un palet
	 */
	private int rotationInformation120() {
		this.act.tourne(ANGLE60, false);
		this.act.stopPilote();
		this.act.tourneMesures(-ANGLE120, true);
		int angle = this.sensor.anglePosition120(ANGLE120, this.sensor.prendreMesures(act));
		this.act.tourne(angle, false);
		return VERSPALET;
	}
	
	/**
	 * Tourne de 180 degres sur lui-même et récupère des infos puis se tourne vers
	 * l'élément le plus proche
	 * 
	 * @return VERSPALET
	 */
	private int rotationInformation90() {
		this.act.tourne(ANGLE45, false);
		this.act.stopPilote();
		this.act.tourneMesures(-ANGLE90, true);
		int angle = this.sensor.anglePosition90(ANGLE90, this.sensor.prendreMesures(act));
		this.act.tourne(angle, false);
		return VERSPALET;
	}

	/**
	 * Avance vers l'élément le plus proche en vérifiant que c'est pas un mur ou
	 * qu'il ne passe pas à côté du palet
	 * 
	 * @return l'état suivant selon les paramètres
	 */
	private int avancerVers() {
		this.act.mouvement(2000, true);

		int status = this.sensor.detectPalet();
		if (status == PALET) {
			this.act.stopPilote();
			this.act.choperPalet();
			this.act.tournePoleNord();
			return GOCAMPADVERSE;
		} else if (status == REPO) {
			this.act.stopPilote();
			return REPOPALET;
		} else {
			this.act.stopPilote();
			this.act.tourne(ANGLE180, false);
			return REPOPALET;
		}
	}

	/**
	 * Méthode qui permet de s'orienter vers le camp adverse et d'aller y poser le
	 * palet
	 * 
	 * @return soit CHERCHETOURNE, soit REPOPALET si le truc rencontre un mur en
	 *         revenant vers le centre
	 */
	private int allerVersCampAdverse() {
		this.act.mouvement(4000, true);

		//Tant que ça renvoie pas un mur on passe pas
		int test = this.sensor.detectPalet();
		while (test == REPO) {
			test = this.sensor.detectPalet();
		}
		
		//Si on voit un mur on passe là
		if (this.sensor.detectPalet() == MUR) {
			this.act.stopPilote();
			this.act.lacherPalet();
			this.act.mouvement(450, true);
			return APRESPOINT;
		}
		return GOCAMPADVERSE;
	}

}