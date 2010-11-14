import java.util.*;

public class MyBot {
    // The DoTurn function is where your code goes. The PlanetWars object
    // contains the state of the game, including information about all planets
    // and fleets that currently exist. Inside this function, you issue orders
    // using the pw.IssueOrder() function. For example, to send 10 ships from
    // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
    //
    // There is already a basic strategy in place here. You can use it as a
    // starting point, or you can throw it out entirely and replace it with
    // your own. Check out the tutorials and articles on the contest website at
    // http://www.ai-contest.com/resources.
    public static void DoTurn(PlanetWars pw) {
	// (1) If we currently have a fleet in flight, just do nothing.
	/*if (pw.MyFleets().size() >= 1) {
	    return;
	}*/
	int max_turns=30;
	for(Planet p : pw.MyPlanets())
	{
		int score = (int)p.NumShips()/2;
		int best = -1;
		Planet dest = new Planet(0,0,0,0,0,0);//java suga :P
		int losing = 0;
		for(Fleet f : pw.EnemyFleets())
		{
			if(f.DestinationPlanet()==p.PlanetID())
				losing+=f.NumShips();
		}
		if(losing>=score/2)
			continue;
		for (Planet q : pw.NotMyPlanets()) 
		{
			boolean has_sent = false;
			for (Fleet f : pw.MyFleets())
			{
				if(f.DestinationPlanet()==q.PlanetID())
				{
					has_sent = true;
					break;
				}
			
			}
			if(has_sent)
				continue;
	  		int enemy_score = (int)q.NumShips();
	   		 if(enemy_score>=score)
	   		 	continue;
	   		 int lost = score-enemy_score ;
	   		 
	   		 int turns = pw.Distance(p.PlanetID(),q.PlanetID());
	   		 if(turns>max_turns)continue;
	   		 
	   		 int win = (max_turns-turns)*q.GrowthRate();
	   		 if(q.Owner()>1)
	   		 	win+=(max_turns-turns)*q.GrowthRate();//inimigo nao ganha
	   		 if(win>best)
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

