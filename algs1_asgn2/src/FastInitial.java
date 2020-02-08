/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac FastInitial.java
 * Execution:    java FastInitial input8.txt
 *  * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *
 * Problem: Given a set of N distinct points in the plane, draw every 
 * (maximal) line segment that connects a subset of 4 or more of the points.
 * 
 * Description:  A faster, sorting-based solution. O(N^2 log N) time / ~N space 
 * Remarkably, it is possible to solve the problem much faster than the
 * brute-force solution. Given a point p, the following method determines
 * whether p participates in a set of 4 or more collinear points: 
 * - Think of p as the origin. 
 * - For each other point q, determine the slope it makes with p. 
 * - Sort the points according to the slopes they makes with p. 
 * - Check if any 3 (or more) adjacent points in the sorted order have equal
 *   slopes with respect to p. If so, these points (with p) are collinear.
 *   
 * Applying this method for each of the N points in turn yields an
 * efficient algorithm to the problem. The algorithm solves the problem
 * because points that have equal slopes with respect to p are collinear,
 * and sorting brings such points together. The algorithm is fast because
 * the bottleneck operation is sorting. 
 * 
 * APIs. Each program should take the name of an input file as a
 * command-line argument, read the input file (in the format specified
 * below), print to standard output the line segments discovered (in
 * the format specified below), and draw to standard draw the line
 * segments discovered (in the format specified below).  
 * 
 * 
 * 
 * POOR INITIAL ATTEMPT: 
 * (x) outputs permutations
 * (x) possibly NOT ~N in space (several arrays copied within loops)
 * (-) need a linked list of points to join with "->" and draw endpoints.
 * 
 */

public class FastInitial {
    
    public static void main(String[] args) {
        In file = new In(args[0]);
        int N = file.readInt();
        
        Point[] points = new Point[N];
        
        int i = 0;
        while(!file.isEmpty()){
            int x = file.readInt();
            int y = file.readInt();
            points[i] = new Point(x,y);
            i++;
        }
        
        // repo draw window coordinate system
        StdDraw.setXscale(0,32768);
        StdDraw.setYscale(0,32768);
        
        // print out and *ordered* sequence of points
        Quick.sort(points);

        // for each point calc the slope with every other point
        // CANT just create an array of slopes to compare with
        // since that would be creating p*N memory
        for (int w = 0; w < N; w++) {
            Point p = points[w];
            Double[] slopes = new Double[N];
            Double[] oSlopes = new Double[N];

            for (int x = 0; x < N; x++) {
                Point q = points[x];
                slopes[x] = p.slopeTo(q);
                oSlopes[x] = slopes[x];
                //StdOut.println(p +"-"+ q +"="+ slopes[x]);
                // degenerate point slope (p.slopeTo(p)) is -INF
            }
            Quick.sort(slopes);
            // after the slopes are sorted find a run of 3 or more
            int cnt = 1;
            boolean run = false;
            Stack<Double> runSlopes = new Stack<Double>();
            for (int z=1; z < N; z++) {
                double s1 = slopes[z];
                double s0 = slopes[z-1];
                //StdOut.println(s0 +" | "+ s1 + " | " + (s1==s0));
                if ( s1 == s0 ) {
                    if (!run) {
                        run = true;
                        cnt = 2;  // found duplicate
                    } else {
                        runSlopes.push(s0);
                        cnt++; // at least triplicate
                    }
                    if (cnt > 3) runSlopes.pop();
                    continue;
                }
                run = false;
                cnt = 1;
            }
            StdOut.println("\nSTACK SIZE: "+ runSlopes.size() );
            while(!runSlopes.isEmpty()) {
                double key = runSlopes.pop();
                StdOut.println("Found Run of 3 or more for slope: " + key);
                // scan slopes for key slope connecting p
                // retrieving point index
                Queue<Point> pq = new Queue<Point>();
                pq.enqueue(p);
                for ( int y=0; y<N; y++) {
                    if (oSlopes[y] == key) pq.enqueue(points[y]);
                }
                while (!pq.isEmpty()) {
                    StdOut.print(pq.dequeue());
                    if (!pq.isEmpty()) StdOut.print(" -> ");
                }
            }
        }

    }
}
