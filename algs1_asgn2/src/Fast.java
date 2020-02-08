/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac Fast.java
 * Execution:    java Fast input8.txt
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
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Fast {
    
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

        // Array.sort(points) within its iteration may miss points
        Point[] srcPoints = new Point[N];
        for( int a = 0; a < N; a++) srcPoints[a] = points[a];
        List<Point[]> lines = new ArrayList<Point[]>();
        
        for (Point p: srcPoints) {
          Arrays.sort(points,p.SLOPE_ORDER);
          
          int cc = 0;
          StdOut.println("<<<"+ p +">>>");
          for (Point q: points) {
              StdOut.println(cc++ +":" + q +" : "+ p.slopeTo(q));  
          }
          // find a run of 3 or more slopes in the slope sorted point list
          int cnt = 1;
          int start = 0;
          boolean run = false;
          for (int z=1; z<N; z++) {
              double s1 = p.slopeTo(points[z]);
              double s0 = p.slopeTo(points[z-1]);
              
              //StdOut.println((z-1)+" "+ s0 +" | "+ s1 + " | " + (s1==s0));
              
              if (s1 == s0) { // found duplicate or in a run > 2
                  if (!run) start = z-1; // dont reset each iter!
                  run = true; 
                  cnt++; 
                  if (z < N-1) continue; // internal run
              } 
              // dont branch else here; otherwise processing logic may
              // not catch runs that complete with the end of array 
              if ( run && cnt < 3 ) {
                  run = false;
                  cnt = 1;
                  continue;
              }
              
              if( run && cnt >= 3) {
                  /*
                  StdOut.println("FOUND RUN of length:"+ cnt +"@"+ start);
                  */
             
                  // now *order* the points in this run and cache them
                  List<Point> pl = new ArrayList<Point>();
                  pl.add(p);
                  for (int ii=start; ii<(start+cnt); ii++)
                      pl.add(points[ii]);
                  Point[] pa = new Point[pl.size()];
                  pa = pl.toArray(pa);
                  Arrays.sort(pa);

                  /* DEBUG RUN array
                  for (int jj=0; jj<pa.length; jj++)
                      StdOut.print(pa[jj] + " ");
                  StdOut.println();
                  */
                  
                  if ( lines.isEmpty() ) lines.add(pa);
                  else { // check existing lines before we add another one
                      boolean duplicate = false;
                      for (Point[] line: lines) {
                        if (pa.length == line.length) {
                            duplicate = true;
                            for(int k=0; k < line.length; k++) {
                                if (pa[k].compareTo(line[k]) != 0) {
                                    duplicate = false;
                                    break; // no match
                                }
                            }
                        }
                        if ( duplicate ) break;
                      }
                      // dont add line if its already there
                      if ( !duplicate ) lines.add(pa);
                  }
                  // processed line, reset
                  run = false;
                  cnt = 1;
              }
          }
        }
        
        // Echo and display line segments
        for (Point[] ll: lines) {
            for( int pi=0; pi<ll.length; pi++) { 
                StdOut.print(ll[pi]);
                if (pi != ll.length-1) StdOut.print(" -> ");
            }
            StdOut.println();            
        }
        
        // repo draw window coordinate system
        StdDraw.setXscale(0,32768);
        StdDraw.setYscale(0,32768);
        
        // Draw all the points from the input file
        for (Point dp: points)
            dp.draw();
        
        // Draw all line segment end points
        for (Point[] dl: lines)
            dl[0].drawTo(dl[dl.length-1]);
    }
}