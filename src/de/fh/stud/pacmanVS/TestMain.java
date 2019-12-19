package de.fh.stud.pacmanVS;
import static de.fh.pacmanVS.enums.VSPacmanTileType.*;

import java.util.ArrayList;
import java.util.List;

import de.fh.pacmanVS.VSPacmanStartInfo;
import de.fh.pacmanVS.enums.VSPacmanTileType;
import de.fh.util.Vector2;
import de.fh.pacmanVS.enums.Team;

public class TestMain {
	public static void drawWorld(VSPacmanTileType[][] view){
		String view_row = "";
		//System.out.println("viewsize: " + view.length + "*" + view[0].length);
		for (int x = 0; x < view[0].length; x++) {
			for (int y = 0; y < view.length; y++) {
				view_row += " " + tileToS(view[y][x]);
			}
			System.out.println(view_row);
			view_row = "";
		}
		System.out.println("-------------------------------");
	}
	
	public static String tileToS(VSPacmanTileType p) {
		switch(p) {
		case EMPTY: return " ";
		case DOT:   return "•";
		default:    return p.toString();
		}
	}
	
	
	public static void main(String[] args) {
		// DOT
		// EMPTY
		// GHOST
		// GHOST_AND_DOT
		// PACMAN
		// WALL
		
		
//		VSPacmanTileType[][] WorldBaseTestMap= {
//				{WALL,WALL,WALL,WALL,WALL},
//				{WALL,DOT ,DOT ,DOT ,WALL},
//				{WALL,DOT ,WALL,DOT ,WALL},
//				{WALL,DOT ,DOT ,DOT ,WALL},
//				{WALL,WALL,WALL,WALL,WALL}
//		};
//		Knoten.createWorldBase(WorldBaseTestMap, Team.BLUE);
//		for(int i=0;i<Knoten.WorldBaseBig.length;i++) {
//			System.out.println(Integer.toBinaryString(Knoten.WorldBaseBig[i]));
//		}

		
		VSPacmanTileType[][] mapMirror= {
		// 0	1		2		3		4		5		6		7		8		9		10		11		12		13		14		15		16		17		18		19		20		21	 
		{WALL	,WALL	,WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL},//0
		{WALL	,WALL	,WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	WALL},//1
		{WALL	,PACMAN	,EMPTY,	EMPTY,	WALL,	WALL,	DOT,	WALL,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	WALL,	DOT,	WALL,	WALL,	EMPTY,	EMPTY,	PACMAN,	WALL},//2
		{WALL	,WALL	,WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL},//3
		{WALL	,WALL	,DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL},//4
		{WALL	,WALL	,DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL},//5
		{WALL	,WALL	,DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL},//6
		{WALL	,WALL	,DOT,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	DOT,	WALL,	WALL},//7
		{WALL	,WALL	,DOT,	DOT,	DOT,	WALL,	DOT,	DOT,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	DOT,	DOT,	WALL,	DOT,	DOT,	DOT,	WALL,	WALL},//8
		{WALL	,WALL	,WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	WALL,	WALL},//9
		{WALL	,WALL	,WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL},//10
		{WALL	,PACMAN	,EMPTY,	EMPTY,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	EMPTY,	EMPTY,	PACMAN,	WALL},//11
		{WALL	,WALL	,WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL},//12
		{WALL	,WALL	,WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	WALL,	WALL},//13
		{WALL	,WALL	,DOT,	DOT,	DOT,	WALL,	DOT,	DOT,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	DOT,	DOT,	WALL,	DOT,	DOT,	DOT,	WALL,	WALL},//14
		{WALL	,WALL	,DOT,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	DOT,	WALL,	WALL},//15
		{WALL	,WALL	,DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL},//16
		{WALL	,WALL	,DOT,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	DOT,	WALL,	WALL},//17
		{WALL	,WALL	,DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL},//18
		{WALL	,WALL	,WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL,	WALL,	DOT,	WALL,	WALL,	WALL},//19
		{WALL	,PACMAN	,EMPTY,	EMPTY,	WALL,	WALL,	WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	DOT,	DOT,	WALL,	WALL,	WALL,	WALL,	EMPTY,	EMPTY,	PACMAN,	WALL},//20
		{WALL	,WALL	,WALL,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	DOT,	WALL,	WALL,	WALL},//21
		{WALL	,WALL	,WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL,	WALL},//22	
		};
		VSPacmanTileType[][] map=new VSPacmanTileType[mapMirror[0].length][mapMirror.length];
		for(int x=0;x<mapMirror[0].length;x++) {
			for(int y=0;y<mapMirror.length;y++) {
				map[x][y] = mapMirror[y][x];
			}
		}
		drawWorld(map);
		
		VSPacmanStartInfo asd = new VSPacmanStartInfo();
		asd.setTeam(Team.BLUE);
		List<Vector2> l = new ArrayList<Vector2>();
		l.add(new Vector2(12,2));
		l.add(new Vector2(14,2));
		l.add(new Vector2(16,2));
		
		List<Vector2> l2 = new ArrayList<Vector2>();
		l2.add(new Vector2(12,6));
		l2.add(new Vector2(14,6));
		l2.add(new Vector2(16,6));
		asd.setOpponentSpawn(l);
		asd.setTeammateSpawns(l2);
		Base.createWorldBase(map, asd);
		
		
//		for(int i=0;i<Knoten.WorldBase.length;i++) {
//			System.out.println(i+" "+Integer.toBinaryString(Knoten.WorldBase[i]));
//		}
		
		int[] PacPos=new int[6];
		
//		PacPos[0]=Base.xyToIndex[1] [2];	// eigener pacman 1
//		PacPos[1]=Base.xyToIndex[1] [11];	// eigener Pacman 2
//		PacPos[2]=Base.xyToIndex[1] [20];	// eigener pacman 3
//		PacPos[3]=Base.xyToIndex[20][2];	// gegner  Pacman 1
//		PacPos[4]=Base.xyToIndex[20][11];	// gegner  Pacman 2
//		PacPos[5]=Base.xyToIndex[20][20];   // gegner  Pacman 3

		PacPos[0]=16;
		PacPos[1]=102;	// eigener Pacman 2
		PacPos[2]=187;	// eigener pacman 3
		PacPos[3]=8;	// gegner  Pacman 1
		PacPos[4]=52;	// gegner  Pacman 2
		PacPos[5]=199;   // gegner  Pacman 3
		
		int[] zug = {0,3,1,4,1,5};
		WorldState.zugreihenfolge=zug;
		//private WorldState(int[] world,int[][] carriedDots,int[] PacPos,int amZug,int round,int ourTeamDotsSecured,int enemyTeamDotsSecured){
		WorldState root= new WorldState(Base.WorldBaseBig,new int[6][0],PacPos,3,42,7,7);
		ArrayList<WorldState> childNodes = root.expand_AllDirectionsAndWait();
		root.print();
		for(int i=0;i<childNodes.size();i++) {
			childNodes.get(i).print();
		}
	/*	
		int[] shortestDistance=null;
		
		System.out.println("Kürzester weg zu jedem feld (OHNE berücksichtigung das sich geister in den Weg stellen");
		shortestDistance= Base.BreitensucheOhneGegnerHindernis(PacPos);
		Base.DrawDistanceInfo(map, shortestDistance);
		
		System.out.println("\n\nKürzester weg zu jedem feld (MIT berücksichtigung das sich geister in den Weg stellen");
		shortestDistance= Base.BreitensucheMitGegnerHindernis(PacPos);
		Base.DrawDistanceInfo(map, shortestDistance);
		
		System.out.println("\n\nstarte Benchmark für BreitensucheMitGegnerHindernis Methode (100 sekunden dauer)");
		for(int i=0;i<10;i++) {
			long start=System.nanoTime();
			int counter=0;
			while((System.nanoTime()-start) < 10000000000L) {
				Base.BreitensucheMitGegnerHindernis(PacPos);
				counter++;
			}
			System.out.println((counter/10)+" durchschnittliche Methoden-aufrufe pro sekunde in den letzten 10 sekunden");
		}	

		System.out.println("starte Benchmark für BreitensucheOhneGegnerHindernis Methode (100 sekunden dauer)");
		
		for(int i=0;i<10;i++) {
			long start=System.nanoTime();
			int counter=0;
			while((System.nanoTime()-start) < 10000000000L) {
				Base.BreitensucheOhneGegnerHindernis(PacPos);
				counter++;
			}
			System.out.println((counter/10)+" durchschnittliche Methoden-aufrufe pro sekunde in den letzten 10 sekunden");
		}
*/
		
		
		
		
		

	}
}
