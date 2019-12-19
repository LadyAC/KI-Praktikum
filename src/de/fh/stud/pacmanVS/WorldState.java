package de.fh.stud.pacmanVS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.fh.pacmanVS.VSPacmanPercept;
//import de.fh.pacmanVS.enums.Team;
import de.fh.pacmanVS.enums.VSPacmanAction;
import de.fh.util.Vector2;

import static de.fh.pacmanVS.enums.VSPacmanAction.*;
import static de.fh.stud.pacmanVS.constants.*;
public class WorldState {
	public static int[] zugreihenfolge=new int[6];
	public static int[] spawnPosition=new int[6];
	
	public static int DotsOnEachSide=-42; //TODO: wert am anfang des Spiels setzen		// anzahl der Dots die ein Team beim spielstart besitzt
	// (zur prüfung ob ein spiel gewonnen wurde beim Rollout)
	public static int RolloutDepth=200;	// maximale tiefe eine RolloutSimulation
//	public static int NumberOfPacmans; 	// anzahl der pacmans im spiel ( =2 bei 1vs1 =6 bei 3vs3 )
	public static Random rn=new Random();
	public VSPacmanAction action;		// aktion mit dem dieser Zustand erreicht wurde
	
	// 0= unser pacman 1
	// 1= unser pacman 2
	// 2= unser pacman 3
	// 3= gegner pacman 1
	// 4= gegner pacman 2
	// 5= gegner pacman 3
	//beispiel    [0]=1 [1]=4 [2]=2 [3]=3 [4]=0 [5]=5
	//bedeutet wenn wir Blau sind zugreihenfolge:		BLAU 2 -> ROT 2 -> BLAU 3 -> ROT 1 -> BLAU 1 -> ROT 3

	private static final int[] EMPTY_ARRAY=new int[0];

	int round;
	int[] world;
	int[][] carriedDots;//=new int[6][];
	int[] PacPos;//=new int[6];
	int amZug; // id die auf das statische zugereihenfolge array verweist  
	int ourTeamDotsSecured;
	int enemyTeamDotsSecured;
	//public int bewertung;
	
//	public int PacmanamZug() {
//		return spawnPosition[amZug];
//	}
//	
	
	// Konstruktor der aus einen VSPacmanPercept Objekt das uns der Server liefert ein WorldState Objekt erstellt
	// funktioniert nur für das erste percept da wir nur im ersten Zug wissen welcher pacman welche Dots trägt
	public WorldState(VSPacmanPercept percept){	
		//TODO Sven CarriedDots
		carriedDots = new int[6][0];
		round=percept.getElapsedTurns();
		world=Base.WorldBaseBig;
		Vector2 v;
		List<Vector2> vList=percept.getRemainingOwnDots();
		for(int i=0;i<vList.size();i++){
			v=vList.get(i);
			world[Base.Vector2ToInt(v)*2] |= B14 ;
		}
		vList=percept.getRemainingOpponentDots();
		for(int i=0;i<vList.size();i++){
			v=vList.get(i);
			world[Base.Vector2ToInt(v)*2] |= B14 ;
		}
		PacPos=new int[6];
		v=percept.getPosition();
		PacPos[0]=Base.Vector2ToInt(v);
		int index=1;
		vList=percept.getTeammates();
		for(int i=0;i<vList.size();i++){
			v=percept.getTeammates().get(i);
			PacPos[index++] = Base.Vector2ToInt(v);
		}
		vList=percept.getOpponents();
		for(int i=0;i<vList.size();i++){
			v=percept.getOpponents().get(i);
			PacPos[index++] = Base.Vector2ToInt(v);
		}
		
		amZug=(zugreihenfolge[0]==0)?0:1; 
		ourTeamDotsSecured = 0;
		enemyTeamDotsSecured = 0;
	}
	
	
	// Konstruktor für Simulation
	public WorldState(int[] world,int[][] carriedDots,int[] PacPos,int amZug,int round,int ourTeamDotsSecured,int enemyTeamDotsSecured){
		this.world=world;
		this.carriedDots=carriedDots;
		this.PacPos=PacPos;
		this.amZug=amZug;
		this.round=round;
		this.ourTeamDotsSecured=ourTeamDotsSecured;
		this.enemyTeamDotsSecured=enemyTeamDotsSecured;
	}
	
	// Konstruktor für Tree expansion
	public WorldState(int[] world,int[][] carriedDots,int[] PacPos,int amZug,int round,int ourTeamDotsSecured,int enemyTeamDotsSecured,VSPacmanAction action){
		this(world,carriedDots,PacPos,amZug,round,ourTeamDotsSecured,enemyTeamDotsSecured);
		this.action=action;
	}
	
	public ArrayList<WorldState> expand_AllDirectionsAndWait() {
		ArrayList<WorldState> neu=expand_AllDirections();
		int RoundNext,amZugNeu;
		if(amZug==5) {
			amZugNeu=0;
			RoundNext=round+1;
		}else {
			amZugNeu=amZug+1;
			RoundNext=round;
		}
		neu.add(new WorldState(world,carriedDots,PacPos,amZugNeu,RoundNext,ourTeamDotsSecured,enemyTeamDotsSecured,VSPacmanAction.WAIT));
		return neu;
	}

	
	public ArrayList<WorldState> expand_AllDirections() {
		/*	die expand Methode simuliert einen schritt in alle 4 Himmelsrichtungen
		und wenn diese Aktionen nicht damit enden das eine Wand oder ein Pacman
		deselben Teams den Weg versperren wird ein Knoten Objekt erzeugt und in
		die Arraylist eingefügt welche die Methode am ende zurueckliefert.	 */
		
		ArrayList<WorldState> neu=new ArrayList<WorldState>(4);
		int posNeu,shift = -42,ourTeamDotsSecuredNew,enemyTeamDotsSecuredNew,pID,RoundNext,amZugNeu;	
		int currentPacman=zugreihenfolge[amZug];
		int PacManPosition=PacPos[currentPacman]<<1;
		int PacmanPosData=world[PacManPosition];
		int[] worldNew,tmpCarry,PacPosNew;
		int[][] carriedDotsNew;
		boolean isOwnSide = false,isOurPacman=currentPacman<3;
		VSPacmanAction expandAction=WAIT;
		if(amZug==5){
			amZugNeu=0;
			RoundNext=round+1;
		}else {
			amZugNeu=amZug+1;
			RoundNext=round;
		}
		for(int direction=0;direction<4;direction++){
			posNeu=-42;
			switch(direction){
			case 0:	if((PacmanPosData&B1)!=0)			{expandAction=GO_WEST;	posNeu=PacManPosition-2;		 }	break;	//links expandieren
			case 1:	if((PacmanPosData&B2)!=0)			{expandAction=GO_EAST;	posNeu=PacManPosition+2;		 }	break;	//rechts expandieren
			case 2:	if((shift=(PacmanPosData&E5N2))!=0)	{expandAction=GO_NORTH;	posNeu=PacManPosition-(shift>>1);}	break;	//oben expandieren
			case 3: if((shift=(PacmanPosData&E5N7))!=0)	{expandAction=GO_SOUTH;	posNeu=PacManPosition+(shift>>6);}	break;	//unten expandieren
			}
			if(posNeu==-42) continue;
			posNeu>>=1;
			pID=(isOurPacman)?(posNeu==(PacPos[0])||posNeu==(PacPos[1])||posNeu==(PacPos[2]))?43:(posNeu==(PacPos[3]))?3:(posNeu==(PacPos[4]))?4:(posNeu==(PacPos[5]))?5:42
				:(posNeu==(PacPos[3])||posNeu==(PacPos[4])||posNeu==(PacPos[5]))?43:(posNeu==(PacPos[0]))?0:(posNeu==(PacPos[1]))?1:(posNeu==PacPos[2])?2:42;
			//pID: 42=kein pacman auf dem Feld 	43=Pacman des eigenen Teams auf dem Feld 	sonst: id des gegner pacmans auf dem feld
			posNeu<<=1;
			if(pID!=43){// feld wird nicht durch pacman deselben Teams blockiert
				isOwnSide=(((isOurPacman)?B13:0)^(world[posNeu]&B13))==0;// true wenn der pacman auf seiner teamseite steht
				ourTeamDotsSecuredNew=ourTeamDotsSecured;
				enemyTeamDotsSecuredNew=enemyTeamDotsSecured;
				worldNew=world;	// wenn dich die dots im neuen Knoten unverändert sind ist eine referenz auf das ursprüngliche array ausreichend
				PacPosNew=PacPos.clone();	//die Position des pacman der am zug ist ändert sich auf jeden fall (ansosnsten wird der knoten nicht expandiert) daher kopie notwendig
				carriedDotsNew=carriedDots.clone();
				if(pID!=42){ // auf dem feld was wir betreten wollen steht ein pacman des anderen teams 
					if(isOwnSide){ // betretenes feld gehört selben team wie der pacman der am zug ist
						PacPosNew[currentPacman]=posNeu>>1;	// pacman trägt seine neue Position ein
						if(carriedDotsNew[currentPacman].length!=0){// rechne Dots die der Pacman der am Zug ist dabeihat seinem Team an und lösche die Liste 
							if(isOurPacman) 
								ourTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;		
							else 			
								enemyTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;
							carriedDotsNew[currentPacman]=EMPTY_ARRAY;
						}
						PacPosNew[pID]=spawnPosition[pID];	// der gegner Pacman der bereits auf dem Feld stand stirbt und legt seine Dots zurück
						tmpCarry=carriedDotsNew[pID];
						if(tmpCarry.length>0){
							worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
							for(int i=0;i<tmpCarry.length;i++)
								worldNew[carriedDotsNew[pID][i]]|=B14;
								//worldNew[carriedDotsNew[currentPacman][i]]|=B14; // lege den dot wieder in die welt evtl. ändern
						}
					}else{ //betretenes feld gehört anderem team
						PacPosNew[currentPacman]=spawnPosition[currentPacman];
						tmpCarry=carriedDotsNew[currentPacman];
						if(tmpCarry.length>0) {
							worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
							for(int i=0;i<tmpCarry.length;i++)
								worldNew[tmpCarry[i]]|=B14; // lege den dot wieder in die welt
							carriedDotsNew[currentPacman]=EMPTY_ARRAY; // entferne alle dots die der Pacman getragen hat aus der liste	
						}
					}
				}else{// auf dem feld was wir betreten wollen steht noch kein pacman
					PacPosNew[currentPacman]=posNeu>>1;
					if(isOwnSide){ // betretenes feld gehört unserem team (selbes team -> kann hier keine dots fressen)
						if(carriedDotsNew[currentPacman].length!=0) {// rechne Dots die der Pacman der am Zug ist dabeihat seinem Team an und lösche die Liste 
							if(isOurPacman) 
								ourTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;		
							else 			
								enemyTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;
							carriedDotsNew[currentPacman]=EMPTY_ARRAY;
						}						
					}else if((worldNew[posNeu]&B14)!=0){ //betretenes feld ist auf Gegner und auf dem feld lag ein Dot
						worldNew=world.clone(); //dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
						worldNew[posNeu]&=~B14; //entferne Dot aus der Welt
						tmpCarry=carriedDotsNew[currentPacman];
						int[] carryNew=new int[tmpCarry.length+1];	
						System.arraycopy(tmpCarry, 0, carryNew, 0, tmpCarry.length);
						carryNew[tmpCarry.length]=PacPosNew[currentPacman];
						carriedDotsNew[currentPacman]=carryNew;	// array wird durch ein größeres array ersetzt das zusätzlich den neu gefressenen Dot enthält			
					}
				}
				neu.add(new WorldState(worldNew,carriedDotsNew,PacPosNew,amZugNeu,RoundNext,ourTeamDotsSecuredNew,enemyTeamDotsSecuredNew,expandAction));  // Erstelle neuen Knoten mit den Berechneten werten und füge ihn in die liste ein
			}
		}
		return neu;
	}
	
	/*
	public WorldState expand(VSPacmanAction expandAction){
		int posNeu,shift,ourTeamDotsSecuredNew,enemyTeamDotsSecuredNew,pID,RoundNext,amZugNeu;
		int currentPacmanID=zugreihenfolge[amZug];
		int PacManPosition=PacPos[currentPacmanID]<<1;
		int PacmanPosData=world[PacManPosition];
		int[] worldNew,tmpCarry,PacPosNew;
		int[][] carriedDotsNew;
		boolean isOwnSide,isOurPacman=currentPacmanID<3;
		if(amZug==5) {
			amZugNeu=0;
			RoundNext=round+1;
		}else {
			amZugNeu=amZug+1;
			RoundNext=round;
		}
		
		
		
		posNeu=-42;
		switch(expandAction) {
		case GO_WEST:	if((PacmanPosData&B1)!=0)	{		posNeu=PacManPosition-2;	}		break;		//links expandieren
		case GO_EAST:	if((PacmanPosData&B2)!=0)	{		posNeu=PacManPosition+2;	}		break;		//rechts expandieren
		case GO_NORTH:	if((shift=(PacmanPosData&E5N2))!=0) {	posNeu=PacManPosition-(shift>>1);}	break;		//oben expandieren
		case GO_SOUTH: if((shift=(PacmanPosData&E5N7))!=0)	{posNeu=PacManPosition+(shift>>6);}	break;		//unten expandieren
		default: System.out.println("falscher parameter bei expand method (zulässig: GO_WEST,GO_EAT,GO_NORTH oder GO_SOUTH)"); return null;
		}
		if(posNeu==-42) return null;
			pID=(isOurPacman)?(posNeu==PacPos[0]||posNeu==PacPos[1]||posNeu==PacPos[2])?43:(posNeu==PacPos[3])?3:(posNeu==PacPos[4])?4:(posNeu==PacPos[5])?5:42
				:(posNeu==PacPos[3]||posNeu==PacPos[4]||posNeu==PacPos[5])?43:(posNeu==PacPos[0])?0:(posNeu==PacPos[1])?1:(posNeu==PacPos[2])?2:42;
			//pID: 42=kein pacman auf dem Feld 	43=Pacman des eigenen Teams auf dem Feld 	sonst: id des gegner pacmans auf dem feld

		if(pID!=43){// feld wird nicht durch pacman deselben Teams blockiert
			isOwnSide=(((isOurPacman)?B13:0)^(world[posNeu]&B13))==0;// true wenn der pacman auf seiner teamseite steht
			ourTeamDotsSecuredNew=ourTeamDotsSecured;
			enemyTeamDotsSecuredNew=enemyTeamDotsSecured;
			worldNew=world;	// wenn dich die dots im neuen Knoten unverändert sind ist eine referenz auf das ursprüngliche array ausreichend
			PacPosNew=PacPos.clone();	//die Position des pacman der am zug ist ändert sich auf jeden fall (ansosnsten wird der knoten nicht expandiert) daher kopie notwendig
			carriedDotsNew=carriedDots.clone();
			if(pID!=42){ // auf dem feld was wir betreten wollen steht ein pacman des anderen teams 
				if(isOwnSide){ // betretenes feld gehört selben team wie der pacman der am zug ist
					PacPosNew[currentPacmanID]=posNeu>>1;	// pacman trägt seine neue Position ein
					if(carriedDotsNew[currentPacmanID].length!=0){// rechne Dots die der Pacman der am Zug ist dabeihat seinem Team an und lösche die Liste 
						if(isOurPacman) 
							ourTeamDotsSecuredNew+=carriedDotsNew[currentPacmanID].length;		
						else 			
							enemyTeamDotsSecuredNew+=carriedDotsNew[currentPacmanID].length;
						carriedDotsNew[currentPacmanID]=EMPTY_ARRAY;
					}
					PacPosNew[pID]=spawnPosition[pID];	// der gegner Pacman der bereits auf dem Feld stand stirbt und legt seine Dots zurück
					tmpCarry=carriedDotsNew[pID];
					if(tmpCarry.length>0){
						worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
						for(int i=0;i<tmpCarry.length;i++)
							worldNew[carriedDotsNew[currentPacmanID][i]]|=B14; // lege den dot wieder in die welt
					}
				}else{ //betretenes feld gehört anderem team
					PacPosNew[currentPacmanID]=spawnPosition[currentPacmanID];
					tmpCarry=carriedDotsNew[currentPacmanID];
					if(tmpCarry.length>0) {
						worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
						for(int i=0;i<tmpCarry.length;i++)
							worldNew[tmpCarry[i]]|=B14; // lege den dot wieder in die welt
						carriedDotsNew[currentPacmanID]=EMPTY_ARRAY; // entferne alle dots die der Pacman getragen hat aus der liste	
					}
				}
			}else{// auf dem feld was wir betreten wollen steht noch kein pacman
				PacPosNew[currentPacmanID]=posNeu>>1;
				if(isOwnSide){ // betretenes feld gehört unserem team (selbes team -> kann hier keine dots fressen)
					if(carriedDotsNew[currentPacmanID].length!=0) {// rechne Dots die der Pacman der am Zug ist dabeihat seinem Team an und lösche die Liste 
						if(isOurPacman) 
							ourTeamDotsSecuredNew+=carriedDotsNew[currentPacmanID].length;		
						else 			
							enemyTeamDotsSecuredNew+=carriedDotsNew[currentPacmanID].length;
						carriedDotsNew[currentPacmanID]=EMPTY_ARRAY;
					}						
				}else if((worldNew[posNeu]&B14)!=0){ //betretenes feld ist auf Gegner und auf dem feld lag ein Dot
					worldNew=world.clone(); //dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
					worldNew[posNeu]&=~B14; //entferne Dot aus der Welt
					tmpCarry=carriedDotsNew[currentPacmanID];
					int[] carryNew=new int[tmpCarry.length+1];	
					System.arraycopy(tmpCarry, 0, carryNew, 0, tmpCarry.length);
					carryNew[tmpCarry.length]=PacPosNew[currentPacmanID];
					carriedDotsNew[amZug]=carryNew;	// array wird durch ein größeres array ersetzt das zusätzlich den neu gefressenen Dot enthält			
				}
			}
			return new WorldState(worldNew,carriedDotsNew,PacPosNew,amZugNeu,RoundNext,ourTeamDotsSecuredNew,enemyTeamDotsSecuredNew,expandAction);  // Erstelle neuen Knoten mit den Berechneten werten und füge ihn in die liste ein
		}
		return null;
	}
	*/
	private int getScore() {
		return ourTeamDotsSecured-enemyTeamDotsSecured;
	}
	
	private boolean isLastMove() {
		return (round==400&&amZug==5);
	}
	
	public void print() {
		String zustand="";
		for(int i=0;i<PacPos.length;i++) {
			zustand+=PacPos[i]+" ";
		}
		zustand+="("+PacPos[zugreihenfolge[amZug]]+") ";
		zustand+=this.action;
		System.out.println(zustand);
	}
	
	
	public int SimulateGame() {
		
		WorldState AktuellerKnoten=this;
		int MaxSimTiefe=1200; //1200 entspricht 200 Ruden bei 6 pacman
		int SimTiefe=0;
		int bestScore,BestIndex;
		WorldState tmp;
		ArrayList<WorldState> kandidaten;
		boolean maximize=WorldState.zugreihenfolge[AktuellerKnoten.amZug]<3;
		while(true){
		

		//TODO simulationsabbruch wenn sich die heuristik des gewählten knoten deutlich verschlechtert oder verbessert hat im vergleich zur start der simulation
			
			
			
			kandidaten=AktuellerKnoten.expand_AllDirectionsAndWait();// schritt 1 expandiere aktuellen knoten
			tmp=kandidaten.get(0);
			bestScore=tmp.getScore();
			// schritt 2 prüfe ob das simulationsende durch rundenzahl oder erreicht ist falls ja -> return best score
			if(tmp.isLastMove() || MaxSimTiefe==SimTiefe){
				if(maximize){ // letzter schritt wurde von unserem pacman gemacht (score maximieren)
					for(int i=1;i<kandidaten.size();i++)
						if(kandidaten.get(i).getScore()>bestScore)
							bestScore=kandidaten.get(i).getScore();
				}else{		  // letzter schritt wurde von gegner pacman gemacht (score minimieren)
					for(int i=1;i<kandidaten.size();i++)
						if(kandidaten.get(i).getScore()<bestScore)
							bestScore=kandidaten.get(i).getScore();
				}
				return bestScore;
			}
			//TODO simulationstiefe abfragen und eventuell simulation abbrechen
			
			// schritt 3 hat das team was zuletzt dran war in einem der knoten gewonnen dutch einsammeln aller Dots? ja -> return score
			for(int i=0;i<kandidaten.size();i++)
				if(maximize){//unser pacman hat den letzten zug gemacht
					if(kandidaten.get(i).ourTeamDotsSecured==WorldState.DotsOnEachSide) 
						return kandidaten.get(i).getScore();
				}else// gegner pacman hat den letzteb zug gemacht
					if(kandidaten.get(i).enemyTeamDotsSecured==WorldState.DotsOnEachSide)
						return kandidaten.get(i).getScore();
			
			
			// schritt 4 Bewerte die Knoten heuristisch und wähle besten ----------------------OPTION 1-------------------
//BestIndex=0;
//if(maximize){ //unser pacman hat den letzten zug gemacht	
//	bestScore=42;// TODO Heuristik methode für gegner züge einfügen
//}else{
//	bestScore=42;// TODO Heuristik methode für unsere züge einfügen
//}
//for(int i=0;i<kandidaten.size();i++) {
//	int score;
//	if(maximize){ //unser pacman hat den letzten zug gemacht (das heißt diesen zug macht der gegner -> score minimieren)
//		score=42;// TODO Heuristik methode für gegner züge einfügen (falls wir dafür überhaupt untschiedliche heuristiken haben)
//		if(score<bestScore) {
//			bestScore=score;
//			BestIndex=i;
//		}
//	}else{		//gegner pacman hat den letzten zug gemacht (das heißt diesen zug machen wir -> score maximieren)
//		score=42;// TODO Heuristik methode für unsere züge einfügen (falls wir dafür überhaupt untschiedliche heuristiken haben)
//		if(score<bestScore) {
//			bestScore=score;
//			BestIndex=i;
//		}
//	}
//}
//AktuellerKnoten=kandidaten.get(BestIndex);
			//------------------------------------------------------- OPTION 1 ENDE -----------------------------------------
			

			// schritt 4 alternativ: wähle knoten zufällig------------------------------OPTION 2------------------------------
			AktuellerKnoten=kandidaten.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(kandidaten.size()));
			// --------------------------------------------------------OPTION 2 ENDE------------------------------------------
			//TODO: sich für heuristische oder zufällige version entscheiden und die andere version löschen/auskomentieren
		}

		
		
		
		
		// (das team was jetzt dran ist kann während der gegner dran ist nicht gewinnen)
		
		// schritt 5 wenn wir dran sind: wähle knoten mit höchster bewertung  wenn gegner am zug ist: wähle knoten mit geringster bewertung
		// schritt 6 prüfe ob das simulationsende durch maximale Simulationstiefe erreicht ist falls ja -> return score von gewählten knoten
		// nehme gewählten knoten und starte damit wieder bei schritt 1


		
		
		/*
		Simuliert ein Spiel für maximal x(=200?) Runden oder bis eins der Beiden Teams gewonnen 
		hat und liefert einen Score zurück
		*/
	}
	
	@Override
	public boolean equals(Object Weltzustand){		
		// muss true zurückliefern wenn die zustände inhaltlich identisch sind
		// dabei muss beachtet werden das die reihenfolge der Dots im 
		// carriedDots Array egal ist und es nur darauf amkommt das insgesammt
		// dieselben Dots in der Liste sind!!
		//TODO
		return false;
	}
	
	
	
	
}
