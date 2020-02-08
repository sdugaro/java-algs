/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac Solver.java
 * Execution:    java Solver puzzle04.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *
 * Problem:
 * 
 *   
 * Reference:
 * 
 * API:
 <code>
    public Board(int[][] blocks)           // construct a board from an N-by-N array of blocks
                                           // (where blocks[i][j] = block in row i, column j)
    public int dimension()                 // board dimension N
    public int hamming()                   // number of blocks out of place
    public int manhattan()                 // sum of Manhattan distances between blocks and goal
    public boolean isGoal()                // is this board the goal board?
    public Board twin()                    // a boadr that is obtained by exchanging two adjacent blocks in the same row
    public boolean equals(Object y)        // does this board equal y?
    public Iterable<Board> neighbors()     // all neighboring boards
    public String toString()               // string representation of this board (in the output format specified below)

    public static void main(String[] args) // unit tests (not graded)
 </code>
 * 
 */

// seems like java.lang is imported automatically
//import java.lang.StringBuilder;
import static java.lang.Math.*; // static import to avoid package qualified names

public class Board {
    
    // immutable data type
    private final int N;          // dimension
    private final int[][] tiles;
    
    /*
     * construct a board from an N-by-N array of blocks
     * (where blocks[i][j] = block in row i, column j)
     */
    public Board(int[][] blocks) {
        N = blocks.length;
        tiles = new int[N][N];
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                tiles[i][j] = blocks[i][j];
            }
    }
    
    /*
     * board dimension N
     */
    public int dimension() {
        return N;
    }
    
    /*
     * Hamming priority function. The number of blocks in the wrong position, 
     * plus the number of moves made so far to get to the search node. 
     * Intuitively, a search node with a small number of blocks in the wrong
     * position is close to the goal, and we prefer a search node that have
     * been reached using a small number of moves.
     * 
     * 
     * @return the number of blocks out of position
     */
    public int hamming() {
        int sum = 0;
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                if (tiles[i][j] == 0) continue;
                if (tiles[i][j] != (j + i*N + 1)) // tile posn from 1
                    sum++;
            }
        return sum;
    }
    
    /*
     * Manhattan priority function. The sum of the Manhattan distances 
     * (sum of the vertical and horizontal distance) from the blocks to their
     * goal positions, plus the number of moves made so far to get to the
     * search node. 
     * 
     * Given an integer i its goal position on the board is 
     * ([x,y]=[ceil(i/N)-1,(i-1)%N]
     * ie on a 2x2 board, tile 1 should be at (1/2)-1,(2-1)%2 or (0,1)
     * ie on a 2x2 board, tile 2 should be at (2/2)-1,(2-1)%2 or (0,1)
     * ie on a 3x3 board, tile 2 should be at (2/3)-1,(2-1)%3 or (0,1)
     * ie on a 3x3 board, tile 4 should be at (4/3)-1,(4-1)%3 or (1,0) 
     * if tile 2 is at (i,j) then the manhattan distance is |i-1| + |j-0| 
     * this function returns this number summed over all tiles.  
     * 
     * @return sum of Manhattan distances between the blocks and their goal positions.
     */
    public int manhattan() {
        int sum = 0;
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++) {
                int v = tiles[i][j];
                if (v==0) continue;
                // ceil returns a double. * will return int if arg is int *
                int goalx = (int) ceil((double)v/N-1);
                int goaly = (v-1) % N;
                int dist = abs(i-goalx) + abs(j-goaly);
                //StdOut.println(v+" (i,j) = ("+i+","+j+ ") (x,y) = ("+ goalx +","+ goaly +") "+dist);
                sum += dist;
            }
        return sum;
    }
    
    /*
     * is this board the goal board?
     * the hamming function returns 0 if no tile is out of position 
     */
    public boolean isGoal() { 
        return (hamming() == 0);
    }
    
    /*
     * a board that is obtained by exchanging two adjacent blocks in the same row
     * 
     * You will use it to determine whether a puzzle is solvable: exactly one of a board
     * and its twin are solvable. A twin is obtained by swapping two adjacent blocks 
     * (the blank does not count) in the same row. Your solver will use only one twin. 
     * 
     */
    public Board twin() {
        int[][] twin = new int[N][N];
        
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                twin[i][j] = tiles[i][j];
        
        boolean found = false;
        // now swap any two adjacent (non-zero) tiles
        for (int i=0; i<N; i++) {
            for (int j=1; j<N; j++ ) {
                if (twin[i][j] !=0 && twin[i][j-1] !=0) { // swap
                    int tmp = twin[i][j];
                    twin[i][j] = twin[i][j-1];
                    twin[i][j-1] = tmp;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        return new Board(twin);
    }
    
    /*
     * does this board equal y?
     * @see java.lang.Object#equals(java.lang.Object)
     * Java has some arcane rules for implementing equals(), 
     * discussed on p. 103 of Algorithms, 4th edition. Note that the argument to
     * equals() is required to be Object. 
     * Date.java
     <code>
     public boolean equals(Object x) {
        if (x == this) return true;
        if (x == null) return false;
        if (x.getClass() != this.getClass()) return false;
        Date that = (Date) x; // cast guaranteed to succeed.
        return (this.month == that.month) && (this.day == that.day) && (this.year == that.year);
     }
     </code>
     *   
     */
    public boolean equals(Object y) {
        if (y == this) return true;
        if (y == null) return false;
        if (y.getClass() != this.getClass()) return false;
        
        Board that = (Board) y;
        if ( N != that.dimension() ) return false;
        for (int i=0; i<N; i++)
            for (int j=0; j<N; j++)
                if ( tiles[i][j] != that.tiles[i][j] )
                    return false;
        return true;
    }
    
    /*
     * all neighboring boards.
     * How do I return an Iterable<Board>? 
     * Add the items you want to a Stack<Board> or Queue<Board> and return that.
     * 
     * neighbors are all possible tile swaps around the 0/blank tile
     * we should return 2-4 neighbors.
     * 4 = blank in the inside row/ inside column
     * 3 = blank in the middle row & first or last column
     * 3 = blank in the center col & top or bottom row
     * 2 = any corner.
     * 
     * first copy the current board and find coordinates (i,j) of the 0 tile. 
     * neighbors are left (i-1), right (i+1), up (j+1), dn (j-1) if they exist.
     * if i|j @ 0, one left -1 < 0 steps over the leftmost boundary
     * if i|j @ N-1, one right N > N-1 steps over the rightmost boundary
     * 
     */
    public Iterable<Board> neighbors() {
        
        int[][] neighbor = new int[N][N];
        Stack<Board> neighbors = new Stack<Board>();
        
        int i0=0, j0=0;
        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                neighbor[i][j] = tiles[i][j];
                if (tiles[i][j] == 0) {
                    i0 = i;
                    j0 = j;
                }
            }
        }
        
        for (int j=-1; j<=1; j++) {
            if (j==0) continue;
            
            int move = j0 + j;
            if ( move < 0 || move >=N ) continue;
            
            int zero = neighbor[i0][j0];
            neighbor[i0][j0] = neighbor[i0][move];
            neighbor[i0][move] = zero;
            neighbors.push(new Board(neighbor));
            // now swap back for additional processing
            zero = neighbor[i0][move];
            neighbor[i0][move] = neighbor[i0][j0];
            neighbor[i0][j0] = zero;
        }
        
        for (int i=-1; i<=1; i++) {
            if (i==0) continue;
            int move = i0 + i;
            if ( move < 0 || move >=N ) continue;
            int zero = neighbor[i0][j0];
            neighbor[i0][j0] = neighbor[move][j0];
            neighbor[move][j0] = zero;
            neighbors.push(new Board(neighbor));
            // now swap back for additional processing
            zero = neighbor[move][j0];
            neighbor[move][j0] = neighbor[i0][j0];
            neighbor[i0][j0] = zero; 
        }
        return neighbors;
    }
    
    /*
     * string representation of this board
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(N + "\n");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                s.append(String.format("%2d ", tiles[i][j]));
            }
            s.append("\n");
        }
        return s.toString();
    }

    /*
     * unit tests (not graded)
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
        StdOut.println(initial);
        StdOut.println("manhattan: " + initial.manhattan());
        StdOut.println("hamming  : " + initial.hamming());
        StdOut.print("twin: ");
        StdOut.println(initial.twin());
        StdOut.println("-----\n");
        
        
        for (Board b: initial.neighbors())
            StdOut.println(b);
        
    }
}
