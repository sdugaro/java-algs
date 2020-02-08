
/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac SAP.java
 * Execution:    java SAP circle10.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *               
 * Problem: Shortest ancestral path. 
 * An ancestral path between two vertices v and w in a digraph is a directed 
 * path from v to a common ancestor x, together with a directed path from w 
 * to the same ancestor x. A shortest ancestral path is an ancestral path of 
 * minimum total length. For example, in the digraph at left (digraph1.txt), 
 * the shortest ancestral path between 3 and 11 has length 4 (with common 
 * ancestor 1). In the digraph at right (digraph2.txt), one ancestral path 
 * between 1 and 5 has length 4 (with common ancestor 5), but the shortest 
 * ancestral path has length 2 (with common ancestor 0).
 * 
 * Corner cases: All methods should throw a java.lang.NullPointerException 
 * if any argument is null. All methods should throw a 
 * java.lang.IndexOutOfBoundsException if any argument vertex is invalid 
 * (not between 0 and G.V() - 1) . 
 * You can rely on a java.lang.NullPointerException being throw if you 
 * attempt to invoke a method on a null reference and a 
 * java.lang.IllegalArgumentException being thrown if you call a method 
 * in Digraph.java with a vertex outside the prescribed range. A good API 
 * documents the requisite behavior for all possible arguments, but you 
 * should not need much (if any) extra code to deal with these corner cases. 
 * 
 * Performance requirements: All methods (and the constructor) should take 
 * time at most proportional to E + V in the worst case, where E and V are 
 * the number of edges and vertices in the digraph, respectively. Your data 
 * type should use space proportional to E + V. 
 * 
 * Test client: The test client takes the name of a digraph input file as 
 * as a command-line argument, constructs the digraph, reads in vertex pairs 
 * from standard input, and prints out the length of the shortest ancestral 
 * path between the two vertices and a common ancestor that participates in 
 * that path: Here is a sample execution:
 * 
 * Make the data type SAP immutable: save the associated digraph in an 
 * instance variable. However, because our Digraph data type is mutable, 
 * you must first make a defensive copy by calling the copy constructor. 
 * this also ensures you NEVER HAVE 2 REFERENCES TO THE SAME OBJECT
 * Note how the constructor Point(Point p) takes a Point and makes a copy of it
 * that's a copy constructor. This is a defensive copy because the original Point
 * is protected from change by taking a copy of it.

 <code>
 class Point {
  final int x;
  final int y;

  Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  Point(Point p) {
    this(p.x, p.y);
  }
 }

  // digraph copy constructor:
  // Initializes a new digraph that is a deep copy of original Digraph G 
  public Digraph(Digraph G) {
    this(G.V());
    this.E = G.E();
    for (int v = 0; v < G.V(); v++) {
      // reverse so that adjacency list is in same order as original
      Stack<Integer> reverse = new Stack<Integer>();
      for (int w : G.adj[v]) {
        reverse.push(w);
      }
      for (int w : reverse) {
        adj[v].add(w);
      }
    }
  }
 
 </code>
 * 
 * A Vertext IS considered an ancestor of itself

    % more digraph1.txt             % java SAP digraph1.txt
    13                              3 11
    11                              length = 4, ancestor = 1
     7  3                            
     8  3                           9 12
     3  1                           length = 3, ancestor = 5
     4  1
     5  1                           7 2
     9  5                           length = 4, ancestor = 0
    10  5
    11 10                           1 6
    12 10                           length = -1, ancestor = -1
     1  0
     2  0
 *
 * 'Shortest Path' impiles breadth first search.
 * We are dealing with directed paths so familiarize with ALGS Data Structure
 * BreadFirstDirectedPaths:
 <code> 
    private static final int INFINITY = Integer.MAX_VALUE;
    private boolean[] marked;  // marked[v] = is there an s->v path?
    private int[] edgeTo;      // edgeTo[v] = last edge on shortest s->v path
    private int[] distTo;      // distTo[v] = length of shortest s->v path
    
    // is there a directed path from source to v
    public boolean hasPathTo(int v) {
        return marked[v];
    }
    // number of edges from source to v
    public int distTo(int v) {
        return distTo[v];
    }
    // sequence of vertices on a shorted path
    public Iterable<Integer> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        Stack<Integer> path = new Stack<Integer>();
        int x;
        for (x = v; distTo[x] != 0; x = edgeTo[x])
            path.push(x);
        path.push(x);
        return path;
    }
    public Iterable<Integer> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        Stack<Integer> path = new Stack<Integer>();
        int x;
        for (x = v; distTo[x] != 0; x = edgeTo[x])
            path.push(x);
        path.push(x);
        return path;
    }
 */

public class SAP {

    /* make class immutable by copying Digraph passed to constructor
     * once our contructor completes data can no longer be modified
     */
    private final Digraph DG;
    
    /* constructor takes a digraph (not necessarily a DAG)
     * 
     */
    public SAP(Digraph G) {
        DG = new Digraph(G); // deep copy G to not hold reference to it. 
    }
    
    /* throw an IndexOutOfBoundsException unless 0 <= v < V
     * From Digraph, but not public
     * Digraph should just do this
     */
    private void validateVertex(int v) {
        int V = DG.V();
        if (v < 0 || v >= V)
            throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (V-1));
    }
    
    /* length of shortest ancestral path between v and w; -1 if no such path
     * We need only populate an Iterable with a single v and w and pass this
     * to the multiple sources code
     * 
     */
    public int length(int v, int w) {
        Queue<Integer> vQ = new Queue<Integer>();
        Queue<Integer> wQ = new Queue<Integer>();
        vQ.enqueue(v);
        wQ.enqueue(w);
        return length(vQ,wQ);
    }

    /* a common ancestor of v and w that participates in a shortest ancestral path;
     * -1 if no such path
     * 
     */
    public int ancestor(int v, int w) {
        Queue<Integer> vQ = new Queue<Integer>();
        Queue<Integer> wQ = new Queue<Integer>();
        vQ.enqueue(v);
        wQ.enqueue(w);
        return ancestor(vQ,wQ);
    }

    /* length of shortest ancestral path between any vertex in v and any 
     * vertex in w; -1 if no such path
     * 
     * BFS from multiple sources
     * BFSDirectedPaths(Digraph G, Iterable<Integer> sources)
     * bfs(Digraph G, Iterable<Integer> sources). 
     * Each vertex in v is queued prior to recursive bfs
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        int minLength = Integer.MAX_VALUE;
        
        // validate arguments wrt to digraph
        if ( v == null) throw new NullPointerException("Argument v is null");
        if ( w == null) throw new NullPointerException("Argument w is null");
        //for ( int x: v ) validateVertex(x);
        //for ( int x: w ) validateVertex(x);

        BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(DG,v);
        BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(DG,w);
        // now check if the length of the path from v,w to each other vertex
        // caching the minimum length found so far
        for( int i=0; i<DG.V(); i++) {
            if (vBFS.hasPathTo(i) && wBFS.hasPathTo(i)) { // sum and compare
                int vil = vBFS.distTo(i);
                int wil = wBFS.distTo(i);
                minLength = Math.min(minLength,(vil+wil));
            }            
        } 
        if ( minLength < Integer.MAX_VALUE) return minLength;
        else return -1;
    }

    /* a common ancestor that participates in shortest ancestral path; 
     * -1 if no such path
     * 
     * On construction BFSDirectedPaths runs bfs on G to populate
     * marked[],edgeTo[], and distTo[] arrays from a set of sources.
     * :: hasPathTo(int x) : is there a directed path from v|w to vertex x
     * if there is a path between v and x, w and x, then x is an ancestor
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        int minLength = Integer.MAX_VALUE;
        int minAncestor = Integer.MAX_VALUE;
        
        // validate arguments wrt to digraph
        if ( v == null) throw new NullPointerException("Argument v is null");
        if ( w == null) throw new NullPointerException("Argument w is null");
        //for ( int x: v ) validateVertex(x);
        //for ( int x: w ) validateVertex(x);
        
        BreadthFirstDirectedPaths vBFS = new BreadthFirstDirectedPaths(DG,v);
        BreadthFirstDirectedPaths wBFS = new BreadthFirstDirectedPaths(DG,w); 
        // now check if there is a path from v,w to each other vertex
        for (int i=0; i< DG.V(); i++) {
            if (vBFS.hasPathTo(i) && wBFS.hasPathTo(i)) {
                int length = vBFS.distTo(i) + wBFS.distTo(i);
                if ( length < minLength ) {
                    minLength = length;
                    minAncestor = i;
                }
            }
        }
        if ( minAncestor < Integer.MAX_VALUE ) return minAncestor;
        else return -1;
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        //StdOut.println(G.toString());
        SAP sap = new SAP(G);
        //StdOut.println(sap.length(null,null));
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
    
}
