/*******************ec*****olor*************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac SeamCarver.java
 * Execution:    java SeamCarver circle10.txt
 * Dependencies: algs4.jar for StdDraw.java, In.java
 *               
 * Seam-carving is a content-aware image resizing technique where the image
 * is reduced in size by one pixel of height (or width) at a time. A vertical
 * seam in an image is a path of pixels connected from the top to the bottom
 * with one pixel in each row. (A horizontal seam is a path of pixels connected
 * from the left to the right with one pixel in each column.) 
 * Unlike standard content-agnostic resizing techniques (e.g. cropping and scaling), 
 * the most interesting features (aspect ratio, set of objects present, etc.) 
 * of the image are preserved.
 * 
 * 
 * Finding and removing a seam involves three parts and a tiny bit of notation:
 * 
 * Notation: 
 * In image processing, pixel (x, y) refers to the pixel in column x and row y, 
 * with pixel (0, 0) at the upper left corner and pixel (W − 1, H − 1) at the 
 * bottom right corner. This is consistent with the Picture data type in algs4.jar. 
 * Warning: this is the opposite of the standard mathematical notation used in 
 * linear algebra where (i, j) refers to row i and column j and with Cartesian 
 * coordinates where (0, 0) is at the lower left corner.
 * a 3-by-4 image:
      (0, 0)      (1, 0)      (2, 0)  
      (0, 1)      (1, 1)      (2, 1)  
      (0, 2)      (1, 2)      (2, 2)  
      (0, 3)      (1, 3)      (2, 3)  

 * We also assume that the color of a pixel is represented in RGB space, using 
 * three integers between 0 and 255. This is consistent with the java.awt.Color 
 * data type.
 * 
 * Energy calculation:
 * The first step is to calculate the energy of each pixel, which is a measure 
 * of the importance of each pixel—the higher the energy, the less likely that 
 * the pixel will be included as part of a seam (as we'll see in the next step). 
 * In this assignment, you will implement the dual gradient energy function, 
 * which is described below. The energy is high (white) for pixels in the image 
 * where there is a rapid color gradient. The seam-carving technique avoids 
 * removing such high-energy pixels.
 * 
 * Seam identification:
 * The next step is to find a vertical seam of minimum total energy. This is 
 * similar to the classic shortest path problem in an edge-weighted digraph 
 * except for the following: 
 * 1) The weights are on the vertices instead of the edges.
 * 2) We want to find the shortest path from any of the W pixels in the top row 
 *   to any of the W pixels in the bottom row.
 * 3) The digraph is acyclic, where there is a downward edge from pixel (x, y)
 * to pixels (x − 1, y + 1), (x, y + 1), and (x + 1, y + 1), assuming that the 
 * coordinates are in the prescribed range.
 * 
 * Seam removal:
 * The final step is to remove from the image all of the pixels along the seam.
 * 
 * Corner cases:
 * Your code should throw an exception when called with invalid arguments.
 * By convention, the indices x and y are integers between 0 and W − 1 and between 
 * 0 and H − 1 respectively, where W is the width of the curent image and H is 
 * the height. Throw a java.lang.IndexOutOfBoundsException if either x or y is 
 * outside its prescribed range. Throw a java.lang.NullPointerException if either
 * removeVerticalSeam() or removeHorizontalSeam() is called with a null argument.
 * Throw a java.lang.IllegalArgumentException if removeVerticalSeam() or 
 * removeHorizontalSeam() is called with an array of the wrong length or if the 
 * array is not a valid seam (i.e., either an entry is outside its prescribed 
 * range or two adjacent entries differ by more than 1). 
 * Throw a java.lang.IllegalArgumentException if either removeVerticalSeam() or 
 * removeHorizontalSeam() is called when either the width or height is less than
 * or equal to 1. 
 * 
 * Constructor: The data type may not mutate the Picture argument to the constructor.
 * 
 * 
 * Resources:
 * 
 * http://introcs.cs.princeton.edu/java/31datatype/Picture.java.html 
 * 
 * ArrayList supports dynamic arrays that can grow as needed. 
 * Standard Java arrays are of a fixed length. After arrays are created, they cannot grow 
 * or shrink, which means that you must know in advance how many elements an array will hold.
 * Array lists are created with an initial size. When this size is exceeded, the collection 
 * is automatically enlarged. When objects are removed, the array may be shrunk.
 * 
 * Vector implements a dynamic array. It is similar to ArrayList, but with two differences:
 * Vector is synchronized (thread safe - only one thread can exec an api method at a time)
 * Vector proves to be very useful if you don't know the size of the array in advance or you
 * just need one that can change sizes over the lifetime of a program.
 * 
 * For efficent image processing; nothing beats a 2D integer array.
 * 
 */

import java.awt.Color;
import java.lang.Math;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.IndexMinPQ;

public class SeamCarver {
    
    private int width;
    private int height;
    private int[] edgeTo;    // path to incoming edges on shortest path 
    private double[] distTo; // distance of shortest path from s to v
    private int[][] img;     // rgb array representing the current picture
    private IndexMinPQ<Double> pq; // priority queue of vertex keys/editable weights
    
   /* create a seam carver object based on the given picture
    * initialize image buffer from picture for processing.
    * 
    * OPTIMIZATIONS
    * 1. maintain color information in a 2D array in integers instead of
    *   private Vector<Vector<Color>> img;
    * While removing a pixel is made convenient with this data structure
    * as it will resize itself. This is only reasonable for small pics and
    * is otherwise terribly inefficient with memory; using 10x more mem for
    * a 20x20 pic to 22x more mem for a 200x200 pic. Note that in Java a
    * 2D array is an array of arrays;
    *   
    * Picture.get() returns an awt.Color object from which we can get the 
    * integer representation of the color in the sRGB color space:
    *   int Color.getRGB() 
    * we can also construct an awt.Color object from this int representation.
    * Note that Java does not have unsigned ints (excluding negative values)
    * 
    * 2. defer creating a Picture object until the client calls picture()
    * This mean we don't need to create a new Picture object after removing a
    * seam, we more efficiently work on img (the 2D integer resentation of pic)
    * removing seams and collapsing the array (System.arraycopy()) to shift the
    * elements within the array. When the pic is called we convert img ints to
    * awt.Colors in a newly created picture object, and return the reference to
    * our 'current' picture (or rather the last one requested) 
    *  
    * 3. Track the state of transpositions, and only transpose when the 
    * client asks to remove a different seam than currently being processed.
    * That is, if 50 horizontal seam removals are requested, you only need
    * to do 2 transposes (1 to get there 1 to get back) not 100 to always
    * be in the 'vertical' image state.
    * 
    */
   public SeamCarver(Picture picture) {
       if ( picture == null ) 
           throw new java.lang.NullPointerException("Picture is null");
	   
       width = picture.width();
       height = picture.height();
       img = new int[height][width];  // pixel(x,y) = (column[i],row[j])
       
       for (int i = 0; i < height; i++) {  
           for (int j=0; j < width; j++) {
               Color c = picture.get(j, i);
               img[i][j] = c.getRGB(); 
           }
       }       
   }
   
   /* current picture.
    * copy image buffer to Picture object and return for display
    * 
    * If our class is orientation aware in that we run vertical 
    * find/remove on both horizontal and vertical we should be 
    * aware of which state we are in to transpose before returning. 
    */
   public Picture picture() {

       Picture pic = new Picture(width,height); // w,h changes with scaling ops
       for (int i = 0; i < height; i++) {
           for (int j = 0; j < width; j++) {
               pic.set(j, i, new Color(img[i][j]));
           }
       }
       return pic;
   }
   
   /* width of current picture 
    */
   public int width() {
       return width;
   }
   
   /* height of current picture 
    */
   public int height() {
       return height;
   }
   
   /* energy of pixel at column x and row y
    * Test Client: PrintEnergy
    * 
    * Computing the energy of a pixel:
    * We will use the dual gradient energy function:
    * The energy of pixel (x, y) is sqrt(Δx^2(x, y) + Δy^2(x, y)), where the square
    * of the x-gradient Δx^2(x, y) = Rx(x, y)^2 + Gx(x, y)^2 + Bx(x, y)^2, and where
    * the central differences Rx(x, y), Gx(x, y), and Bx(x, y) are the differences
    * in the red, green, and blue components between pixel (x + 1, y) and pixel 
    * (x − 1, y). The square of the y-gradient Δy2(x, y) is defined analogously. 
    * 
    * We define the energy of pixels at the border of the image to be 1000 so that
    * it is strictly larger than the energy of any interior pixel.
    * 
    * As an example, consider the 3-by-4 image with RGB values (each component is
    * an integer between 0 and 255) as shown in the table below.
    (255,101,51)      (255,101,153)       (255,101,255)  
    (255,153,51)      (255,153,153)       (255,153,255)  
    (255,203,51)      (255,204,153)       (255,205,255)  
    (255,255,51)      (255,255,153)       (255,255,255)
    
    * The ten border pixels have energy 1000. Only the pixels (1, 1) and (1, 2) 
    * are nontrivial. We calculate the energy of pixel (1, 2) in detail:
    Rx(1, 2) = 255 − 255 = 0,
    Gx(1, 2) = 205 − 203 = 2,
    Bx(1, 2) = 255 − 51 = 204,
    yielding Δx^2(1, 2) = 2^2 + 204^2 = 41620.

    Ry(1, 2) = 255 − 255 = 0,
    Gy(1, 2) = 255 − 153 = 102,
    By(1, 2) = 153 − 153 = 0,
    yielding Δy^2(1, 2) = 102^2 = 10404.
    * Thus, the energy of pixel (1, 2) is sqrt(41620 + 10404) = sqrt(52024) = 288.1
    * 
    * @throws java.lang.IndexOutOfBoundsException if either x is outside of
    * [0,W-1] or y is outside of [0.H-1]
    * 
    */
   public  double energy(int x, int y) {
       
       if ( x < 0 || x > width -1 )
           throw new java.lang.IndexOutOfBoundsException("width out of bounds");
       if ( y < 0 || y > height - 1) 
           throw new java.lang.IndexOutOfBoundsException("height out of bounds");
       
       if ( x == 0 || y == 0 || x == width-1 || y == height-1 ) return 1000;
       
       Color x0 = new Color(img[y][x+1]);
       Color x1 = new Color(img[y][x-1]);
       Color y0 = new Color(img[y+1][x]);
       Color y1 = new Color(img[y-1][x]);
       
       int Rx = x0.getRed()   - x1.getRed();
       int Gx = x0.getGreen() - x1.getGreen();
       int Bx = x0.getBlue()  - x1.getBlue();
       int Ry = y0.getRed()   - y1.getRed();
       int Gy = y0.getGreen() - y1.getGreen();
       int By = y0.getBlue()  - y1.getBlue();
       
       double eX = Rx*Rx + Gx*Gx + Bx*Bx;
       double eY = Ry*Ry + Gy*Gy + By*By;
       
       return Math.sqrt(eX + eY);
   }

   /* relax the weight on the vertex w given DirectedEdge v to w,
    * and update accordingly. lookup the weight with the energy 
    * function where possible, boundary weight exists on top row
    * from source s=0 to boundary pixels w, weight from bottom
    * row of pixels to sink t=V are zero weight edges.
    */
   private void relax(int v, int w) {
       double weight = 0.0;
       if (w < 2+width*height - width - 1) {
       // lookup energy for vertex w shifted by virtual source
           weight = energy((w-1)%width,(w-1)/width);
       }
           
       if (distTo[w] > distTo[v] + weight) {
           distTo[w] = distTo[v] + weight;
           edgeTo[w] = v;
           // update or queue distance of relaxed vertex
           if (pq.contains(w)) pq.decreaseKey(w,distTo[w]);
           else                pq.insert(w,distTo[w]);  
       }
   }

   /* sequence of indices for vertical seam.
    * Test Client: PrintSeams.java
    * - returns an array of length H such that entry x is the column number
    *   of the pixel to be removed from row x of the image. 
    * - when there are multiple vertical seams with minimal total energy
    *   return any of them.
    * 
    * You can build edgeTo and distTo lookup tables without the need of a graph
    * due to the topological nature of the problem. For example for vertical seams,
    * the seam can only go down in three directions and therefore for each pixel
    * (vertex), you have at most 3 relax() calls. A graph data structure is 
    * unnecessary for the traversal and a 2d energy array can be traversed in 
    * order to determine the shortest path.
    * 
    * Topology: 3 downward children -> a topological order exists
    * DFS: take a top pixel (vertex) relax its 3 children to the bottom.
    * Shortest path in DAGs: How to choose which edge to relax?
    * 1) Dijkstras Algorithm (non-negative weights/relax IndexMinPQ)
    * 2) Bellman-Ford (any weight/relax every vertex w*h times)
    * 3) Topological sort (only relax every vertex one time)
    * - all directed edges point down -> picture is already in topological order
    * - relax all vertices on a row before starting the next row
    * 
    * Use a virtual vertex above connecting to top row of pixels to start
    * and a virtual vertex below connecting to bottom row of pixels to end.
    *  
    */
   public int[] findVerticalSeam() {
	   
       int V = 2 + width*height; // all pixels plus virtual top and bottom
       edgeTo = new int[V];       
       distTo = new double[V];   
       pq = new IndexMinPQ<Double>(V);  
       
       // initialize pseudo graph
       for (int i=0; i<V; i++)
           distTo[i] = Double.POSITIVE_INFINITY;
       distTo[0] = 0.0;
       
       pq.insert(0, distTo[0]); // initialize the source
       while (!pq.isEmpty()) {
           int v = pq.delMin(); // relax at most 3 adjacent DAG edges
           
           if (v == 0) { // top boundary 
               for( int i=1; i < width; i++) relax(0,i);
           } else if ( v >= (V - 1 - width)) { 
               // bottom boundary to sink
               relax(v,V-1);
           } else {
               relax(v,v+width); // 1 vertex down
               if ( v%width == 0) { 
               // right boundary only relax dn/left
                   relax(v,v+width-1);
                   continue;
               } else if ( v%width == 1 ) { 
               // left boundary only relax dn/right
                   relax(v,v+width+1);
                   continue;
               }
               // otherwise relax dn/right & left
               relax(v,v+width-1);
               relax(v,v+width+1);
           }
       }
       
       // Now from sink node trace back edgeTo entries to chain
       // shortest path vertex numbers;
       int v = V - 1; 
       Stack<Integer> vStack = new Stack<Integer>();
       while (edgeTo[v] != 0 ) {
           vStack.push(edgeTo[v]);
           v = edgeTo[v];
       }
       
       // translate the vertex number to its column number (offset by 1 
       // for source) where the array index represents the row number of
       // the image, the array entry represents the column number of the
       // image together defining a vertical seam. 
       int row = 0;
       int[] column = new int[height];
       while (!vStack.isEmpty()) {
           column[row] = (vStack.pop() - 1)%width;
           row++;
       }
       
       // *Free these large graph arrays* to save memory. We don't need to
       // hold a reference to them in our instance variables after were done.
       edgeTo = null;       
       distTo = null;   
       pq = null; 
       
       return column;
   }

   /* remove vertical seam from current picture
    * Typically called after findVerticalSeam, but should work for any seam.
    * 
    * A seam in an array of length H such that entry x is the column number
    * of the pixel to be removed from row x of the image. That is we traverse
    * the 2D img array with H rows, each row removes a column (pixel) and all
    * columns to the right in the row (remainder of the array are shifted left)
    * 
    * System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    * Copies an array from the specified source array, beginning at the specified
    * position, to the specified position of the destination array.
    * 
    * @throws java.lang.NullPointerException if called with a null argument
    * @throws java.lang.IllegalArgumentException if called with array of wrong length
    * @throws java.lang.IllegalArgumentException if not a valid seam 
    *  - an entry is outside its range or two adjacent entries differ by more than 1
    * @throws java.lang.IllegalArgumentException if width or height is <= 1
    * 
    *   
    * 
    */
   private void removeSeam(int[] seam) {
       for (int i=0; i < height; i++) {
    	   int col = seam[i]; // remove the pixel at col by shifting cols>col left
    	   int[] row = img[i];// get a reference to the 1D row array
    	   int num = width - col - 1; // number of elements to copy
    	   //System.out.printf("removing %d/%d copying %d\n",col,width,num);
    	   System.arraycopy(row, col+1, row, col, num);
       }
       width--;
   }
   
   public void removeVerticalSeam(int[] seam) {
       
       if ( seam == null ) 
           throw new java.lang.NullPointerException("seam is null");
       if ( width <= 1 ) 
           throw new java.lang.IllegalArgumentException("image width is <= 1");
       if ( seam.length != height ) 
           throw new java.lang.IllegalArgumentException("vertical seam length != height");

	   if ( seam[0] < 0 || seam[0] >= width )
		   throw new java.lang.IllegalArgumentException("seam entry -ve or > width");
       for( int i=1; i < seam.length; i++) { // check entries
    	   if ( seam[i] < 0 || seam[i] >= width )
    		   throw new java.lang.IllegalArgumentException("seam entry -ve or >= width");
           if ( Math.abs(seam[i-1]-seam[i]) > 1 ) // large difference in adjacent entries
               throw new java.lang.IllegalArgumentException("large seam difference");
       }   
       removeSeam(seam);
   }
   
   /* make pixel (i,j) (j,i) in our image making the width
    * the height and vice versa. img is initialized with height vectors
    * (rows) of length width. Transposing creates width vectors (rows)
    * of length height (cols); 0<=x<=width(cols) 0<=y<=height(rows)
    * Color x0 = new Color(img[y][x]); 
    * 
    * img = new int[height][width];  // pixel(x,y) = img[y][x] 
    * imgT = new int[width][height]; // pixel(x,y) = img[x][y]
    */
   private void transpose() {
	   int[][] imgT = new int[width][height];
	   
       for (int i=0; i < height; i++) {
           for (int j=0; j < width; j++) {
        	   imgT[j][i] = img[i][j];
           }
       }
       int temp = width;
       width = height;
       height = temp;
       img = imgT;
       
   }
   
   /* sequence of indices for horizontal seam
    * Test Client: PrintSeams.java
    * 
    * http://algs4.cs.princeton.edu/42directed/Topological.java
    * Based on topological sort algorithm for computing shortest path in a DAG
    * DO NOT create and EdgeWeightedDigraph, instead construct a 2d energy array
    * using SeamCarver::energy(). Traverse this matrix creating some select entries
    * as reachable from (x,y) to calculate where the seam is located.
    * 
    * http://algs4.cs.princeton.edu/24pq/IndexMinPQ.java
    * Supports change the key methods. client refers to keys with an integer
    * index between 0 and NMAX-1 associated with each key
    * 
    * The behavior of findHorizontalSeam() is analogous to that of findVerticalSeam()
    * except that it should return an array of length W such that entry y is the row
    * number of the pixel to be removed from column y of the image.
    * Simply transpose the image findVerticalSeam and transpose back.
    *
    */
   public int[] findHorizontalSeam() {
	   transpose();
       int[] seam = findVerticalSeam();
       transpose();
       return seam;
   }
   
   /* remove horizontal seam from current picture
    * Typically called with the output of findHorizontalSeam,
    * But should work for ANY seam.
    * 
    */
   public void removeHorizontalSeam(int[] seam) {
	   
       if ( seam == null ) 
           throw new java.lang.NullPointerException("seam is null");
       if ( height <= 1 ) 
           throw new java.lang.IllegalArgumentException("image height is <= 1");
       if ( seam.length != width ) 
           throw new java.lang.IllegalArgumentException("horizontal seam length != width");
       
       if ( seam[0] < 0 || seam[0] >= height )
		   throw new java.lang.IllegalArgumentException("seam entry -ve or >= height");
       for( int i=1; i < seam.length; i++) { // check entries
    	   if ( seam[i] < 0 || seam[i] >= height )
    		   throw new java.lang.IllegalArgumentException("seam entry -ve or > height");
           if ( Math.abs(seam[i-1]-seam[i]) > 1 ) // large difference in adjacent entries
               throw new java.lang.IllegalArgumentException("large seam difference");
       }

       transpose();
       removeSeam(seam);
       transpose();
   }
      
/*    
   public static void main(String[] args)
   {
       Picture inputImg = new Picture(args[0]);
       System.out.printf("image is %d pixels wide by %d pixels high.\n", inputImg.width(), inputImg.height());
       
       SeamCarver sc = new SeamCarver(inputImg);
       System.out.printf("img is %d pixels wide by %d pixels high.\n", sc.width(), sc.height());
       
       //sc.transpose();
       int[] seam = sc.findHorizontalSeam();
       sc.removeHorizontalSeam(seam);
       System.out.printf("transposed img is %d pixels wide by %d pixels high.\n", sc.width(), sc.height());

       seam = sc.findHorizontalSeam();
       sc.removeHorizontalSeam(seam);

       seam = sc.findVerticalSeam();
       sc.removeVerticalSeam(seam);

       seam = sc.findHorizontalSeam();
       sc.removeHorizontalSeam(seam);
       
       //Picture p = sc.picture();     
   }
*/   
}
