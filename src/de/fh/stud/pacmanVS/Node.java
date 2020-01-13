package de.fh.stud.pacmanVS;
import static de.fh.pacmanVS.enums.VSPacmanAction.*;
import static de.fh.stud.pacmanVS.constants.*;

import java.lang.Math;
import java.util.ArrayList;

import de.fh.pacmanVS.enums.VSPacmanAction;

public class Node {
	private static int idCounter=0;// nur für debugging
	public static int Xsize;
	Node parent;				// der ElternKnoten (null wenn dier Knoten die Wurzel des Baums ist
	Node[] Children;
	MCTS tree;
	WorldState Weltzustand;
	VSPacmanAction action;		// aktion mit dem dieser Zustand erreicht wurde
	int simulationCount=0;		// anzahl all aller spielsimulationen von diesem Knoten und aller Kind Knoten
	double totalScore=0;				// gesammt score aller gespielten Simulationen von diesem knoten und aller kind knoten (optional: + heuristische bewertung dieses Knoten)
	
	double ownScore=0;				// nur für debugging
	double heuristicScore;
	final int id;						// nur für debugging
	
	public Node(WorldState Weltzustand,MCTS tree) {	// Kosntruktor für wurzelknoten
		this(Weltzustand,null,tree);
	}
	
	public Node(WorldState Weltzustand,Node parent,MCTS tree) {
		this.id=idCounter++;
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
		double Score;
		BackpropagationScore bScore = new BackpropagationScore();
		
		switch(action) {
		case WAIT: 		bScore.importFromVolatile(tree.WaitScore); 		break;
		case GO_NORTH: 	bScore.importFromVolatile(tree.GoNorthScore);	break;
		case GO_SOUTH:	bScore.importFromVolatile(tree.GoSouthScore);	break;
		case GO_WEST: 	bScore.importFromVolatile(tree.GoWestScore);	break;
		case GO_EAST: 	bScore.importFromVolatile(tree.GoEastScore);	break;
		default: System.err.println("Fehler in BackPropagation Methode: action="+action+" DAS PROBLEM MUSS BEHOBEN WERDEN!");
			Score=123456789;
		}
		while(currentNode!=null){
			currentNode.simulationCount++;
			currentNode.totalScore+=bScore.gameScore;
			currentNode.ownScore+=bScore.PacScore[WorldState.zugreihenfolge[currentNode.Weltzustand.amZug]];
			currentNode=currentNode.parent;
		}
	}
	
	
	private static final int WAIT_PUNNISH=4;
	public double getUCB1(boolean unserZug) {
		double Score;
		if(unserZug) {
			Score= (simulationCount==0)? Double.MAX_VALUE : (totalScore+ownScore)/simulationCount+15*Math.sqrt(Math.log(tree.root.simulationCount)/simulationCount);			
		}else{
			Score= (simulationCount==0)? Double.MAX_VALUE : (totalScore-ownScore)/simulationCount+15*Math.sqrt(simulationCount/Math.log(tree.root.simulationCount));
		}

		if(constants.DEBUG_UCB1) System.out.println("UCB1= "+Score+"    NodeCount="+simulationCount+" TreeCount="+tree.root.simulationCount+" totalScore="+totalScore+"() ownScore="+ownScore);

		if(Weltzustand.action==WAIT) {
			int punnish=unserZug?-WAIT_PUNNISH:WAIT_PUNNISH;
			if(WorldState.zugreihenfolge[Weltzustand.amZug]<3) {
				if((Weltzustand.world[Weltzustand.PacPos[WorldState.zugreihenfolge[Weltzustand.amZug]]]&B13)==0) {
					return Score+punnish;
				}
			}else {
				if((Weltzustand.world[Weltzustand.PacPos[WorldState.zugreihenfolge[Weltzustand.amZug]]]&B13)!=0) {
					return Score+punnish;
				}
			}
			// Knoten bei denen ein Wait ausgeführt wurde von einem Pacman der nicht an der genze steht bekommen einen schlechteren UCB1 Score damit
			// für diese aktionen keine tiefen äste gebildset werden
			int prevZug=Weltzustand.amZug-1;
			if(prevZug<0) 
				prevZug=5;
			if(Math.abs(Base.IntToVector2(Weltzustand.PacPos[WorldState.zugreihenfolge[prevZug]]).getX()-(Xsize/2))>3) {
				if(Score>0) {
					//System.err.println("Score reduced");
					Score-=4;
				}
			}			
		}
		
		return Score;
	}
	
	
	public String NodeToString() {
		String result="";
		result+="parentID "+((parent==null)?"null":parent.id)+" ownID="+id+" ownScore="+ownScore+" totalScore="+totalScore+" simulationcount="+simulationCount+" UCB1="+getUCB1(WorldState.zugreihenfolge[Weltzustand.amZug]<3);
		if(Children!=null) {
			for(int i=0;i<Children.length;i++) {
				result+=" ChildID="+Children[i].id;
			}
		}

		return result;
	}

}
