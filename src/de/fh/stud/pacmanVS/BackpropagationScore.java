package de.fh.stud.pacmanVS;

public class BackpropagationScore {
	double gameScore;
	double[] PacScore=new double[6];
	
	
	public void importFromVolatile(BackpropagationScoreVolatile vScore) {
		gameScore=vScore.gameScore;
		PacScore[0]=vScore.PacScore1;
		PacScore[1]=vScore.PacScore2;
		PacScore[2]=vScore.PacScore3;
		PacScore[3]=vScore.PacScore4;
		PacScore[4]=vScore.PacScore5;
		PacScore[5]=vScore.PacScore6;
	}
	
	public BackpropagationScoreVolatile exportToVolatile() {
		BackpropagationScoreVolatile vScore=new BackpropagationScoreVolatile();
		vScore.gameScore=gameScore;
		vScore.PacScore1=PacScore[0];
		vScore.PacScore2=PacScore[1];
		vScore.PacScore3=PacScore[2];
		vScore.PacScore4=PacScore[3];
		vScore.PacScore5=PacScore[4];
		return vScore;
	}
}

