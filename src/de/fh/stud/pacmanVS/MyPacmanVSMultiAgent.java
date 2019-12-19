package de.fh.stud.pacmanVS;

import de.fh.agent.PacmanVSMultiAgent;
import de.fh.agent.VSPacmanAgent;

public class MyPacmanVSMultiAgent extends PacmanVSMultiAgent {

	//Diese Klasse ermoeglicht es euch, 3 Agenten gleichzeitig zu starten.
	//Ebenso könnt ihr mit dieser Klasse die Kommunikation zwischen Agenten vereinfachen.
	//Ihr könnt hier z.B. statische Variablen anlegen, auf die alle 3 Agenten zugreifen können.
	
	public static void main(String[] args) {
		start(new MyPacmanVSMultiAgent(), "127.0.0.1", 5000);
	}

	//Hier könnt ihr eigene Agentenklassen zurückgeben
	
	@Override
	public VSPacmanAgent agent1() {
		return new MyPacmanVSAgent("Arktos");
	}

	@Override
	public VSPacmanAgent agent2() {
		return new MyPacmanVSAgent("Zapdos");
	}

	@Override
	public VSPacmanAgent agent3() {
		return new MyPacmanVSAgent("Lavados");
	}

}
