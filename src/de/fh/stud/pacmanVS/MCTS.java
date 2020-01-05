package de.fh.stud.pacmanVS;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	public volatile int WaitScore,GoNorthScore,GoSouthScore,GoWestScore,GoEastScore;// hier schreiben die Threads die die Spiele Simulieren ihre Ergebnisse rein
	// TODO: BestActionSoFar wert zuweisen nach BackPropagation
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
		System.out.println("MCTS Thread gestarted Phase:"+this.phaser.getPhase());
		doIteration();
		if(constants.DEBUG_ROOT) {
			System.out.print("Wurzel: ");
			root.Weltzustand.print();			
		}
		if(constants.DEBUG_CHILDNODES) {
			for(int i=0;i<root.Children.length;i++) {
				System.out.print("Kindknoten "+i+" : ");
				root.Children[i].Weltzustand.print();
			}
		}


		
		while(true){
			doIteration();
			if(constants.DEBUG_TREE) {
				System.out.println(TreeTraversel());
			}
			iterationCounterSinceRootChange++;
			if(RoundActionUsed!=lastRoundActionNumber){
//				System.out.println("MCTS Thread hat festgestellt das der Main Thread den austausch der Wurzel angeordnet hat");
				if(constants.DEBUG_ROOT) {
					System.out.println("Iterationen seit Wurzeltausch: "+iterationCounterSinceRootChange);
				}
				ChangeRoot(this.NewRoot);
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
//System.out.println("start iteration");
		// Step 1&2: Selection and Expansion
		Node[] Selected=SelectionAndExpansion();
//System.out.println("#1 Selection&Expansion done");
		// Step 3: Rollout/Playout
		SimulateGames(Selected);
//System.out.println("#2 Simulation done");
		// Step 4 BackPropagation
		for(int i=0;i<Selected.length;i++) 
			Selected[i].BackPropagation(); 
//System.out.println("#3 Backproagation done");
		// ermittle besten Zug nach aktuellen stand
		double bestScore=Double.MIN_VALUE,tmpScore;
		int index=0;
		for(int i=0;i<root.Children.length;i++){
			Node n=root.Children[i];
			tmpScore=((double)n.totalScore)/n.simulationCount;
			if(bestScore<tmpScore) {
				bestScore=tmpScore;
				index=i;
			}
		}
		//System.out.println("MCTS THREAD: bestactionsofar Updated");
		if(constants.DEBUG_BESTACTION){
			if(BestActionSoFar!=root.Children[index].action || iterationCounterSinceRootChange%1000==0) {
				System.out.println("Bisher bester Zug: "+BestActionSoFar+" (iterationen: "+iterationCounterSinceRootChange+")");
			}
		}
		BestActionSoFar=root.Children[index].action;


		
		// Tausche die Wurzel des Baums aus falls der Main Thread das angeordnet hat
//System.out.println("end iteration");
	}
	
	
	private void SimulateGames(Node[] Selected) {
		String debugString="";
		
		phaser = new Phaser(Selected.length);
//		phaser.bulkRegister(Selected.length);		// telling the Phaser that how many Simulations/Threads need to finish their work before 
		//System.out.println("MCTS thread:	Bitte "+Selected.length+" Threads mit eine Simulation zu starten");
		Playout playTmp;
		if(phaser.getPhase()!=0) {
			
		};
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
			debugString+="  playout id="+playTmp.id+" phase="+playTmp.phaser.getPhase() ;
			playTmp.toSimulate=Selected[i].Weltzustand;

//			System.out.println("send to: Playout id="+playTmp.id+" activation-signal phase= "+playTmp.phaser.getPhase());
			playTmp.phaser.arrive(); // Signal an den entsprechenden thread die arbeit auszuführen
			
			//System.out.println("MCTS THREAD:  Bitte Playout Thread_"+playTmp.id+" aufzuwachen");
			
			//try {Thread.sleep(50);}catch (Exception e) {} 	//verzögerung damit die Debug ausgaben in der richtigen reihenfolge auf der console landen
			
		}
		
		//try {Thread.currentThread().sleep(50);} catch (InterruptedException e) {}
//		System.out.println("MCTS phase (nach wartezeit)"+phaser.getPhase());
		
		//phaser.awaitAdvance(0);
		try {
			phaser.awaitAdvanceInterruptibly(0, 1, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(phaser.getPhase()==0) {
			System.err.println("MCTS left with timeout unarrived="+phaser.getUnarrivedParties()+" \n"+debugString+"\n");
			System.err.println(playWait.id+" phase: "+playWait.phaser.getPhase()+" "+playWait.running);
			System.err.println(playGoNorth.id+" phase: "+playGoNorth.phaser.getPhase()+" "+playGoNorth.running);
			System.err.println(playGoSouth.id+" phase: "+playGoSouth.phaser.getPhase()+" "+playGoSouth.running);
			System.err.println(playGoWest.id+" phase: "+playGoWest.phaser.getPhase()+" "+playGoWest.running);
			System.err.println(playGoEast.id+" phase: "+playGoEast.phaser.getPhase()+" "+playGoEast.running);
		}
		//System.out.println("MCTS thread:	Alle Playout Threads haben ihre Simulationen abgeschlossen");
	}
	
	
	
	public Node[] SelectionAndExpansion(){
		Node Selected=root;
		if(constants.DEBUG_SELECTION) {
			System.out.print("Beginne SelectionAndExpansion bei Wurzel des Baums: ");
			root.Weltzustand.print();
		}
		while(true){
			if(Selected.Children==null){  // Knoten ist ein Blattknoten bei dem noch nie verssucht wurde zu expandierene
				Selected.expand();		// -> expandiere Blattknoten
				if(Selected.Children.length==0){ // knoten ist immer noch ein Blattknoten weil expansion keine Knoten erzeugen konnte
					if(constants.DEBUG_SELECTION) {
						System.out.print("keine Kindknoten vorhanden: Return Knoten -> ");
						Selected.Weltzustand.print();
					}
					Node[] result = {Selected};
					return result;
				}
				if(Selected.Children[0]!=null){
					if(constants.DEBUG_SELECTION) {
						System.out.println("Es wurden "+Selected.Children.length+" neue Blattknoten erzeugt und zurückgegeben mit runde="+Selected.Children[0].Weltzustand.round+"_"+Selected.Children[0].Weltzustand.amZug);
					}
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
					double MaxUCB1score=Double.NEGATIVE_INFINITY; 
					int index=0;
					double[] UCB1Scores=new double[Selected.Children.length];
					
					if(constants.DEBUG_UCB1) {
						System.out.println("Berechne UCB1 score der kindknoten");
					}
					
					for(int i=0;i<Selected.Children.length;i++){
						if(constants.DEBUG_UCB1) {
							System.out.print("KindKnoten: ");
							Selected.Children[i].Weltzustand.print();
						}						
						if((UCB1Scores[i]=Selected.Children[i].getUCB1())>MaxUCB1score) {
							MaxUCB1score=UCB1Scores[index=i];
						}
					}
					if(MaxUCB1score==Double.MIN_VALUE) {
						System.err.println("UCB1 SCore ist Double.MIN_VALUE");
						System.err.println("anzahl verglichener knoten: "+Selected.Children.length);
						for(int i=0;i<Selected.Children.length;i++){
							System.err.println("Score ="+UCB1Scores[index=i]+" -> "+(UCB1Scores[index=i]>Double.MIN_VALUE));
						}
					}
					if(constants.DEBUG_UCB1) {
						System.out.println("waehle kindknoten nummer "+index+" mit UCB1 Score="+MaxUCB1score);
					}
					Selected=Selected.Children[index];
					if(constants.DEBUG_SELECTION) {
						System.out.println("Selection: KindKnoten nummer "+index+" mit UCB1 Score="+MaxUCB1score+" (aktion ="+Selected.action+")"+"runde="+Selected.Weltzustand.round+"_"+Selected.Weltzustand.amZug);
					}
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
			System.out.println("!!!!!!!!!!!!!!Mit der neuen Wurzel des Baums stimmt was nicht!!!!");
		if(constants.DEBUG_ROOT) {
			System.out.println("MCTS Thread: tausche Wurzel aus");
		}
		
		//System.out.print("neue Wurzel: ");
		//newRoot.Weltzustand.print();
		
		root=newRoot;
		root.parent=null;
		doIteration();	// stellt sicher das die KindKnoten erzeugt werden bevor der Main Thread erfährt das die wurzel ausgetauscht wurde
		RoundActionUsed++;
	}
	

	public String TreeTraversel(){
		StringBuilder treeStringbuilder = new StringBuilder();
		treeStringbuilder.append("TREE START:\n");
		TreeTraversel(root,treeStringbuilder);
		treeStringbuilder.append("TREE END\n");
		return treeStringbuilder.toString();
	}
	
	private void TreeTraversel(Node n,StringBuilder treeStringbuilder){
		treeStringbuilder.append(n.NodeToString());
		treeStringbuilder.append("\n");
		if(n.Children!=null) {
			for(int i=0;i<n.Children.length;i++) {
				TreeTraversel(n.Children[i],treeStringbuilder);
			}		
		}

	}
	
	
}
