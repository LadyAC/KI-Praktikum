package de.fh.stud.pacmanVS;

import java.util.concurrent.Phaser;

public class Playout extends Thread{
	public volatile double score;
	int id;
	MCTS tree;
	Phaser MCTSphaser;
	Phaser phaser;
	volatile WorldState toSimulate; 
	// 0=waiting for new worldstate	
	// 1=Worldstate needs to be simulated	
	// 2=Simulating

	
	public Playout(int id,MCTS tree) {
		super("PlayoutThread_"+id);
		this.MCTSphaser=tree.phaser;
		this.id=id;
		this.tree=tree;
		this.phaser=new Phaser(1);
	}
	
	public void run() {
		while(true){
			phaser.awaitAdvance(phaser.getPhase());	// warte auf anweisung des MCTS threads weiterzuarbeiten
			
//			System.out.println("PlayoutThread "+id+": führe Simulation aus (MCTS-Phase="+tree.phase+")");
			int score=toSimulate.SimulateGame();
			switch(id){
			case 0: tree.WaitScore=score;  		break;
			case 1: tree.GoWestScore=score;		break;
			case 2: tree.GoEastScore=score;		break;
			case 3: tree.GoNorthScore=score;	break;
			case 4: tree.GoSouthScore=score;	break;
			default: System.out.println("invalid Playout ID ="+id);
			}
//			System.out.println("PlayoutThread "+id+": habe die Simulation Abgeschlossen");
			MCTSphaser.arriveAndDeregister();	// sage dem MCTS thread das die Berechnung abgeschlossen ist
//			System.out.println("mctsphaser registered parties "+MCTSphaser.getRegisteredParties());
		}
		

	}
}
