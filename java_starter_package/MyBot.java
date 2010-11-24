import java.util.*;

public class MyBot {
  	static final int MAX_NUM = 150;
  
    static int[] dono = new int [MAX_NUM];
    static int[] ataca = new int [MAX_NUM];
    static int[] atacado = new int [MAX_NUM];
    static int[] demora = new int [MAX_NUM];
    static double[][] distancia = new double[MAX_NUM][2];
    static int max_turns;
    
    public static void calculateFleets(PlanetWars pw){
		for(int i=0;i<pw.NumPlanets();i++)
			ataca[i]=atacado[i]=demora[i]=0;
		
		for (Fleet f : pw.MyFleets())
		{
			ataca[f.DestinationPlanet()]+=f.NumShips();
			if(f.TurnsRemaining()>demora[f.DestinationPlanet()])
				demora[f.DestinationPlanet()]=f.TurnsRemaining();
		}
		for(int i=0;i<pw.NumPlanets();i++)
			if(demora[i]==0)demora[i]=9990;			
		for (Fleet f : pw.EnemyFleets())
		{
			atacado[f.DestinationPlanet()]+=f.NumShips();
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
			int tera = q.NumShips()+demora[q.PlanetID()]*q.GrowthRate();
			if(atacado[q.PlanetID()]>tera && pw.Distance(p.PlanetID(),q.PlanetID())<demora[q.PlanetID()] && score>(atacado[q.PlanetID()] - tera))
			{
				int attack = atacado[q.PlanetID()] - tera;  
				pw.IssueOrder(p, q,attack);
				score-=attack;		
				atacado[q.PlanetID()] -= attack;
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
    	int score =  atacado[q.PlanetID()] + q.NumShips();
    	if(q.Owner()>1)//planeta do inimigo
			  score+=turns*q.GrowthRate();
 		return score;
    }
    
    
    public static void DoTurn(PlanetWars pw) {
		 
		int max_fleets = 1;//por turno
		max_turns = 0;//mudar de acordo com a situcao
		for(Planet p1 : pw.MyPlanets())
			for(Planet p2 : pw.NotMyPlanets())
				max_turns+=pw.Distance(p1.PlanetID(),p2.PlanetID());
		if(pw.MyPlanets().size()*pw.NotMyPlanets().size()>0)
			max_turns/=(pw.MyPlanets().size()*pw.NotMyPlanets().size());
		max_turns=3*max_turns/2+10;

		calculateFleets(pw);//acha frotas
		calculateDistance(pw);//acha distancia carteada para a heuristica
	

		for(Planet p : pw.MyPlanets())
		{
			int score = p.NumShips();
			Planet dest = null;
			Set<Integer> foi = new HashSet<Integer>();
		
			if(atacado[p.PlanetID()]>=score)//evita perder planetas
				continue;
			score-= atacado[p.PlanetID()];//para nao perder o planeta
			score = defend(p,pw,score);//tenta defender
			
			for(int i=0;i<max_fleets;i++)
			{
				int attack = 0;
				double best = -1;
				for (Planet q : pw.NotMyPlanets()) 
				{
					if(foi.contains(q.PlanetID()))//nao ataca duas vezes o msm planeta no msm turno
						continue;
						
					int turns = pw.Distance(p.PlanetID(),q.PlanetID());
			   		if(turns>max_turns)//nao ataca se demorar muito...
			   			continue;	
						
					int has_sent = ataca[q.PlanetID()];
					int enemy_score = calculateExpectedEnemyScore(q,turns);
	
					if(has_sent>0)
					{
						if(turns<demora[q.PlanetID()])
							calculateExpectedEnemyScore(q,demora[q.PlanetID()]);
			   			if(enemy_score>has_sent && 2*(enemy_score-has_sent+1)<score)
			   			{
							best = 1;
							attack=2*(1+enemy_score-has_sent);
							dest = q;
							foi.add(dest.PlanetID());	
							i--;
						   break;		
						} 	
						continue;
							
					}
	
			   		if(enemy_score>=score+has_sent)continue;//nao ataca se for perder 	
			   		
			   		if(calculateExpectedWin(q,turns,enemy_score)>best)//tenta atacar onde mais ganha naves, sem atacar mais que o necessario
			   		{
						best = calculateExpectedWin(q,turns,enemy_score);
						dest = q;
						attack = 1+enemy_score - has_sent;
			   		}
				}
				if(best>0 && attack>0)
				{
					 pw.IssueOrder(p, dest,attack);
					 score-=attack;		
					 foi.add(dest.PlanetID());	
					
	 			}
			}
		}
		setDono(pw);
    }
    public static void setDono(PlanetWars pw)
	{
		for(Planet p : pw.Planets())
		{
			dono[p.PlanetID()] = p.Owner();
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

