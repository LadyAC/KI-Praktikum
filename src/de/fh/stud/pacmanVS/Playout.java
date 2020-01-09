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
			double score=toSimulate.SimulateGame();
			switch(id){
			case 0: tree.WaitScore=score;  		break;
			case 1: tree.GoWestScore=score;		break;
			case 2: tree.GoEastScore=score;		break;
			case 3: tree.GoNorthScore=score;	break;
			case 4: tree.GoSouthScore=score;	break;
			default: System.out.println("invalid Playout ID ="+id);
			}
			tree.phaser.arrive();
		}
		

	}
}
