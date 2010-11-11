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
    *
    */
    public static void DoTurn(PlanetWars pw) {

	int max_turns=30;//cartas...
	for(Planet p : pw.MyPlanets())
	{
		int score = p.NumShips();
		int best = -1;
		Planet dest = new Planet(0,0,0,0,0,0);//java suga :P
		int losing = 0;
		for(Fleet f : pw.EnemyFleets())
		{
			if(f.DestinationPlanet()==p.PlanetID())
				losing+=f.NumShips();
		}
		if(2*losing>=score)//evita perder planetas
			continue;
		score-=losing;//ataca com o que sobra
		for (Planet q : pw.NotMyPlanets()) 
		{
			boolean has_sent = false;
			int enemy_score=0;
			for (Fleet f : pw.MyFleets())//jah atacaram esse planeta nesse turno, sem isso todo mundo ataca o msm
			{
				if(f.DestinationPlanet()==q.PlanetID())
				{
					has_sent = true;
					break;
				}
			}
			for (Fleet f : pw.EnemyFleets())//jah atacaram esse planeta nesse turno, sem isso todo mundo ataca o msm
			{
				if(f.DestinationPlanet()==q.PlanetID())
				{
					enemy_score+=f.NumShips();
				}
			}
			if(has_sent)
				continue;
	  		enemy_score += (int)q.NumShips();
	  			   			   		 
	   		int turns = pw.Distance(p.PlanetID(),q.PlanetID());
	   		if(turns>max_turns)
	   			continue;
	   		 
	   		if(q.Owner()>1)//planeta do inimigo
	  			enemy_score+=turns*q.GrowthRate();
	  		
	  		if(enemy_score>=score)//nao vale a pena?(toh carteando...)
	   		 	continue;
	   		 	
	   		 int win = (max_turns-turns)*q.GrowthRate() - enemy_score;
	   		 if(q.Owner()>1)
	   		 	win+=(max_turns-turns)*q.GrowthRate();//inimigo nao ganha
	   		 if(win>best)//tenta atacar onde mais ganha naves
	   		 {
				best = win;
				dest = q;
	   		 }
	    	}
	    if(best>0)
	    {
	    	 pw.IssueOrder(p, dest, score);
	    }
	}
	
	/*
	// (2) Find my strongest planet.
	Planet source = null;
	double sourceScore = Double.MIN_VALUE;
	for (Planet p : pw.MyPlanets()) {
	    double score = (double)p.NumShips();
	    if (score > sourceScore) {
		sourceScore = score;
		source = p;
	    }
	}
	// (3) Find the weakest enemy or neutral planet.
	Planet dest = null;
	double destScore = Double.MIN_VALUE;
	for (Planet p : pw.NotMyPlanets()) {
	    double score = 1.0 / (1 + p.NumShips());
	    if (score > destScore) {
		destScore = score;
		dest = p;
	    }
	}
	// (4) Send half the ships from my strongest planet to the weakest
	// planet that I do not own.
	if (source != null && dest != null) {
	    int numShips = source.NumShips() / 2;
	    pw.IssueOrder(source, dest, numShips);
	}*/
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

