package de.fh.stud.pacmanVS;
import java.lang.Math;
import java.util.ArrayList;

import de.fh.pacmanVS.enums.VSPacmanAction;

public class Node {
	
	Node parent;				// der ElternKnoten (null wenn dier Knoten die Wurzel des Baums ist
	Node[] Children;
	MCTS tree;
	WorldState Weltzustand;
	VSPacmanAction action;		// aktion mit dem dieser Zustand erreicht wurde
	int simulationCount=0;		// anzahl all aller spielsimulationen von diesem Knoten und aller Kind Knoten
	int totalScore=0;				// gesammt score aller gespielten Simulationen von diesem knoten und aller kind knoten (optional: + heuristische bewertung dieses Knoten)
	
	
	public Node(WorldState Weltzustand,MCTS tree) {	// Kosntruktor für wurzelknoten
		this(Weltzustand,null,tree);
	}
	
	public Node(WorldState Weltzustand,Node parent,MCTS tree) {
		this.Weltzustand=Weltzustand;
		this.parent=parent;
		this.action=Weltzustand.action;
		this.tree=tree;
	}

	public void expand(){
		ArrayList<WorldState> neueZustaende=Weltzustand.expand_AllDirectionsAndWait();
		Children=new Node[neueZustaende.size()];
		for(int i=0;i<neueZustaende.size();i++) {
			Children[i]= new Node(neueZustaende.get(i),this,tree);
		}
			
		//Weltzustand=null;
		/*Der Weltzustand des Knoten wird nur für eine Spielesimulation und für das expandieren 
		  neuer Knoten benötigt die Knoten werden aber erst expandiert nachdem von diesem Knoten 
		  bereits eine Simulation erfolgt ist daher wird der Weltzustand nach dem expandieren nicht 
		  mehr benötigt und der Speicher kann von Garbage-Collector freigegeben werden*/
	}
	
	public void BackPropagation(){
		Node currentNode=this;
		int Score;
		switch(action) {
		case WAIT: 		Score=tree.WaitScore;		break;
		case GO_NORTH:	Score=tree.GoNorthScore;	break;
		case GO_SOUTH:	Score=tree.GoSouthScore;	break;
		case GO_WEST: 	Score=tree.GoWestScore;		break;
		case GO_EAST: 	Score=tree.GoEastScore;		break;
		default: System.out.println("Fehler in BackPropagation Methode: action="+action+" DAS PROBLEM MUSS BEHOBEN WERDEN!");
			Score=123456789;
		}

		while(currentNode.parent!=null){
			currentNode.simulationCount++;
			currentNode.totalScore+=Score;
			currentNode=currentNode.parent;
		}
	}
	
	
	
	public double getUCB1() {
		return (simulationCount==0)? Double.MAX_VALUE : ((double)totalScore)/simulationCount+2*Math.sqrt(Math.log(tree.root.simulationCount)/simulationCount);
	}
	
	
	
	
	
	
	
	
	
	
}
