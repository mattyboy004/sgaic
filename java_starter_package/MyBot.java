import java.util.*;

public class MyBot {
    /**
    *terceiro Bot, melhoria m cima do segundo
    *A ideia do bot eh soh mandar naves para planetas que vai consquistar imediatemante e ter "lucro" com isso,
    *ou seja, apos consquistar vai ganhar bastante naves nesse planeta( ou evitar o oponente de ganhar)
    *
    *mudanca 0: atcar com valores diferentes de metadinha do atual OK!
    *mudanca 1: versao anterior nao levava em conta que o planeta do inimigo cresce com no intervalo de vc mandar as naves e elas chegarem OK!
    *mudanca 2: levar em conta as naves que jah tao indo para aquela direcao OK!
    *resultado pos 1100 para 800 \o/
    *mudanca 3: ataca soh com o que precisa
    *mudanca 4: outras bases podem auxiliar
    *mudanca 5: leva um pouco menos em conta que vai conquistar de fato o planeta do inimigo
    ******
    * mudanca 6: versao anterior fica atacando soh o msm... arrumando isso possbilitando cada planeta atacar mais de um alvo
    *mudanca 7: calculo dinamico de max_t	
    * mudanca8: defesa
    */
    static int[] dono = new int [150];
    public static void DoTurn(PlanetWars pw) {
	 
	
	//ArrayList<Integer> perde = new ArrayList<Integer> (pw.NumPlanets())
	int max_fleets = 1;//por turno
	int max_turns = 0;//mudar de acordo com a situcao
	for(Planet p1 : pw.MyPlanets())
		for(Planet p2 : pw.NotMyPlanets())
			max_turns+=pw.Distance(p1.PlanetID(),p2.PlanetID());
	if(pw.MyPlanets().size()*pw.NotMyPlanets().size()>0)
		max_turns/=pw.MyPlanets().size()*pw.NotMyPlanets().size();
	max_turns=3*max_turns/2+10;

	
	int[] ataca = new int [pw.NumPlanets()];
	int[] atacado = new int [pw.NumPlanets()];
	int[] demora = new int [pw.NumPlanets()];
	double[][] distancia = new double[pw.NumPlanets()][2];
	
	for(int i=0;i<pw.NumPlanets();i++)
	{
		ataca[i]=atacado[i]=demora[i]=0;
		distancia[i][0]=distancia[i][1]=0;
	}
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
	for(Planet p : pw.MyPlanets())
	{
		int score = p.NumShips();
		
		Planet dest = null;//java suga :P
		int losing = atacado[p.PlanetID()];
		
		if(losing>=score)//evita perder planetas
			continue;
		score-=losing;//ataca com o que sobra, meio burro...
		
		Set<Integer> foi = new HashSet<Integer>();//java sugao
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
		
		
		
		for(int i=0;i<max_fleets;i++)
		{
			int attack = 0;
			double best = -1;
			for (Planet q : pw.NotMyPlanets()) 
			{
				if(foi.contains(q.PlanetID()))
					continue;
				int has_sent = ataca[q.PlanetID()];
				int enemy_score = atacado[q.PlanetID()];
			
					  		
				enemy_score += (int)q.NumShips();
		  			   			   		 
		   		int turns = pw.Distance(p.PlanetID(),q.PlanetID());
		   		if(turns>max_turns)
		   			continue;
				if(has_sent>0)
				{
					
					int dif = turns - demora[q.PlanetID()];
					if(q.Owner()>1 && dono[q.PlanetID()]<2)
						enemy_score+=demora[q.PlanetID()]*q.GrowthRate();
					if(q.Owner()>1)
						enemy_score+=(dif)*q.GrowthRate();
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
				
		   		if(q.Owner()>1)//planeta do inimigo
		  			enemy_score+=turns*q.GrowthRate();
		  		
		   		 	
		   		 double win =  - enemy_score;
				 win+=q.GrowthRate()*(max_turns-turns)*distancia[q.PlanetID()][1]/(2*distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);
				 
				 
		   		 if(q.Owner()>1)
		   		 {
		   		 	win+=enemy_score;//inimigo perde
				 	win+=q.GrowthRate()*(max_turns-turns)*distancia[q.PlanetID()][1]/(2*distancia[q.PlanetID()][0]+distancia[q.PlanetID()][1]);//deixa de ganhar			 
				 }
				 if(score<=enemy_score-has_sent)
					continue; 
		   		 if(win>best)//tenta atacar onde mais ganha naves, sem atacar mais que o necessario
		   		 {
					best = win;
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

