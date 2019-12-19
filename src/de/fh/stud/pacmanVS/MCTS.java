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
	public volatile int WaitScore,GoNorthScore,GoSouthScore,GoWestScore,GoEastScore;// hier schreiben die Threads die die Spiele Simulieren ihre Ergebnisse rein
	public volatile int phase=0;	// TODO entferne public volatile (sobald die Debugausgaben nicht mehr benötigt werden)
	// TODO: BestActionSoFar wert zuweisen nach BackPropagation
	public Phaser phaser;
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
		System.out.println("MCTS Thread gestarted");
		doIteration();
		System.out.print("Wurzel: ");
		root.Weltzustand.print();//Debug Ausgabe
		for(int i=0;i<root.Children.length;i++) {
			System.out.print("Kindknoten "+i+" : ");
			root.Children[i].Weltzustand.print();
		}
		
		while(true){
			doIteration();
			iterationCounterSinceRootChange++;
			if(RoundActionUsed!=lastRoundActionNumber){
				//System.out.println("MCTS Thread hat festgestellt das der Main Thread den austausch der Wurzel angeordnet hat");
				System.out.println("Iterationen seit Wurzeltausch: "+iterationCounterSinceRootChange);
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
		
		// Step 1&2: Selection and Expansion
		Node[] Selected=SelectionAndExpansion(); //implemented
		
		// Step 3: Rollout/Playout
		SimulateGames(Selected);
		
		// Step 4 BackPropagation
		for(int i=0;i<Selected.length;i++) 
			Selected[i].BackPropagation(); 
		
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
		BestActionSoFar=root.Children[index].action;
		//System.out.println("Bisher bester Zug: "+BestActionSoFar+" (iterationen: "+iterationCounterSinceRootChange+")");

		
		// Tausche die Wurzel des Baums aus falls der Main Thread das angeordnet hat

	}
	
	
	private void SimulateGames(Node[] Selected) {
		phaser.bulkRegister(Selected.length);		// telling the Phaser that how many Simulations/Threads need to finish their work before 
		//System.out.println("MCTS thread:	Bitte "+Selected.length+" Threads mit eine Simulation zu starten");
		Playout playTmp;
		for(int i=0;i<Selected.length;i++){
			switch(Selected[i].Weltzustand.action){
			case WAIT:		playTmp=playWait;		break;
			case GO_NORTH:	playTmp=playGoNorth;	break;
			case GO_SOUTH:	playTmp=playGoSouth;	break;
			case GO_WEST:	playTmp=playGoWest;		break;
			case GO_EAST:	playTmp=playGoEast;		break;  
			default:		playTmp=null;
			}
			playTmp.toSimulate=Selected[i].Weltzustand;
			playTmp.phaser.arrive();
			//System.out.println("MCTS THREAD:  Bitte Playout Thread_"+playTmp.id+" aufzuwachen");
			
			//try {Thread.sleep(50);}catch (Exception e) {} 	//verzögerung damit die Debug ausgaben in der richtigen reihenfolge auf der console landen
			
		}
		
		phaser.awaitAdvance(phase++);
		//System.out.println("MCTS thread:	Alle Playout Threads haben ihre Simulationen abgeschlossen");
	}
	
	
	
	public Node[] SelectionAndExpansion(){
		Node Selected=root;
		while(true){
			if(Selected.Children==null){// Node is a Leaf node
				Selected.expand();
//				System.out.println("Baum expandiert");
//				System.out.println("Parent Node:");
//				System.out.print("PacPos -> ");
//				for(int i=0;i<6;i++)
//					System.out.print(Selected.Weltzustand.PacPos[i]+" \t");
//				System.out.print("\t Pacman auf Feld "+Selected.Weltzustand.PacPos[WorldState.zugreihenfolge[Selected.Weltzustand.amZug]]+" am Zug");
//				for(int i=0;i<Selected.Children.length;i++) {
//					System.out.println("\nChildnode "+i+":");
//					System.out.print("PacPos -> ");
//					for(int i2=0;i2<6;i2++) {
//						System.out.print(Selected.Children[i].Weltzustand.PacPos[i2]+" \t");
//					}
//					System.out.print("\tAction -> "+Selected.Children[i].Weltzustand.action+" amZug: "+Selected.Children[i].Weltzustand.PacPos[WorldState.zugreihenfolge[Selected.Children[i].Weltzustand.amZug]]);
//				}
//				System.out.println("\nnode expansion done");
				if(Selected.Children[0]!=null){
					return Selected.Children;
				}else{
//					System.out.println("Select Node has no Children after expansion (this might be caused by a bug or it's just Blocked)");
					Node[] result = {Selected};
					return result;
				}
			}else{
				if(Selected.Children.length==0){
//					System.out.println("Select Node has no Children (this might be caused by a bug)");
					Node[] result = {Selected};
					return result;
				}else{
					double MaxUCB1score=Double.MIN_VALUE;
					int index=0;
					double[] UCB1Score=new double[Selected.Children.length];
					for(int i=0;i<Selected.Children.length;i++){
						if((UCB1Score[i]=Selected.Children[i].getUCB1())>MaxUCB1score) {
							MaxUCB1score=UCB1Score[index=i];
							if(MaxUCB1score==Double.MAX_VALUE) break;
						}
					}
					Selected=Selected.Children[index];
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
			System.err.println("Mit der neuen Wurzel des Baums stimmt was nicht!!!!");
		
		//System.out.println("MCTS Thread: tausche Wurzel aus");
		root=newRoot;
		root.parent=null;
		doIteration();	// stellt sicher das die KindKnoten erzeugt werden bevor der Main Thread erfährt das die wurzel ausgetauscht wurde
		RoundActionUsed++;
	}
	

	
	
	
}
