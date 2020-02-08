/*************************************************************************
 * Name: Steve Dugaro 
 *
 * Compilation:  javac BaseballElimination.java
 * Execution:    java BaseballElimination 
 * Dependencies: algs4.jar for StdOut In
 *
 * The baseball elimination problem.
 * In the baseball elimination problem, there is a division consisting of
 * N teams. At some point during the season, team i has w[i] wins, l[i] 
 * losses, r[i] remaining games, and g[i][j] games left to play against 
 * team j. A team is mathematically eliminated if it cannot possibly finish
 * the season in (or tied for) first place. The goal is to determine
 * exactly which teams are mathematically eliminated. For simplicity, we
 * assume that no games end in a tie (ie, Major League Baseball) and that
 * there are no rainouts (every scheduled game is played).
 * 
 * A maxflow formulation.   We solve the baseball elimination problem by
 * reducing it to the maxflow problem. To check whether team x is eliminated, 
 * we consider two cases:
 * 1. Trivial elimination. If the maximum number of games team x can win is
 *    less than the number of wins of some other team i, then team x is
 *    trivially eliminated. That is, if w[x] + r[x] < w[i], then team x is 
 *    mathematically eliminated.
 * 2. Nontrivial elimination. Create a flow network and solve a maxflow
 *    problem in it. In the network, feasible integral flows correspond
 *    to outcomes of the remaining schedule. There are vertices corresponding
 *    to teams (other than team x) and to remaining divisional games (not 
 *    involving team x). Intuitively, each unit of flow in the network 
 *    corresponds to a remaining game. As it flows through the network from 
 *    s to t, it passes from a game vertex, say between teams i and j, then
 *    through one of the team vertices i or j, classifying this game as being
 *    won by that team.
 *    
 * More precisely, the flow network includes the following edges and capacities.
 * - We connect an artificial source vertex s to each game vertex i-j and set
 *   its capacity to g[i][j]. If a flow uses all g[i][j] units of capacity on
 *   this edge, then we interpret this as playing all of these games, with the
 *   wins distributed between the team vertices i and j.
 * - We connect each game vertex i-j with the two opposing team vertices to 
 *   ensure that one of the two teams earns a win. We do not need to restrict
 *   the amount of flow on such edges.
 * - Finally, we connect each team vertex to an artificial sink vertex t. We 
 *   want to know if there is some way of completing all the games so that team
 *   x ends up winning at least as many games as team i. Since team x can win 
 *   as many as w[x] + r[x] games, we prevent team i from winning more than that
 *   many games in total, by including an edge from team vertex i to the sink 
 *   vertex with capacity w[x] + r[x] - w[i].
 * - If all edges in the maxflow that are pointing from s are full, then this 
 *   corresponds to assigning winners to all of the remaining games in such a 
 *   way that no team wins more games than x. If some edges pointing from s 
 *   are not full, then there is no scenario in which team x can win the division.
 *   
 * The flow networks that arise in the baseball elimination problem have special
 * structure, e.g., they are bipartite and the edge capacities are small integers.
 * As a result, the Ford­Fulkerson algorithm performs significantly faster than
 * its worst­case guarantee of V*E^2    
 * 
 * JAVA NOTES:
 * - dont mix arrays with generics.
 */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class BaseballElimination {
	
	private int N;     // num teams
	private int[]   w; // wins
	private int[]   l; // lossess
	private int[]   r; // games remaining
	private int[][] g; // games team i has left to play against team j
	private Iterable<String>[] certificates; // set of teams proving team i is eliminated.
	private Map<String, Integer> teams; // map name to array index
	 
	/* create a baseball division from given filename in format specified
	 *                 w[i] l[i] r[i]        g[i][j]
	 * i  team         wins loss left   Atl Phi NY  Mon
	 * ------------------------------------------------
	 * 0  Atlanta       83   71    8     -   1   6   1
	 * 1  Philadelphia  80   79    3     1   -   0   2
	 * 2  New York      78   78    6     6   0   -   0
	 * 3  Montreal      77   82    3     1   2   0   -
	 * 
	 * % more teams4.txt
	 * 4
	 * Atlanta       83 71  8  0 1 6 1
	 * Philadelphia  80 79  3  1 0 0 2
	 * New_York      78 78  6  6 0 0 0
	 * Montreal      77 82  3  1 2 0 0
	 */
	public BaseballElimination(String filename)
	{
		In fd = new In(filename);
		N = fd.readInt();
		
		w = new int[N];
		l = new int[N];
		r = new int[N];
		g = new int[N][N];
		teams = new HashMap<String,Integer>();
		certificates = new Iterable[N]; // initialized null by default
		
		for( int i=0; i<N; i++) 
		{ 
			teams.put(fd.readString(),i);
			w[i] = fd.readInt();
			l[i] = fd.readInt();
			r[i] = fd.readInt();
			for( int j=0; j<N; j++) {
				g[i][j] = fd.readInt();
			}
			//System.out.println(certificates[i]);  
		}
	}

	 /* number of teams
	  */
	 public int numberOfTeams() 
	 {
		 return teams.size();
	 }

	 /* all teams
	  */
	public Iterable<String> teams()
	{
		return teams.keySet(); // returns a Set<String> implements iterable
	}
 
	/* number of wins for given team
	 * throw a java.lang.IllegalArgumentException if team is invalid.
	 */
	public int wins(String team)
	{
		if( !teams.containsKey(team)) {
			String err = team + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		return w[teams.get(team)];
	}

	/* number of losses for given team
	 * throw a java.lang.IllegalArgumentException if team is invalid.
	 */
	public int losses(String team)
	{
		if( !teams.containsKey(team)) {
			String err = team + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		return l[teams.get(team)];
	}

	/* number of remaining games for given team
	 * throw a java.lang.IllegalArgumentException if team is invalid.
	 */
	public int remaining(String team)
	{
		if( !teams.containsKey(team)) {
			String err = team + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		return r[teams.get(team)];
		
	}

	/* number of remaining games between team1 and team2 
	 * throw a java.lang.IllegalArgumentException if team# is invalid.
	 */
	public int against(String team1, String team2)
	{
		String err;
		if( !teams.containsKey(team1)) {
			err = team1 + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		if( !teams.containsKey(team2)) {
			err = team2 + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		return g[teams.get(team1)][teams.get(team2)];
	}

	/* is given team eliminated?
	 * throw a java.lang.IllegalArgumentException if team is invalid.
	 * 
	 *  If we dont have a certificate of elimination (null) we need to compute one.
	 *  If triviallyEliminated() the certificate will be set and true returned.
	 *  Otherwise, we run maxflow/mincut to nontrivially get a definitive answer.
	 *  
	 */
	public boolean isEliminated(String team)
	{
		if( !teams.containsKey(team)) {
			String err = team + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}

		// calling the certificateOfElimination will cache an iterable 
		// (possibly) empty but not null; There are no size or isEmpty
		// methods so if its possible to iterate we have an elimination
		boolean isEliminated = false;
		for (String s: certificateOfElimination(team))
		{
			isEliminated = true;
			break;
		}
		return isEliminated;
	}
	
	/* return true and set the corresponding certificate of elimination
	 * if we can conclude that a team is trivially eliminated. ie, that
	 * w[i] + r[i] < w[j] for all other teams j. Those teams that satisfy
	 * this condition are included in the set of team that. Note that we
	 * can only confirm if a team has been eliminated, we cant make any
	 * claims about a team not being eliminated
	 */
	private boolean isTriviallyEliminated(String team)
	{
		int iTeam = teams.get(team);
		ArrayList<String> cut = new ArrayList<String>();
		//Bag<String> bag = new Bag<String>();
		int metric = w[iTeam]+r[iTeam];
		// interested in both keys and values in map
		for (Map.Entry<String, Integer> entry : teams.entrySet())
		{
			int i = entry.getValue();
			if( i == iTeam) continue;
			if( metric < w[i]) cut.add(entry.getKey());
		}
		if( !cut.isEmpty() ) {
			certificates[iTeam] = cut;
			return true;
		}
		return false;
	}

	/* Given a team build a baseball elimination flow network.
	 * The argument team represents the source vertex s (v=0), all other
	 * teams are assigned a vertex number after this, and have an edge
	 * pointing to the imaginary sink t (V-1). The following vertices
	 * gij represent games between every other team in the division and
	 * have an edge pointing to it from the source. The bipartite graph
	 * is connected with infinite weighted edges that connect each game
	 * vertex to the two opponent team vertices. Given the matrix of 
	 * games played between division rivals not including our team we
	 * only track above the diagonal hiding the column representing team. 
	 * 
	 * Graph Vertex indexing looks like this
	 *   [s][team v][game vertices][t]
	 * v [0][  N-1 ][ numGameVerts][V-1] 
	 *  
	 */
	private FlowNetwork buildGraph(String team)
	{
		int v = 1;
		int numGameVerts = 0; 
		for(int i=1; i < N-1; i++) numGameVerts += i;
		int V = 2 + numGameVerts + (N-1);
		FlowNetwork G = new FlowNetwork(V);

		// List of all other team names for looking up edge weights
		// Each team has a flow edge to the network sink (t)
		ArrayList<String> otherTeams = new ArrayList<String>();
		for (String s: teams()) {
			if( !s.equals(team) ) {
				otherTeams.add(s);
				int wt = wins(team)+ remaining(team) - wins(s);
				G.addEdge(new FlowEdge(v,V-1,wt));
				v++;
			}
		}		
		
		// Create Flow Edges from source to games remaining vertex
		// and infinite bipartite edges representing the game outcome
		for( int i=0; i < otherTeams.size(); i++) 
		{
			for( int j=i+1; j < otherTeams.size(); j++) 
			{// add edge from source to game vertex
				int gij = against(otherTeams.get(i),otherTeams.get(j));
				G.addEdge(new FlowEdge(0,v,gij));
			 // and and edge from game vertex to each opponent
				G.addEdge(new FlowEdge(v,i+1,Double.POSITIVE_INFINITY));
				G.addEdge(new FlowEdge(v,j+1,Double.POSITIVE_INFINITY));
				v++;
			}
		}
		
		return G;
	}

	
	/* Create a flow network & solve maxflow to get mincut
	 *
	 * Integer flows correspond to outcomes of the remaining schedule.
	 * Each unit of flow corresponds to a remaining game.
	 * Flow from source team vertex (s) to game vertex g(ij) to winning team
	 * vertex w(i) with no flow capacity (infinite). 
	 * 
	 * If all edges in the max flow pointing from s are full, then its possible
	 * that team s could win the division via one of these paths (not eliminated).
	 * In other words if some edges are NOT full, then team s cant possibly win
	 * enough games to win the division which mathematically eliminates it. So the
	 * team vertex with flow that doesnt get maxed out will not be in the maxflow
	 * that is, it will be in the min cut of s. max flow verts = teams s 
	 * could beat, min cut = teams s coultn possibly beat.
	 *  
	 * By the Integrality theorm, there WILL exist an integer max flow. 
	 * A min s-t cut partitions the vertices into 2 disjoint sets (s)in{A}
	 * & (t)in{B} where the sum of the capacities of edges From {A} to {B}
	 * is as small as possible. 
	 * 
	 * NB: we expect the wrapper to these private helpers to have validated team
	 * 1. build the FlowNetwork
	 * 2. run the Ford-Fulkerson Algorithm on this network
	 * 3. request the min cut. FF.inCut(v) returns true if the vertex is on
	 *    the s side of the mincut. we are interested in the team vertices
	 *    that are on the s side of the min cut as these teams have maxed
	 *    out capacities
	 *  
	 */
	private void runNonTrivialElimination(String team)
	{
		FlowNetwork G = buildGraph(team);
		//StdOut.println(G); // initial state
		FordFulkerson FF = new FordFulkerson(G,0,G.V()-1);
		//StdOut.println(G); // max flow
		
		// We only need to know which 'team verts' are in the mincut.
		// We indexed the team verts right after the source vert 0
		// so looping over our hash in the same way we can map back.

		int v=1;
		ArrayList<String> cut = new ArrayList<String>();
		for (String s: teams()) {
			if( !s.equals(team) ) {
				if(FF.inCut(v)) cut.add(s);
				v++;
			}
		}
		//System.out.println("Non-Trivial min cut="+cut.toString());
		
		int i = teams.get(team);
		certificates[i] = cut; // may be empty
	}

	/* subset R of teams that eliminates given team; null if not eliminated
	 * 
	 * Needs to work even if isEliminated() has not been called first.
	 * The sucess of one method should never depend on the client calling
	 * another method. So we haven't obtained a certificate, produce one.
	 * 
	 * To verify the validity of COE(R) compute a(R) = (w(R)+g(R))/|R|
	 * w(R) total number of wins of teams in R
	 * g(R) total number of remaining fames between teams in R
	 * |R|  number of reams in R
	 * check max number of games team can win  < a(R)
	 */
	public Iterable<String> certificateOfElimination(String team)
	{
		if( !teams.containsKey(team)) {
			String err = team + " is not in the division.";
			throw new java.lang.IllegalArgumentException(err);
		}
		
		int i = teams.get(team);
		if( certificates[i] == null )
		{// need to compute elimination.
			if( !isTriviallyEliminated(team) )
			{// init and run maxflow network to be conclusive 
				runNonTrivialElimination(team);
			} 
		}
		return certificates[i];
	}
	
	public static void main(String[] args) {
		BaseballElimination division = new BaseballElimination(args[0]);
		for (String team : division.teams()) {
			if (division.isEliminated(team)) {
				StdOut.print(team + " is eliminated by the subset R = { ");
				for (String t : division.certificateOfElimination(team)) {
					StdOut.print(t + " ");
				}
				StdOut.println("}");
			} else {
				StdOut.println(team + " is not eliminated");
			}
		}
	}

}
