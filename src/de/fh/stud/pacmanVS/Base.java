package de.fh.stud.pacmanVS;

import de.fh.pacmanVS.enums.VSPacmanTileType;
import de.fh.util.Vector2;

import static de.fh.pacmanVS.enums.VSPacmanTileType.*;
import java.util.ArrayList;
import java.util.List;

import de.fh.pacmanVS.VSPacmanStartInfo;
import de.fh.pacmanVS.enums.Team;

import static de.fh.pacmanVS.enums.Team.*;
import static de.fh.stud.pacmanVS.constants.*;

public class Base {
	public static boolean worldBaseCreated=false;
	public static int[][] xyToIndex;
	public static int WorldBase[];
	public static int WorldBaseBig[];
	
	
	public static int Vector2ToInt(Vector2 posXY) {
		return xyToIndex[posXY.getX()][posXY.getY()];
	}
	
 	public static void createWorldBase(VSPacmanTileType[][] world, VSPacmanStartInfo startInfo){
 		
 	 	Team team = startInfo.getTeam();
 		xyToIndex=new int[world.length][world[0].length];
		if(worldBaseCreated) {
			System.out.println("createWorldBase sollte nur beim ersten percept aufgerufen werden");
		}else {
			int walkable=0;
			ArrayList<Field> tmp=new ArrayList<Field>();
			for(int y=1;y<world[0].length-1;y++) 
				for(int x=1;x<world.length-1;x++) 
					if(world[x][y]!=WALL){
						xyToIndex[x][y]=walkable;
						walkable++;
						tmp.add(new Field(x,y));
					}
			WorldBase = new int[walkable];
			for(int i=0;i<tmp.size();i++) {
				int x=tmp.get(i).x;
				int y=tmp.get(i).y;
				if(world[x-1][y]!=WALL)
					WorldBase[i]|=B1;
				if(world[x+1][y]!=WALL)
					WorldBase[i]|=B2;
				if(world[x][y-1]!=WALL) 
					for(int i2=i-1,shift=1;true;i2--,shift++)
						if(tmp.get(i2).x==x &&tmp.get(i2).y==y-1){
							WorldBase[i]|=(shift<<2);
							break;
						}
				if(world[x][y+1]!=WALL) 
					for(int i2=i+1,shift=1;true;i2++,shift++)
						if(tmp.get(i2).x==x &&tmp.get(i2).y==y+1){
							WorldBase[i]|=(shift<<7);
							break;
						}
				if(team==BLUE&&(x+1)<=world.length/2 || team==RED&&(x+1)>world.length/2)
					WorldBase[i]|=(1<<12);
			}
			WorldBaseBig = new int[walkable*2];
			for(int i=0;i<walkable;i++) {
				WorldBaseBig[i*2]=WorldBase[i];
				WorldBaseBig[i*2+1]=0;
			}
			//TODO Sven
			List<Vector2> ownSpwans = startInfo.getTeammateSpawns();
			List<Vector2> otherspawns = startInfo.getOpponentSpawn();
			int index = 0;
			for(int i = 0; i<ownSpwans.size();i++)
				WorldState.spawnPosition[index++]=Base.Vector2ToInt(ownSpwans.get(i));
			for(int i = 0; i<otherspawns.size();i++)
				WorldState.spawnPosition[index++]=Base.Vector2ToInt(ownSpwans.get(i));
			
			//TODO Sven Ende
			
			worldBaseCreated=true;
		}
	}
	
	public static int[] BreitensucheOhneGegnerHindernis(int[] pac) {
		int[] oL=new int[WorldBase.length+1];
		int[] w=WorldBaseBig.clone();
		int eI,nI,ds,sh,st,re,wr,cmp,ptmp,exp;
		for(int i=0;i<pac.length;i++) {
			wr=1;
			cmp=(0B111111<<(ds=((i+2)<<3)&0B11000));	//cmp=(E6<<(ds=(i+2)*8%32));   
			if(i>1){
				w[oL[re=0]=ptmp=(pac[i]<<1)+1]|=((i<3)?0B110000000000000:0B100000000000000);
				while(re!=wr){
					if((st=((exp=w[eI=oL[re++]])>>ds)&0B111111)!=0B111111)			st++;
					if(((exp=w[eI-1])&0B1)!=0&&(((w[(nI=eI-2)])&cmp)==0)) 			w[oL[wr++]=nI]|=(st|0B11000000)<<ds;
					if((exp&0B10)!=0&&(((w[(nI=eI+2)])&cmp)==0))					w[oL[wr++]=nI]|=(st|0B10000000)<<ds;
					if((sh=exp&0B1111100)!=0&&(((w[nI=eI-(sh>>1)])&cmp)==0))  		w[oL[wr++]=nI]|=(st|0B1000000)<<ds;
					if((sh=exp&0B111110000000)!=0&&(((w[nI=eI+(sh>>6)])&cmp)==0))	w[oL[wr++]=nI]|=st<<ds;
				}
			}else{
				w[oL[re=0]=ptmp=pac[i]<<1]|=((i<3)?0B110000000000000:0B100000000000000);
				while(re!=wr){
					if((st=((exp=w[eI=oL[re++]])>>ds)&0B111111)!=0B111111) 		st++;
					if((exp&0B1)!=0&&((w[nI=eI-2]&cmp)==0))						w[oL[wr++]=nI]|=(st|0B11000000)<<ds;
					if((exp&0B10)!=0&&((w[nI=eI+2]&cmp)==0))					w[oL[wr++]=nI]|=(st|0B10000000)<<ds;
					if((sh=exp&0B1111100)!=0&&((w[nI=eI-(sh>>1)]&cmp)==0))  	w[oL[wr++]=nI]|=(st|0B1000000)<<ds;
					if((sh=exp&0B111110000000)!=0&&((w[nI=eI+(sh>>6)]&cmp)==0)) w[oL[wr++]=nI]|=st<<ds;
				}
			}
			w[ptmp]=w[ptmp]&~(0B11111111<<ds)|0B1000000<<ds;
		}
		return w;
	}

	public static int[] BreitensucheMitGegnerHindernis(int[] pac) {
		int[] w=WorldBaseBig.clone();
		int[] cOl=new int[384];
		int[] nOl=new int[384];
		int[] w_cOl=new int[12];
		int[] w_nOl=new int[12];
		int[] swap;
		int exp,steps=0,wTmp,ds,r,eI,nI,sh,cmp,area=0B1000000000000,tmp,nTmp,oI,todo=1;
		for(int i=0,t;i<6;i++){
			w_cOl[t=((w[pac[i]]&0B1000000000000)==0)?((i<3)?i+6:i):(i<3)?i:(i+6)]=1;
			cOl[t<<5]=(pac[i]<<1);
		}
		while(todo!=0){
			if(steps!=0B111111)steps++;
			for(int i=0;i<12;i++){
				r=0;
				if(i<6){
					if(i==3) area=0;
					oI=i+6;
					ds=(i+2)<<3&0B11000;
				}else{
					if(i==9) area=0B1000000000000;
					oI=i-6;
					ds=i<<3&0B11000;
				}
				cmp=0B111111<<ds;
				wTmp=w_cOl[i];
				if(i>>1==0||i>>1==0B11)//i%6<1
					while(r!=wTmp){
						if(((exp=w[eI=cOl[tmp=((i<<5)+r++)]])&0B1)!=(cOl[tmp]=0)&&((nTmp=w[(nI=eI-2)])&cmp)==0
						&&((tmp=(((area^nTmp)>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0)) 
							w[(nOl[(tmp<<5)+w_nOl[tmp]++]=nI)]|=(steps|0B11000000)<<ds;// nach links expandieren
						if((exp&0B10)!=0&&((nTmp=w[nI=eI+2])&cmp)==0&&((tmp=(((area^nTmp)>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0))
							w[nOl[(tmp<<5)+w_nOl[tmp]++]=nI]|=(steps|0B10000000)<<ds;// nach rechts expandieren
						if((sh=exp&0B1111100)!=0&&((nTmp=w[(nI=eI-(sh>>=1))])&cmp)==0&&((tmp=(((area^nTmp)>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0)) 
							w[nOl[(tmp<<5)+w_nOl[tmp]++]=nI]|=(steps|0B1000000)<<ds;// nach oben expandieren
						if((sh=exp&0B111110000000)!=0&&((nTmp=w[(nI=eI+(sh>>=6))])&cmp)==0&&(((tmp=(((area^nTmp)>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0))) 
							w[nOl[(tmp<<5)+w_nOl[tmp]++]=nI]|=steps<<ds;// nach unten expandieren
					}
				else
					while(r!=wTmp){
						if(((exp=w[eI=cOl[tmp=((i<<5)+r++)]])&0B1)!=(cOl[tmp]=0)&&(w[(nI=eI-2)+1]&cmp)==0
						&&((tmp=(((area^w[eI-2])>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0))
							w[((nOl[(tmp<<5)+w_nOl[tmp]++]=nI))+1]|=(steps|0B11000000)<<ds;	// nach links expandieren
						if((exp&0B10)!=0 && (w[(nI=eI+2)+1]&cmp)==0&&((tmp=(((area^w[eI+2])>>12)&1)==0?i:oI)<6||tmp<9 
						&& (w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0))
							w[(nOl[(tmp<<5)+w_nOl[tmp]++]=nI)+1]|=(steps|0B10000000)<<ds; // nach rechts expandieren
						if((sh=exp&0B1111100)!=0&&(w[(nI=eI-(sh>>=1))+1]&cmp)==0&&((tmp=(((area^w[eI-sh])>>12)&1)==0?i:oI)<6||tmp<9
						&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0)) 
							w[(nOl[(tmp<<5)+w_nOl[tmp]++]=nI)+1]|=(steps|0B1000000)<<ds; // nach oben expandieren
						if((sh=exp&0B111110000000)!=0 && (w[(nI=eI+(sh>>=6))+1]&cmp)==0
						&& ((tmp=(((area^w[eI+sh])>>12)&1)==0?i:oI)<6||tmp<9&&(w[nI+1]&0B11111111111111111111111100000000)==0||tmp>=9
						&&(w[nI]&0B11111111111111110000000000000000)==0&&(w[nI+1]&0B11111111)==0)) 
							w[(nOl[(tmp<<5)+w_nOl[tmp]++]=nI)+1]|=steps<<ds;	// nach unten expandieren
					}
				w_cOl[i]=0;
			}
			todo=0;
			
			for(int i=0;i<12;i++)	
				if((todo+=w_nOl[i])>0) 
					break;
			
			swap=cOl;	cOl=nOl;	 nOl=swap;	// tausche referenzen cOl and nOl
			swap=w_cOl; w_cOl=w_nOl; w_nOl=swap;// tausche referenzen w_cOl and w_nOl
		}
		
//		for(int i=0;i<6;i++)
//			w[tmp=(pac[i]<<1)+(i<2?0:1)]=(w[tmp]&~(E8<<(ds=(i+2)<<3)))|(1<<(B7<<ds));
		
		w[(pac[0]<<1)]  =(w[(pac[0]<<1)]  &0B11111111000000001111111111111111)|0B00000000010000000000000000000000;
		w[(pac[1]<<1)]  =(w[(pac[1]<<1)]  &0B00000000111111111111111111111111)|0B01000000000000000000000000000000;
		w[(pac[2]<<1)+1]=(w[(pac[2]<<1)+1]&0B11111111111111111111111100000000)|0B00000000000000000000000001000000;
		w[(pac[3]<<1)+1]=(w[(pac[3]<<1)+1]&0B11111111111111110000000011111111)|0B00000000000000000100000000000000;
		w[(pac[4]<<1)+1]=(w[(pac[4]<<1)+1]&0B11111111000000001111111111111111)|0B00000000010000000000000000000000;
		w[(pac[5]<<1)+1]=(w[(pac[5]<<1)+1]&0B00000000111111111111111111111111)|0B01000000000000000000000000000000;
		return w;
	}
	
	public static void DrawDistanceInfo(VSPacmanTileType[][] view,int[] worldAndData) { 
		int Xsize=view.length*6+1;
		int Ysize=view[0].length;
		int left,right;
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<Xsize;i++)
			sb.append('*');
		sb.append('\n');
		for(int y=0;y<Ysize;y++) {
			for(int x=0;x<view.length;x++) 
				if(view[x][y]==WALL) 
					sb.append("*#####");
				else {
					left=worldAndData[xyToIndex[x][y]*2+1]&E6;
					if(left==0 && ((worldAndData[xyToIndex[x][y]*2+1]&E7)!=B7)) {
						left=-1;
					}
					right=(worldAndData[xyToIndex[x][y]*2+1]>>24)&E6;
					if(right==0 && (((worldAndData[xyToIndex[x][y]*2+1]>>24)&E7)!=B7)) {
						right=-1;
					}
					sb.append(drawNumbers(left,right));
				}
			sb.append("*\n");
			for(int x=0;x<view.length;x++) 
				if(view[x][y]==WALL) 
					sb.append("*#####");
				else {
					left=(worldAndData[xyToIndex[x][y]*2]>>24)&E6;
					right=(worldAndData[xyToIndex[x][y]*2+1]>>16)&E6;
					if(left==0 && (((worldAndData[xyToIndex[x][y]*2]>>24)&E7)!=B7)) {
						left=-1;
					}
					if(right==0 && (((worldAndData[xyToIndex[x][y]*2+1]>>16)&E7)!=B7)) {
						right=-1;
					}
					sb.append(drawNumbers(left,right));	
				}
			sb.append("*\n");
			for(int x=0;x<view.length;x++) 
				if(view[x][y]==WALL) 
					sb.append("*#####");
				else {
					left=(worldAndData[xyToIndex[x][y]*2]>>16)&E6;
					right=(worldAndData[xyToIndex[x][y]*2+1]>>8)&E6;
					if(left==0 && (((worldAndData[xyToIndex[x][y]*2]>>16)&E7)!=B7)) {
						left=-1;
					}
					if(right==0 && (((worldAndData[xyToIndex[x][y]*2+1]>>8)&E7)!=B7)) {
						right=-1;
					}
					sb.append(drawNumbers(left,right));
				}
					
			sb.append("*\n");		
			for(int i=0;i<Xsize;i++)
				sb.append('*');
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}	
	private static String drawNumber(int number,int side) {
		String numberString="-";
		if(number>=0) {
			numberString=Integer.toString(number);
		}
		if(side==1&&number<10)
			return " "+numberString;
		if(number<10) 
			return (numberString+" ");
		return numberString;
	}
	private static String drawNumbers(int left,int right) {
		return "*"+drawNumber(left,0)+" "+drawNumber(right,1);
	}
	
	
	
	
	
	
	
	
	
}
