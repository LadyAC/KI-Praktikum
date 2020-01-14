package de.fh.stud.pacmanVS;

import java.util.ArrayList;
import de.fh.pacmanVS.enums.VSPacmanAction;
import static de.fh.pacmanVS.enums.VSPacmanAction.*;
import static de.fh.stud.pacmanVS.constants.*;

public class Heuristik {

	private static final int DEATH_PUNNISH_SCORE=500;
	private static final int DOT_REWARD_SCORE=300;
	private static final int ENEMY_PAC_SCORE_PER_DOT=50;
	public static void HeuristischeEmpfehlungen(WorldState state,Node[] ChildNodes) {
		int[] world_OH=state.world.clone();
		int[] empfehlungen;
		int pos=state.PacmanPosAmZug();
		int PacID=state.PacIDAmZug();
		int PacPosData=world_OH[pos];
		boolean unserZug=PacID<3;
		int posNeu;
		// wait wird niemals empfohlen
		// ein Zug bei dem der Pacman stirbt wird niemals empfohlen
		int[] pacSameTeam=new int[3];
		if(unserZug)
			for(int i=0;i<3;i++) 
				pacSameTeam[i]=state.PacPos[i]<<1;
		else 
			for(int i=3;i<6;i++) 
				pacSameTeam[i-3]=state.PacPos[i]<<1;
		ArrayList<VSPacmanAction> actions=state.possibleActions(pacSameTeam);// ermittle die gültigen züge die kein Wait sind
		ArrayList<Integer> posNeuList=new ArrayList<Integer>(4);
		empfehlungen=new int[actions.size()];
		
		// schritt 2: ermittle die Züge bei denen der Pacman in einen gegner hineinrennt		

		for(int i=0;i<actions.size();i++){
			switch(actions.get(i)) {
			case GO_EAST:
				posNeu=pos+2;
				break;
			case GO_WEST:
				posNeu=pos-2;
				break;
			case GO_NORTH:
				posNeu=pos-((PacPosData&E5N2)>>1);
				break;
			case GO_SOUTH:
				posNeu=pos+((PacPosData&E5N7)>>6);
				break;
			default: System.err.println("Error default case in heuristik empfehlung");
			posNeu=-42;
			}
			posNeuList.add(posNeu);
			if(unserZug) {
				for(int i2=3;i2<6;i2++) {
					if(posNeu==(state.PacPos[i2]<<1)){
						if((world_OH[posNeu]&B13)==0) 
							empfehlungen[i]=-DEATH_PUNNISH_SCORE;//pacman am zug stirbt (wir)    	negative bewertung
						else 
							empfehlungen[i]=DEATH_PUNNISH_SCORE;//pacman auf feld stirbt (gegner)	positive bewertung
						break;
					}
				}
				if(empfehlungen[i]==0 && (world_OH[posNeu]&B13)==0){
					if((world_OH[posNeu]&0B1)!=0){//links
						int posNeuNeu=posNeu-2;
						for(int i2=3;i2<6;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=-DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					if((world_OH[posNeu]&0B10)!=0){//rechts
						int posNeuNeu=posNeu+2;
						for(int i2=3;i2<6;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=-DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					int shift;
					if((shift=(world_OH[posNeu]>>2)&E5)!=0){//oben
						int posNeuNeu=posNeu-2*shift;
						for(int i2=3;i2<6;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=-DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					if((shift=(world_OH[posNeu]>>7)&E5)!=0){//unten
						int posNeuNeu=posNeu+2*shift;
						for(int i2=3;i2<6;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=-DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
				}
				//TODO: mögliche züge von der neuen position ermitteln
				// überprüfen ob diese positionen unser gebiet sind und falls nein
				// überprüfen ob in einem der angrenzenden felder ein gegner steht
				// falls ja negativ bewerten
			}else {
				for(int i2=0;i2<3;i2++) {
					if(posNeu==(state.PacPos[i2]<<1)){
						if((world_OH[posNeu]&B13)!=0) {
							//pacman am zug stirbt (gegner)		positive bewertung
							empfehlungen[i]=DEATH_PUNNISH_SCORE;
						}else {
							//pacman auf feld stirbt (wir)		negative bewertung
							empfehlungen[i]=-DEATH_PUNNISH_SCORE;
						}						
						break;
					}					
				}
				
				if(empfehlungen[i]==0 && (world_OH[posNeu]&B13)==1){
					if((world_OH[posNeu]&0B1)!=0){//links
						int posNeuNeu=posNeu-2;
						for(int i2=0;i2<3;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					if((world_OH[posNeu]&0B10)!=0){//rechts
						int posNeuNeu=posNeu+2;
						for(int i2=0;i2<3;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					int shift;
					if((shift=(world_OH[posNeu]>>2)&E5)!=0){//oben
						int posNeuNeu=posNeu-2*shift;
						for(int i2=0;i2<3;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
					if((shift=(world_OH[posNeu]>>7)&E5)!=0){//unten
						int posNeuNeu=posNeu+2*shift;
						for(int i2=0;i2<3;i2++){
							if(posNeuNeu==(state.PacPos[i2]<<1)){
								empfehlungen[i]=DEATH_PUNNISH_SCORE;
								break;
							}
						}
					}
				}
			}			
		}
		// setzte für diese züge einen strafwert ein (berücksitige das gegner einen negativen score bevorzugen und bei ihnen positive werte eine strafe sind)
		
		
		
		ArrayList<Integer> enemyDotPos=new ArrayList<Integer>(WorldState.DotsOnEachSide);// erstelle eine liste aller vorhandenen feindlichen dots
		if(unserZug){
			for(int i=0;i<world_OH.length;i+=2)
				if((world_OH[i]&E2N12)==B14)
					enemyDotPos.add(i);	
		}else
			for(int i=0;i<world_OH.length;i+=2)
				if((world_OH[i]&E2N12)==E2N12)
					enemyDotPos.add(i);
		

		
		
		if(enemyDotPos.size()!=0){
			Base.BreitensucheOhneGegnerHindernis(pos/2,world_OH); // suche den kürzesten weg zu jedem feld auf der karte
			
//			Base.DrawDistanceInfo(de.fh.stud.pacmanVS.MyPacmanVSAgent.view, world_OH);
//			for(int i=0;i<enemyDotPos.size();i++) {
//				System.out.println("Heuristik: enemy Dot at "+Base.IntToVector2(enemyDotPos.get(i)/2)+" ("+enemyDotPos.get(i)+")   entfernung="+(((world_OH[enemyDotPos.get(i)])>>16)&E6));
//			}
			
			
			int minDistance=Integer.MAX_VALUE;
			int minIndex=-42;
			int distance;
//			System.out.println("Heuristik: suche dot mit geringester entfernung:");
			for(int i=0;i<enemyDotPos.size();i++) {
				distance=((world_OH[enemyDotPos.get(i)])>>16)&E6;
				if(minDistance>distance) {
//					System.out.println("Heuristik: neues minimum: distance="+distance+" enemyDotPos.get(0)="+Base.IntToVector2(enemyDotPos.get(i)/2));
					minIndex=i;
					minDistance=distance;
				}
			}
			VSPacmanAction action=Base.actioToField(enemyDotPos.get(minIndex), world_OH, 0);
			if(unserZug) {
				for(int i=0;i<actions.size();i++) {
					if(actions.get(i)==action) {
						if(empfehlungen[i]>=0) {
							empfehlungen[i]+=DOT_REWARD_SCORE;
						}
						break;
					}
				}
			}else{
				for(int i=0;i<actions.size();i++) {
					if(actions.get(i)==action) {
						if(empfehlungen[i]<=0) {
							empfehlungen[i]-=DOT_REWARD_SCORE;
						}
						break;
					}
				}				
			}
		}
		// empfehlung für gehe zu dot mit geringster entfernung ende
		//sind gegner im eigenen gebiet?
		ArrayList<Integer> EnemysInOurAreaPOS=new ArrayList<Integer>(3);
		ArrayList<Integer> EnemysInOurAreaID=new ArrayList<Integer>(3);
		if(unserZug){
			for(int i=3;i<6;i++) 
				if((world_OH[state.PacPos[i]<<1]&B13)!=0) {
					EnemysInOurAreaPOS.add(state.PacPos[i]);
					EnemysInOurAreaID.add(i);
				}
			if(EnemysInOurAreaPOS.size()>0) {
				int[] world_MH=state.world.clone();
				Base.BreitensucheMitGegnerHindernis(state.PacPos, world_MH);
				for(int i=0;i<EnemysInOurAreaID.size();i++){
					int pacID=EnemysInOurAreaID.get(i);
					int ds=pacID<<3&0B11000;
					int iInc=pacID<2?0:1;
					if(((world_MH[(WorldState.spawnPosition[3]<<1)+iInc]>>ds)&E6)==0) {// gegner pacman i hat KEINEN garantierten weg nach hause
						VSPacmanAction action=Base.actioToField(EnemysInOurAreaPOS.get(i)<<1, world_MH, pacID);
						for(int i2=0;i2<actions.size();i2++) {
							if(actions.get(i2)==action) {
								if(empfehlungen[i2]>=0){
									empfehlungen[i2]+=ENEMY_PAC_SCORE_PER_DOT*(1+state.carriedDots[EnemysInOurAreaID.get(i)].length);
								}
								break;
							}
						}
					}
				}
			}
		}else{
			for(int i=0;i<3;i++) 
				if((world_OH[state.PacPos[i]<<1]&B13)==0) {
					EnemysInOurAreaPOS.add(state.PacPos[i]);
					EnemysInOurAreaID.add(i);
				}
			if(EnemysInOurAreaPOS.size()>0) {
				int[] world_MH=state.world.clone();
				Base.BreitensucheMitGegnerHindernis(state.PacPos, world_MH);
				for(int i=0;i<EnemysInOurAreaPOS.size();i++) {
					int pacID=EnemysInOurAreaID.get(i);
					int ds=pacID<<3&0B11000;
					int iInc=pacID<2?0:1;
					if(((world_MH[(WorldState.spawnPosition[3]<<1)+iInc]>>ds)&E6)==0) {// gegner pacman i hat KEINEN garantierten weg nach hause
						VSPacmanAction action=Base.actioToField(EnemysInOurAreaPOS.get(i)<<1, world_MH, pacID);
						for(int i2=0;i2<actions.size();i2++) {
							if(actions.get(i2)==action) {
								if(empfehlungen[i2]<=0){
									empfehlungen[i2]-=40*(1+state.carriedDots[EnemysInOurAreaID.get(i)].length);
								}
								break;
							}
						}
					}
				}
			}
		}
//		System.out.println("-------------------------------------------------START---------------------------------------------------------");
		for(int i=0;i<ChildNodes.length;i++) {
//			System.out.println("ChildenNode["+i+"]= "+ChildNodes[i].Weltzustand.action);
		}
		
		for(int i=0;i<empfehlungen.length;i++) {
			ChildNodes[i].heuristicScore=empfehlungen[i];
//			System.out.println("heuristic Check confirmed: Node/empfehlung: "+ChildNodes[i].Weltzustand.action+"/"+actions.get(i)+" ("+empfehlungen[i]+")");
		}
		if(empfehlungen.length+1==ChildNodes.length) {
//			System.out.println("numbers look ok");
		}else {
			System.out.println("something is wrong: "+(empfehlungen.length+1)+" != "+ChildNodes.length);
			System.out.print("parent node: ");
			state.print();
			System.out.println("");
			for(int i=0;i<pacSameTeam.length;i++) {
				System.out.println("pacSameTeam ="+pacSameTeam[i]+"  -> "+Base.IntToVector2(pacSameTeam[i])+"-> "+Base.IntToVector2(pacSameTeam[i]/2));
			}
			state.possibleActions(pacSameTeam);
		}
//		System.out.println("-------------------------------------------------END---------------------------------------------------------");
	}


	
}
