package de.fh.stud.pacmanVS;

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
	static boolean firstpercept=true;
	static int updatestatecounter=0;
	static MCTS SearchTree;
	public static Thread MainThread;
	long StartTimeUpdateState;
	
	public static VSPacmanTileType[][] view;
	
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
		if(firstpercept){
			view=percept.getTotalLevel();
			seconds = 1.8;
			Node.Xsize=percept.getTotalLevel().length;
			System.out.println("Erstes Percept erhalten");
			if(constants.DEBUG_PERCEPTS) {
				printPercept(percept);
			}
			firstpercept=false;
			MainThread=Thread.currentThread();
			WorldState.DotsOnEachSide = percept.getRemainingOwnDots().size();
			Vector2 spawn = startInfo.getOpponentSpawn().get(0);
			Vector2 pos = percept.getOpponents().get(0);
			boolean start=(pos.getX()!=spawn.getX() || pos.getY()!=spawn.getY());
			if(start){
				int[] zug = {3,0,4,1,5,2};
				WorldState.zugreihenfolge = zug;
			}else{
				int[] zug = {0,3,1,4,2,5};
				WorldState.zugreihenfolge = zug;
			}
			Base.createWorldBase(percept.getTotalLevel(), startInfo);
			WorldState w = new WorldState(percept);
			if(constants.DEBUG_PERCEPTS) {
				printPercept(percept);
			}
			System.out.println("Suchbaum thread gestartet nach "+(System.nanoTime()-StartTimeUpdateState)/1000000+" milisekunden");
			SearchTree=new MCTS(w);
			SearchTree.start();
		}else{	
			seconds = 1;
			if(constants.DEBUG_PERCEPTS) {
				System.out.println("Percept von server erhalten :");
				printPercept(percept);
			}

			while(true){
				if(SearchTree.lastRoundActionNumber==SearchTree.RoundActionUsed){
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
//									System.out.println("match: "+PacPosNode[i2]+" == "+positionOwnTeam[i3]+" TOTALFOUND: "+totalFound);
									break;
								}
//								else {
//									System.out.println("no match: "+PacPosNode[i2]+" != "+positionOwnTeam[i3]);
//								}
							}
							if(!found) {
								break;
							}
						}
						if(totalFound==3){
							for(int i2=3;i2<6;i2++){
								found=false;
								for(int i3=0;i3<3;i3++) {
									if(ChildNodes[i].Weltzustand.PacPos[i2]==positionEnemyTeam[i3]){
										found=true;
										totalFound++;
//										System.out.println("match: "+PacPosNode[i2]+" == "+positionEnemyTeam[i3]+" TOTALFOUND: "+totalFound);
										break;
									}
//									else {
//										System.out.println("no match: "+PacPosNode[i2]+" != "+positionEnemyTeam[i3]);
//									}
								}
								if(!found)
									break;
							}
						}
						if(totalFound==6) {
							newRoot=ChildNodes[i];
							for(int i2=0;i2<percept.getRemainingOwnDots().size();i2++) {
								if((newRoot.Weltzustand.world[Base.Vector2ToInt(percept.getRemainingOwnDots().get(i2))*2]&B14)==0){
									System.err.println("Dot position "+percept.getRemainingOwnDots().get(i2)+" fehlt im weltbild");
								}
							}
							for(int i2=0;i2<percept.getRemainingOpponentDots().size();i2++) {
								if((newRoot.Weltzustand.world[Base.Vector2ToInt(percept.getRemainingOpponentDots().get(i2))*2]&B14)==0){
									System.err.println("Dot and position "+percept.getRemainingOpponentDots().get(i2)+" fehlt im weltbild");
								}
							}
							
							
							
							break;
						}else {
//							System.out.println("Childnode "+i+" totalmacht "+totalFound+" / 6");
						}
					}
					
					
					if(newRoot!=null) {
						if(constants.DEBUG_CHILDNODES) {
							System.out.println("gegner hat aktion aktion: "+newRoot.action+" gewählt");
						}
						
						SearchTree.NewRoot=newRoot;
						//System.out.println("Main Thread hat MCTS Thread angewiesen seine Wurzel auszutauschen (wegen neuen percept von server)");
					}else {// Debug Ausgaben
						System.out.println("FEHLER!!!!  die aktuelle Situation wurde vom Suchbaum nicht vorhergesehen ");
						printPercept(percept);
					}					
					
					SearchTree.lastRoundActionNumber++;
					while(SearchTree.lastRoundActionNumber!=SearchTree.RoundActionUsed) {
						try {
							if(constants.DEBUG_THREADSYNC) {
								System.out.println("MCTS-thread hat wurzel (nach gegner aktion) noch nicht angepasst Arrived:"+SearchTree.phaser.getArrivedParties()+"/"+SearchTree.phaser.getRegisteredParties()+" Phase: "+SearchTree.phaser.getPhase());
							}
							Thread.currentThread().sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					if(constants.DEBUG_ROOT) {
						System.out.print("Neue Wurzel im Suchbaum: ");
						SearchTree.root.Weltzustand.print();
					}
					if(constants.DEBUG_CHILDNODES)
						for(int i=0;i<SearchTree.root.Children.length;i++) {
							System.out.print("Kindknoten "+i+": ");
							SearchTree.root.Children[i].Weltzustand.print();
						}
					break;
				}else{
					if(constants.DEBUG_THREADSYNC) {
						System.out.println("MCTS Thread ist noch nicht bereit für eine neue Wurzel versuche in einigen milisekunden erneut "+SearchTree.lastRoundActionNumber+"/"+SearchTree.RoundActionUsed);
					}
					try {
						Thread.currentThread().sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
		return SelectedAction;
	}

	@Override
	protected void onGameover(VSPacmanGameResult gameResult) {
		
	}

}
