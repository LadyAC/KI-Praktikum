package de.fh.stud.pacmanVS;

import java.util.ArrayList;
import java.util.List;

import de.fh.pacmanVS.VSPacmanPercept;
//import de.fh.pacmanVS.enums.Team;
import de.fh.pacmanVS.enums.VSPacmanAction;
import de.fh.util.Vector2;

import static de.fh.pacmanVS.enums.VSPacmanAction.*;
import static de.fh.stud.pacmanVS.constants.*;
public class WorldState {
	public static int[] OwnBorderFields; // initialisiert durch Base.createWorldBase
	public static int[] zugreihenfolge=new int[6];
	public static int[] spawnPosition=new int[6];
	public static int DotsOnEachSide=-42; //TODO: wert am anfang des Spiels setzen		// anzahl der Dots die ein Team beim spielstart besitzt
	// (zur prüfung ob ein spiel gewonnen wurde beim Rollout)
	public static int RolloutDepth=25*6;	// maximale tiefe eine Simulation
//	public static int NumberOfPacmans; 	// anzahl der pacmans im spiel ( =2 bei 1vs1 =6 bei 3vs3 )
//	public static Random rn=new Random();
	public VSPacmanAction action;		// aktion mit dem dieser Zustand erreicht wurde
	
	// 0= unser pacman 1
	// 1= unser pacman 2
	// 2= unser pacman 3
	// 3= gegner pacman 1
	// 4= gegner pacman 2
	// 5= gegner pacman 3
	//beispiel    [0]=1 [1]=4 [2]=2 [3]=3 [4]=0 [5]=5
	//bedeutet wenn wir Blau sind zugreihenfolge:		BLAU 2 -> ROT 2 -> BLAU 3 -> ROT 1 -> BLAU 1 -> ROT 3

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
	
	public int PacmanPosAmZug() {
		return PacPos[zugreihenfolge[amZug]]<<1;
	}
	
	public int PacIDAmZug() {
		return zugreihenfolge[amZug];
	}
	
	public ArrayList<VSPacmanAction> possibleActions(){
		ArrayList<VSPacmanAction> actions=new ArrayList<VSPacmanAction>(4);
		int posNeu,shift = -42;	
		int PacManPosition=PacmanPosAmZug();
		int PacmanPosData=world[PacManPosition];
		VSPacmanAction expandAction=WAIT;
		for(int direction=0;direction<4;direction++){
			posNeu=-42;
			switch(direction){
			case 0:	if((PacmanPosData&B1)!=0)			{expandAction=GO_WEST;	posNeu=PacManPosition-2;		 }	break;	//links expandieren
			case 1:	if((PacmanPosData&B2)!=0)			{expandAction=GO_EAST;	posNeu=PacManPosition+2;		 }	break;	//rechts expandieren
			case 2:	if((shift=(PacmanPosData&E5N2))!=0)	{expandAction=GO_NORTH;	posNeu=PacManPosition-(shift>>1);}	break;	//oben expandieren
			case 3: if((shift=(PacmanPosData&E5N7))!=0)	{expandAction=GO_SOUTH;	posNeu=PacManPosition+(shift>>6);}	break;	//unten expandieren
			}
			if(posNeu==-42) continue;
			actions.add(expandAction);
		}
		return actions;
	}
	
	
	
	public ArrayList<WorldState> expand_AllDirectionsAndWait() {
		if(amZug==5 && round == 400 || DotsOnEachSide==enemyTeamDotsSecured || DotsOnEachSide==ourTeamDotsSecured) {			
//			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
//					+ "amZug="+amZug
//					+" round="+round
//					+" DotsOnEachSide="+DotsOnEachSide
//					+" enemyTeamDotsSecured="+enemyTeamDotsSecured
//					+" ourTeamDotsSecured="+ourTeamDotsSecured);
//			
			return new ArrayList<WorldState>(0);
		}
		
		
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
							if(isOurPacman) ourTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;		
							else 			enemyTeamDotsSecuredNew+=carriedDotsNew[currentPacman].length;
							carriedDotsNew[currentPacman]=new int[0];
						}
						PacPosNew[pID]=spawnPosition[pID];	// der gegner Pacman der bereits auf dem Feld stand stirbt und legt seine Dots zurück
						tmpCarry=carriedDotsNew[pID];
						if(tmpCarry.length>0){
							worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
							for(int i=0;i<tmpCarry.length;i++)
								worldNew[carriedDotsNew[pID][i]]|=B14;  // lege den dot wieder in die welt
							carriedDotsNew[pID]=new int[0];
						}
						
					}else{ //betretenes feld gehört anderem team
						PacPosNew[currentPacman]=spawnPosition[currentPacman];
						tmpCarry=carriedDotsNew[currentPacman];
						if(tmpCarry.length>0) {
							worldNew=world.clone();//dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
							for(int i=0;i<tmpCarry.length;i++)
								worldNew[tmpCarry[i]]|=B14; // lege den dot wieder in die welt
							carriedDotsNew[currentPacman]=new int[0]; // entferne alle dots die der Pacman getragen hat aus der liste	
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
							carriedDotsNew[currentPacman]=new int[0];
						}						
					}else if((worldNew[posNeu]&B14)!=0){ //betretenes feld ist auf Gegnerseite und auf dem feld lag ein Dot
						worldNew=world.clone(); //dot informationen verändern sich im neuen Knoten -> kopie statt referenz nötig
						worldNew[posNeu]&=~B14; //entferne Dot aus der Welt
						tmpCarry=carriedDotsNew[currentPacman];
						int[] carryNew=new int[tmpCarry.length+1];	
						System.arraycopy(tmpCarry, 0, carryNew, 0, tmpCarry.length);
						carryNew[tmpCarry.length]=posNeu;
						carriedDotsNew[currentPacman]=carryNew;	// array wird durch ein größeres array ersetzt das zusätzlich den neu gefressenen Dot enthält			
					}
				}
				neu.add(new WorldState(worldNew,carriedDotsNew,PacPosNew,amZugNeu,RoundNext,ourTeamDotsSecuredNew,enemyTeamDotsSecuredNew,expandAction));  // Erstelle neuen Knoten mit den Berechneten werten und füge ihn in die liste ein
			}
		}
		return neu;
	}
	
	
	public double calculateScoreImproved() {
		final int PointsPerDot=10000;
		final double EatenAndSafeWayRatio=0.8;
		final double EatenAndUnsafeWayRatio=0.3;
		double Score=PointsPerDot*(ourTeamDotsSecured-enemyTeamDotsSecured);
		//int[] distanceWorld=world.clone();
		int[] distanceSecureWorld=world.clone();
		Base.BreitensucheMitGegnerHindernis(PacPos,distanceSecureWorld);
		//Base.BreitensucheOhneGegnerHindernis(PacPos,distanceWorld);
		int ds,iInc;
		for(int i=0;i<6;i++) {
			iInc=(i<2)?0:1;
			
			ds=((i+2)<<3)&0B11000;
			if(i<3 && (distanceSecureWorld[PacPos[i]]&B13)==0 || i>=3 && (distanceSecureWorld[PacPos[i]]&B13)!=0) { // pacman befindet sich auf gegner Feld
				int distance;
				int minDistance=Integer.MAX_VALUE;
				for(int i2=0;i2<OwnBorderFields.length;i2++) {
					distance=(((distanceSecureWorld[OwnBorderFields[i2]+iInc])>>ds)&E6);
					if(distance>0 && distance<minDistance) {
						minDistance=distance;
					}
				}
				if(minDistance<Integer.MAX_VALUE) {
					// PACMAN kann nach hause laufen mit "distance" schritten... 
					if(minDistance<400-round) {//..und kann diesen weg in den verbliebenen runden auch ausführen
						if(i<3) 
							Score+=carriedDots[i].length*EatenAndSafeWayRatio*PointsPerDot;
						else
							Score-=carriedDots[i].length*EatenAndSafeWayRatio*PointsPerDot;					
					}
				}else{
					// PACMAN hat keinen garantierten weg nach hause
					if(i<3)
						Score+=carriedDots[i].length*EatenAndUnsafeWayRatio*PointsPerDot;
					else
						Score-=carriedDots[i].length*EatenAndUnsafeWayRatio*PointsPerDot;
				}
				
				
				
			}			
		}
		
		//System.out.println("calculated Score= " +Score/PointsPerDot+"   \tursprünglich="+(ourTeamDotsSecured-enemyTeamDotsSecured));
		if(this.ourTeamDotsSecured>this.enemyTeamDotsSecured) {
			return (Score/PointsPerDot)+2;
		}
		if(this.ourTeamDotsSecured<this.enemyTeamDotsSecured) {
			return (Score/PointsPerDot)-2;
		}
		return Score/PointsPerDot;
	}
	
	private double getScore() {
	//	return (tempScore==Double.NEGATIVE_INFINITY)?tempScore=calculateScoreImproved():tempScore;
		return ourTeamDotsSecured-enemyTeamDotsSecured;
	}
	
	private boolean isLastMove() {
		return (round==400&&amZug==5);
	}
	
	private boolean isVictoryBySecuredDots(){
		return (DotsOnEachSide==enemyTeamDotsSecured || DotsOnEachSide==ourTeamDotsSecured);
	}
	
	String Vector2Compact(Vector2 v) {
		return "x"+v.getX()+"y"+v.getY();
	}
	
	public void print() {
		String zustand="runde: "+round+"_"+amZug+"";
		for(int i=0;i<PacPos.length;i++) {
			zustand+=" "+Vector2Compact(Base.IntToVector2(PacPos[i]))+((constants.DEBUG_NODE_REPAWN_DATA)?(" respawn:"+Base.IntToVector2(spawnPosition[i])):(""))+"["+carriedDots[i].length+"]";//PacPos[i]+
		}
		zustand+="(AmZug-> "+Vector2Compact(Base.IntToVector2(PacPos[zugreihenfolge[amZug]]))+") ";
		zustand+=" Secured: "+ourTeamDotsSecured+"/"+enemyTeamDotsSecured+" ";
		zustand+=action;
		System.out.println(zustand);
	}
	int anzKindKnoten;
	
	public BackpropagationScore SimulateGame() {
		BackpropagationScore SimulationScore= new BackpropagationScore();
//		long sTime=System.nanoTime();
		WorldState AktuellerKnoten=this;
		int MaxSimTiefe=RolloutDepth; //1200 entspricht 200 Ruden bei 6 pacman
		int SimTiefe=0;
		double bestScore;
		WorldState tmp;
		ArrayList<WorldState> kandidaten;
		if(round>400 || round==400 && amZug==5) {
			System.err.println("Simulation gestarted für folgende runde: "+this.round+"_"+this.amZug);
			print();
			System.exit(0);
		}
		while(true){
			SimTiefe++;
			kandidaten=AktuellerKnoten.expand_AllDirections();// schritt 1 expandiere aktuellen knoten
			anzKindKnoten=kandidaten.size();
			if(kandidaten.size()==0) {
				SimulationScore.gameScore=AktuellerKnoten.getScore();
				return SimulationScore;
			}
			tmp=kandidaten.get(0);
			// schritt 2 prüfe ob das simulationsende durch rundenzahl oder erreicht ist falls ja -> return best score
			if(SimTiefe==MaxSimTiefe||tmp.isLastMove()){	
				bestScore=tmp.getScore();
				boolean maximize=WorldState.zugreihenfolge[AktuellerKnoten.amZug]<3;
				if(maximize){ // letzter schritt wurde von unserem pacman gemacht (score maximieren)
					for(int i=1;i<kandidaten.size();i++)
						if(kandidaten.get(i).getScore()>bestScore)
							bestScore=kandidaten.get(i).getScore();
				}else{		  // letzter schritt wurde von gegner pacman gemacht (score minimieren)
					for(int i=1;i<kandidaten.size();i++)
						if(kandidaten.get(i).getScore()<bestScore)
							bestScore=kandidaten.get(i).getScore();
				}
				SimulationScore.gameScore=bestScore;
				return SimulationScore;
			}
			for(int i=0;i<kandidaten.size();i++) {
				if(kandidaten.get(i).isVictoryBySecuredDots()) {
					SimulationScore.gameScore=kandidaten.get(i).getScore();
					return SimulationScore;
				}
			}
			int index=java.util.concurrent.ThreadLocalRandom.current().nextInt(kandidaten.size());
			WorldState neuerKnoten=kandidaten.get(index);
			for(int i=0;i<6;i++) {
				int carried=AktuellerKnoten.carriedDots[i].length;
				int carriedNeu=neuerKnoten.carriedDots[i].length;
				if(carried!=carriedNeu){
					if(carriedNeu==0) {// pacman ist hatte vorher dots und hat jetzt keine mehr
						if(spawnPosition[i]==neuerKnoten.PacPos[i]) {// pacman steht auf seiner spawnposition (ist also gestorben)
							SimulationScore.PacScore[i]-=carried*0.5;	// 0,5 punkte abzug für jeden getragenen dot für den Pacman der gessstorben ist
							int oldpos=AktuellerKnoten.PacPos[i];
							for(int i2=0;i2<6;i2++) {
								if(neuerKnoten.PacPos[i2]==oldpos){
									SimulationScore.PacScore[i2]+=carried*0.5;	// 0,5 Punkte für jeden dot den der getötete getragen hat für den killer
									break;
								}
							}
						}else{ // wenn der pacman nicht gestprben ist aber trotzdem seine dots verloren hat muss er sie gesichert haben
							SimulationScore.PacScore[i]+=carried*0.5;	// 0,5 punkte für jeden dot der gesichert wurde
						}
					}else{ // wenn sich die anzahl der getragenen dots verändert hat aber nicht auf 0 hat der Pacman wohl einen dot gesammelt
						SimulationScore.PacScore[i]+=0.5;
					}
				}
			}
			AktuellerKnoten=neuerKnoten;
		}
	}
}
