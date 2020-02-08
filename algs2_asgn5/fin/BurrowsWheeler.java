/******************************************************************************
 * Name: Steve Dugaro 
 *
 * Compilation:  javac BurrowsWheeler.java
 * Execution:     java BurrowsWheeler - < abra.txt | java BurrowsWheeler +  
 * Dependencies: algs4.jar     
 *
 * The Burrows-Wheeler compression algorithm consists of three algorithmic
 * components, which are applied in succession: Note the difference between
 * ecoding and compression. 1&2 preps the data so it can be compressed well.
 * 1. Burrows-Wheeler transform encoding: Given a typical English text file,
 *    transform it into a text file in which sequences of the same character
 *    occur near each other many times.
 * 2. Move-to-front encoding: Given a text file in which sequences of the same
 *    character occur near each other many times, convert it into a text file
 *    in which certain characters appear more frequently than others.
 * 3. Huffman compression: Given a text file in which certain characters appear
 *    more frequently than others, compress it by encoding frequently occurring
 *    characters with short codewords and rare ones with long codewords.
 *  
 *    
 * The Burrows-Wheeler decompression algorithm applies the inverse operations
 * in reverse order.
 * 1. Huffman decompression
 * 2. Move-to-front decoding
 * 3. Burrows-Wheeler transform decoding
 * 
 *  
 *
 * Burrows-Wheeler transform. 
 * The goal of the Burrows-Wheeler transform is not to compress a message, 
 * but rather to transform it into a form that is more amenable to compression. 
 * The transform rearranges the characters in the input so that there are LOTS
 * OF CLUSTERS with repeated characters, but in such a way that it is still
 * possible to recover the original input. It relies on the following intuition:
 * if you see the letters 'hen' in English text, then most of the time the
 * letter preceding it is 't' or 'w'. If you could somehow group all such
 * preceding letters together (mostly t's and some w's), then you would have
 * an easy opportunity for data compression.
 * 
 * Burrows-Wheeler encoding. 
 * The Burrows-Wheeler transform of a string s of length N is defined as follows:
 * Consider the result of sorting the N circular suffixes of s. The Burrows-Wheeler
 * transform is the last column in the sorted suffixes array t[], preceded by the
 * row number first in which the original string ends up. Continuing with the 
 * "ABRACADABRA!" example above, we highlight the two components of the
 * Burrows-Wheeler transform in the table below.
 * 
 i     Original Suffixes          Sorted Suffixes       t    index[i]
--    -----------------------     -----------------------    --------
 0    A B R A C A D A B R A !     ! A B R A C A D A B R A    11
 1    B R A C A D A B R A ! A     A ! A B R A C A D A B R    10
 2    R A C A D A B R A ! A B     A B R A ! A B R A C A D    7
*3    A C A D A B R A ! A B R     A B R A C A D A B R A !   *0 (orig string)
 4    C A D A B R A ! A B R A     A C A D A B R A ! A B R    3
 5    A D A B R A ! A B R A C     A D A B R A ! A B R A C    5
 6    D A B R A ! A B R A C A     B R A ! A B R A C A D A    8
 7    A B R A ! A B R A C A D     B R A C A D A B R A ! A    1
 8    B R A ! A B R A C A D A     C A D A B R A ! A B R A    4
 9    R A ! A B R A C A D A B     D A B R A ! A B R A C A    6
10    A ! A B R A C A D A B R     R A ! A B R A C A D A B    9
11    ! A B R A C A D A B R A     R A C A D A B R A ! A B    2                                                      *
 * Since the original string ABRACADABRA! ends up in row 3, we have first = 3.
 * Thus, the Burrows-Wheeler transform is
3
ARD!RCAAAABB
 *
 * Notice how there are 4 consecutive As and 2 consecutive Bs — these clusters
 * make the message easier to compress. This is the message we write out in HEX.
 * 
% java BurrowsWheeler - < ../data/abra.txt
ARD!RCAAAABB
  * Wheres the 3? 0 Hex is not a char
% java BurrowsWheeler - < abra.txt | java edu.princeton.cs.algs4.HexDump 16
00 00 00 03 41 52 44 21 52 43 41 41 41 41 42 42
128 bits
 * Also, note that the integer 3 is represented using 4 bytes (00 00 00 03). 
 * The character 'A' is represented by hex 41, the character 'R' by 52, etc.
 * We only process 256 ASCII characters = 256 HEX chars from 00-FF.
 * Therefore the codeword width need only be 8 bits (2^8=256) / 1 byte blocks
 * 
 * NOTE: BinaryStdIn returns 8 bits as char (16-bit unsigned)
 * StdIn/StdOut reads sequences of unicode characters.
 * Use BinaryStIn/BinaryStdOut to read and write sequences of bytes.
 * Refer to Huffman.java for compress/expand examples ie:
BinaryStdOut::write(int x) {
// writes a 32 bit int into 4 bytes
// The unsigned right shift operator ">>>" shifts a zero into the leftmost position 
    writeByte((x >>> 24) & 0xff);
    writeByte((x >>> 16) & 0xff);
    writeByte((x >>>  8) & 0xff);
    writeByte((x >>>  0) & 0xff);
}
public static void write(char x) {
   if (x >= 0 && x < 256) writeByte(x);
}
public static String readString() {
    if (isEmpty()) throw new RuntimeException("Reading from empty input stream");

    StringBuilder sb = new StringBuilder();
    while (!isEmpty()) {
        char c = readChar();
        sb.append(c);
    }
    return sb.toString();
}    
 *
 *
 * Burrows-Wheeler decoding. 
 * Now, we describe how to invert the Burrows-Wheeler transform and recover the
 * original input string. If the jth original suffix (original string, shifted j
 * characters to the left) is the ith row in the sorted order, we define next[i]
 * to be the row in the sorted order where the (j + 1)st original suffix appears.
 * For example, if first is the row in which the original input string appears,
 * then next[first] is the row in the sorted order where the 1st original suffix
 * (the original string left-shifted by 1) appears; next[next[first]] is the row
 * in the sorted order where the 2nd original suffix appears; next[next[next[first]]]
 * is the row where the 3rd original suffix appears; and so forth.
 *    first = 3 : row in sorted order where original    string appears  (0 original)
 *  next[3] = 7 : row in sorted order where original<<1 string apppears (1 original)
 *  next[7] = 11: row in sorted order where original<<2 string appears  (2 original)
 * next[11] = 2 : row in sorted order where original<<3 string appears  (3 original)
 * etc...
 *               i = row of original suffixes (mapping)
 *    index[0] = 3  first
 *    index[1] = 7  n[first]
 *    index[2] = 11 n[n[first]]
 *    index[3] = 4  n[n[n[first]]]
 *    index[4] = 8  n[n[n[n[first]]]
 *    index[5] = 5  n[n[n[n[n[first]]]]
 *    index[6] = 9  n[n[n[n[n[n[first]]]]]
 *    index[7] = 2  n[n[n[n[n[n[n[first]]]]]]
 *    index[8] = 6  n[n[n[n[n[n[n[n[first]]]]]]]
 *    index[9] = 10 n[n[n[n[n[n[n[n[first]]]]]]]]
 *    index[10]= 1  n[n[n[n[n[n[n[n[n[first]]]]]]]]
 *    index[11]= 0  n[n[n[n[n[n[n[n[n[n[first]]]]]]]]
 * The CircularSuffixArray.index[] tracks the original row given the sorted row.
 * Inversely we want the original row number, given the sorted row.
 * ! building the next array given a row i of the sorted suffixes we can can
 *   examine the next index in this sequence to build up the next array. ie
 *   next[0] = 3
 *   next[1] = 0
 *   next[2] = 6
 *   next[3] = 7
 *   next[4] = 8
 *   next[5] = 9
 *   next[6] = 10
 *   next[7] = 11
 *   next[8] = 5
 *   next[9] = 2
 *   next[10]= 1
 *   next[11]= 4
 * ! the thing is we don't have index[i], but do have t[] (Burrow-Wheeler Xform)
 *   so can we extract index[i] from t???
 * 
 * Decoding the message given t[], first, and the next[] array: 
 * The input to the Burrows-Wheeler decoder is the last column t[] of the sorted 
 * suffixes along with first. From t[], we can deduce the first column of the
 * sorted suffixes because it consists of precisely the same characters, but in
 * sorted order.
 * 
 i      Sorted Suffixes     t      next
--    -----------------------      ----
 0    ! ? ? ? ? ? ? ? ? ? ? A        3
 1    A ? ? ? ? ? ? ? ? ? ? R        0
 2    A ? ? ? ? ? ? ? ? ? ? D        6
*3    A ? ? ? ? ? ? ? ? ? ? !        7
 4    A ? ? ? ? ? ? ? ? ? ? R        8
 5    A ? ? ? ? ? ? ? ? ? ? C        9
 6    B ? ? ? ? ? ? ? ? ? ? A       10
 7    B ? ? ? ? ? ? ? ? ? ? A       11
 8    C ? ? ? ? ? ? ? ? ? ? A        5
 9    D ? ? ? ? ? ? ? ? ? ? A        2
10    R ? ? ? ? ? ? ? ? ? ? B        1
11    R ? ? ? ? ? ? ? ? ? ? B        4
 *
 * Now, given the next[] array and first, we can reconstruct the original input
 * string because charAt[0] of the ith original suffix is the ith character in
 * the input string. That is, Column 0 of the cycled string is the same as the
 * original input string. In the example above, since first = 3, we know that 
 * the original input string appears in row 3; thus, the original input string
 * starts with 'A' (and ends with '!' from t[]). Since next[first] = next[3] = 7,
 * the next original suffix appears in row 7; thus, the next character in the
 * original input string is 'B'. Since next[next[first]] = next[7] = 11, the 
 * next original suffix appears in row 11; thus, the next character in the 
 * original input string is 'R'. Constructing the next[] array from t[] and 
 * first. Amazingly, the information contained in the Burrows-Wheeler transform
 * suffices to reconstruct the next[] array, and, hence, the original message! 
 * 
 * Here's how. It is easy to deduce a next[] value for a character that appears
 * !exactly once! in the input string. For example, consider the suffix that
 * starts with 'C'. By inspecting the first column, it appears 8th in the sorted
 * order. The next original suffix after this one will have the character 'C' as
 * its last character. By inspecting the last column, the next original appears
 * 5th in the sorted order. Thus, next[8] = 5. Similarly, 'D' and '!' each occur
 * only once, so we can deduce that next[9] = 2 and next[0] = 3.
 * 
 i      Sorted Suffixes     t      next
--    -----------------------      ----
 0    ! ? ? ? ? ? ? ? ? ? ? A        3
 1    A ? ? ? ? ? ? ? ? ? ? R        *
 2    A ? ? ? ? ? ? ? ? ? ? D
*3    A ? ? ? ? ? ? ? ? ? ? !
 4    A ? ? ? ? ? ? ? ? ? ? R        * * encode MDS sorted so s[1] < s[4]
 5    A ? ? ? ? ? ? ? ? ? ? C
 6    B ? ? ? ? ? ? ? ? ? ? A
 7    B ? ? ? ? ? ? ? ? ? ? A
 8    C ? ? ? ? ? ? ? ? ? ? A        5
 9    D ? ? ? ? ? ? ? ? ? ? A        2
10    R ? ? ? ? ? ? ? ? ? ? B        # # encode MSD sorted so s[10] < s[11]
11    R ? ? ? ? ? ? ? ? ? ? B        #
 *
 * But what about characters that appear more than once? We need to resolve this
 * ambiguity. The key rule is that If sorted row i and j both start with the same
 * character and i < j, then next[i] < next[j]. This rule implies next[10] = 1 
 * and next[11] = 4. Why is this rule valid? The rows are sorted so row 10 is 
 * lexicographically less than row 11. Thus the ten unknown characters in row 10
 * must be less than the ten unknown characters in row 11 (since both start with
 * 'R'). We also know that between the two rows that end with 'R', row 1 is less
 * than row 4. But, the ten unknown characters in row 10 and 11 are precisely the
 * first ten characters in rows 1 and 4. Thus, next[10] = 1 and next[11] = 4 or
 * this would contradict the fact that the suffixes are sorted.
 * ! the rows are cyclic, sorted or not, shifting row 10 R??????B left has to 
 * give us row 1 because sorting put it there.
 * 
 * So.. as we read in t[i] we maintain a lexicographically ordered symbol table
 * (by chars we read in) and track the positions in which we read them in a queue
 * for each char. when this is done we know how much mem to allocate for next[]
 * O(N) and simply unload all the queues we built in symbol table order.
 * 
 * ST (Transform Table)
 * [!]-[3]               next[i] = [3 0 6 7 8 9 10 11 5 2 1 4]
 * [A]-[0 6 7 8 9]
 * [B]-[10 11]
 * [C]-[5]
 * [D]-[2]
 * [R]-[1 4]
 * 
 * Our Table could be |R| (256) but its not necessary to allocate the space or
 * think about the Alphabet so much. We could also use a regular hash table and
 * sort the keys, afterwards, but why add O(NlgN) if we could/need to go linear.
 * Need A Binary Search Tree SymbolTable or better
 * - worst case w/ BST keys are already sorted so we end up with a list and d=N
 * - best case  w/ BST tree is perfectly balanced so d=ln(N) (log base 2)
 * - other tree based implementations of SymbolTables are probably smart enough
 *   to self balance when needed... Either way a tree ST is needed to preserve 
 *   the lexicographic ordering of the character keys: 
 * - java.util.TreeMap:
 *   - Red-Black Tree: sorted by natural ordering of keys
 *   - Gurarnteed log(N) for get() (as well as contains(),put(),remove())
 *   - not synchronized
 * - edu.princeton.cs.algs4.RedBlackBST
 * Also need a Queue to track positions of the data read in
 * - edu.princeton.cs.algs4.Queue:: enqueue(@tail)/dequeue(@head)
 * - java.util.Queue:: add(@tail)/remove(@head) JUST AN INTERFACE!!!
 *   - need to instantiate a concrete implementation of the interface (LinkedList) 
 *   
 * Now to reconstruct/decode the string we need the lexicographically sorted char
 * array corresponding to t. first tells us the row of the sorted suffix grid our
 * original string is on. the first column is sorted[t[i]] the last column is t[i]
 * we have these two columns now, so we know the first and last char of the original
 * message... and now need to reconstruct whats in between using the next[] array
 * 
 *   first = 3
 * next[i] = [3 0 6 7 8 9 10 11 5 2 1  4 ]
 *    t[i] = [A R D ! R C A  A  A A B  B ]
 *    s[i] = [! A A A A A B  B  C D R  R ]
 *      i     0 1 2 3 4 5 6  7  8 9 10 11
 * 
 i    s[]                  t[]    next[]
--    ----------------------------------
 0    ! ? ? ? ? ? ? ? ? ? ? A        3  
 1    A ? ? ? ? ? ? ? ? ? ? R        0 
 2    A ? ? ? ? ? ? ? ? ? ? D        6
*3    A ? ? ? ? ? ? ? ? ? ? !        7
 4    A ? ? ? ? ? ? ? ? ? ? R        8
 5    A ? ? ? ? ? ? ? ? ? ? C        9
 6    B ? ? ? ? ? ? ? ? ? ? A       10
 7    B ? ? ? ? ? ? ? ? ? ? A       11
 8    C ? ? ? ? ? ? ? ? ? ? A        5
 9    D ? ? ? ? ? ? ? ? ? ? A        2
10    R ? ? ? ? ? ? ? ? ? ? B        1
11    R ? ? ? ? ? ? ? ? ? ? B        4
 *  
 *     3  = first   
 *   s[3] = A
 *   N[3] = 7  s[7] = B
 *             N[7] = 11  s[11] = R
 *                        N[11] = 4  s[4] = A
 *                                   N[4] = 8  s[8] = C
 *                                             N[8] = 5  s[5] = A
 *                                                       N[5] = 9 
 *  s[9] = D
 *  N[9] = 2  s[2] = A
 *            N[2] = 6   s[6] = B
 *                       N[6] = 10  s[10] = R
 *                                  N[10] = 1  s[1] = A
 *                                             N[1] = 0  s[0] = !
 *                                                       N[0] = 3  = first.
 *                                                       
 *  Steps = N = |t| = |s| = |N| = length(original encoded message)
 *                              
 * 
 * Performance requirements:
 * The running time of your Burrows-Wheeler encoder should be proportional to N + R
 * (or better) in the worst case, excluding the time to construct the circular suffix
 * array. The running time of your Burrows-Wheeler decoder should be proportional to
 * N + R (or better) in the worst case.
 * 
 * Test that input can be encoded and decoded as so 
% java BurrowsWheeler - < abra.txt | java BurrowsWheeler +
ABRACADABRA!
 * 
 * Complete bz2 compression & decompression ~ 1.2M -> ~ 400k over half the file size.
% java BurrowsWheeler - < ../data/mobydick.txt | java MoveToFront - | java edu.princeton.cs.algs4.Huffman - > /tmp/moby.txt.bw.m2f.huff
% java edu.princeton.cs.algs4.Huffman + < /tmp/moby.txt.bw.m2f.huff | java MoveToFront + | java BurrowsWheeler + > /tmp/moby.txt.bw.m2f.huff.huff.m2f.bw.txt
 * 
 */

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;
import edu.princeton.cs.algs4.StdOut;

import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.LinkedList;


/*
// DEBUG
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
*/

public class BurrowsWheeler
{
    /*
     * Apply Burrows-Wheeler encoding, reading from stdin and writing to stdout
     * Must be O(N+R) worst case.
     * 
     * Output the 'first' integer as 4 bytes followed by the HEX representation
     * of the transform column characters ( 1 byte each ).
     * 
     * > echo $CLASSPATH
     * /home/sdugaro/workspace/PLUGINS/algs4.jar:.
     *bin/> java BurrowsWheeler - < ../data/abra.txt | java edu.princeton.cs.algs4.HexDump 16
     * char[] input = s.toCharArray();
     */
    public static void encode()
    {
    	//long startTime = System.nanoTime();
    	
    	String s = BinaryStdIn.readString();
    	CircularSuffixArray csa = new CircularSuffixArray(s);
    	
    	/*
    	// Original cyclic grid from single string offset
    	StdOut.println("\nINPUT CYCLIC GRID");
    	for(int j=0; j< csa.length(); j++)
    	{// offset the index to cycle the array
    		StdOut.printf("%4d: ",j);
        	for(int i=0; i < csa.length(); i++)
        	{
        		StdOut.printf("%c ",s.charAt((i+j)%csa.length()));
        	}
    		StdOut.println();
    	}
    	
    	// Natural Ordering (Radix Index sorted) cyclic suffix sorted grid
    	StdOut.println("\nORDERED CYCLIC GRID");
    	for(int j=0; j< csa.length(); j++)
    	{// offset the index to cycle the array
    		int k = csa.index(j);
    		StdOut.printf("%4d: ",k);
        	for(int i=0; i < csa.length(); i++)
        	{
        		StdOut.printf("%c ",s.charAt((i+k)%csa.length()));
        	}
    		StdOut.println();
    	}
    	StdOut.println("\nBURROWS-WHEELER TRANSFORM");
    	*/
    	
    	// Burrows-Wheeler Transform t[] is the last column in the
    	// suffix sorted ordered cyclic grid, and first is the row
    	// corresponding to index[0] (how the string cyclicly shifted)
    	// We dont want to nest loops thats N^2, So first pass count
    	// until you find the 0th element. Thats first. O(N). And
    	// Second pass write out the chars in the last column. O(N)
    	int first = -1;
    	for(int i=0; i < csa.length(); i++)
    	{
    		if(csa.index(i) == 0)
    		{
    			first = i;
    			break;
    		}
    	}
    	//StdOut.printf("%d ",first);
    	
    	BinaryStdOut.write(first);
    	for(int i=0; i < csa.length(); i++)
    	{
    		int l = csa.length();
    		int k = csa.index(i);
    		char c = s.charAt((k+l-1)%l);
    		//StdOut.printf("%c ",c);
    		BinaryStdOut.write(c);
    	}
        BinaryStdOut.close();
    	/*
    	StdOut.println();
    	StdOut.println("\nElapsed Time is " + (System.nanoTime()-startTime)/1e6 + " msec");
    	*/	
    	
    }

    /* Apply Burrows-Wheeler decoding, reading from stdin and writing to stdout
     * 
     * from the 'first' integer and the Burrows-Wheeler Transform t[] of the
     * original string. We can build a next[] array that allows us to reconstruct
     * (ie decode) the original string. To build next[] and subsequently decode
     * t[] we need a sorted version of t[] which we'll call s[] 
     * 
     * We read the chars straight into a symbol table that maintains sorted order.
     * lg(N) to insert * N insertsions also O(NlogN). Each char key maintains a 
     * queue of positions in the stream the charater was read so that building 
     * char s[] from this data structure is straight forward and fast with 
     * concatenations via StringBuilder, just like BinaryStdIn.readString()
     * 
     * We could read the input all at once and convert it to a char array
     *    String t = BinaryStdIn.readString();
     *    char[] s = t.toCharArray();
     * But that keeps 2 copies of the data, both of which we wont be using plus
     * it would have to be sorted O(NlgN). Suggested that KEY INDEX COUNTING
     * Radix sort could be used to sort the char array t[] into s[]... This is
     * actually linear AND stable. O(N+R) in time and O(N+R) in space. NOT NlgN.
     * It uses ~ N+R additional space, ~11N+4R array access; R = 256. 
     * Note there isnt an implementation we can use in algs4 and we get this 
     * for free by reading directly into an ordered tree data strucuture.
     * In addition, the data read in is dequeued/freed into a char[] that we
     * actually use and the number of TreeMap keys equals the number of unique
     * characters seen, which is likely considerably less than R. 
     * 
     */
    public static void decode()
    {

    	// Read the (Burrows Wheeler Transform) t[] into a symbol table
        // Equivalently we could use Algs4::RedBlackBST<char> && Queue<int> here
    	Map<Character,Queue<Integer>> ST = new TreeMap<Character,Queue<Integer>>();
    	
    	int N = 0;
    	int first = BinaryStdIn.readInt();
        while (!BinaryStdIn.isEmpty()) {
            char c = BinaryStdIn.readChar();
            if(!ST.containsKey(c) )
            	ST.put(c,new LinkedList<Integer>());
            ST.get(c).add(N);
            N++;
        }
        BinaryStdIn.close();

        /* DEBUG
        StringBuilder sl = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sl.append("----------------------------------------------\n");
        sb.append(String.format("[Symbol Table] size=%d entries=%d\n",ST.size(),N));
        sb.append(sl.toString());
    	for (Map.Entry<Character,Queue<Integer>> entry : ST.entrySet())
    	{
    		char c = entry.getKey();
    		sb.append(String.format("H[%c]-[ ",c));
    		Queue<Integer> q = entry.getValue();
    		for(Integer i: q) sb.append(String.format("%d ", i));
    		sb.append("]\n");
    	}
    	sb.append(sl.toString());
        StdOut.println(sb.toString());
		StdOut.printf("%4s%4s%4s\n","i","s[]","n[]"); 
         */
        
        int i = 0;
        int[]  n = new int[N];   // next[] 
        char[] s = new char[N];  // sorted t[]
    	for (Map.Entry<Character,Queue<Integer>> entry : ST.entrySet())
    	{
    		char c = entry.getKey();
    		Queue<Integer> q = entry.getValue();
    	    while (q.peek() != null)
    	    {
    			s[i] = c;
    			n[i] = q.remove();
    			//StdOut.printf("%4d%4c%4d\n",i,s[i],n[i]);
    			i++;
    	    }
    	}
    	//StdOut.println(sl.toString());
    	//StringBuilder src = new StringBuilder();
    	
    	// Now Reconstruct the original word writing directly to stdout
    	// by following the nested next pointers.. 
    	for(int j=0; j < N; j++)
    	{
    		//src.append(s[first]);
    		BinaryStdOut.write(s[first]);
    		first = n[first];
    	}
    	//StdOut.println(src.toString());
    	BinaryStdOut.close();
    	
    }

    /* if args[0] is '+', apply Burrows-Wheeler decoding
     * if args[0] is '-', apply Burrows-Wheeler encoding
     * 
     * NOTE: 
     * == compares the REFERENCES to strings for equality
     * String.equals() compares the VALUES for equality
     * 'c' is a literal char "c" is a literal String
     * 
     */
    
    //DEBUG I/O Redirection via IDE for debug.
    //Must be configured prior to BinaryStdIn/Out as global
    //public static void main(String[] args) throws IOException    
    public static void main(String[] args)
    {
    	//String fileOut = "/tmp/out.txt";
    	//String fileIn = "data/abra.txt";
    	//String fileIn = "data/abra.txt.bwt"; // abra.txt encoded
    	//System.setIn(new FileInputStream(new File(fileIn)));
    	//System.setOut(new PrintStream(new File(fileOut)));
    	if( args.length == 0 ) return;
    	
    	String mode = args[0];
    	if( mode.equals("+") ) decode();
    	else if( mode.equals("-") ) encode();
    }
}
