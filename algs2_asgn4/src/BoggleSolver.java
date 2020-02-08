/*************************************************************************
 * Name: Steve Dugaro 
 *
 * Compilation:  javac BoggleSolver.java
 * Execution:     java BoggleSolver dictionary-algs4.txt board4x4.txt  
 * Dependencies: algs4.jar BoggleTST.java BoggleBoard.java
 *
 * Write a program to play the word game Boggle
 * The Boggle game involves a board made up of 16 cubic dice, where each
 * die has a letter printed on each of its sides. At the beginning of the
 * game, the 16 dice are shaken and randomly distributed in a 4-by-4 tray
 * with only the top sides of the dice visible. The players compete to
 * accumulate points by building valid words out of the dice according
 * to the following rules:
 * 1 A valid word must be composed by following a sequence of adjacent
 *   dice—two dice are adjacent if they are horizontal, vertical, or 
 *   diagonal neighbors.
 * 2 A valid word can use each die at most once.
 * 3 A valid word must contain at least 3 letters.
 * 4 A valid word must be in the dictionary 
 * 5 Qu special case (counts as 2 letters)
 *
 * Words are scored according to their length
 * 0-2 0
 * 3-4 1
 * 5   2
 * 6   3
 * 7   5
 * 8+  11
 *
 * Write a Boggle Solver that finds ALL valid words for a given boggle
 * board using the given dictionary, with the following immutable data type
 * 
 * Approach:
 * - A natural/fast data structure for storing/searching dictionary is a trie.
 * - need a standard set data type to represent found words TreeSet/HashSet
 *   Note that a trie will replace the key so if it exists so its like a set.
 * - BoggleBoard is provided, familiarize
 * - Need a DFS to enumerate all simple paths in the boggle graph
 *   (all strings composed by following sequences of adjacent dice)
 * - ignore special case Qu for now
 * - implement !critical! backtracking optimization: (aka string search)
 *   when the current path corresponds to a string that is NOT a prefix
 *   of any word in the dictionary, there is no need to expand the path
 *   further. (trie pruning)
 * - Need a data structure for the dictionary that supports the operation 
 *   prefixQuery(P): given a prefix is there any word in the dict that
 *   starts with P? (trie APIs -- dont have this need to extend )
 *   
 * Optimizations:
 * - can dictionary be represented more efficiently? Alphabet is A-Z
 *   * R or R^2 branching makes a lot of sense for an english dictionary
 *   * R each [a b .... y z] starts a trie (should use at least this!)
 *   * R^2 each [aa ab ac ...  zx zy zz] starts a trie 
 *   * R^2 is prob best since boggle deals with 3 letter words or higher
 *   * NOTE: there is a 2 letter dictionary to test R^2 branching.
 *   * branching off a hashmap its easy enough to key R or R^2 given
 *   * the hash value of the first char or sum of 2 chars.
 * - doing a prefixQuery(Pn) is almost identical to Pn-1; 1 letter longer
 * - non recursive implementatin of prefixQuery()
 * - precompute the Boggle Graph (set of cubes adjacent to each cube)
 *   but keep lighter than a heavyweight graph object as speed is key!
 *   
 * Notes
 * - Generics and arrays dont mix:
 *   One could create an object array and cast it such as
 *     boggleGraph = (TST<Integer>[]) new Object[26];
 *   This compiles but throws ClassCastException.
 *   Its more convenient and just as fast to use a hashmap anyway
 *   
 * - There are various implementations of a sets, all of which are simply
 *   symbol tables without the associated value. A SET is implemented with
 *   a balanced binary tree which is a classic/efficient implementation
 *   for symbol tables. The "ultimate" symbol table data structure are
 *   balanced search trees => all operations O(lnN) and ordered. Traversal
 *   is O(N) since each node in the tree must be be visited once. 
 *   - Hash Tables are another approach to symbol tables which can do better.
 *   Linear Probing is best for use with csv lookups/sets/dicts per lecture
 *   - How are we using the set? to just maintain a unique list of words.
 *   Clobbering a key doesn't matter; order doesnt matter; get words fast. 
 *   Return all words O(N). Insert O(1) vs O(lgN)
 *   - So a java.util HashSet is preferred over TreeSet for our needs
 *   - HashSet: O(1) add,remove,contains : not ordered
 *   iteration performance depends on the initial capacity... specify about
 *   twice the size you expect the set to grow
 *   - TreeSet: O(lgN) add remove contains: ordered with ordered ops
 *   ascencing/decending natural sort order specificed in the constructor
 *   - Adding nulls to a TreeSet throws NullPointerException, nulls ok in HashSet
 *   - initial capacity of HashSet?
 *   - 64 words on a 2x2 board, 10305 on a 3x3 board, 12e+6 on a 4x4 board
 *   - this represents all possible paths, and is consistent when all chars 
 *     on the board are unique. This can be less for us when we count the
 *     number of words in our word set as we don't store duplicates. But we
 *     can precalculate this maximal size for our hashset.
 *   - 3^N where N is the number of tiles is an increasingly loose guess
 *   - The reality is that the number of words we can accumilate on the
 *     boggle board is never more than the number of words in the dictionary
 *     since we compare against the dictionary to accept a word. So the size
 *     of our word hashMap can be the min(||dictionary||,3^N)  
 *   
 *   
 * - Exploring the Boggle board is a 2D DFS Maze Exploration problem.
 *   We cant use/visit the same dice twice thats an invalid path
 *   This is the Boggle Graph, but we don't need a heavy graph object to
 *   search it. For each dice we have a posn/char and we need to get at all
 *   the adjacent characters to begin forming a word (at most 8 neighbor dice
 *   depending on whether at a boundary), then recurse for each of those.
 *   At each die we can move in 8 possible directions unless at a boundary.
 *   - Each die forms a set of paths/words, which we can accumilate in a set
 *   as we traverse the Boggle Graph.
 *   - We start with the base case which is the first dice at posn (i,j)=(0,0) 
 *   DepthFirstPaths: (1D)
 *     int p
 *     boolean marked[]
 *     dfs(G,p)
 *     
 *     dfs(G,v) {
 *       marked[v] = true;
 *       for( int n: G.adj(v) ) {
 *         if( !marked[n] ) {
 *           dfs(G,n)
 *         }  
 *       }
 *     }
 *   - need a method to get all traversable positions to (i,j) on the board
 *   - need to pass the current state of marked positions so we dont revist
 *   - need to collect all words built up over path traversal (uniquely)=hashSet
 *   - start with 2x2 custom board and dictionary of words of length 2.
 *     
 * - Prune BoggleGraph by comparing against the trie dictionary, if the
 *   dictionary doesnt have an entry with the prefix, we dont need to 
 *   continue our DFS for word paths. The great thing about tries is that
 *   descending its nodes define part of the key ie prefixes. 
 *   - Trie::longestPrefixOf(query) gives us a key in the trie thats a prefix
 *     of the query word... Not what we want.. we want the opposite. given a 
 *     prefix (ie first few letters) is there the beginnings of a word (key)
 *     in the trie? We just need to know whether we should continue looking
 *     for paths with that prefix or stop. Need to extend Trie class to add
 *     new prefixQuery() method, this amounts to checking if a subtrie exists  
 *   - Trie search hits are fast, search misses are faster yet.
 *            
 * - String vs StringBuilder
 *   - Java Strings are immutable: once created cant change, a copy is made so
 *     so concatenation O(N) while substring search O(1) -- offset into char array
 *   - Java String Builder is EFFICIENT for significant amount of contatenations
 *     as we are doing here (prefix building/reversing a string) O(1) concat
 *   
 * - Dictionary Data Type must be immutable: an object that cannot be changed
 *   programmatically; Once the constructor has completed execution, that
 *   instance can't be altered. Test11 requires a "defensive" deep copy of
 *   dictionary be done, as some of the data in there gets mutated. 
 *  
 */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
//import java.util.Collections;

public class BoggleSolver
{
	/* For tracking positions on a BoggleBoard
	 * static nested class does not maintain a reference to its enclosing class
	 */
	private static class Posn
	{
		public int x;
		public int y;
		Posn(int x, int y)
		{
			this.x=x;
			this.y=y;
		}		
		public String toString() {
			return "(" + this.x + ", " + this.y + ")";
		}
	}
	
	private int N; // number of words in the dictionary
	private final Map<Integer,BoggleTST<Integer>> dictionary;
	
    /* Initialize the dictionary data structure using an array of strings
     * as the dictionary. (You can assume each word in the dictionary contains
     * only the uppercase letters A through Z.)
     *
     * Create a TST rooted at each letter. ord(c)/chr(i) == casting in java
     * - (int)A = 65; (int)Z=90; (char)90 = 'Z' (char) 65='A'
     * - TernarySearchTree stores characters and values in the nodes (not keys)
     * 
     * Initialize tries one for each letter in R=26 branched graph
     * Extends to R^2 branched graph where h=hash(charAt(0),charAt(1)) [26*26]=676 keys
     * AA 65*65(4225) AB 65*66(4290) *collision* BA 66*65(4290) ... ZZ 90*90(8100)
     * We start with R branching a Ternary Search Tree for each letter.
     * 
     * Count the number of words read in by the dictionary as this can be
     * used to initialize the capacity of our words HashSet, so that we
     * dont need to wait for it grow itself.
     * 
     * !NOTE! Our initialization of an R branched Trie Dictionary may not have
     * a Trie for every letter in the alphabet; we create a trie for the words
     * that start with a letter. Some dictionaries dont have words that start
     * with V or X for instance, so HashMap will return null if we ask for them
     * which will lead to a NullPointerException if we attempt to invoke a
     * trie method on this. Even though dictionaries are supposed to be sorted
     * we should check if we already have the key before constructing a new trie.
     * 
     *  HashMap generics dont respect primitive types so we key on Integer.
     */
	
    public BoggleSolver(String[] dictionary)
    {
    	if (dictionary == null) 
    		throw new IllegalArgumentException("No dictionary provided.");
    	
    	this.N = 0;
    	this.dictionary = new HashMap<Integer,BoggleTST<Integer>>();

    	// defensive copy for immutability test
    	String[] dCopy = new String[dictionary.length];
    	System.arraycopy(dictionary, 0, dCopy, 0, dictionary.length );
    	
    	for(String s: dCopy) {
    		int c = ((int) s.charAt(0));
    		if(!this.dictionary.containsKey(c) )
    			this.dictionary.put(c,new BoggleTST<Integer>()); 
    		this.dictionary.get(c).put(s,0);
    		this.N+=1;
    	}
    }

    /* 
     * println() textual representation of this object
     */
    /*
    public String toString() {
        StringBuilder sb = new StringBuilder("[Boggle Dictionary]\n");
        sb.append("----------------------------------------------\n");
		for (Map.Entry<Integer,BoggleTST<Integer>> entry : this.dictionary.entrySet())
		{
			int c = entry.getKey();
			BoggleTST<Integer> st = entry.getValue();
            sb.append(String.format("H[%d] %s [%3s] ",c,(char)c,st.size()));
            for (String s: st.keys()) sb.append(s+" ");
            sb.append("\n");
		}
		sb.append("----------------------------------------------\n");
        return sb.toString().trim();
    }
    */

    /* For a board at position (x,y) return all possible adjacent positions
     * We can return a maximum of 8 possible points around a point p on the
     * board. Dont return point p or any point that lies outside of the board
     * boundry. ie. cornders should only return 3 pts, edges 5, and interior
     * positions 8 
     */    
    private List<Posn> adj(BoggleBoard b, int r, int c)
    {
    	ArrayList<Posn> neighbors = new ArrayList<Posn>();
    	if(b == null) return neighbors;
    	if(r < 0 || r > b.rows() || c < 0 || c > b.cols()) return neighbors;
    	// assert we have a board within the board limits
    	
        for( int i=-1; i<= 1; i++) {
        	for( int j=-1; j<= 1; j++) {
        		int nx = r+i;
    			int ny = c+j;
    			if (nx == r && ny == c) continue; // dont return center pt
    			if (nx >= 0 && nx < b.rows() && ny >= 0 && ny < b.cols())
    				neighbors.add(new Posn(nx,ny));
    		}
    	}
     
        /*
    	StdOut.printf("adj(%d,%d)[%s]\n",r,c,b.getLetter(r, c));
	    for( Posn p: neighbors) {
	      StdOut.printf(" %s %s\n",p,b.getLetter(p.x,p.y));
	    }
	    StdOut.println();
	    */
	
    	return neighbors;
    }
    
    /* DFS on 2D BoggleBoard to construct simple paths from all possible moves.
     * w is the current word. words is the set of words (valid or not) obtained
     * by traversing all possible paths. marked[][] tells us if we have visited
     * the tile aready while exploring a current path. This maintains the local
     * knowledge about tiles that have been visited along the current path, but
     * in order for us to find all the paths we have to unmark as we wind out
     * for the other neighboring points to have their turn in the search. 
     */
    private void dfsPathsFrom(BoggleBoard b, Posn posn, String w, boolean[][] marked, HashSet<String> words)
    {
    	
    	int i = posn.x;
    	int j = posn.y;
    	char c = b.getLetter(i,j); 
    	
    	words.add(w+c);    	
    	marked[i][j] = true;
    	StdOut.printf("Added %s\n",w+c);
    	for( Posn n: adj(b,i,j) ) {
  	        StdOut.printf("%s %s\n",n,marked[n.x][n.y]);
    		if( !marked[n.x][n.y] )
    			dfsPathsFrom(b,n,w+c,marked,words);
    	}
    	marked[i][j] = false; // pick up bread crumb for other paths to visit.
    }
    
    /* Same as above but with boggle specific pruning critera
     * 
     * 1. Dont accumilate words that are < 3 characters long
     * 2. return quickly if the path we are exploring is not in our trie dictionary
     *    (the prefix we have assembled thus far, if a word can be built will exist)
     * 3. Use StringBuilder instead of String as concatenation is O(1). 
     */
    private void dfsBogglePaths(BoggleBoard b, Posn posn, StringBuilder prefix, boolean[][] marked, HashSet<String> words)
    {
    	
    	int  i = posn.x;
    	int  j = posn.y;    	
    	char c = b.getLetter(i,j);
    	   	
    	if( c == 'Q' ) prefix.append("QU");
    	else prefix.append(c); // word candidate ""->A->AT
    	int t = ((int) prefix.charAt(0)); // trie table lookup
    	
    	String word = prefix.toString(); 
    	BoggleTST<Integer> trie = this.dictionary.get(t);
    	//StdOut.printf("Checking prefix %s\n",word);
    	if( trie==null || !trie.prefixQuery(word) )
    	{// prefix is not in the dictionary; stop exploring path
    		//StdOut.printf("PRUNED %s\n",word);
    		return;    		
    	}
    	
    	if( word.length()>2 && trie.contains(word) )
    	{
    		words.add(word);
    		//StdOut.printf("Added [%s]",word);
    		//for (String k: trie.keys()) StdOut.print(k+" ");
    		//StdOut.println();	
    	}
    	    
    	marked[i][j] = true;
    	for( Posn n: adj(b,i,j) ) {
  	        //StdOut.printf("%s %s\n",n,marked[n.x][n.y]);
    		if( !marked[n.x][n.y] )
    			//dfsBogglePaths(b,n,prefix,marked,words);
    			dfsBogglePaths(b,n,new StringBuilder(word),marked,words);
    	}
    	marked[i][j] = false; // pick up bread crumb for other paths to visit.

    }
    
    
    /* Returns the set of all valid words in the given Boggle board, as an Iterable.
     * 
     * From each tile use DFS to explore all possible paths.
     * Its possible a valid word can be formed in multiple ways, ensure each valid
     * word is only represented once. This implies using a set. We dont need them
     * to be ordered and our main criteria is speed so a HashSet is best here.
     * 
     * Exploring all possible words is a DFSPaths/Maze Exploration problem.
     * From each die there are adjacent dice, in at most 8 possible directions.
     * If we can move in a direction we can append a character/create a path
     *     
     */
    public Iterable<String> getAllValidWords(BoggleBoard board)
    {
    	if (board == null) 
    		throw new IllegalArgumentException("No board provided.");

    	HashSet<String> words = new HashSet<String>(N);    	
    	/* There is a set of words (paths) from each die position.
    	 * So we have to run dfs from each position, looking at each
    	 * possible word from that source.  
    	 */

    	boolean marked[][];
    	StringBuilder letter;
    	int rows = board.rows();
    	int cols = board.cols();    	
    	for(int i=0; i < rows; i++) {
    		for(int j=0; j < cols; j++) {
    			marked = new boolean[rows][cols];
    			letter = new StringBuilder(""); 
    			//dfsPathsFrom(board,new Posn(i,j),"",marked,words);
    			dfsBogglePaths(board,new Posn(i,j),letter,marked,words);
    		 }
    	 }
    	
    	/*
    	List sorted = new ArrayList(words);
    	Collections.sort(sorted);
    	return sorted;
    	*/
    	
    	return words;
    }

    /* Returns the score of the given word if it is in the dictionary, zero otherwise.
     * (You can assume the word contains only the uppercase letters A through Z.)
     * 
     * Test prob requires this to double check dict hasnt been modified.
     */
    public int scoreOf(String word)
    {
    	int t = ((int) word.charAt(0)); // trie table lookup
    	BoggleTST<Integer> trie = this.dictionary.get(t);
    	if( trie==null || !trie.contains(word) ) return 0;
   	
    	switch( word.length() ) 
    	{
    	case 0:
    	case 1:
    	case 2:
    		return 0;
    	case 3:
    	case 4:
    		return 1;
    	case 5:
    		return 2;
    	case 6:
    		return 3;
    	case 7:
    		return 5;
    	default:
    		return 11;
    	}    	
    }
    
    /* Test client:  java BoggleSolver dict.txt board.txt
     * Prints out all valid words for the given board using the given dictionary
     *  
     * Test 12: ensure we can read more than one boggle solver at a time
     */
	public static void main(String[] args)
	{

	    //In in = new In(args[0]);	    
	    //In in = new In("data/dictionary-2letters.txt");
	    In in = new In("data/dictionary-algs4.txt");
	    //In in2 = new In("data/dictionary-nursery.txt");
	    //In in = new In("data/dictionary-common.txt");
	    
	    String[] dictionary = in.readAllStrings();
	    BoggleSolver solver = new BoggleSolver(dictionary);
	    //BoggleBoard board = new BoggleBoard(args[1]);
	    //BoggleBoard board = new BoggleBoard("data/board-q.txt");

	    //BoggleBoard board = new BoggleBoard(2,2);
	    //char [][] by2 = {{'N','T'},{'L','I'}};
	    //BoggleBoard board = new BoggleBoard(by2); // char[][]
	    
	    char[][] tb = {{'N','I','W','B'},
	    		       {'L','S','O','D'},
	    		       {'E','E','O','N'},
	    		       {'T','X','M','A'}};
	    BoggleBoard board = new BoggleBoard(tb);
	    
	    //StdOut.println(solver);
	    StdOut.println(board);
	    
	    
	    
	    
	    //solver.adj(board, 5, 5);
	    //solver.adj(board, 1, 1);
	    //solver.adj(board, 3, 3);
	    // List<Posn> pts = solver.adj(board,3,2);
	    //for( Posn p: pts) {
	    //	StdOut.printf("%s %s\n",p,board.getLetter(p.x,p.y));
	    //}
	    
		long startTime = System.nanoTime();
	    // run on 10 random hasbro boards

	    int score = 0;
	    /*
	    for(int i=0; i < 5; i++) {
	      StdOut.printf("\nHasbro board: %d\n",i);
	      StdOut.println(board);
	      for (String word : solver.getAllValidWords(board))
	      {
	        StdOut.println(word);
	        score += solver.scoreOf(word);
	      }
	      StdOut.println("Score = " + score);
	      score=0;
	    }
	    */


	    for( int i = 0; i < 10; i++) {
          StdOut.printf("\nHasbro board: %d\n",i);
          board = new BoggleBoard();
          StdOut.println(board);
          StdOut.println();

  	      score = 0;
  	      for (String word : solver.getAllValidWords(board))
  	      {
  	        StdOut.println(word);
  	        score += solver.scoreOf(word);
  	      }
  	      StdOut.println("Score = " + score);
	    }
		
		
	    StdOut.println("Elapsed Time is " + (System.nanoTime() - startTime)/1000 + " usec");
	}
    
    
}

