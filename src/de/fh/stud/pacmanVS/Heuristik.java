package de.fh.stud.pacmanVS;

import java.util.ArrayList;
import de.fh.pacmanVS.enums.VSPacmanAction;
import static de.fh.pacmanVS.enums.VSPacmanAction.*;
import static de.fh.stud.pacmanVS.constants.*;

public class Heuristik {

	public static void HeuristischeEmpfehlungen(WorldState state,WorldState[] Childnodes) {
		int[] world_OH=state.world.clone();
		int[] empfehlungen;
		// wait wird niemals empfohlen
		// ein Zug bei dem der Pacman stirbt wird niemals empfohlen
		
		// schritt 1: ermittle die gültigen züge die kein Wait sind
		ArrayList<VSPacmanAction> actions=state.possibleActions();
		ArrayList<Integer> posNeuList=new ArrayList<Integer>(4);
		empfehlungen=new int[actions.size()];
		
		// schritt 2: ermittle die Züge bei denen der Pacman in einen gegner hineinrennt		
		int pos=state.PacmanPosAmZug();
		int PacID=state.PacIDAmZug();
		int PacPosData=world_OH[pos];
		boolean unserZug=PacID<3;
		int posNeu;
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
				posNeu=pos-((PacPosData&E5N7)>>6);
				break;
			default: System.err.println("Error default case in heuristik empfehlung");
			posNeu=-42;
			}
			posNeuList.add(posNeu);
			if(unserZug) {
				for(int i2=3;i2<6;i2++) {
					if(posNeu==(state.PacPos[i2]>>1)){
						if((world_OH[posNeu]&B13)==0) {
							//pacman am zug stirbt (wir)    	negative bewertung
							empfehlungen[i]=-200;
						}else {
							//pacman auf feld stirbt (gegner)	positive bewertung
							empfehlungen[i]=200;
						}
						break;
					}
				}			
			}else {
				for(int i2=0;i2<3;i2++) {
					if(posNeu==(state.PacPos[i2]>>1)){
						if((world_OH[posNeu]&B13)!=0) {
							//pacman am zug stirbt (gegner)		positive bewertung
							empfehlungen[i]=200;
						}else {
							//pacman auf feld stirbt (wir)		negative bewertung
							empfehlungen[i]=-200;
						}						
						break;
					}					
				}	
			}			
		}
		
		
		
		// setzte für diese züge einen strafwert ein (berücksitige das gegner einen negativen score bevorzugen und bei ihnen positive werte eine strafe sind)
		ArrayList<Integer> enemyDotPos=new ArrayList<Integer>(WorldState.DotsOnEachSide);// schritt 3: 	erstelle eine liste aller verbleibenden feindlichen dots
		if(unserZug){
			for(int i=0;i<world_OH.length;i+=2)
				if((world_OH[i]&E2N12)==B14)
					enemyDotPos.add(i);	
		}else
			for(int i=0;i<world_OH.length;i+=2)
				if((world_OH[i]&E2N12)==E2N12)
					enemyDotPos.add(i);
		
		int[] posNeuArray=new int[posNeuList.size()+1];
		for(int i=0;i<posNeuList.size();i++) {
			posNeuArray[i]=posNeuList.get(i);
		}
		posNeuArray[posNeuList.size()]=pos;
		
		Base.BreitensucheOhneGegnerHindernis(posNeuArray,world_OH);
		
		int[] avgDistance=new int[actions.size()];
		for(int i=0;i<actions.size();i++){
			int ds=((i+2)<<3)&0B11000;
			if(i>1){
				for(int i2=0;i2<enemyDotPos.size();i2+=2){
					avgDistance[i]+=(world_OH[enemyDotPos.get(i2)+1]>>ds)&E6;
				}
			}else{
				for(int i2=0;i2<enemyDotPos.size();i2+=2){
					avgDistance[i]+=(world_OH[enemyDotPos.get(i2)]>>ds)&E6;
				}
			}
			avgDistance[i]/=(double)actions.size();
		}
		
		// schritt 3.1	ermittle den kürzesten weg zu jedem dieser knoten und ermittle dabei auch den kürzessten weg
		
		// schritt 4: 	wenn der pacman auf eigener feldseite,nicht auf einer feldgrenze ermittle den kürzesten weg zur grenze 	
		//(kürzester weg zur grenze = kürzester weg ins gegner gebiet -1 schritt)
		// schritt 5: 	wenn es gegner im eigenen gebiet gibt ermittle ob sie einen garantierten weg nach hause haben und ermittle den kürzesten weg zu ihnen
	}

	
}
