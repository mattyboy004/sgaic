import java.util.*;
import java.io.*;
public class MyBot {
    /**
    *quarto Bot, melhoria m cima do segundo
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
    *mudanca 8: defesa	
    */
    static int[] dono = new int [150];

    static	FileWriter fstream ;
    static     BufferedWriter out;
    public static void DoTurn(PlanetWars pw) {
	 
	
	//ArrayList<Integer> perde = new ArrayList<Integer> (pw.NumPlanets())//porra de java suga do caralho porra de array de "integer" vtnc .get, .set pqp sugacao grautuita,foda-se , vou declrar um array grandao que nao suga tanto..
	//int [] perde= new int[pw.NumPlanets()];//tudo isso soh pra declarar um array...
	int max_fleets = 3;//por turno
	int max_turns = 0;//mudar de acordo com a situcao
	for(Planet p1 : pw.MyPlanets())
		for(Planet p2 : pw.NotMyPlanets())
			max_turns+=pw.Distance(p1.PlanetID(),p2.PlanetID());
	if(pw.MyPlanets().size()*pw.NotMyPlanets().size()>0)
		max_turns/=pw.MyPlanets().size()*pw.NotMyPlanets().size();
	max_turns=30;

	
	int[] ataca = new int [pw.NumPlanets()];
	int[] atacado = new int [pw.NumPlanets()];
	int[] demora = new int [pw.NumPlanets()];
	double[] dist = new double [pw.NumPlanets()];
	for(int i=0;i<pw.NumPlanets();i++)
		dist[i]=ataca[i]=atacado[i]=demora[i]=0;

	for(Planet p : pw.Planets())
		for(Planet q : pw.EnemyPlanets())
			if(dist[p.PlanetID()]<pw.Distance(p.PlanetID(),q.PlanetID()))
				dist[p.PlanetID()]=pw.Distance(p.PlanetID(),q.PlanetID());
	
	for (Fleet f : pw.MyFleets())
	{

		ataca[f.DestinationPlanet()]+=f.NumShips();
		if(pw.GetPlanet(f.DestinationPlanet()).Owner()!=1 && f.TurnsRemaining()>demora[f.DestinationPlanet()])
		{
			demora[f.DestinationPlanet()]=f.TurnsRemaining();
		}
	}
	for(int i=0;i<pw.NumPlanets();i++)
		if(demora[i]==0)demora[i]=999;
		
	for (Fleet f : pw.EnemyFleets())
	{
		atacado[f.DestinationPlanet()]+=f.NumShips();
		if(pw.GetPlanet(f.DestinationPlanet()).Owner()==1 && f.TurnsRemaining()<demora[f.DestinationPlanet()])
		{
			demora[f.DestinationPlanet()]=f.TurnsRemaining();
		}
		
	}
	for(Planet p : pw.MyPlanets())
	{
		int score = p.NumShips();
		
		Planet dest = null;//java suga :P
		int losing = atacado[p.PlanetID()];
		
		if(losing>=score)//evita perder planetas
			continue;
		score-=losing;//ataca com o que sobra, meio burro...
		for(Planet q :pw.MyPlanets())
		{
			if(q.NumShips()<atacado[q.PlanetID()])
			{
				int turns = pw.Distance(p.PlanetID(),q.PlanetID());
				if(turns<=demora[q.PlanetID()] && score>1+atacado[q.PlanetID()] - q.NumShips())
				{
					pw.IssueOrder(p, q, 1-q.NumShips()+atacado[q.PlanetID()]);
					score-=1+atacado[q.PlanetID()]-q.NumShips();
					atacado[q.PlanetID()]-=1+atacado[q.PlanetID()]-q.NumShips();
					
				}	
			}
		}
		Set<Integer> foi = new HashSet<Integer>();//java sugao
		dono[p.PlanetID()]=p.Owner();
		for(int i=0;i<max_fleets;i++)
		{
			int attack = 0;
			double best = -1;
			for (Planet q : pw.NotMyPlanets()) 
			{
				if(foi.contains(q.PlanetID()))
					continue;
				int has_sent = ataca[q.PlanetID()];
				int enemy_score=atacado[q.PlanetID()];
			
					  		
				enemy_score += (int)q.NumShips();
		  			   			   		 
		   		int turns = pw.Distance(p.PlanetID(),q.PlanetID());
		   		if(turns>max_turns)
		   			continue;
				/*if(has_sent>0 )
				{
					
					int dif = turns - demora[q.PlanetID()];
					if(dif<0)dif=0;
					if(q.Owner()>1 && dono[q.PlanetID()]<2)
					{		
						
						enemy_score+=demora[q.PlanetID()]*q.GrowthRate();
					}					
					if(q.Owner()>1)
						enemy_score+=(dif)*q.GrowthRate();
				
		   			if(enemy_score>has_sent && enemy_score-has_sent+1<score)
		   			{
						best = 1;
						attack=(1+enemy_score-has_sent);
						dest = q;
						 //pw.IssueOrder(p, dest,attack);	
						break;
					} 	
					continue;
							
				}*/
				dono[q.PlanetID()]=q.Owner();
		   		if(q.Owner()>1)//planeta do inimigo
		  			enemy_score+=turns*q.GrowthRate();
		  		
		  		
		   		  	
		   		 double win = - enemy_score;
				 win+=(max_turns-turns)*q.GrowthRate();
				

		   		 if(q.Owner()>1)
		   		 	win+=enemy_score;//inimigo perde

				 if(score<1+enemy_score-has_sent)
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
				 ataca[dest.PlanetID()]+=attack;	
 			}
		}
	}
	//setDono(pw);
    }
    public static void setDono(PlanetWars pw)
	{
		for(Planet p : pw.Planets())
		{
			dono[p.PlanetID()] = p.Owner();
		}
	}
    public static void main(String[] args) {
	try{	
	 fstream = new FileWriter("out.txt");
        out = new BufferedWriter(fstream);
	}catch(Exception e)
	{
		//tomanocu
	}
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
		out.close();
	} catch (Exception e) {
	    // Owned.
	}
    }
}

