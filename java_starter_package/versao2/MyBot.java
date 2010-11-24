import java.util.*;

public class MyBot {
    /**
    *Segundo Bot, melhoria m cima do primeiro
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
    *
    */
    static public PlanetWars pw;
    public static void DoTurn(PlanetWars pw) {
	 
	int max_turns=30;//cartas...
	//ArrayList<Integer> perde = new ArrayList<Integer> (pw.NumPlanets())//porra de java suga do caralho porra de array de "integer" vtnc .get, .set pqp sugacao grautuita,foda-se , vou declrar um array grandao que nao suga tanto..
	//int [] perde= new int[pw.NumPlanets()];//tudo isso soh pra declarar um array...


	
	for(Planet p : pw.MyPlanets())
	{
		int score = p.NumShips();
		int best = -1;
		Planet dest = null;//java suga :P
		int losing = 0;
		for(Fleet f : pw.EnemyFleets())
		{
			if(f.DestinationPlanet()==p.PlanetID())
				losing+=f.NumShips();
		}
		if(2*losing>=score)//evita perder planetas
			continue;
		score-=losing;//ataca com o que sobra, meio burro...
		int attack = 0;
		for (Planet q : pw.NotMyPlanets()) 
		{
			int has_sent = 0;
			int enemy_score=0;
			for (Fleet f : pw.MyFleets())
			{
				if(f.DestinationPlanet()==q.PlanetID())
				{
					has_sent +=f.NumShips();
					
				}
			}
			for (Fleet f : pw.EnemyFleets())
			{
				if(f.DestinationPlanet()==q.PlanetID())
				{
					enemy_score+=f.NumShips();
				}
			}
				  		
			enemy_score += (int)q.NumShips();
	  			   			   		 
	   		int turns = pw.Distance(p.PlanetID(),q.PlanetID());
	   		if(turns>max_turns)
	   			continue;
	   		 
	   		if(q.Owner()>1)//planeta do inimigo
	  			enemy_score+=turns*q.GrowthRate();
	  		
	  		if(enemy_score-has_sent>=score)//nao vale a pena?(toh carteando...)
	   		 	continue;
	   		 	
	   		 int win = (max_turns-turns)*q.GrowthRate() - enemy_score;
	   		 if(q.Owner()>1)
	   		 	win+=enemy_score;//inimigo perde
			  
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
	    }
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
			 pw = new PlanetWars(message);
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

