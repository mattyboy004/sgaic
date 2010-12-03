import java.util.List;

public class SPAMBot
{
	static final int MAX_NUM = 150;
	static final int MAX_TURN = 55;

	static int[][] ataca = new int[MAX_NUM][MAX_TURN];
	static int[][] atacado = new int[MAX_NUM][MAX_TURN];
	static int[] demora = new int[MAX_NUM];
	static double[][] distancia = new double[MAX_NUM][3];
	static double lucro[] = new double[3];
	static double total[] = new double[3];
	
	
	static int max_turns;
	static PlanetWars pw;
	
	static final int NEUTRO = 0;
	static final int ME = 1;
	static final int ENEMY = 2; 
	static final int DEFAULT_DISTANCE[] = {0, 1, 99999};
	
	static double treinados[][][] = new double[3][4][4];
	
	
	public static boolean isEnemy(int enemy)
	{
		return enemy != ME && enemy != NEUTRO;
	}

	// Poda! Determina qual o número de turnos o algoritmo vai prucurar
	public static void calculateMaxTurns()
	{
		max_turns = 0;// mudar de acordo com a situcao
		for (Planet p1 : pw.MyPlanets())
			for (Planet p2 : pw.NotMyPlanets())
				max_turns += pw.Distance(p1.PlanetID(), p2.PlanetID());
		if (pw.MyPlanets().size() * pw.NotMyPlanets().size() > 0)
			max_turns /= (pw.MyPlanets().size() * pw.NotMyPlanets().size());
		max_turns = 3 * max_turns / 2 + 10;
	}
	
	// Contabiliza todas as naves de fleets que estão viajando e quem elas vão atacar
	public static void checkTravelingShips(int who[][], List<Fleet> fleets)
	{
		for (Fleet f : fleets)
		{
			who[f.DestinationPlanet()][f.TurnsRemaining()] += f.NumShips();
				if (f.TurnsRemaining() < demora[f.DestinationPlanet()])
					demora[f.DestinationPlanet()] = f.TurnsRemaining();
		}
	}
	
	// Iniciar quem está sendo atacado e quem vai atacar em 0 e depois calcula usando ckeckTravelingShips
	public static void calculateFleets()
	{
		for (int i = 0; i < pw.NumPlanets(); i++)
		{
			demora[i] = MAX_TURN;
			for (int j = 0; j < MAX_TURN; j++)
				ataca[i][j] = atacado[i][j] = 0;
		}
		
		checkTravelingShips(ataca, pw.MyFleets());
		checkTravelingShips(atacado, pw.EnemyFleets());
	}

	// Calcula a média da distância de cada planeta ao planetas amigos e inimigos. Com isso avalia o quão
	// protegido o planeta está e, consequentemente, o qual bizu ele é.
	public static void calculateDistance()
	{
		for (int i = 0; i < pw.NumPlanets(); i++)
			distancia[i][ME] = distancia[i][ENEMY] = 0.0;

		for (Planet p : pw.Planets())
		{
			calculateDistanceOfFrom(p, pw.MyPlanets(), ME);
			calculateDistanceOfFrom(p, pw.EnemyPlanets(), ENEMY);	
		}
	}
	
	// Função auxiliar de calculateDistance. Faz o cáclculo para cada planeta, dado se está avaliando o meu planeta 
	// ou inimigo
	public static void calculateDistanceOfFrom(Planet p, List<Planet> planets, int index)
	{
		for (Planet q : planets)
			distancia[p.PlanetID()][index] += pw.Distance(p.PlanetID(), q.PlanetID());
		if (planets.size() > 0)
			distancia[p.PlanetID()][index] /= planets.size();
		else
			distancia[p.PlanetID()][index] = DEFAULT_DISTANCE[index];
	}

	//TODO melhorar esse método
	//Está meio antintuitivo. Escolhe o planeta a ser defendido por p em ordem numérica. 
	//Idéia de melhoria: Dar p o planeta a ser protegido e procurar um planeta bizu para defendê-lo
	public static int defend(Planet p, PlanetWars pw, int score)
	{
		for (Planet q : pw.MyPlanets())// defesa
		{
			int tera = q.NumShips();
			for (int i = 1; i < MAX_TURN; i++)
			{
				tera = tera + q.GrowthRate() - atacado[q.PlanetID()][i] + ataca[q.PlanetID()][i];
				if (tera < 0)
				{
					int attack = -tera;
					if (score > attack && pw.Distance(p.PlanetID(), q.PlanetID()) <= i)
					{
						ataca[q.PlanetID()][i] += attack;
						pw.IssueOrder(p, q, attack);
						score -= attack;
					}
					break;
				}
			}
		}
		return score;
	}

	// Não usado por enquanto (era usado antes). Calcula "pontos heurísticos" ganhos em se atacar um planeta
	public static double expectedWin(Planet q, int turns, double peso[])
	{
		int tem = 1;
		double win = 0;
		for (int j = turns + 1; j <= max_turns; j++)
		{
			win += (peso[2] + peso[3]) * q.GrowthRate();
			tem += q.GrowthRate() + ataca[q.PlanetID()][j]
					- atacado[q.PlanetID()][j];
			if (tem < 0)
				break;
		}
		return win;
	}

	//Income de cada jogador
	public static void calculateIncome()
	{
		lucro[ME]=lucro[ENEMY]=0.0;
		for(Planet p : pw.MyPlanets())
			lucro[ME]+=p.GrowthRate();
		for(Planet p : pw.EnemyPlanets())
			lucro[ENEMY]+=p.GrowthRate();
		
	}
	
	//total de cada jogador
	public static void calculateTotal()
	{
		total[ME]=total[ENEMY]=0.0;
		
		for(Planet p : pw.MyPlanets()) 
			total[ME]+=p.NumShips();
		for(Fleet f : pw.MyFleets())
			total[ME]+=f.NumShips();
			
		for(Planet p : pw.EnemyPlanets())
			total[ENEMY]+=p.NumShips();
		for(Fleet f : pw.EnemyFleets())
			total[ENEMY]+=f.NumShips();
	}
	
	public static void setUpHeuristicParams()
	{
		calculateMaxTurns(); 
		calculateFleets();
		calculateDistance();
		calculateIncome();
		calculateTotal();
	}

	//quantos de p podem atacar sem perder p
	public static int calculateFleetsToAttack(Planet p)
	{
		int tem = p.NumShips();
		int score = tem;
		for (int i = 1; i < max_turns; i++)
		{
			tem = tem + p.GrowthRate() - atacado[p.PlanetID()][i] + ataca[p.PlanetID()][i];
			if (tem < score)
				score = tem;
		}
		return score;
	}
	
	public static void setPeso(Planet q,double [] peso)
	{
		// peso pras naves que eu perco, peso pra tirar nave do inimigo,lucro estimado por turno, lucro tirado do inimigo
		
		for(int i=0;i<4;i++)
		{
			peso[i]=treinados[q.Owner()][i][0]
					+
					treinados[q.Owner()][i][1]*(distancia[q.PlanetID()][ENEMY]/(distancia[q.PlanetID()][ENEMY]+distancia[q.PlanetID()][ME]))
					+
					treinados[q.Owner()][i][2]*(lucro[ENEMY]/(lucro[ENEMY]+lucro[ME]))
					+
					treinados[q.Owner()][i][3]*(total[ENEMY]/(total[ENEMY]+total[ME]));
					
					
		}	
	}
	
	public static int calculateEndNumberOfShips(Planet q, int turns)
	{
		boolean eh_inimigo = isEnemy(q.Owner());
		int enemy_score = q.NumShips();
		for (int j = 1; j <= turns; j++)
		{
			if (!eh_inimigo)
			{
				int tira = Math.max(atacado[q.PlanetID()][j], ataca[q.PlanetID()][j]);
				enemy_score = enemy_score - tira;
				if (enemy_score < 0)
				{
					if (atacado[q.PlanetID()][j] > ataca[q.PlanetID()][j])
					{
						eh_inimigo = true;
						enemy_score = -enemy_score;
					}
					else if (atacado[q.PlanetID()][j] < ataca[q.PlanetID()][j])
					{
						enemy_score = -1;
						break;
					}
					else
						enemy_score = 0;
				}
			}
			else
				enemy_score = enemy_score + q.GrowthRate() + atacado[q.PlanetID()][j] - ataca[q.PlanetID()][j];

		}
		return enemy_score;
	}
	
	public static int[] findMaxPointsPlanet(Planet p, int score)
	{
		double[] peso = new double[5];
		double best = -1;
		int ans[] = new int[2];
		ans[0] = 0;
		for (Planet q : pw.NotMyPlanets())
		{
			setPeso(q,peso);

			int turns = pw.Distance(p.PlanetID(), q.PlanetID());
			
			if (turns > max_turns)// nao ataca se demorar muito...
				continue;

			int enemy_score = calculateEndNumberOfShips(q, turns);
			if (enemy_score < 0 || enemy_score + 1 >= score)
				continue;
				
			double win = (peso[0] + peso[1]) * enemy_score + expectedWin(q, turns, peso);
			if (win > best)
			{
				best = win;
				ans[1] = q.PlanetID();
				ans[0] = enemy_score + 1;
			}

		}
		return ans;
	}
	
	public static void DoTurn(PlanetWars pw)
	{
		SPAMBot.pw = pw;
		setUpHeuristicParams();
		
		int max_fleets = 1; // máximo de fleets de ataque que cada planeta pode lançar em um turno
		
		for (Planet p : pw.MyPlanets())
		{
			int score = calculateFleetsToAttack(p); 
			if (score <= 0)// faz nada, senao perde o planeta
				continue;
			score = defend(p, pw, score);// tenta defender

			for (int i = 0; i < max_fleets; i++)
			{
				int attack[] = findMaxPointsPlanet(p, score);
				if (attack[0] > 0)
				{
					Planet dest = new Planet(attack[1],0,0,0,0.0,0.0);
					ataca[dest.PlanetID()][pw.Distance(p.PlanetID(), dest.PlanetID())] += attack[0];
					pw.IssueOrder(p, dest, attack[0]);
					score -= attack[0];
				}
			}
		}

	}

	public static void extractPesos()
	{
		/*for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
			{
				treinados[NEUTRO][i][j] = Double.valueOf(args[4*i + j]);
				treinados[ENEMY][i][j] = Double.valueOf(args[16 + 4*i + j]);
			}*/
		//valor treinado
		treinados[NEUTRO][0][0] = -1.079587;
		treinados[NEUTRO][0][1] = 1.130782;
		treinados[NEUTRO][0][2] = -1.531771;
		treinados[NEUTRO][0][3] = 0.674925;
		treinados[NEUTRO][1][0] = -1.509200;
		treinados[NEUTRO][1][1] = 1.689354;
		treinados[NEUTRO][1][2] = -0.553758;
		treinados[NEUTRO][1][3] = -1.552669;
		treinados[NEUTRO][2][0] = -0.753139;
		treinados[NEUTRO][2][1] = 0.955866;
		treinados[NEUTRO][2][2] = 1.441853;
		treinados[NEUTRO][2][3] = -0.457302;
		treinados[NEUTRO][3][0] = 1.199000;
		treinados[NEUTRO][3][1] = 1.181125;
		treinados[NEUTRO][3][2] = -1.643316;
		treinados[NEUTRO][3][3] = 1.283406;
		treinados[ENEMY][0][0] = -0.899427;
		treinados[ENEMY][0][1] = -0.713701;
		treinados[ENEMY][0][2] = -0.334825;
		treinados[ENEMY][0][3] = 1.675558;
		treinados[ENEMY][1][0] = 1.758860;
		treinados[ENEMY][1][1] = 0.621091;
		treinados[ENEMY][1][2] = -0.218183;
		treinados[ENEMY][1][3] = 1.172351;
		treinados[ENEMY][2][0] = 1.365594;
		treinados[ENEMY][2][1] = -0.781913;
		treinados[ENEMY][2][2] = 1.417029;
		treinados[ENEMY][2][3] = -1.859076;
		treinados[ENEMY][3][0] = 1.513254;
		treinados[ENEMY][3][1] = -0.132584;
		treinados[ENEMY][3][2] = -1.857090;
		treinados[ENEMY][3][3] = -1.338340;

	}

	// nao mexer...
	public static void main(String[] args)
	{
		extractPesos();
		
		String line = "";
		String message = "";
		int c;
		try
		{
			while ((c = System.in.read()) >= 0)
			{
				switch (c)
				{
					case '\n' :
						if (line.equals("go"))
						{
							PlanetWars pw = new PlanetWars(message);

							DoTurn(pw);
							pw.FinishTurn();
							message = "";
						}
						else
						{
							message += line + "\n";
						}
						line = "";
						break;
					default :
						line += (char) c;
						break;
				}
			}
		}
		catch (Exception e)
		{
			// Owned.
		}
	}
}
