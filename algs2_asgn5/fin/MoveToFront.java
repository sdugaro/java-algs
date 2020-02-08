/******************************************************************************
 * Name: Steve Dugaro 
 *
 * Compilation:  javac MoveToFront.java
 * Execution:     java MoveToFront - < abra.txt | java MoveToFront +  
 * Dependencies: algs4.jar 
 *
 *
 * Move-to-front encoding and decoding. 
 * The main idea of move-to-front encoding is to maintain an ordered sequence of
 * the characters in the alphabet, and repeatedly read in a character from the
 * input message, print out the position in which that character appears, and
 * move that character to the front of the sequence. As a simple example, if the
 * initial ordering over a 6-character alphabet is A B C D E F, and we want to 
 * encode the input CAAABCCCACCF, then we would update the move-to-front sequences
 * as follows:

move-to-front    in   out
-------------    ---  ---
 A B C D E F      C    2 
 C A B D E F      A    1
 A C B D E F      A    0
 A C B D E F      A    0
 A C B D E F      B    2
 B A C D E F      C    2
 C B A D E F      C    0
 C B A D E F      C    0
 C B A D E F      A    2
 A C B D E F      C    1
 C A B D E F      C    0
 C A B D E F      F    5
 F C A B D E  
 
 * If the same character occurs next to each other many times in the input,
 * then many of the output values will be small integers, such as 0, 1, and 2.
 * The extremely high frequency of certain characters makes an ideal scenario
 * for Huffman coding.
 * 
 * Move-to-front encoding. (ASCII is 7 bit 2^7 [0-127]) 
 * Your task is to maintain an ordered sequence of the 256 EXTENDED ASCII
 * characters. Initialize the sequence by making the ith character in the
 * sequence equal to the ith extended ASCII character. Now, read in each 
 * 8-bit character c from standard input one at a time, output the 8-bit
 * index in the sequence where c appears, and move c to the front.
 * 
% java MoveToFront - < abra.txt | java  edu.princeton.cs.algs4.HexDump 16
41 42 52 02 44 01 45 01 04 04 02 26
96 bits
 *
 * Move-to-front decoding. 
 * Initialize an ordered sequence of 256 characters, where extended ASCII
 * character i appears ith in the sequence. Now, read in each 8-bit character
 * i (but treat it as an integer between 0 and 255) from standard input one
 * at a time, write the ith character in the sequence, and move that character
 * to the front. Check that the decoder recovers any encoded message.
 *
% java MoveToFront - < abra.txt | java MoveToFront +
ABRACADABRA!
 *
 * Performance requirements. 
 * The running time of move-to-front encoding and decoding should be
 * proportional to R N (or better) in the worst case and proportional
 * to N + R (or better) in practice on inputs that arise when compressing
 * typical English text, where N is the number of characters in the input
 * and R is the alphabet size.
 * 
 * ENCODING NOTES:
 * - Initially I thought to use algs4|BinarySearch::indexOf(int[] a, int key)
 * O(lgN) search hit. returns -1 if key is not found. But ran into strange
 * behavior it would find the first query, say A, but after A was moved to
 * front, secondary queries would return -1. java.util.Arrays.binarySearch
 * and lecture notes identify the problem as the input array needs to be 
 * SORTED for this work. otherwise its undefined. mutating the ascii array
 * via move to front invalidates the usage of this algorithm. so discarded.
 * Was convenient to dump array with StdOut.println(Arrays.toString(ascii));
 * - Thought java.util.Arrays might have an indexOf, but you would have to
 * convert it to a list first, which points to just using alist.
 * java.util.Arrays.asList(theArray).indexOf(o) 
 * -java.util.LinkedList implements the Dequeue interface which has:
 * indexOf(),remove(index),addFirst/push(),set(index,element),add() to end
 * All the api methods we need. We dont instantinate the base class List
 * since that doesnt have the same interface and/or implementation of these.
 * Note that with lists we dont allocate all the memory beforehand, as we
 * would with an array. This means the memory is not-contiguous, but we
 * dont need to do any bubbling here which is also a plus.
 *
 * DECODING NOTES:
 * - could use a linked list again for this. but the index we are reading
 * points us directly to the charater in the table we want each time O(1).
 * That is we dont have to traverse list pointers to get there O(N/2). 
 * Moving to front is also straight forward, as we just need to shift the
 * elements right by 1, and index is the number of elements to do that to,
 * which gives us everything we need for System.arraycopy(s,0,s,1,index),
 * cache the character, write it to the front and we're done.
 *
 * CORNER CASE:
 * - if the input is a, the output should not be length 1 rather, length 13
 * 
 */

import java.util.LinkedList;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;
    

// DEBUG
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.util.Arrays; // for dumping array contents

public class MoveToFront
{
	private static final int R = 256; // alphabet size
	private static final int W = 8;   // codeword width
	
    /* apply move-to-front encoding, reading from stdin and writing to stdout
     * 
     * Read a char from stdin; find corresponding index in the extended ascii
     * table, write this index to stdout, exchange left neighbours with c to
     * the front of the array. loop for all chars provided in std in. 
     * 
     * This outputs a mapping of "runs" of characters in the input, so Huffman
     * coding can know how to codify more frequently occurring characters
     * with fewer bits.
     * 
     * There are only 256 (2^8 possible) index values > 0.
     * These can be uniquely identified with 8 bits, so we output as such
     * Note that an int is 32 bit signed, and we need to write 8 bit unsigned.
     * 
     *  Note that for a run of the same character, the character will already
     *  be at the front, so avoid removing it and putting back in the same 
     *  place. We could Character tmp = remove(idx) but we dont use this value
     *  for anything so theres no need.
     */
    public static void encode()
    {    	
    	LinkedList<Character> ascii = new LinkedList<Character>();
    	for(int i=0; i < R; i++) ascii.add((char)i);
    	//StdOut.println(ascii);

    	int idx;
    	char key;
        while (!BinaryStdIn.isEmpty())
        {
            key = BinaryStdIn.readChar();
            idx = ascii.indexOf(key); // could return -1
            //StdOut.printf("in[%d|%c] indexof[%d]\n",(int)key,key,idx);
            BinaryStdOut.write(idx,W); // least sig 8 bits of the 32-bit int
            if( idx > 0 ) ascii.push(ascii.remove(idx));  
            //StdOut.println(ascii);
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
        
    }

    /* apply move-to-front decoding, reading from stdin and writing to stdout
     * 
     * Read in each 8-bit character i (treated as an integer between 0 and 255)
     * write the character for that index in the ascii table, and bubble that
     * character to the front of the table.
     * 
     * Note that a Java char is 16bit, covering 2^16(0-65535) unicode chars
     * but it is unsigned so doing subtraction with it will produce bad values.
     * Since we can keep everything positive, we can use char instead of int
     * to use half the space.
     * 
     * The input provids us with the index to lookup the character directly
     * we write and cache this. The index is also the distance from the front
     * of the ascii table, or the number of elements we need to shift right
     * to complete the move to front, and sync with the next read character.
     *  
     */
    public static void decode()
    {
    	char[] ascii = new char[R];
    	for(int i=0; i<R; i++) ascii[i]=(char)i;
    	//StdOut.println(Arrays.toString(ascii));
    
    	char idx,tmp;
        while (!BinaryStdIn.isEmpty())
        {
            idx = BinaryStdIn.readChar();
            tmp = ascii[idx];
            BinaryStdOut.write(tmp,W);
            //StdOut.printf("in[%d|%c]\n",(int)idx,tmp);
            
            // Shift all values right, and put tmp in front
            System.arraycopy(ascii,0,ascii,1,idx);
            ascii[0] = tmp;
        	//StdOut.println(Arrays.toString(ascii));
        }
        BinaryStdIn.close();
        BinaryStdOut.close();
    }

    /* if args[0] is '+', apply Move-To-Front decoding
     * if args[0] is '-', apply Move-To-Front encoding
     * 
     * NOTE:
     * == compares the REFERENCES to strings for equality
     * String.equals() compares the VALUES for equality
     * 'c' is a literal char "c" is a literal String
     * possible there could be no args. do nothing. 
     */ 
    
    //DEBUG I/O Redirection via IDE for debug.
    //Must be configured prior to BinaryStdIn/Out as global
    //public static void main(String[] args) throws IOException    
    public static void main(String[] args)
    {
    	//String fileOut = "/tmp/out.txt";
    	//String fileIn = "data/abra.txt";
    	//String fileIn = "data/stars.txt"; 
    	//String fileIn = "data/abra.txt.mtf"; // abra.txt encoded
    	//System.setIn(new FileInputStream(new File(fileIn)));
    	//System.setOut(new PrintStream(new File(fileOut)));
    	
    	if( args.length == 0 ) return;
    	String mode = args[0];
    	
    	//String mode = "-";
    	if( mode.equals("+") ) decode();
    	else if( mode.equals("-") ) encode();
    }
}
