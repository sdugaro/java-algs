/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac PointSET.java
 * Execution:    java PointSET circle10.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *
 * Problem: Write a mutable data type KdTree.java that uses a 2d-tree to
 * implement the same API (but replace PointSET with KdTree). A 2d-tree
 * is a generalization of a BST to two-dimensional keys. The idea is to
 * build a BST with points in the nodes, using the x- and y-coordinates
 * of the points as keys in strictly alternating sequence.
 * 
 * The prime advantage of a 2d-tree over a BST is that it supports efficient
 * implementation of range search and nearest neighbor search. Each node 
 * corresponds to an axis-aligned rectangle in the unit square, which 
 * encloses all of the points in its subtree. The root corresponds to the 
 * unit square; the left and right children of the root corresponds to the
 * two rectangles split by the x-coordinate of the point at the root; 
 * and so forth.
 * 
 * Search and insert:
 * The algorithms for search and insert are similar to those for BSTs,
 * but at the root we use the x-coordinate (if the point to be inserted
 * has a smaller x-coordinate than the point at the root, go left;
 * otherwise go right); then at the next level, we use the y-coordinate
 * (if the point to be inserted has a smaller y-coordinate than the point
 * in the node, go left; otherwise go right); then at the next level
 * the x-coordinate, and so forth. 
 * 
 * Draw: 
 * A 2d-tree divides the unit square in a simple way: all the points to 
 * the left of the root go in the left subtree; all those to the right 
 * go in the right subtree; and so forth, recursively. Your draw() 
 * method should draw all of the points to standard draw in black and
 * the subdivisions in red (for vertical splits) and blue (for horizontal 
 * splits). This method need not be efficient—it is primarily for debugging. 
 *     
 * Reference:
 * 
 * Point2D data type: how are points compared when determining insertion.
 * no need to implement a comparable/Comparator interface because Point2D
 * already does it with a static final Comparator<Point2D>: 
 * Point2D.X_ORDER.compare(p,q)
 <code>
     
    // Compares two points by x-coordinate.
    public static final Comparator<Point2D> X_ORDER = new XOrder();
    // compare points according to their x-coordinate
    private static class XOrder implements Comparator<Point2D> {
        public int compare(Point2D p, Point2D q) {
            if (p.x < q.x) return -1;
            if (p.x > q.x) return +1;
            return 0;
        }
    }
    // Compares two points by y-coordinate.
    public static final Comparator<Point2D> Y_ORDER = new YOrder();    
    // compare points according to their y-coordinate
    private static class YOrder implements Comparator<Point2D> {
        public int compare(Point2D p, Point2D q) {
            if (p.y < q.y) return -1;
            if (p.y > q.y) return +1;
            return 0;
        }
    }
 </code>
 * 
 * Node data type: There are several reasonable ways to represent a node
 * in a 2d-tree. One approach is to include the point, a link to the 
 * left/bottom subtree, a link to the right/top subtree, and an axis-aligned
 * rectangle corresponding to the node. Unlike the Node class for BST, this
 * Node class is static because it does not refer to a generic Key or Value 
 * type that depends on the object associated with the outer class. This saves 
 * the 8-byte inner class object overhead. Also, since we don't need to 
 * implement the rank and select operations, there is no need to store the 
 * subtree size. 
 * 
 * Writing KdTree: Write a simplified version of insert() which does everything
 * except set up the RectHV for each node. Write the contains() method, and use 
 * this to test that insert() was implemented properly. Note that insert() and 
 * contains() are best implemented by using private helper methods analogous to 
 * those found by looking at BST.java. We recommend using orientation as an 
 * argument to these helper methods. 
 <code>
     // does there exist a key-value pair with given key?
    public boolean contains(Key key) {
        return get(key) != null;
    }

    // return value associated with the given key, or null if no such key exists
    public Value get(Key key) {
        return get(root, key);
    }

    private Value get(Node x, Key key) {
        if (x == null) return null;
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) return get(x.left, key);
        else if (cmp > 0) return get(x.right, key);
        else              return x.val;
    } 

    // Insert key-value pair into BST
    // If key already exists, update with new value
    public void put(Key key, Value val) {
        if (val == null) { delete(key); return; }
        root = put(root, key, val);
        assert check();
    }

    private Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(key, val, 1);
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) x.left  = put(x.left,  key, val);
        else if (cmp > 0) x.right = put(x.right, key, val);
        else              x.val   = val;
        x.N = 1 + size(x.left) + size(x.right);
        return x;
    }
 </code>
 * 
 * Now add the code to insert() which sets up the RectHV for each Node. Next, 
 * write draw(), and use this to test these rectangles. Finish up KdTree with 
 * the nearest and range methods.  
 * 
 * API:
 <code>
 public class KdTree {
   public           KdTree()                               // construct an empty set of points 
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

public class KdTree {
    
    private int  size;       
    private Node root;
    private RectHV unitSquare = new RectHV(0.0,0.0,1.0,1.0);
    
    
    /* Data Type to represent a node in a 2D-Tree
     * - static because it DOES NOT refer to a GENERIC Key or Value type that
     *   depends on the object associated with the outerclass.
     * - Since we dont need to implement rank and select operations there
     *   is no need to store the subtree size.
     *
     */
    private static class Node {
        private Point2D p;    // the point.. levels alternate in x,y
        private RectHV  rect; // the axis-aligned rect corresponding to this node
        private Node    lb;   // the left/bottom subtree
        private Node    rt;   // the right/top subtree
        private int     level;// level%2 tells us if this node is vert or horiz
        
        //N.B. java does not have unsigned types, so level maxes out at 2^31

        public Node(Point2D q, int d, Node l, Node r, RectHV hv ) {
            p = q;
            level = d;
            lb = l;
            rt = r;
            rect = hv;
        }
        public Node(Point2D q, int d, Node l, Node r, double xMin, double yMin, double xMax, double yMax) {
            p = q;
            level = d;
            lb = l;
            rt = r;
            rect = new RectHV(xMin,yMin,xMax,yMax);
        }

    }
    
    /* construct an empty set of points
     * 
     */
    public KdTree() {
        root = null;
        size = 0;
    }
    
    /* is the set empty?
     * 
     */
    public boolean isEmpty() {
        return (size == 0);
    }
    
    /* number of points in the set
     * 
     */
    public int size() {
       return size; 
    }
    
    /* Add the point to the set (if it is not already in the set)
     * Nneed to implement contains, but first get insertion working
     * with proper orientations and without the complexity of rect.
     *  
     * recursive tree traversal: we use a private helper function
     * that returns a node, initially root = null, and the helper
     * returns a new first root at level 0. subsequent insertions
     * get the current root with which to recursively compare down 
     * the tree so as to determine where to insert a new node, 
     * unwinding back up to and returning the original root.
     * Comparing 2D value nodes alternates modulo 2 where 0 is a
     * vertical space partition, and 1 is a horizontal space partition:
     * 0%2=0 at root (vert), 1%2=1 (horiz) for the roots children,
     * 2%2=0 for the roots grandchildren and so on. inserting into
     * the root compares x coordinates, inserting into the roots
     * childrent compares y coordinates, inserting into grandchild
     * compares x again, etc...
     <code>
     // Insert key-value pair into BST
     // If key already exists, update with new value
     public void put(Key key, Value val) {
        if (val == null) { delete(key); return; }
        root = put(root, key, val);
        assert check();
     }

     private Node put(Node x, Key key, Value val) {
        if (x == null) return new Node(key, val, 1);
        int cmp = key.compareTo(x.key);
        if      (cmp < 0) x.left  = put(x.left,  key, val);
        else if (cmp > 0) x.right = put(x.right, key, val);
        else              x.val   = val;
        x.N = 1 + size(x.left) + size(x.right);
        return x;
     }
     </code>
     * 
     * size is tracked by the class and not in/summed over nodes
     * 
     * After inserting all the nodes with their orientation (level%2)
     * we deal with the space partitioning rectangles. we initially 
     * start with the unit square. 
     */
    public void insert(Point2D p) {
      //StdOut.println("Inserting: " + p);
      if (root == null ) { // root is initially null
          root = new Node(p,0,null,null,unitSquare);
          size = 1;
      } else if (!contains(p)) {
          //root = insert(root,p,root.level,root.rect);
          root = insert(root,p,root.level,root.rect);
          //root = insert(root,p,root.level,0.0,0.0,1.0,1.0);
      //} else {
      //    StdOut.println("Skipping : "+ p + " [already inserted]");
      }
    }
    
    /* insert helper method
     * rectHV is initially the unit square, and given the orientation
     * of the node and position of the point we can define a rect
     * region for the node. ie for parent rectangle, its children
     * split it vertically at p, the left node assuming the rect
     * [r.xmin,r.ymin,p.x   ,r.ymax] while the right node assumes
     * [p.x   ,r.ymin,r.xmax,r.ymax]. Horizontally the top node
     * [r.xmin,p.y   ,r.xmax,r.ymax] while the bottom node is
     * [r.xmin,r.ymin,p.y   ,r.ymax]. Note that the splitting p
     * is the parents p and not the p being inserted
     *  
     * NOTE: RectHV is a timing HOG
     */
    /*
    private Node insert(Node n, Point2D p, int level, RectHV r) {
        if ( n == null ) { // some branch is null, insert + track size
            //StdOut.println("Size: "+ size +" Level:"+ level +" Node: "+ n);
            size++;
            return new Node(p,level,null,null,r);
        }
        
        if (n.level % 2 == 0 ) { // vert space partition, compare x
            // smaller x coordinate than the point in the parent Node n
            if ( Point2D.X_ORDER.compare(p,n.p) < 0 ) { // go left
                RectHV rL = new RectHV(r.xmin(),r.ymin(),n.p.x(),r.ymax());
                n.lb = insert(n.lb, p, n.level+1, rL);
            } else { // go right
                RectHV rR = new RectHV(n.p.x(),r.ymin(),r.xmax(),r.ymax());
                n.rt = insert(n.rt, p, n.level+1, rR);
            }
        } else { // horiz space partition, compare y
            // smaller y coordinate than the point in the parent Node n
            if ( Point2D.Y_ORDER.compare(p,n.p) < 0 ) { // choose bottom (left)
                RectHV rB = new RectHV(r.xmin(),r.ymin(),r.xmax(),n.p.y());
                n.lb = insert(n.lb, p, n.level+1, rB);
            } else { // choose top (right)
                RectHV rT = new RectHV(r.xmin(),n.p.y(),r.xmax(),r.ymax());
                n.rt = insert(n.rt, p, n.level+1, rT);
            }
        }
        return n;
    }
    */
    private Node insert(Node n, Point2D p, int level, RectHV r) {
        return insert(n,p,level,r.xmin(),r.ymin(),r.xmax(),r.ymax());
    }
    
    // NOTE: constructing new Objects in a recursive stack is BAD, 
    // this memory is kept around and subsequently slows thing down.
    private Node insert(Node n, Point2D p, int level, double xMin, double yMin, double xMax, double yMax) {
        if ( n == null ) { // some branch is null, insert + track size
            //StdOut.println("Size: "+ size +" Level:"+ level +" Node: "+ n);
            size++;
            return new Node(p,level,null,null,xMin,yMin,xMax,yMax);
        }
        
        if (n.level % 2 == 0 ) { // vert space partition, compare x
            // smaller x coordinate than the point in the parent Node n
            if ( Point2D.X_ORDER.compare(p,n.p) < 0 ) { // go left
                //RectHV rL = new RectHV(r.xmin(),r.ymin(),n.p.x(),r.ymax());
                n.lb = insert(n.lb, p, n.level+1, xMin, yMin, n.p.x(), yMax);
            } else { // go right
                //RectHV rR = new RectHV(n.p.x(),r.ymin(),r.xmax(),r.ymax());
                n.rt = insert(n.rt, p, n.level+1, n.p.x(), yMin, xMax, yMax);
            }
        } else { // horiz space partition, compare y
            // smaller y coordinate than the point in the parent Node n
            if ( Point2D.Y_ORDER.compare(p,n.p) < 0 ) { // choose bottom (left)
                //RectHV rB = new RectHV(r.xmin(),r.ymin(),r.xmax(),n.p.y());
                n.lb = insert(n.lb, p, n.level+1, xMin, yMin, xMax, n.p.y());
            } else { // choose top (right)
                //RectHV rT = new RectHV(r.xmin(),n.p.y(),r.xmax(),r.ymax());
                n.rt = insert(n.rt, p, n.level+1, xMin, n.p.y(), xMax, yMax);
            }
        }
        return n;
    }
    
    /* does the set contain point p?
     * 
     */
    public boolean contains(Point2D p) {
        return contains(root,p);
    }
    
    private boolean contains(Node n, Point2D p) {
        if (n == null) return false;
        if (p.equals(n.p)) return true;
        
        if (n.level % 2 == 0 ) { 
            // smaller x coordinate than the point in the parent Node n
            if ( Point2D.X_ORDER.compare(p,n.p) < 0 ) { // go left
                return contains(n.lb,p);
            } else { // go right
                return contains(n.rt,p);
            }
        } else { // horiz space partition, compare y
            // smaller y coordinate than the point in the parent Node n
            if ( Point2D.Y_ORDER.compare(p,n.p) < 0 ) { // choose bottom (left)
               return contains(n.lb,p);
            } else { // choose top down (right)
                return contains(n.rt,p);
            }
        }
    }
    
    /* Return a Queue of points that can be iterated over
     * by traversing the tree in a breadth first search /
     * levelOrder traversal.
     * 
     * Java doesn't support default arguments inline
     * You need to overload the method. Here we support
     * passing a query point to also print out distance 
     */
    
    private void levelOrderPrint(Point2D queryP) {
        double dsq = -0.0;
        Queue<Node> q = new Queue<Node>();
        q.enqueue(root);
        while(!q.isEmpty()) {
            Node n = q.dequeue();
            if ( n == null ) continue;
            String orient = "-";
            if ( n.level % 2 == 0 ) orient = "|"; 
            if ( queryP != null ) dsq = n.p.distanceSquaredTo(queryP);
            StdOut.println(n.level +" : "+ n.p +" "+ orient +" "+ n.rect + " " + dsq);
            q.enqueue(n.lb);
            q.enqueue(n.rt);
        }
    }
    /*
    private Iterable<Point2D> levelOrderI() {
        Queue<Node> q = new Queue<Node>();
        Queue<Point2D> p = new Queue<Point2D>();
        q.enqueue(root);
        while(!q.isEmpty()) {
            Node n = q.dequeue();
            if ( n == null ) continue;
            p.enqueue(n.p);
            q.enqueue(n.lb);
            q.enqueue(n.rt);
        }
        return p;
    }


    private Iterable<Point2D> levelOrder() {
        Queue<Point2D> q = new Queue<Point2D>();
        levelOrder(root,q);
        return q;
    }
    
    private void levelOrder(Node x, Queue<Point2D> q) {
        if ( x == null ) return;
        q.enqueue(x.p);
        levelOrder(x.lb,q);
        levelOrder(x.rt,q);
    }
    */

    private Iterable<Node> levelOrderN() {
        Queue<Node> q = new Queue<Node>();
        levelOrderN(root,q);
        return q;
    }
    
    private void levelOrderN(Node n,Queue<Node> q) {
        if ( n == null ) return;
        q.enqueue(n);
        levelOrderN(n.lb,q);
        levelOrderN(n.rt,q);
    }
    
    /* Return a Queue of points that can be iterated over
     * by traversing the tree in a depth first search /
     * inOrder Traversal.
     */
    
    /*
    private Iterable<Point2D> inOrderI() {
        Stack<Node> s = new Stack<Node>();
        Queue<Point2D> p = new Queue<Point2D>();
        Node n = root;
        while(!s.isEmpty() || n != null) {
            // not null, push and go down left tree
            if (n != null) {
                s.push(n);
                n = n.lb;
            } else { 
            // no left child, pop, process the node,
            // point to the right branch.
                Node x = s.pop();
                p.enqueue(x.p);
                n = x.rt;
            }
        }
        return p;
    }

    private void inOrderPrint() {
        Node n = root;
        Stack<Node> s = new Stack<Node>();
        s.push(n);
        while(!s.isEmpty()) {
            // not null, push and go down left tree
            if (n != null) {
                s.push(n);
                n = n.lb;
            } else { 
            // no left child, pop, process the node,
            // point to the right branch.
                Node x = s.pop();
                String orient = "-";
                if ( x.level % 2 == 0 ) orient = "|";
                StdOut.println(x.level +":"+ x.p +" "+ orient);
                n = x.rt;
            }
        }
    }

    private Iterable<Point2D> inOrder() {
        Queue<Point2D> q = new Queue<Point2D>();
        inOrder(root,q);
        return q;
    }
    
    private void inOrder(Node x, Queue<Point2D> q) {
        if ( x == null ) return;
        inOrder(x.lb,q);
        q.enqueue(x.p);
        inOrder(x.rt,q);
    }
    */
    
    /* draw all points to standard draw. 
     * levelOrderN returns an iterable queue of nodes, which
     * is redundant and unnecessarily duplicates space since 
     * this class is the data structure maintainingthe data tree.
     * This is fine here, but for Range Search and Nearest
     * Neighbor search we should just hit each node once.
     * 
     * 
     */
    public void draw() {
        unitSquare.draw();
        for (Node n: levelOrderN()) {
            StdDraw.setPenRadius(0.0025);
            if (n.level % 2 == 0 ) {
              // dont draw rect, draw splitting line
              StdDraw.setPenColor(StdDraw.RED);
              Point2D l0 = new Point2D(n.p.x(),n.rect.ymin());
              Point2D l1 = new Point2D(n.p.x(),n.rect.ymax());
              n.p.drawTo(l0);
              n.p.drawTo(l1);
            } else {
              StdDraw.setPenColor(StdDraw.BLUE);
              Point2D l0 = new Point2D(n.rect.xmin(),n.p.y());
              Point2D l1 = new Point2D(n.rect.xmax(),n.p.y());
              n.p.drawTo(l0);
              n.p.drawTo(l1);
            }
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.01);
            n.p.draw();
        }   
    }
    
    /* All points that are inside the rectangle
     */
    public Iterable<Point2D> range(RectHV rect) {
        Queue<Point2D> q = new Queue<Point2D>();
        range(root,q,rect);
        return q;
    }

    /* Range search helper: Breadth First Search 
     * To find all points contained in a given query rectangle, start at the 
     * root and recursively search for points in both subtrees using the 
     * following pruning rule: if the query rectangle does not intersect the 
     * rectangle corresponding to a node, there is no need to explore that 
     * node (or its subtrees). A subtree is searched only if it might contain 
     * a point contained in the query rectangle.
     * 
     * RectHV::intersects will tell us if one rect is contained in another.

     */
    private void range(Node n, Queue<Point2D> q, RectHV r) {
      if (n == null) return;             // null node reached; backup
      if (!n.rect.intersects(r)) return; // query rect not in tree domain; prune
      if (r.contains(n.p)) q.enqueue(n.p);
      // BFS children
      range(n.lb,q,r);
      range(n.rt,q,r);
    }
    
    /* a nearest neighbor in the set to point p; null if the set is empty
     * 
     */
    public Point2D nearest(Point2D p) {
        if (isEmpty()) return null;
        return nearest(root,p,root.p,10.0);        
    }
    
    /* Nearest neighbor search helper: 
     * To find a closest point to a given query point, start at the root and 
     * recursively search in both subtrees using the following pruning rule: 
     * if the closest point discovered so far is closer than the distance 
     * between the query point and the rectangle corresponding to a node, 
     * there is no need to explore that node (or its subtrees). That is, a 
     * node is searched only if it might contain a point that is closer than 
     * the best one found so far. The effectiveness of the pruning rule depends 
     * on quickly finding a nearby point. To do this, organize your recursive 
     * method so that when there are two possible subtrees to go down, you 
     * always choose the subtree that is on the same side of the splitting 
     * line as the query point as the first subtree to explore—the closest 
     * point found while exploring the first subtree may enable pruning of 
     * the second subtree.
     */
    
    
    /* 
     * TODO: when points are on the boundary, contains appears to fail and
     * searches dont descend far enough. check horizontal8.txt
     *  
     * **** Always choose the subtree that on the same side of the splitting
     * line as the query point as the first subtree to explore
     * 
     * **** Timing tests fail when measuring 
     * RectHV.distanceSquaredTo() and Point2D.distanceSquaredTo()
     * since they were not originally used.
     *  
     * Taking the square root over and over to compare distance could potentially
     * slow things down when the measurement before the square root suffices.
     * 
     */
    /*
    private Point2D nearest(Node n, Point2D q, Point2D minPoint, double minDist) {
        //StdOut.println("minPt "+ minPoint +" "+ (float) minDist);
        Point2D ourPoint = n.p;
        double  ourDist  = ourPoint.distanceTo(q);
        //StdOut.println("ourPt "+ ourPoint +" "+ (float) ourDist +" "+ n.rect);
        
        if ( ourDist < minDist) { // we're closer than the current min
            if (n.lb!=null && n.lb.rect.contains(q)) { // only search this subdivision
                ourPoint = nearest(n.lb,q,ourPoint,ourDist);
            }
            if ( n.rt!=null && n.rt.rect.contains(q)) { // only search this subdivision
                ourPoint = nearest(n.rt,q,ourPoint,ourDist);
            }
        } else { // minPoint is still closer
            ourPoint = minPoint;
        }
        return ourPoint;
    }
    */

    /* Need To consider BOTH subtrees and used distSquaredTo
     * - return current minPoint if null node reached
     * - return current minPoint if this points distance isnt closer
     */
    private Point2D nearest(Node n, Point2D q, Point2D minPoint, double minDist) {
        //StdOut.println("minPt "+ minPoint +" "+ (float) minDist);
        
        if (n == null) return minPoint; // nothing here
        // like contains, but distanceSquaredTo is 0 for q inside, > 0 outside
        // we prune subtrees when q is outside, if q is inside it will be 0,
        // that is always smaller than minDist
        if (minDist < n.rect.distanceSquaredTo(q)) return minPoint;
        
        Point2D ourPoint = n.p;
        double  ourDist  = ourPoint.distanceSquaredTo(q);
        //StdOut.println("ourPt "+ ourPoint +" "+ (float) ourDist +" "+ n.rect);
        
        if ( ourDist <= minDist) { // we're closer than the current min
            ourPoint = nearest(n.lb,q,ourPoint,ourDist);
            ourPoint = nearest(n.rt,q,ourPoint,ourDist);
        } else { // minPoint is still closer
            ourPoint = minPoint;
        }
        return ourPoint;
    }

    /* unit testing of the methods (optional)
     * 
     */
    public static void main(String[] args) {
        // Read in a set of points from a file
        In file = new In(args[0]);
        KdTree points = new KdTree();
    
        
        while(!file.isEmpty()){
            double x = file.readDouble();
            double y = file.readDouble();
            points.insert(new Point2D(x,y));
        }
        //points.insert(new Point2D(0.5,0.5));
        points.draw();
        
        /*
        points.inOrderPrint();
        for (Point2D p: points.inOrder())
            StdOut.print(p);
        StdOut.println();
        for (Point2D p: points.inOrderI())
            StdOut.print(p);
        StdOut.println();
        */

        /*
        points.levelOrderPrint(null);

        for (Point2D p: points.levelOrder())
            StdOut.print(p);
        StdOut.println();
        for (Point2D p: points.levelOrderI())
            StdOut.print(p);
        StdOut.println();
        */
        
        /*
        Point2D q0 = new Point2D(0.5,1.0);
        Point2D q1 = new Point2D(0.0,0.0);
        StdOut.println("- contains: "+ q0 +" "+ points.contains(q0));
        StdOut.println("- contains: "+ q1 +" "+ points.contains(q1));
        */
        
        StdDraw.setPenRadius(0.006);
        StdDraw.setPenColor(StdDraw.GREEN);
        RectHV r0 = new RectHV(0.0,0.0,0.75,0.75);
        //RectHV r0 = new RectHV(0.25,0.25,1.0,1.0);
        StdOut.println("Range Search ----------------- "+r0);
        for (Point2D p: points.range(r0)) {
            StdOut.println(p);
            p.draw();
        }
        StdDraw.setPenRadius(0.001);
        r0.draw();


        StdDraw.setPenRadius(0.01);
        StdDraw.setPenColor(StdDraw.ORANGE);
        //Point2D pQuery = new Point2D(1.0,1.0);
        //Point2D pQuery = new Point2D(0.25,0.25);
        //Point2D pQuery = new Point2D(0.5,0.0);
        Point2D pQuery = new Point2D(0.5,0.9);
        //points.levelOrderPrint(pQuery);
        StdOut.println("Nearest Neighbor Search ------ "+pQuery);
        pQuery.draw();
        Point2D pNearest = points.nearest(pQuery);
        StdOut.println(pNearest);
        pNearest.draw();
        
    }

}
