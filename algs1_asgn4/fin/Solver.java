/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac Solver.java
 * Execution:    java Solver puzzle04.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *
 * Problem: The 8-puzzle problem is a puzzle invented and popularized by
 * Noyes Palmer Chapman in the 1870s. It is played on a 3-by-3 grid with
 * 8 square blocks labeled 1 through 8 and a blank square. Your goal is
 * to rearrange the blocks so that they are in order, using as few moves
 * as possible. You are permitted to slide blocks horizontally or vertically
 * into the blank square.
 * 
 * Input Files:
 *  The shortest solution to puzzle[T].txt requires exactly T moves.
 *  A good way to automatically run your program on our sample puzzles is to
 *  use the client PuzzleChecker.java.
 *   
 *   
 * Reference:
 * 
 * We define a search node of the game to be a board, the number of moves
 * made to reach the board, and the previous search node. First, insert the
 * initial search node (the initial board, 0 moves, and a null previous
 * search node) into a priority queue. Then, delete from the priority queue
 * the search node with the minimum priority, and insert onto the priority
 * queue all neighboring search nodes (those that can be reached in one move
 * from the dequeued search node). Repeat this procedure until the search
 * node dequeued corresponds to a goal board.
 * 
 * A key observation: To solve the puzzle from a given search node on the
 * priority queue, the total number of moves we need to make (including those
 * already made) is at least its priority, using either the Hamming or Manhattan
 * priority function. (For Hamming priority, this is true because each block
 * that is out of place must move at least once to reach its goal position.
 * For Manhattan priority, this is true because each block must move its
 * Manhattan distance from its goal position. Note that we do not count the
 * blank square when computing the Hamming or Manhattan priorities.) 
 * Consequently, when the goal board is dequeued, we have discovered not only
 * a sequence of moves from the initial board to the goal board, but one that
 * makes the fewest number of moves.
 * 
 * A critical optimization. Best-first search has one annoying feature: search
 * nodes corresponding to the same board are enqueued on the priority queue
 * many times. To reduce unnecessary exploration of useless search nodes, when
 * considering the neighbors of a search node, don't enqueue a neighbor if its
 * board is the same as the board of the previous search node. 
 * 
 * 
 *** Priority Queues *** 
 * - are great for finding the M largest(smallest) items in a huge stream of
 *   N items (where there is not enough memory to store N items)
 *   pq.insert(item);
 *   if( pq.size() > M ) pq.delMin();
 * - Key extends Comparable<Key>
 * - minPQ uses greater() instead of less() in its binary heap implementation.
 * - is a complete binary tree: every node has 2 children except for leaves
 * - key is in the nodes; parent key is no smaller than childrens keys.  
 *   
 *
 * 
 * API:
 <code>
 public Solver(Board initial)           // find a solution to the initial board (using the A* algorithm)
 public boolean isSolvable()            // is the initial board solvable?
 public int moves()                     // min number of moves to solve initial board; -1 if unsolvable
 public Iterable<Board> solution()      // sequence of boards in a shortest solution; null if unsolvable
 public static void main(String[] args) // solve a slider puzzle (given below)
 </code>
 *
 * 
 */

public class Solver {
    
    /*
     * the node being added to the priority queue to conduct A* search
     * This node is added to a minPQ and must therefore implement
     * the comparable interface to return a key to compare to other
     * SearchNodes and with which to insert
     * This key is either the manhattan or hamming distance plus
     * the number of moves.
     * 
     * Its is a wrapper class for the Board class used by A* search
     * Outer class can access its inner class private member variables
     */
    private class SearchNode implements Comparable<SearchNode> {
        private Board board;
        private int moves;
        private SearchNode prev;
        
        public SearchNode(Board b) {
            board = b;
            moves = 0;
            prev = null;
        }
        
        public boolean isGoal() {
            return board.isGoal();
        }
        
        public int priority() {
            return board.manhattan() + moves;
        }
        
        public int compareTo(SearchNode that) {
            int thisPriority = this.priority();
            int thatPriority = that.priority();
            if ( thisPriority < thatPriority ) return -1;
            else if ( thisPriority > thatPriority) return 1;
            else return 0;
        }
        
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("\nmanhattan: " + board.manhattan());
            s.append("\nmoves    : " + moves);
            s.append("\npriority : " + priority());
            s.append("    "+board.toString());
            return s.toString();
        }
        
    }
    
    /*
     * Find a solution to the initial board (using the A* algorithm)
     * Best First Search:
     * 1) insert the initial board, 0 moves, and null prev search node
     * 2) delete the search node with minimum priority, and insert all
     * neighboring search nodes;
     * 3) repeat until the search node dequeued is the goal board;
     * 
     * 
     * Use just one priority queue. Put both the original board and the
     * twin in the same priority queue. When you get a solution, check
     * if the original board or the twin is at the root of the solution.
     * If the original board is at the root of the solution, then the
     * problem is soluble. 
     */
    
    // run two puzzle instances to check for solvability.
    // exactly one of a board and its twin are solvable
    private SearchNode root;
    private SearchNode twin;
    
    public Solver(Board initial) {

        MinPQ<SearchNode> gameQueue = new MinPQ<SearchNode>();
        // initialize null prev board and 0 moves
        root = new SearchNode(initial); 
        twin = new SearchNode(initial.twin());
        gameQueue.insert(root);
        gameQueue.insert(twin);
        
        root = gameQueue.delMin();
        // Now look at neighbors and replace the board with the
        // children that are not the same as the previous board
        
        while (!root.isGoal()) {
            //StdOut.println(root);
            
            int cnt = 0;
            int moves = root.moves + 1;
            // Neighbors are returned in an iterable stack
            for (Board b: root.board.neighbors()) {
                //StdOut.println(moves +"("+ cnt +") " + b);
                
                // increment the move, assign previous searchnode
                SearchNode sn = new SearchNode(b);
                sn.moves = moves;
                sn.prev = root;
                
                // Dont add the search node if this board is the
                // same as the previous search nodes board
                SearchNode prevNode = root.prev;
                if (prevNode != null) {
                    Board prevBoard = root.prev.board;
                    //StdOut.println("EQUALS?"+ b.equals(prevBoard));
                    if (!b.equals(prevBoard)) gameQueue.insert(sn); 
                } else gameQueue.insert(sn);
                cnt++;
            }
            root = gameQueue.delMin();
        }
        // reached goal 
        //StdOut.println(root);
    }
    
    /*
     * is the initial board solvable?
     * We put both the initial search node and its twin into the gameQueue;
     * One or other of these will provide a solution. Once the priority queue
     * exhausts its search it has either expanded all neighbors of one board
     * or backtracked to expand all neighbors of the other since the goal was
     * not reached with the first attempt. When the search completes we are 
     * left with a root SearchNode that contains the goal board, the number
     * of moves it took, and pointers back to the SearchNode we started with.
     * If we chase these pointers back to the one we started with and it
     * turns out to be the twin, then we know our initial board didn't solve. 
     */
    public boolean isSolvable() {

        SearchNode currNode = root;
        //StdOut.println("curr: "+ currNode);
        while (currNode.prev != null) {
            currNode = currNode.prev;
            //StdOut.println("curr: "+ currNode);
        }
        if (currNode.board.equals(twin.board)) return false;
        return true;
    }
    
    /*
     * min number of moves to solve initial board; -1 if unsolvable
     * Final SearchNode tracks the number of moves so we can return it.
     */
    public int moves() {
        return root.moves;
    }
    
    /*
     * sequence of boards in a shortest solution; null if unsolvable
     * Here we also trace back the pointers in the SearchNodes but
     * push their member boards onto a stack (which is iterable),
     * allowing boards to be popped off in the forward move order
     */
    public Iterable<Board> solution() {
        Stack<Board> solution = new Stack<Board>();
        
        if (!isSolvable()) return null;
        SearchNode currNode = root;
        //StdOut.println("curr: "+ currNode);
        while (currNode.prev != null) {
            solution.push(currNode.board);
            currNode = currNode.prev;
            //StdOut.println("curr: "+ currNode);
        }     
        solution.push(currNode.board);
        return solution;
    }
    
    /*
     * Solve a slider puzzle read in from commandline textfile
     */
    public static void main(String[] args) {

        // create initial board from file
        In in = new In(args[0]);
        int N = in.readInt();
        int[][] blocks = new int[N][N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                blocks[i][j] = in.readInt();
        Board initial = new Board(blocks);

        // solve the puzzle
        Solver solver = new Solver(initial);
        
        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
