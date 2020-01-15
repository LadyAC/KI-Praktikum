package de.fh.stud.pacmanVS;

import java.util.ArrayList;
import java.util.List;


import de.fh.agent.Agent;
import de.fh.agent.VSPacmanAgent;
import de.fh.pacmanVS.VSPacmanGameResult;
import de.fh.pacmanVS.VSPacmanPercept;
import de.fh.pacmanVS.enums.VSPacmanAction;
import de.fh.pacmanVS.enums.VSPacmanActionEffect;
import de.fh.pacmanVS.enums.VSPacmanTileType;
import de.fh.util.Vector2;
import static de.fh.stud.pacmanVS.constants.*;

public class MyPacmanVSAgent extends VSPacmanAgent {
	public String name;
	double seconds;
	static boolean isFirstPercept=true;
	static int updatestatecounter=0;
	static MCTS SearchTree;
	public static Thread MainThread;
	long StartTimeUpdateState;
	static VSPacmanPercept firstPercept;
	private static ArrayList<VSPacmanAction> RecordedActions;
	
	public static void main(String[] args) {
		MyPacmanVSAgent agent = new MyPacmanVSAgent("AgentenName");
		Agent.start(agent, "127.0.0.1", 5000);
	}

	public MyPacmanVSAgent(String name) {
		super(name);
		this.name=name;
	}
	
	public static void printPercept(VSPacmanPercept percept) {
		System.out.println("pacman positionen laut percept");
		System.out.print("Eigene Pacman: ");
		System.out.print(percept.getPosition()+"\t");
		System.out.print(percept.getTeammates().get(0)+"\t");
		System.out.println(percept.getTeammates().get(1)+"\t");
		System.out.print("Gegner Pacman: ");
		System.out.print(percept.getOpponents().get(0)+"\t");
		System.out.print(percept.getOpponents().get(1)+"\t");
		System.out.println(percept.getOpponents().get(2)+"\t");
		System.out.println("AmZug: "+percept.getPosition());		
	}
	
	

	@Override
	public void updateState(VSPacmanPercept percept, VSPacmanActionEffect actionEffect) {
		StartTimeUpdateState=System.nanoTime();
		if(isFirstPercept){
			seconds = 1.8;
			Node.Xsize=percept.getTotalLevel().length;
			System.out.println("Erstes Percept erhalten");
			firstPercept=percept;
			if(constants.DEBUG_PERCEPTS) {			//DEBUG
				printPercept(percept);				//DEBUG
			}										//DEBUG
			isFirstPercept=false;
			MainThread=Thread.currentThread();
			WorldState.DotsOnEachSide = percept.getRemainingOwnDots().size();
			Vector2 spawn = startInfo.getOpponentSpawn().get(0);
			Vector2 pos = percept.getOpponents().get(0);
			boolean start=(pos.getX()!=spawn.getX() || pos.getY()!=spawn.getY());
			if(start){
				int[] zug = {3,0,4,1,5,2};
				WorldState.zugreihenfolge = zug;
				RecordedActions=null;
				System.out.println("gegner hat angefangen");
			}else{
				int[] zug = {0,3,1,4,2,5};
				RecordedActions=new ArrayList<VSPacmanAction>();
				RecordedActions.add(VSPacmanAction.WAIT);
				WorldState.zugreihenfolge = zug;
				System.out.println("wir haben angefangen");
			}
			Base.createWorldBase(percept.getTotalLevel(), startInfo);
			WorldState w = new WorldState(percept);
			SearchTree=new MCTS(w);
			SearchTree.start();
		}else{	
			seconds = 0.3;
			if(constants.DEBUG_PERCEPTS) {							//DEBUG
				System.out.println("Percept von server erhalten :");//DEBUG
				printPercept(percept);								//DEBUG
			}														//DEBUG

			while(true){
				if(SearchTree.lastRoundActionNumber==SearchTree.RoundActionUsed) {
					int[] positionOwnTeam=new int[3];
					int[] positionEnemyTeam=new int[3];
					positionOwnTeam[0]=Base.Vector2ToInt(percept.getPosition());
					positionOwnTeam[1]=Base.Vector2ToInt(percept.getTeammates().get(0));
					positionOwnTeam[2]=Base.Vector2ToInt(percept.getTeammates().get(1));
					for(int i=0;i<3;i++)
						positionEnemyTeam[i]=Base.Vector2ToInt(percept.getOpponents().get(i));	
					Node[] ChildNodes=SearchTree.root.Children;
					Node newRoot=null;
					boolean found;
					int totalFound;
					for(int i=0;i<ChildNodes.length;i++){
						totalFound=0;
						for(int i2=0;i2<3;i2++){ // gehe unsere pacman 1-3 durch (laut suchbaum node)
							found=false;
							for(int i3=0;i3<3;i3++) {
								if(ChildNodes[i].Weltzustand.PacPos[i2]==positionOwnTeam[i3]){
									found=true;
									totalFound++;
									break;
								}
							}
							if(!found)
								break;
						}
						if(totalFound==3){
							for(int i2=3;i2<6;i2++){
								found=false;
								for(int i3=0;i3<3;i3++) {
									if(ChildNodes[i].Weltzustand.PacPos[i2]==positionEnemyTeam[i3]){
										found=true;
										totalFound++;
										break;
									}
								}
								if(!found)
									break;
							}
						}
						if(totalFound==6) {		// alle 6 Pacman positionen vom percept müssen mit einem der kindknoten übereinstimmen
							newRoot=ChildNodes[i];
							for(int i2=0;i2<percept.getRemainingOwnDots().size();i2++) {													//DEBUG
								if((newRoot.Weltzustand.world[Base.Vector2ToInt(percept.getRemainingOwnDots().get(i2))*2]&B14)==0){			//DEBUG
									System.err.println("Dot position "+percept.getRemainingOwnDots().get(i2)+" fehlt im weltbild");			//DEBUG
								}																											//DEBUG
							}																												//DEBUG
							for(int i2=0;i2<percept.getRemainingOpponentDots().size();i2++) {												//DEBUG
								if((newRoot.Weltzustand.world[Base.Vector2ToInt(percept.getRemainingOpponentDots().get(i2))*2]&B14)==0){	//DEBUG
									System.err.println("Dot and position "+percept.getRemainingOpponentDots().get(i2)+" fehlt im weltbild");//DEBUG
								}																											//DEBUG
							}																												//DEBUG
							break;
						}
					}
					
					
					if(newRoot!=null) {
						if(RecordedActions!=null)
							RecordedActions.add(newRoot.action);
						if(constants.DEBUG_CHILDNODES) {					//DEBUG
							System.out.println("gegner hat aktion aktion: "	//DEBUG
							+newRoot.action+" gewählt");					//DEBUG
						}													//DEBUG
						SearchTree.NewRoot=newRoot;
					}else {// Debug Ausgaben
						System.out.println("FEHLER!!!!  die aktuelle Situation wurde vom Suchbaum nicht vorhergesehen ");
						printPercept(percept);
						System.out.println("Versuche Baum zu erstteln unter der annahme das der gegner im ersten Zug ein Wait ausgeführt hat");
						
						for(int i=0;i<RecordedActions.size();i++) {
							System.out.println(RecordedActions.get(i));
						}
						
						
						WorldState RecreatedRoot= new WorldState(firstPercept);
						WorldState.zugreihenfolge= new int[] {3,0,4,1,5,2};	// anpassung der Zugreihenfolge als wenn der gegner angefangen hat
						ArrayList<WorldState> recreatedChildStates=WorldState.SimulateRecordedActions(RecreatedRoot,RecordedActions,percept);
						
						WorldState ReconstructedRootState=null;
						//---------------------------------------------- a
						for(int i=0;i<recreatedChildStates.size();i++){
							totalFound=0;
							for(int i2=0;i2<3;i2++){ // gehe unsere pacman 1-3 durch (laut suchbaum node)
								found=false;
								for(int i3=0;i3<3;i3++) {
									if(recreatedChildStates.get(i).PacPos[i2]==positionOwnTeam[i3]){
										found=true;
										totalFound++;
										break;
									}
								}
								if(!found)
									break;
							}
							if(totalFound==3){
								for(int i2=3;i2<6;i2++){
									found=false;
									for(int i3=0;i3<3;i3++) {
										if(recreatedChildStates.get(i).PacPos[i2]==positionEnemyTeam[i3]){
											found=true;
											totalFound++;
											break;
										}
									}
									if(!found)
										break;
								}
							}
							if(totalFound==6) {		// alle 6 Pacman positionen vom percept müssen mit einem der kindknoten übereinstimmen
								ReconstructedRootState=recreatedChildStates.get(i);
								break;
							}
						}
						
						if(ReconstructedRootState==null) {
							System.err.println("Reconstruktion des Baums gescheitert");
						}else {
							System.out.println("°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°");
							System.out.println("°°°°°°°°°°°°°°°°°°°°°°°DER BAUM WURDE REPARIERT°°°°°°°°°°°°°°°°°°°°°°°°°°");
							System.out.println("°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°°");
							SearchTree.NewRoot=new Node(ReconstructedRootState,null,SearchTree);
						}
						//---------------------------------------------- b
					}					
					
					SearchTree.lastRoundActionNumber++;
					while(SearchTree.lastRoundActionNumber!=SearchTree.RoundActionUsed) {
						try {
							if(constants.DEBUG_THREADSYNC) {																	//DEBUG
								System.out.println("MCTS-thread hat wurzel (nach gegner aktion) noch nicht angepasst Arrived:"	//DEBUG
								+SearchTree.phaser.getArrivedParties()+"/"+SearchTree.phaser.getRegisteredParties()+" Phase: "	//DEBUG
								+SearchTree.phaser.getPhase());																	//DEBUG
							}																									//DEBUG
							Thread.currentThread().sleep(100);
						} catch (InterruptedException e) {
						}
						
					}
					if(constants.DEBUG_ROOT) {							//DEBUG
						System.out.print("Neue Wurzel im Suchbaum: ");	//DEBUG
						SearchTree.root.Weltzustand.print();			//DEBUG
					}													//DEBUG
					if(constants.DEBUG_CHILDNODES)								//DEBUG
						for(int i=0;i<SearchTree.root.Children.length;i++) {	//DEBUG
							System.out.print("Kindknoten "+i+": ");				//DEBUG
							SearchTree.root.Children[i].Weltzustand.print();	//DEBUG
						}
					break;
				}else{
					if(constants.DEBUG_THREADSYNC) {												//DEBUG
						System.out.println("MCTS Thread ist noch nicht bereit für eine "			//DEBUG
						+ "neue Wurzel versuche in einigen milisekunden erneut "					//DEBUG
								+SearchTree.lastRoundActionNumber+"/"+SearchTree.RoundActionUsed);	//DEBUG
					}																				//DEBUG
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {}
				}
			}
		}
	}

	@Override
	public VSPacmanAction action() {
		while(System.nanoTime()-StartTimeUpdateState < seconds*1000000000){	// gebe Suchbaum MINDESTENS 0,5 Sekunden Zeit für das auswählen eines zuges
			try {
				Thread.currentThread().sleep(200);	
			} catch (InterruptedException e) {
			}
		}
		boolean found=false;
		VSPacmanAction SelectedAction=SearchTree.BestActionSoFar;
		for(int i=0;i<SearchTree.root.Children.length;i++) {
			if(SearchTree.root.Children[i].action==SelectedAction) {
				SearchTree.NewRoot=SearchTree.root.Children[i];
				SearchTree.lastRoundActionNumber++;
				if(constants.DEBUG_ACTION) {
					System.out.println(name+" setzt \t"+SelectedAction+" ein");
				}
				while(SearchTree.lastRoundActionNumber!=SearchTree.RoundActionUsed) {
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(constants.DEBUG_THREADSYNC) {
						System.out.println("MCTS-thread hat wurzel (nach unserer aktion) noch nicht angepasst Arrived:"+SearchTree.phaser.getArrivedParties()+"/"+SearchTree.phaser.getRegisteredParties()+" Phase: "+SearchTree.phaser.getPhase());
					}
					
				}
				if(constants.DEBUG_ROOT) {
					System.out.print("Neue Wurzel im Suchbaum: ");
					SearchTree.root.Weltzustand.print();
				}
				if(constants.DEBUG_CHILDNODES) {
					for(int i2=0;i2<SearchTree.root.Children.length;i2++) {
						System.out.print("Kindknoten "+i2+": ");
						SearchTree.root.Children[i2].Weltzustand.print();
					}
				}

				found=true;
				//System.out.println("MAIN THREAD: ordne wurzeltausch nach eigenen zug an ("+SearchTree.lastRoundActionNumber+")");
				break;
			}
		}
		if(!found) {
			System.err.println("Fehler die gewählte aktion in der action methode hat keinen entsprechenden knoten im baum wurzeltausch nicht möglich");
		}

		System.out.println("action methode sendet aktion "+SelectedAction+" nach "+((System.nanoTime()-StartTimeUpdateState)/1000000)+" milisekunden");
		if(RecordedActions!=null)
			RecordedActions.add(SelectedAction);
		return SelectedAction;
	}

	@Override
	protected void onGameover(VSPacmanGameResult gameResult) {
		
	}

}
