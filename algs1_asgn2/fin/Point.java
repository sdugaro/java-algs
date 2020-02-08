/*************************************************************************
 * Name:
 * Email:
 *
 * Compilation:  javac Point.java
 * Execution:
 * Dependencies: StdDraw.java
 *
 * Description: An immutable data type for points in the plane.
 *
 *
 * Immutable:
 * Once the constructor for an object has completed execution that instance
 * cannot be altered. This means you can pass reference to the object around
 * without worrying that data is going to change. There are never locking
 * issues related to objects that never change (concurrency)
 * 
 * 2 interfaces used to implement sorting in java
 * 
 * Comparatr interface: sort using ALTERNATE ORDER
 * Comparator (java.util->assist to compare two objects; multiple properties):
 * A comparison function, which imposes a total ordering on some collection
 * of objects. Implementing a comparator interface requires the method defn
 * int compare(T x, T y) returning -1 if x<y, 0 if x=y, 1 if x>y
 * 
 * public static final comparator<Student> BY_NAME = new ByName();
 * private static class ByName implements Comparator<Student>
 * { public int compare(Student v, Studen w); }
 * 
 * Comparable Interface: sort using a types NATURAL ORDER
 * Comparable (java.lang->use by default to compare this obect; single property):
 * An interface that imposes a total ordering on the objects of each class
 * that implements it. Implementing a comparable interface requires the defn
 * int compareTo(T o) returning -1 if x<y, 0 if x=y, 1 if x>y
 * 
 * public class Date implements Comparable<Date> {
 *   private final int month,day,year;
 *   public int compareTo(Date that) {
 *     if ( this.year < that.year) return -1; 
 *     if ( this.year > that.year) return 1;
 *     else return 0;
 *  }
 * 
 * public class Insertion {
 * 
 * public static void sort(Comparable[] a) {
 *   int N = a.length;
 *   for ( int i=0; i < N; i++ )
 *     for ( int j=i; j > 0; j--)
 *       if ( less(a[j],a[j-1]) exch(a,j,j-1)
 *       else break;
 *     }
 *   }
 * }
 * private static boolean less (Comparable v, Comparable w)
 * { return v.compareTo(w) < 0; }
 * private static void exch(Comparable[] a, int i, int j)
 * { Item tmp=v; v=w; w=tmp; }
 * 
 * public static void sort(Object[] a,Comparator c)
 * {
 *   int N = a.length;
 *   for (int i=0; i<N; i++)
 *     for (int j=i; j>0 && less(comparator, a[j],a[j-1]; j--}
 *       exch(a,j,j-1);
 * }
 * private static boolean less(comparator c, Object v, Object w)
 * { return c.compare(v,w) < 0; }
 * private static viod exch(Object[] a, int i, int j)
 * { Object tmp=a[i]; a[i]=a[j]; a[j]=tmp; }
 *
 *
 *************************************************************************/

import java.util.Arrays;
import java.util.Comparator;


public class Point implements Comparable<Point> {

    // compare points by slope
    public final Comparator<Point> SLOPE_ORDER = new BySlope();
    
    /* comparator implementation, define a class that implements the interface
     * 
     * Compares points by the slopes they make with the invoking point (x0,y0)
     * (x1,y1) < (x2,y2) if and only if the slope (y1-y0)/(x1-x0) is less
     * than the slope (y2-y0)/(x2-x0)
     * 
     * Implementing a comparator interface requires the method defn
     * int compare(T x, T y) returning -1 if x<y, 0 if x=y, 1 if x>y
     * 
     * Implement the SLOPE_ORDER comparator in Point. The complicating issue
     * is that the comparator needed to compare the slopes that two points
     * q and r make with a third point p, which changes from sort to sort.
     * To do this include a public and final (but not static) instance
     * variable SLOPE_ORDER in Point of type Comparator<Point>. 
     * This Comparator has a compare() method so that compare(q, r)
     * compares the slopes that q and r make with the invoking object p.
     * 
     * Note you can refer directly to instance methods of the outer class
     * (such as slopeTo()); 
     * 
     */
    private class BySlope implements Comparator<Point> {
        public int compare(Point p, Point q) {
            double pSlope = slopeTo(p);
            double qSlope = slopeTo(q);
            if (pSlope < qSlope) return -1;
            if (qSlope < pSlope) return 1;
            return 0;
        }
    }

    private final int x;                              // x coordinate
    private final int y;                              // y coordinate

    // create the point (x, y)
    public Point(int x, int y) {
        /* DO NOT MODIFY */
        this.x = x;
        this.y = y;
    }

    // plot this point to standard drawing
    public void draw() {
        /* DO NOT MODIFY */
        StdDraw.point(x, y);
    }

    // draw line between this point and that point to standard drawing
    public void drawTo(Point that) {
        /* DO NOT MODIFY */
        StdDraw.line(this.x, this.y, that.x, that.y);
    }

    /* slope between this point and that point
     * @return the slope between the invoking point (x0,y0) and the
     * argument point (x1,y1) where slope = (y1-y0)/(x1-x0). 
     * Treat the slope of a horizontal line as +0
     * Treat the slope of a vertical line as +INF
     * Treat the slope of a degenerate line (point and itself) as -INF 
     * 
     * What does it mean for slopeTo() to return positive zero? 
     * Java (and the IEEE 754 floating-point standard) define two
     * representations of zero: negative zero and positive zero.
     <code> 
     double a = 1.0;
     double x = (a - a) /  a;   // positive zero ( 0.0)
     double y = (a - a) / -a;   // negative zero (-0.0)
     </code>
     Note that while (x == y) is guaranteed to be true, Arrays.sort()
     treats negative zero as strictly less than positive zero.
     Thus, to make the specification precise, we require you to
     return positive zero for horizontal line segments. Unless your
     program casts to the wrapper type Double (either explicitly or
     via autoboxing), you probably will not notice any difference
     in behavior; but, if your program does cast to the wrapper type
     and fails only on (some) horizontal line segments, this may be the cause. 
     */
    public double slopeTo(Point that) {
        if ( this.x == that.x && this.y == that.y) // degenerate
            return Double.NEGATIVE_INFINITY;
        if (this.x == that.x)                      // vertical line
            return Double.POSITIVE_INFINITY;
        if ( this.y == that.y)                   // horizontal line
            return 0.0;
        
        // cast return since point coordinates are integers.
        return (double) (that.y - this.y)/(that.x - this.x);
    }

    /* is this point lexicographically smaller than that one?
     * compare points by their y-coordinates and break ties by x-coordinate
     * 
     * The invoking point (x0,y0) is less than the argument point (x1,y1)
     * if and only if either y0 < y1 or (y0=y1 and x0<x1)
     * 
     * Implementing a comparable interface requires the defn 
     *  int compareTo(T o) returning -1 if x<y, 0 if x=y, 1 if x>y
     */
    public int compareTo(Point that) {
        if ( this.y < that.y ) return -1;
        else if ( this.y == that.y && this.x < that.x) return -1;
        else if ( this.y == that.y && this.x == that.x) return 0;
        else return 1;
    }

    // return string representation of this point
    public String toString() {
        /* DO NOT MODIFY */
        return "(" + x + ", " + y + ")";
    }
    
    /*
    public int y() {
        return this.y;
    }
    
    public int x() {
        return this.x;
    }
    */

    // unit test
    public static void main(String[] args) {
        Point a = new Point(0,0);
        Point b = new Point(1,0);
        Point c = new Point(0,1);
        Point d = new Point(0,-1);
        Point e = new Point(-1,0);
        Point f = new Point(1,2);
        Point g = new Point(1,3);
        a.draw();
        b.draw();
        c.draw();
        d.draw();
        e.draw();
        f.draw();
        g.draw();
        a.drawTo(b);
        a.drawTo(c);
        a.drawTo(d);
        a.drawTo(e);
        a.drawTo(f);
        a.drawTo(g);
        
        StdOut.println(a +":"+ b +" "+ a.compareTo(b));
        StdOut.println(a +":"+ c +" "+ a.compareTo(c));
        StdOut.println(a +":"+ d +" "+ a.compareTo(c));
        StdOut.println(a +":"+ e +" "+ a.compareTo(c));
        
        StdOut.println(a +":"+ f +" "+ a.slopeTo(f));
        StdOut.println(a +":"+ g +" "+ a.slopeTo(g));
        StdOut.println(a.SLOPE_ORDER.compare(f, g));
        
        StdOut.println(a.SLOPE_ORDER.compare(g, f));
        StdOut.println(a.SLOPE_ORDER.compare(f, f));
        
        // Java System Sorts. natural order vs alternate comparator order
        String[] sss = {"abc","DEF","dfe","zzz","ert","tyu"};
        Arrays.sort(sss);
        for(String s: sss) StdOut.println(s);
        StdOut.println("-------------");
        Arrays.sort(sss,String.CASE_INSENSITIVE_ORDER);
        for(String s: sss) StdOut.println(s);
        StdOut.println("-------------");
        
        // Sort our points with custom sort comparator order
        Point[] points = {a,b,c,d,e,f,g};
        StdOut.println("Unsorted:");
        for (Point p: points) {
            StdOut.print(p);
            if( p != points[points.length-1]) StdOut.print("->");
        }
        
        StdOut.println("\na Sorted:");
        Arrays.sort(points,a.SLOPE_ORDER);
        for (Point p: points) {
            StdOut.print(p);
            if( p != points[points.length-1]) StdOut.print("->");
        }

        StdOut.println("\nb Sorted:");
        Arrays.sort(points,b.SLOPE_ORDER);
        for (Point p: points) {
            StdOut.print(p);
            if( p != points[points.length-1]) StdOut.print("->");
        }
        StdOut.println("\nc Sorted:");
        Arrays.sort(points,c.SLOPE_ORDER);
        for (Point p: points) {
            StdOut.print(p);
            if( p != points[points.length-1]) StdOut.print("->");
        }

        
    }
}
