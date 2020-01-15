package de.fh.stud.pacmanVS;

import java.util.concurrent.Phaser;
import de.fh.pacmanVS.enums.VSPacmanAction;

public class MCTS extends Thread {
	public Node root;
//	@jdk.internal.vm.annotation.Contended
	//THREAD COMMUNICATION------------------------------MAIN THREAD-----|------MCTS THREAD-----------------
	public volatile Node NewRoot;					//		write 		|		read
	public volatile int lastRoundActionNumber;		//		write		|		read
	public volatile int RoundActionUsed;			//		read 		|		write
	public volatile VSPacmanAction BestActionSoFar;	//		read 		|		write						 (Die Aktuelle Zugempfehlung des Suchbaums)
	
	public Playout playWait,playGoNorth,playGoSouth,playGoWest,playGoEast;
//	private Phaser phaserWait,phaserGoNorth,phaserGoSouth,phaserGoEast,phaserGoWest;
	//public volatile double WaitScore,GoNorthScore,GoSouthScore,GoWestScore,GoEastScore;
	public volatile BackpropagationScoreVolatile WaitScore,GoNorthScore,GoSouthScore,GoWestScore,GoEastScore;// hier schreiben die Threads die die Spiele Simulieren ihre Ergebnisse rein
	public volatile Phaser phaser;
	private int iterationCounterSinceRootChange=0;

	/*	
	 * 		1. Der Main Thread ließt zuerst die Variable RoundActionUsed um sicherzustellen das der MCTS Thread
	 * 		die letzte aktion die übermittelt wurde erfolgreich im Baum übernommen hat (RoundActionUsed==lastActionRoundNumber)
	 *   
	 * 		2. Der Main Thread überschreibt in die Variable NewRoot die der MCTS thead dan am ende seiner aktuellen Iteration
	 * 		als neue Wurzel festlegt
	 * 
	 * 		3. Der Main Thread in die Variable lastActionRoundNumber um 1 und Signalisiert Dem MCTS Thread 
	 *  	damit der diese Variable regelmäßig auf änderungen prüft das er die seine Wurzel anpassen muss
	 *  
	 * 		4. Der MCTS Thread übernimmt sucht die neue Wurzel in den KindKnoten der aktuellen Wurzel und 
	 * 		entfernt die alte wurzel und übernimmt den teil des baums
	 * 
	 * 		5.Der MCTS Thread erhöht Variable RoundActionUsed um 1 und Signalisiert dem Main Thread das 
	 * 		er die änderung erfolgreich ausgeführt hat und die Kommunikation beginnt erneut bei schritt 1 
	 * ________________________________________________________________________________________________________
	 * Der Hauptgrund den Suchbaum in einem eigenen Thread auszuführen ist das wir so jederzeit den Server 
	 * den ausgewählten zug übermitteln können und möglichst auszuschließen das die ein Wait ausführen weil
	 * ein Iterationsschritt im Suchbaum länger gedauert hat als angenommen
	 */
	


	
	
	public void run(){
		System.out.println("MCTS Thread gestarted Phase:"+phaser.getPhase());
		doIteration();
		if(constants.DEBUG_ROOT) {				// DEBUG
			System.out.print("Wurzel: ");		// DEBUG
			root.Weltzustand.print();			// DEBUG
		}										// DEBUG
		if(constants.DEBUG_CHILDNODES) {				// DEBUG
			for(int i=0;i<root.Children.length;i++) {	// DEBUG
				System.out.print("Kindknoten "+i+" : ");// DEBUG
				root.Children[i].Weltzustand.print();	// DEBUG
			}											// DEBUG
		}												// DEBUG


		
		while(true){
			doIteration();
			iterationCounterSinceRootChange++;
			if(RoundActionUsed!=lastRoundActionNumber){		
				if(constants.DEBUG_ITERATIONCOUNTER) {
					System.out.print("Iterationen seit Wurzeltausch: "+iterationCounterSinceRootChange+"\t Knoten im Baum: "+TreeTraverselCount()+" ->");
				}
				ChangeRoot(this.NewRoot);
				if(constants.DEBUG_ITERATIONCOUNTER) {
					System.out.println(" "+TreeTraverselCount());
				}
				iterationCounterSinceRootChange=0;
			}
		}
	}
	
	
	public MCTS(WorldState rootState) {
		phaser=new Phaser();
		root = new Node(rootState,this);
		playWait = new Playout(0,this);
		playGoWest=new Playout(1,this);
		playGoEast=new Playout(2,this);
		playGoNorth=new Playout(3,this);
		playGoSouth=new Playout(4,this);
		playWait.start();
		playGoWest.start();
		playGoEast.start();
		playGoNorth.start();
		playGoSouth.start();
	}
	
	public void doIteration() {
		Node[] Selected=SelectionAndExpansion();// Step 1&2: Selection and Expansion
		SimulateGames(Selected);				// Step 3: Rollout/Playou
		for(int i=0;i<Selected.length;i++) 
			Selected[i].BackPropagation(); 		// Step 4 BackPropagation
		double bestScore=Double.NEGATIVE_INFINITY,tmpScore;// ermittle besten Zug nach aktuellen stand
		int index=0;
		for(int i=0;i<root.Children.length;i++){
			tmpScore=root.Children[i].simulationCount;
			if(bestScore<tmpScore) {
				bestScore=tmpScore;
				index=i;
			}
		}																													//DEBUG
		if(constants.DEBUG_BEST_ACTION && root.Children.length>0															//DEBUG
		&& (BestActionSoFar!=root.Children[index].action || iterationCounterSinceRootChange%10000==0)) {					//DEBUG
			System.out.println("Bisher bester Zug: "+BestActionSoFar+" (iterationen: "+iterationCounterSinceRootChange+")");//DEBUG
		}																													//DEBUG
		if(root.Children.length!=0) {// verhindert out of bound exception wenn der pacman den letzten schritt vor spielenede machen will
			BestActionSoFar=root.Children[index].action;
		}
	}
	
	
	private void SimulateGames(Node[] Selected) {
		phaser = new Phaser(Selected.length);
		Playout playTmp;
		for(int i=0;i<Selected.length;i++){
			switch(Selected[i].Weltzustand.action){
			case WAIT:		
				playTmp=playWait;	
				break;
			case GO_NORTH:	
				playTmp=playGoNorth;	
				break;
			case GO_SOUTH:	
				playTmp=playGoSouth;	
				break;
			case GO_WEST:	
				playTmp=playGoWest;		
				break;
			case GO_EAST:	
				playTmp=playGoEast;		
				break;  
			default:		
				playTmp=null;
			}
			playTmp.toSimulate=Selected[i].Weltzustand;
			playTmp.phaser.arrive(); // Signal an den entsprechenden thread die arbeit auszuführen
		}
		phaser.awaitAdvance(0);
	}
	
	
	
	public Node[] SelectionAndExpansion(){
		Node Selected=root;
		if(constants.DEBUG_SELECTION) {
			System.out.print("Beginne SelectionAndExpansion bei Wurzel des Baums: ");
			root.Weltzustand.print();
		}
		if(constants.RANDOM_DEBUG_UCB1) {
			constants.DEBUG_UCB1=false;	
			if(java.util.concurrent.ThreadLocalRandom.current().nextInt(300000)%300000==0) {//DEBUG
				constants.DEBUG_UCB1=true;												    //DEBUG
			}			
		}
																				
		while(true){
			if(Selected.Children==null){  // Knoten ist ein Blattknoten bei dem noch nie verssucht wurde zu expandierene
				Selected.expand();		// -> expandiere Blattknoten
				if(Selected.Children.length==0){ // knoten ist immer noch ein Blattknoten weil expansion keine Knoten erzeugen konnte
					if(constants.DEBUG_SELECTION) {											//DEBUG
						System.out.print("keine Kindknoten vorhanden: Return Knoten -> ");	//DEBUG
						Selected.Weltzustand.print();										//DEBUG
					}																		//DEBUG
					Node[] result = {Selected};
					return result;
				}
				if(Selected.Children[0]!=null){				
					if(constants.DEBUG_SELECTION) {															//DEBUG
						System.out.println("Es wurden "+Selected.Children.length							//DEBUG
					+" neue Blattknoten erzeugt und zurückgegeben mit runde="								//DEBUG
					+Selected.Children[0].Weltzustand.round+"_"+Selected.Children[0].Weltzustand.amZug);	//DEBUG
					}																						//DEBUG
					Heuristik.HeuristischeEmpfehlungen(Selected.Weltzustand, Selected.Children);
					return Selected.Children;	
				}else{							
					System.err.println("SelectionAndExpansion hat festegestellt das der ausgewählte kindknoten null ist das sollte eigentlich nicht passieren");
					Node[] result = {Selected};
					return result;				
				}
			}else{
				if(Selected.Children.length==0){ // knoten ist ein Blattknoten bei dem ein früherer expanssionsversuch keine neuen Knoten erzeugen konnte
					Node[] result = {Selected};	// gebe den aktuellen knoten zurück
					return result;
				}else{//es sind kindknoten vorhanden ->  berechne die UCB1 werte für die einzelnen Kindknoten und ermittle den knoten mit dem größten score
					if(constants.DEBUG_UCB1) {																	//DEBUG
						if(Selected==root) 																		//DEBUG
							System.out.println("Berechne UCB1 score kindknoten der Wurzel");					//DEBUG	
						System.out.println("Berechne UCB1 score der kindknoten "								//DEBUG
						+(Selected.Weltzustand.PacIDAmZug()<3?"(UCB1 maximieren)":"(UCB1 minimieren)"));		//DEBUG
					}																							//DEBUG
					int index=0;
					double[] UCB1Scores=new double[Selected.Children.length];
					boolean unserZug=Selected.Weltzustand.PacIDAmZug()<3;
					if(constants.DEBUG_UCB1) {									//DEBUG
						System.out.print("KindKnoten: ");						//DEBUG
						Selected.Children[0].Weltzustand.print();				//DEBUG
					}															//DEBUG
					double BestUCB1score=Selected.Children[0].getUCB1(unserZug);
					if(unserZug) {
						for(int i=1;i<Selected.Children.length;i++){
							if(constants.DEBUG_UCB1) {						//DEBUG
								System.out.print("KindKnoten: ");			//DEBUG
								Selected.Children[i].Weltzustand.print();	//DEBUG
							}												//DEBUG
							if((UCB1Scores[i]=Selected.Children[i].getUCB1(unserZug))>BestUCB1score) {
								BestUCB1score=UCB1Scores[index=i];
							}
						}
					}else {
						for(int i=1;i<Selected.Children.length;i++){
							if(constants.DEBUG_UCB1) {						//DEBUG
								System.out.print("KindKnoten: ");			//DEBUG
								Selected.Children[i].Weltzustand.print();	//DEBUG
							}												//DEBUG
							if((UCB1Scores[i]=Selected.Children[i].getUCB1(unserZug))<BestUCB1score) {
								BestUCB1score=UCB1Scores[index=i];
							}
						}
					}					
					if(constants.DEBUG_UCB1) {																	//DEBUG
						System.out.println("waehle kindknoten nummer "+index+" mit UCB1 Score="+BestUCB1score);	//DEBUG
					}																							//DEBUG
					Selected=Selected.Children[index];
					if(constants.DEBUG_SELECTION) {																//DEBUG
						System.out.println("Selection: KindKnoten nummer "+index+" mit UCB1 Score="				//DEBUG
						+BestUCB1score+" (aktion ="+Selected.action+")"+"runde="+Selected.Weltzustand.round+"_" //DEBUG
						+Selected.Weltzustand.amZug);															//DEBUG
					}																							//DEBUG
				}
			}
		}
	}
	
	public void ChangeRoot(Node newRoot){
		boolean found=false;
		for(int i=0;i<root.Children.length;i++)
			if(newRoot==root.Children[i]){
				found=true;
				break;
			}
		if(!found)
			System.out.println("MCTS THREAD: !!!!!!!!!!!!!!Mit der neuen Wurzel des Baums stimmt was nicht!!!!");

		root=newRoot;
		root.parent=null;
		doIteration();	// stellt sicher das die KindKnoten erzeugt werden bevor der Main Thread erfährt das die wurzel ausgetauscht wurde
		RoundActionUsed++;
	}

	public int TreeTraverselCount(){	// nur für Debug ausgaben
		return TreeTraverselCount(root);
	}
	
	private int TreeTraverselCount(Node n){ // nur für Debug ausgaben
		int counter=1;
		if(n.Children!=null) {
			for(int i=0;i<n.Children.length;i++) {
				counter+=TreeTraverselCount(n.Children[i]);
			}		
		}
		return counter;
	}
	
}
