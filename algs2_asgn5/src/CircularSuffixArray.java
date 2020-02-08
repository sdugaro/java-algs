/******************************************************************************
 * Name: Steve Dugaro 
 *
 * Compilation:  javac CircularSuffixArray.java
 * Execution:     java CircularSuffixArray   
 * Dependencies: algs4.jar 
 *
 * Circular suffix array. 
 * 
 * To efficiently implement the key component in the Burrows-Wheeler transform,
 * you will use a fundamental data structure known as the circular suffix array,
 * which describes the abstraction of a sorted array of the N circular suffixes
 * of a string of length N. As an example, consider the string "ABRACADABRA!" 
 * of length 12. The table below shows its 12 circular suffixes and the result
 * of sorting them.

 i       Original Suffixes           Sorted Suffixes         index[i]
--    -----------------------     -----------------------    --------
 0    A B R A C A D A B R A !     ! A B R A C A D A B R A    11
 1    B R A C A D A B R A ! A     A ! A B R A C A D A B R    10
 2    R A C A D A B R A ! A B     A B R A ! A B R A C A D    7
 3    A C A D A B R A ! A B R     A B R A C A D A B R A !    0
 4    C A D A B R A ! A B R A     A C A D A B R A ! A B R    3
 5    A D A B R A ! A B R A C     A D A B R A ! A B R A C    5
 6    D A B R A ! A B R A C A     B R A ! A B R A C A D A    8
 7    A B R A ! A B R A C A D     B R A C A D A B R A ! A    1
 8    B R A ! A B R A C A D A     C A D A B R A ! A B R A    4
 9    R A ! A B R A C A D A B     D A B R A ! A B R A C A    6
10    A ! A B R A C A D A B R     R A ! A B R A C A D A B    9
11    ! A B R A C A D A B R A     R A C A D A B R A ! A B    2

 * We define index[i] to be the index of the original suffix that appears ith
 * in the sorted array. For example, index[11] = 2 means that the 2nd original
 * suffix appears 11th in the sorted order (i.e., last alphabetically).
 * 
 * Implement this api to provide the client access to the index[] values:
 * 
 * Corner cases.
 * - Throw a java.lang.NullPointerException if constructor argument is null 
 * - The method index() should throw a java.lang.IndexOutOfBoundsException
 * if i is outside its prescribed range (between 0 and N − 1).
 * - Unary Alphabet. aaaaax100 will fail with stack overflow. This isn't a
 * typical english word. but should still be handled.
 * 
 * Performance requirements.
 * - Your data type should use space proportional to N. The constructor should
 * take time proportional to N log N (or better) on typical English text; the
 * methods length() and index() should take constant time in the worst case. 
 * NOTE: the substring() method in Java 7.6+ takes time and space proportional
 * to the length of the substring. In other words, you cannot form the N circular
 * suffixes explicitly because that would take both quadratic time and space.
 *
 *-----------------------------------------------------------------------------
 * 
 * From lecture suffix arrays are used for keyword in context substring search
 * "Suffix Sorting": start @ char[i] & take rest of string 
 0 I T W A S B E S T I T W A S
 1 T W A S B E S T I T W A S
 2 W A S B E S T I T W A S
 3 A S B E S T I T W A S
 4 S B E S T I T W A S
 5 B E S T I T W A S
 6 E S T I T W A S
 7 S T I T W A S
 8 T I T W A S
 9 I T W A S
10 T W A S
11 W A S
12 A S
13 S
 * Sorting on the first char brings things you are searching for close together
 .
 0 I T W A S B E S T I T W A S   
 9 I T W A S
 .
 * This should be a linear time process, if each suffix is simply a pointer
 * back into the input string (like < Java7.6 String Data Type). That is, there
 * is one char[] in memory and each String Object (also what substring returns)
 * simply contains offset and length pointers to the char[] reference.
 * 
 * Warm up - Longest repeated substring: 
 * 1) suffix sort 2) sort to cluster 3) compute longest prefix of adjacent suffixes.
 * 
 public String LongestRepeatedSubstring(String s)
 {
   int N = s.length();
   String [] suffixes = new String[N];
   for( int i=0; i < N; i++) suffixes[i] = s.substring(i,N); // expect O(1)
   Arrays.sort(suffixes); // comparison based O(NlogN) dual-pivot quicksort
   
   String lrs="";
   for(int=0; i<N; i++) 
   {
     int len = leastCommonPrefixLength(suffixes[i],suffixes[i+1]);
     if( len > lrs.length()) lrs = suffixes[i].substring(0,len);
   }
   return lrs;
 }
 
 public String LongestCommonPrefixLength(String s,String t)
 {
   int N = Math.min(s.length(),t.length());
   for( int i=0; i<N;i++)
   {
     if( s.charAt(i) != t.charAt(i) return i;
   }
   return N;
 }
 *
 * Sorting:
 * 
 * - Based on forum discussions take MSD.java Radix sorting, use a cutoff value
 * of 15 and make adjustments to it to work with suffix arrays. Insertion sort
 * cutoff for small arrays is important. In this scenario MSD Radix sort beats
 * - 3WayQuickSort for strings but from lecture 3WayStringQuickSort: 
 *   - uses ~2NlnN character compares
 *   - avoids recomparing long common prefixes
 *   - short inner loop, in place, BUT NOT STABLE
 *   - cache friendly (uses adjacent memory)
 *   - MSD uses too much memory for count[]
 *   - MSD has overhead reinitializing count[] and aux[]
 *   - O(1.4WNlgN) time and O(log N + W) space 
 *   * 3-way String Radix  QuickSort is THE method of choice for sorting strings.
 *   - Quick3String.java uses insertion sort for subarrays of size 15 or less.
 * - Arrays.sort(Object[]) in Java Standard Library has cutoff to Insertion sort
 * for |array| < 7 ( worst case O(N^2). Dual Pivot Quicksort after that O(NlgN).
 * 
 * CircularSuffixArray Hints:
 * 
 * Be sure not to create explicit copies of the string (e.g., via the substring() 
 * method in Java's String data type) when you sort the suffixes. That would take
 * QUADRATIC SPACE. Instead for each suffix, you only need to keep an index that
 * indicates which character is the beginning of the suffix. This way you can 
 * build the N suffixes in linear time and space. Then sort this array of indices
 * -- It's just like sorting an array of reference (ie the actual strings!)
 * * This MEANS we shouldn't work with copies of strings or sort strings at all!
 * * We just need a CUSTOM Comparator that tells us how to sort the indices[].
 * * also from the forum
 private boolean less(String s, int i, int j, int d)
 {
     for (int k = d; k < N; k++)
     {
         if (s.charAt((i + k) % N) < s.charAt((j + k) % N)) return true;
         if (s.charAt((i + k) % N) > s.charAt((j + k) % N)) return false;
     }
     return false; // reach here only when the s consists of only one character
 }
 *
 *
 */

import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;
//import java.util.Comparator; 

public class CircularSuffixArray
{
	private final int N;
	private final int[] index;
	private final String source;
	
	/* Custom Comparator class for Arrays.sort(Object[]) 
	 * NOTE: Non-static nested classes (inner classes) have access to other members
	 * of the enclosing class, even if they are declared private. The case here.
	 * Static nested classes do NOT have access to members of the enclosing class.
	 *
	 * The natural order of suffix indices into a source string.
	 * Each char in the string has an index representing its posn in the string
	 * Putting these in a natural order amounts to the result of an MSD Radix Sort
	 * 
	 * NOTE: when looking up a char in a particular 'column' formed by the 
	 * Strings offset index plus the current column, we could run off the end
	 * of the source string, so we have to 'wrap/cycle' the char lookup index,
	 * and perform the char lookup modulo the length of the string. ie:
	 * s = ABCBD i = 0  c=4 i+c = 4%5 = 4 s[4] = D
	 * s'= BCBDA i = 1  c=4 i+c = 5%5 = 0 s[0] = A
	 * 
	 * NOTE: If the string of length N only contains 1 character the loop
	 * is still run N times for N-1 comparisons. We should somehow like to
	 * detect this case a-priori and not get here to have to sort at all.
	 * Perhaps absorb runs something along the lines of Tim Peters' Timsort
	 * 
	 * return +1 if i1 > i2, 0 if i1=i2, else -1
	 * 
	 * WORKS BUT THIS SORT METHOD IS 5X SLOWER THAN THE REFERENCE
	 */
    /*
	private class SuffixComparator implements Comparator<Integer>
	{
		public int compare(Integer i1, Integer i2)
		{
			int i;
			char c1,c2;
			for( i=0; i < N; i++)
			{
				c1 = source.charAt((i1+i)%N);
				c2 = source.charAt((i2+i)%N);
				//StdOut.printf("%d:",i);
				
				if (c1 > c2) {
					//StdOut.printf("%d [%s] > %d [%s]\n",i1,c1,i2,c2);
					return 1;
				}
				else if (c1 < c2) {
					//StdOut.printf("%d [%s] < %d [%s]\n",i1,c1,i2,c2);
					return -1;
				}
			}
			// DEBUG
			//c1 = source.charAt((i1+i)%N);
			//c2 = source.charAt((i2+i)%N);
			//StdOut.printf("%d [%s] = %d [%s]\n",i1,c1,i2,c2);
			return 0; // N compares for N length string
		}
	}
	*/

	/* circular suffix array of s constructor
	 * O(NlgN) worst case time on typical english text.
	 * O(N) space
	 * 
	 * AAAAAAAA* is not typical english text. unary alphabet.
	 * possible worst case performance issue. should not be sorted.
	 * N-1 compares, and each compare loops N times to get to the
	 * end of the string much like AAAAAAAA*B. also not english.
	 * Long runs should collapse to A or AB, but howto unpack indices
	 *  
	 * 
	 */
    public CircularSuffixArray(String s)
    {
    	if( s == null ) throw new NullPointerException("null input argument");
    	
    	source = s;
    	N = s.length();
    	index = new int[N];
    	
    	// For source String s, creating an index pointer for each char to amounts
    	// to the initial cyclic 2D grid above. A 'suffix sort' of this grid amounts
    	// to clustering/sorting each column by char, then sorting each cluster by 
    	// the next char and so on (to MSD Radix Sort). Sorting the index pointers
    	// amounts to sorting the Integer[] of indices by their 'natural order'
    	// That is defined by the comparator which looks up the characters given
    	// only their offset into the original string.
    	
    	/* This works but timing tests take about 5x longer than it should.
    	 * We also have duplicate int arrays one so we can use Integer with
    	 * java generics, the other to copy back the results. 
    	int i; 
    	Integer[] sOffsets = new Integer[N];
    	for(i=0; i< N; i++) sOffsets[i] = i;
		Arrays.sort(sOffsets, new SuffixComparator());
     	// now copy these 'sorted' offsets into the query index[]
		for(i=0; i<N; i++) index[i] = sOffsets[i];
    	*/

    	//Radix sort the Cyclic Grid where the index represents the
    	//offset of the original string. Initializing index to shift
    	//1 in each entry amounts the Original unsorted Offset grid.
    	for(int i=0; i<N; i++) index[i] = i;
    	CircularQuick3String.sort(source,index);
		
    }

    /* Custom comparator for sorting an array of reference (pointer offsets in a String)
     *
     * Note that java comparators and therefore Array.sort()ing wont accept primitive
     * types so the array of offsets needs to be an Integer
     */
        
    /* length of s: O(1) 
     */
    public int length()
    {
    	return N;
    }
    
    /* returns index of ith sorted suffix: O(1)
     */
    public int index(int i)
    {
    	if( i < 0 || i >= N ) throw new IndexOutOfBoundsException("argument out of range");
    	return index[i];
    }
    
    /* unit testing of the methods (optional)
     * 
     */
    public static void main(String[] args)
    {
    	String s = "ABRACADABRA!";
    	//String s = "ABCD!DCBA";
    	//String sa = "AAAAAA"; // time cutoff
    	//String s = sa+sa+sa+sa+sa; // shouldnt sort
    	long startTime = System.nanoTime();
    	// Returns the current value of the most precise available system timer, 
    	// in nanoseconds. This method can only be used to measure elapsed time
    	// and is not related to any other notion of system or wall-clock time.
    	
    	CircularSuffixArray csa = new CircularSuffixArray(s);
    	
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
    	
    	StdOut.println("\nElapsed Time is " + (System.nanoTime()-startTime)/1e6 + " msec");	
    }
}

