package projetLeJOS;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Actions {

	private static final int ANGLE360 = 300;
	private static final int ANGLE180 = 150;

	private static final NXTRegulatedMotor PORTROUEG = Motor.A;
	private static final NXTRegulatedMotor PORTROUED = Motor.B;

	MovePilot pilote;
	RegulatedMotor pince;

	private int poleNord;

	//Constructeur : crée le pilote et fixe les vitesses, crée la pince et sa vitesse, fixe le PoleNord
	public Actions() {

		this.pilote = new MovePilot(55, 55, 145, PORTROUEG, PORTROUED, false); // marche bien
		this.pilote.setLinearSpeed(300);
		this.pilote.setAngularSpeed(100); //TODO : vitesse passée de 150 à 100 à 18h49, ne pas oublier de changer dans la méthode

		this.pince = Motor.C;
		this.pince.setSpeed(2000);

		// Fixation du Pole Nord (le camp adverse) à 0
		poleNord = 0;
	}

	// Les méthodes
	public void mouvement(int distance, boolean enCours) {
		pilote.travel(distance, enCours);
		// 2e paramètre enCours permet de mettre true/false selon si on veut faire une
		// autre action en même temps qu'avancer
	}

	/** Méthode pour faire tourner la voiture
	 * 
	 * @param angle : angle de rotation
	 * @param enCours : true si on veut exécuter les lignes de code qui suivent, sinon false
	 */
	public void tourne(int angle, boolean enCours) {
		pilote.rotate(angle, enCours);

		// On calcule la valeur qui va modifier le pole en enlevant les petites valeurs
		// d'ajustement
		// Et en vérifiant que c'est toujours entre 0 et 300
		int calculPole = angle % 300;

		// Ici on teste pour limiter les variations et éviter d'enlever 7 et pas
		// seulement 2

//		double testReste = (double) Math.abs(angle) / 10;
//
//		if (testReste > 0.5) {
//			if (calculPole < 0) {
//				calculPole += -5;
//			} else {
//				calculPole += 5;
//			}
//		}
		
		//On change la valeur dans poleNord
		int newPole = this.getPoleNord() - calculPole;
		this.setPoleNord(newPole);
	}
	
	/** Méthode pour tourner en prenant des mesures, donc même chose mais réduit la vitesse au début
	 * et la remet à la normale ensuite
	 * 
	 * @param angle : angle de rotation
	 * @param enCours : true si on veut exécuter les lignes de code qui suivent, sinon false
	 */
	public void tourneMesures(int angle, boolean enCours) {
		pilote.setAngularSpeed(50);
		tourne(angle, enCours);

		// On calcule la valeur qui va modifier le pole en enlevant les petites valeurs
		// d'ajustement
		// Et en vérifiant que c'est toujours entre 0 et 300
		int calculPole = angle % 300;

		// Ici on teste pour limiter les variations et éviter d'enlever 7 et pas
		// seulement 2
//
//		double testReste = (double) Math.abs(angle) / 10;
//
//		if (testReste > 0.5) {
//			if (calculPole < 0) {
//				calculPole += -5;
//			} else {
//				calculPole += 5;
//			}
//		}
		
		//On change la valeur dans poleNord
		int newPole = this.getPoleNord() - calculPole;
		this.setPoleNord(newPole);
		
		pilote.setAngularSpeed(100); //TODO : si on rechange la vitesse de base, faut rechanger ça
	}

	public void ouvrirPinces(boolean enCours) {
		pince.rotate(1200, enCours);
	}

	// faire deux méthodes : fermeture avec palet et sans palet

	public void fermerPinces(boolean enCours) {
		pince.rotate(-1200, enCours);
	}

	public void stopPilote() {
		pilote.stop();
	}

	public void choperPalet() {
		ouvrirPinces(false);
		mouvement(180, false);
		fermerPinces(false);
	}

	/**
	 * Méthode qui permet de lacher complètement un palet, donc ouvrir, reculer et
	 * fermer Puis se tourner
	 * 
	 */
	public void lacherPalet() {
		ouvrirPinces(false);
		mouvement(-200, false);
		fermerPinces(false);
		tourne(ANGLE180, false);
	}

	public boolean isMovingPilote() {
		return pilote.isMoving();
	}

	// Renvoie la valeur d'angle à passer pour face le camp adverse
	public int getPoleNord() {
		return this.poleNord;
	}

	public void setPoleNord(int poleNord) {
		this.poleNord = poleNord;
	}
	
	public void tournePoleNord() {
		pilote.rotate(poleNord);
		//this.setPoleNord(0); TODO : changé à 18h41 mais visiblement ça marche mieux
	}
}