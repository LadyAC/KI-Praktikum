package de.fh.stud.pacmanVS;

import java.util.concurrent.Phaser;

public class Playout extends Thread{
	public volatile boolean running=false;
	public volatile double score;
	int id;
	volatile MCTS tree;
	Phaser phaser;
	private int phase=0;
	public volatile WorldState toSimulate;


	
	public Playout(int id,MCTS tree) {
		super("PlayoutThread_"+id);
		this.id=id;
		this.tree=tree;
		this.phaser=new Phaser(1);
	}
	
	public void run() {
		while(true){
			running=false;
			phaser.awaitAdvance(phase);	// warte auf anweisung des MCTS threads weiterzuarbeiten
			phase++;
			running=true;
//			try {Thread.currentThread().sleep(100);} catch (InterruptedException e) {} // delay für debugausgaben
			
			
			
//			System.out.println("recieved: Playout thread id_"+id+" phase="+phaser.getPhase());
//			System.out.println("Playout simulationstartroud: "+toSimulate.round+"_"+toSimulate.amZug);
			int score=toSimulate.SimulateGame();
//			System.out.println("Playout Thread "+id+" phase:"+MCTSphaser.getPhase()+" -> Score: "+score);
			switch(id){
			case 0: tree.WaitScore=score;  		break;
			case 1: tree.GoWestScore=score;		break;
			case 2: tree.GoEastScore=score;		break;
			case 3: tree.GoNorthScore=score;	break;
			case 4: tree.GoSouthScore=score;	break;
			default: System.out.println("invalid Playout ID ="+id);
			}
//			System.out.println("PlayoutThread "+id+": habe die Simulation Abgeschlossen");
			//System.out.println("Playout thread done_"+id);
			tree.phaser.arrive();	// sage dem MCTS thread das die Berechnung abgeschlossen ist
//			System.out.println("Playout thread_"+id+" phase= "+phaser.getPhase()+" tree phaser ARRIVED:"+tree.phaser.getArrivedParties()+" UNARRIVED:"+tree.phaser.getUnarrivedParties()+" Phase: "+tree.phaser.getPhase());
//			System.out.println("mctsphaser registered parties "+MCTSphaser.getRegisteredParties());
		}
		

	}
}
