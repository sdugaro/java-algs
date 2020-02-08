/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac PointSET.java
 * Execution:    java PointSET circle10.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for SET.java
 *               Point2D.java (this package)
 *               RectHV.java  (this package)
 *
 * Problem: Write a mutable data type PointSET.java that represents a set
 * of points in the unit square. Implement the following API by using a
 * red-black BST (using either SET from algs4.jar or java.util.TreeSet).
 * Your implementation should support insert() and contains() in time
 * proportional to the logarithm of the number of points in the set in
 * the worst case; it should support nearest() and range() in time
 * proportional to the number of points in the set.  
 * 
 *   
 * Reference:
 *  algs4::SET uses java.util.TreeSet under the hood
 * 
 * API:
 <code>
 public class PointSET {
   public         PointSET()                               // construct an empty set of points 
   public           boolean isEmpty()                      // is the set empty? 
   public               int size()                         // number of points in the set 
   public              void insert(Point2D p)              // add the point to the set (if it is not already in the set)
   public           boolean contains(Point2D p)            // does the set contain point p? 
   public              void draw()                         // draw all points to standard draw 
   public Iterable<Point2D> range(RectHV rect)             // all points that are inside the rectangle 
   public           Point2D nearest(Point2D p)             // a nearest neighbor in the set to point p; null if the set is empty 

   public static void main(String[] args)                  // unit testing of the methods (optional) 
 }
 </code>
 * 
 */

public class PointSET {

    private SET<Point2D> points;
    
    /* construct an empty set of points
     * 
     */
    public PointSET() {
        points = new SET<Point2D>();
    }
    
    /* is the set empty?
     * 
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }
    
    /* number of points in the set
     * 
     */
    public int size() {
        return points.size();
    }
    
    /* add the point to the set (if it is not already in the set)
     * Existence supposely implemented in SET[TreeSet].add
     */
    public void insert(Point2D p) {
        points.add(p);
    }
    
    /* does the set contain point p?
     * 
     */
    public boolean contains(Point2D p) {
        return points.contains(p);
    }
    
    /* draw all points to standard draw
     * 
     */
    public void draw() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
        for (Point2D p: points)
            p.draw();
    }
    
    /* all points that are inside the rectangle
     * 
     */
    public Iterable<Point2D> range(RectHV rect) {
        Stack<Point2D> stack = new Stack<Point2D>();
        
        for (Point2D p: points) {
            if (rect.contains(p)) stack.push(p);
            /* RectHV implements a contains method already
            double x = p.x();
            double y = p.y();
            StdOut.print("Checking: " + p);
            if (y <= rect.ymax() && y >= rect.ymin()) {
                StdOut.print(" y");
                if (x <= rect.xmax() && x >= rect.xmin()) {
                    StdOut.print(" x");
                    stack.push(p);
                }
            }
            StdOut.println();
            */
        }
        return stack; // from algs4 implements Iterable<Item>
        
    }
    
    /* a nearest neighbor in the set to point p; null if the set is empty
     * 
     */
    public Point2D nearest(Point2D p) {
        if (points.isEmpty()) return null;
        
        double dist;
        double minDist = 10.0; // much larger than assumed unit square
        Point2D minPoint = null;
                
        for (Point2D q: points) {
            dist = p.distanceTo(q);
            //StdOut.println("Checking: "+ q +" dist="+ (float)dist +" min="+ (float) minDist);
            if (dist < minDist) {
                minDist = dist;
                minPoint = q;
            }
        }
        //StdOut.println("Nearest Neighbor: " + minPoint + " min dist="+ (float) minDist);
        return minPoint;
    }

    /* DEBUG
     * iterate over SET printing out its contents
     * private api member variables not evaluated
     */
    private void dump() {
        int i = 0;
        for (Point2D p: points) {
            StdOut.println(i +": "+ p);
            i++;
        }
    }
    /* unit testing of the methods (optional)
     * 
     */
    public static void main(String[] args) {
        
        // Read in a set of points from a file
        In file = new In(args[0]);
        PointSET points = new PointSET();
    
        
        while(!file.isEmpty()){
            double x = file.readDouble();
            double y = file.readDouble();
            points.insert(new Point2D(x,y));
        }
        
        points.dump();
        
        RectHV r0 = new RectHV(0.0,0.0,0.75,0.75);
        StdOut.println(r0);
        for (Point2D p: points.range(r0))
            StdOut.println(p);
        StdDraw.setPenColor(StdDraw.RED);
        r0.draw();
        points.draw();
        
        StdDraw.setPenColor(StdDraw.RED);
        //Point2D pQuery = new Point2D(0.0,0.0);
        Point2D pQuery = new Point2D(0.25,0.25);
        //Point2D pQuery = new Point2D(0.5,0.0);
        //Point2D pQuery = new Point2D(0.5,0.9);
        pQuery.draw();
        StdOut.println("------------------------------");
        Point2D pNearest = points.nearest(pQuery);
        StdDraw.setPenColor(StdDraw.GREEN);
        pNearest.draw();
        StdOut.println(pQuery +" Nearest "+ pNearest);
        
    }
    
}
