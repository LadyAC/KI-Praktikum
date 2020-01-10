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
		switch(action) {
		case WAIT: 		Score=tree.WaitScore;		break;
		case GO_NORTH:	Score=tree.GoNorthScore;	break;
		case GO_SOUTH:	Score=tree.GoSouthScore;	break;
		case GO_WEST: 	Score=tree.GoWestScore;		break;
		case GO_EAST: 	Score=tree.GoEastScore;		break;
		default: System.err.println("Fehler in BackPropagation Methode: action="+action+" DAS PROBLEM MUSS BEHOBEN WERDEN!");
			Score=123456789;
		}
		ownScore=Score;
		while(currentNode!=null){
			currentNode.simulationCount++;
			currentNode.totalScore+=Score;
			currentNode=currentNode.parent;
		}
	}
	
	
	
	public double getUCB1() {
		double Score= (simulationCount==0)? Double.MAX_VALUE : ((double)totalScore)/simulationCount+2*Math.sqrt(2*Math.log(tree.root.simulationCount)/simulationCount);
		if(constants.DEBUG_UCB1) System.out.println("UCB1= "+Score+"    NodeCount="+simulationCount+" TreeCount="+tree.root.simulationCount+" totalScore="+totalScore);

		if(Weltzustand.action==WAIT) {
			if(WorldState.zugreihenfolge[Weltzustand.amZug]<3) {
				if((Weltzustand.world[Weltzustand.PacPos[WorldState.zugreihenfolge[Weltzustand.amZug]]]&B13)==0) {
					return Score-=4;
				}
			}else {
				if((Weltzustand.world[Weltzustand.PacPos[WorldState.zugreihenfolge[Weltzustand.amZug]]]&B13)!=0) {
					return Score-=4;
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
		result+="parentID "+((parent==null)?"null":parent.id)+" ownID="+id+" ownScore="+ownScore+" totalScore="+totalScore+" simulationcount="+simulationCount+" UCB1="+getUCB1();
		if(Children!=null) {
			for(int i=0;i<Children.length;i++) {
				result+=" ChildID="+Children[i].id;
			}
		}

		return result;
	}

}
