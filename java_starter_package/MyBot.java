import java.util.*;

public class MyBot {
  	static final int MAX_NUM = 150;
  	static final int MAX_TURN = 55;
   
    static int[][] ataca = new int [MAX_NUM][MAX_TURN];
    static int[][] atacado = new int [MAX_NUM][MAX_TURN];
    static int[] demora = new int [MAX_NUM];
    static double[][] distancia = new double[MAX_NUM][2];
    static int max_turns;
    
    
    public static void calculateMaxTurns(PlanetWars pw){
    	max_turns = 0;//mudar de acordo com a situcao
		for(Planet p1 : pw.MyPlanets())
			for(Planet p2 : pw.NotMyPlanets())
				max_turns+=pw.Distance(p1.PlanetID(),p2.PlanetID());
		if(pw.MyPlanets().size()*pw.NotMyPlanets().size()>0)
			max_turns/=(pw.MyPlanets().size()*pw.NotMyPlanets().size());
		max_turns=3*max_turns/2+10;
    }
    public static void calculateFleets(PlanetWars pw){
		for(int i=0;i<pw.NumPlanets();i++)
			demora[i]=MAX_TURN;
			
		for(int i=0;i<pw.NumPlanets();i++)
			for(int j=0;j<MAX_TURN;j++)
				ataca[i][j]=atacado[i][j]=0;
				
		for (Fleet f : pw.MyFleets())
		{
			ataca[f.DestinationPlanet()][f.TurnsRemaining()]+=f.NumShips();
			if(f.TurnsRemaining()<demora[f.DestinationPlanet()])
				demora[f.DestinationPlanet()]=f.TurnsRemaining();
		}
		
		for (Fleet f : pw.EnemyFleets())
		{
			atacado[f.DestinationPlanet()][f.TurnsRemaining()]+=f.NumShips();
			if(f.TurnsRemaining()<demora[f.DestinationPlanet()])
				demora[f.DestinationPlanet()]=f.TurnsRemaining();
		}
    
    }
    public static void calculateDistance(PlanetWars pw){
    
    	for(int i=0;i<pw.NumPlanets();i++)
    		distancia[i][0]=distancia[i][1]=0.0;
    		
    	for(Planet p: pw.Planets())
		{
			for(Planet q:pw.MyPlanets())
				distancia[p.PlanetID()][0]+=pw.Distance(p.PlanetID(),q.PlanetID());
			for(Planet q:pw.EnemyPlanets())
				distancia[p.PlanetID()][1]+= pw.Distance(p.PlanetID(),q.PlanetID());
		
			if(pw.MyPlanets().size()>0)
				distancia[p.PlanetID()][0]/=pw.MyPlanets().size();
			else
				distancia[p.PlanetID()][0]=1;
			
			if(pw.EnemyPlanets().size()>0)
				distancia[p.PlanetID()][1]/=pw.EnemyPlanets().size();
			else
				distancia[p.PlanetID()][1]=9999999;
		}
    
    }
    
    public static int defend(Planet p,PlanetWars pw,int score){
    
    	for(Planet q: pw.MyPlanets())//defesa
		{
			int tera = q.NumShips();
			for(int i=1;i<MAX_TURN;i++)
			{
				tera = tera + q.GrowthRate() - atacado[q.PlanetID()][i] + ataca[q.PlanetID()][i];
				if(tera<0)
				{
					int attack = -tera;
					if(score>attack && pw.Distance(p.PlanetID(),q.PlanetID())<=i)
					{
						ataca[q.PlanetID()][i]+=attack;
						pw.IssueOrder(p,q,attack);
					 	score-=attack;		
					}
					break;
				}				
			}
		}
    	return score;    
    }
    
    
    
    public static double calculateExpectedWin(Planet q,int turns,double enemy_score){
    	double win=q.GrowthRate()*(max_turns-turns)*distancia[q.PlanetID()][1]/(distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);//lucro
		if(q.Owner()>1) {
		 	win+=enemy_score;//inimigo perde
		 	win+=q.GrowthRate()*(max_turns-turns)*distancia[q.PlanetID()][1]/(distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);//inimigo deixa de ganhar	
		 }
		return win-enemy_score;
    }
    
    
    public static int calculateExpectedEnemyScore(Planet q,int turns){
    	//TODO
    	return 0;
    }
    
    
    public static void DoTurn(PlanetWars pw) {
		 
		int max_fleets = 1;//por turno
		
		calculateMaxTurns(pw);
		calculateFleets(pw);//acha frotas
		calculateDistance(pw);//acha distancia carteada para a heuristica
			

		for(Planet p : pw.MyPlanets())
		{
			int tem = p.NumShips();
			int score = tem;
			Planet dest = null;
			
			for(int i=1;i<max_turns;i++)
			{
				tem = tem + p.GrowthRate() - atacado[p.PlanetID()][i] + ataca[p.PlanetID()][i];
			 	if(tem<score)
					score = tem;
			}
			
			if(score <= 0)//faz nada
				continue;	
			score = defend(p,pw,score);//tenta defender
			
			for(int i=0;i<max_fleets;i++)
			{
				int attack = 0;
				double best = -1;
				double [] peso=new double[5];//peso pros que eu perco, tirar do inimigo,lucro, lucro tirado do inimigo,lucro do planeta
				for (Planet q : pw.NotMyPlanets()) 
				{
					int win = 0;
					int eh_inimigo = 0;
					if(q.Owner()>1)//inimigo
					{
						peso[0]=-1.0;
						peso[1]=0.8;
						peso[2]=distancia[q.PlanetID()][1]/(distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);
						peso[3]=0.8*distancia[q.PlanetID()][1]/(distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);
						eh_inimigo = 1;
					}
					else
					{
						peso[0]=-1.0;
						peso[1]=0.0;
						peso[2]=distancia[q.PlanetID()][1]/(distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);
						peso[3]=0.0;
					}
				
					int turns = pw.Distance(p.PlanetID(),q.PlanetID());
			   		if(turns>max_turns)//nao ataca se demorar muito...
			   			continue;	

					int enemy_score = q.NumShips();	
					
					for(int j=1;j<=turns;j++)
					{
						if(eh_inimigo==0)
						{
							int tira = Math.max(atacado[q.PlanetID()][j],ataca[q.PlanetID()][j]);
							enemy_score = enemy_score  - tira;
							if(enemy_score<0)
							{
								if(atacado[q.PlanetID()][j]>ataca[q.PlanetID()][j])
								{
									eh_inimigo = 1;
									enemy_score = -enemy_score;   
								}
								else if(atacado[q.PlanetID()][j]<ataca[q.PlanetID()][j])
								{
									enemy_score = -1;
									break;
								}
								else
									enemy_score = 0;
							}
						}
						else
							enemy_score = enemy_score  + q.GrowthRate() + atacado[q.PlanetID()][j] - ataca[q.PlanetID()][j];
						
					}	
					
					if(enemy_score<0 || enemy_score+1>=score)
						continue;
						
					int tmp = enemy_score+1;
					win+=(peso[0]+peso[1])*enemy_score;
					tem = 1;//meeeu

					for(int j=turns+1;j<=max_turns;j++)
					{
						win += (peso[2]+peso[3])*q.GrowthRate();
						tem = tem + q.GrowthRate() + ataca[q.PlanetID()][j] - atacado[q.PlanetID()][j];
						if(tem<0)break;
					}				
					if(win>best)
					{
						best = win;
						dest = q;
						attack = tmp;
					}				
					
				}

				if(best>0 && attack>0)
				{
					 ataca[dest.PlanetID()][pw.Distance(p.PlanetID(),dest.PlanetID())]+=attack;
					 pw.IssueOrder(p, dest,attack);
					 score-=attack;		
				
	 			}
			}
		}
	
    }

	
	//nao mexer...
    public static void main(String[] args) {
	String line = "";
	String message = "";
	int c;
	try {
	    while ((c = System.in.read()) >= 0) {
		switch (c) {
		case '\n':
		    if (line.equals("go")) {
			PlanetWars pw = new PlanetWars(message);
			
			DoTurn(pw);
		        pw.FinishTurn();
			message = "";
		    } else {
			message += line + "\n";
		    }
		    line = "";
		    break;
		default:
		    line += (char)c;
		    break;
		}
	    }
	} catch (Exception e) {
	    // Owned.
	}
    }
}

