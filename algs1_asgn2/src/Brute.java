
/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac Brute.java
 * Execution:    java Brute input8.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *
 * Problem: Given a set of N distinct points in the plane, draw every 
 * (maximal) line segment that connects a subset of 4 or more of the points.
 * 
 * Description: Brute force O(N^4) time / ~N space implementation  
 * examines 4 points at a time and checks whether they all lie on the same
 * line segment, printing out any such line segments to standard output and
 * drawing them using standard drawing. 
 * 
 * To check whether the 4 points p, q, r, and s are collinear, check whether
 * the slopes between p and q, between p and r, and between p and s are all
 * equal.
 * 
 * The order of growth of the running time of your program should be N4 in
 * the worst case and it should use space proportional to N. 
 * 
 */

public class Brute {
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
        // this is NlogN but pails compared to algo N^4
        Quick.sort(points);
        
        //for (int j=0; j < N; j++) StdOut.println(points[j]);
        for ( int w = 0; w < N; w++) 
            for ( int x = w+1; x < N; x++) 
                for ( int y = x+1; y < N; y++)
                    for ( int z = y+1; z < N; z++) 
                    {
                        Point p = points[w];
                        Point q = points[x];
                        Point r = points[y];
                        Point s = points[z];
                        double pq = p.slopeTo(q);
                        double pr = p.slopeTo(r);
                        double ps = p.slopeTo(s);
                        if ( pq == pr && pq == ps ) {
                            StdOut.println(p +"->" + q + "->" + r + "->" + s);
                            p.draw();
                            q.draw();
                            r.draw();
                            s.draw();
                            p.drawTo(s);
                        }
                    }    
    }
}
