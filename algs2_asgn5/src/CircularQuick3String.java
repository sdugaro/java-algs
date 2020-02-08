/******************************************************************************
 *  Compilation:  javac Quick3string.java
 *  Execution:    java Quick3string < input.txt
 *  Dependencies: StdIn.java StdOut.java 
 *
 *  Reads string from standard input and 3-way string quicksort them.
 *
 *  % java Quick3string < shell.txt
 *  are
 *  by
 *  sea
 *  seashells
 *  seashells
 *  sells
 *  sells
 *  she
 *  she
 *  shells
 *  shore
 *  surely
 *  the
 *  the
 *
 * MODIFIED by sdugaro for Circular Suffix Array Radix Sorting
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom; 

/**
 *  The <tt>Quick3string</tt> class provides static methods for sorting an
 *  array of strings using 3-way radix quicksort.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/51radix">Section 5.1</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public class CircularQuick3String {
	
    private static final int CUTOFF =  15;   // cutoff to insertion sort
    
    private static int N;
    private static int[] index;

    // do not instantiate
    private CircularQuick3String() { } 

    /**  
     * Rearranges the array of strings in ascending order.
     *
     * @param a the array to be sorted
     */
    /* Original entry point
    public static void sort(String[] a) {
        StdRandom.shuffle(a);
        sort(a, 0, a.length-1, 0);
        assert isSorted(a);
    }
    */
    
    public static void sort(String s, int[] indices) {
    	N = s.length();
    	index = indices;
    	sort(s,0,N-1,0);
    }
 
    /* ORIGINAL
    // return the dth character of s, -1 if d = length of s
    private static int charAt(String s, int d) { 
        assert d >= 0 && d <= s.length();
        if (d == s.length()) return -1;
        return s.charAt(d);
    }
    */
    
    // return the dth character of s, -1 if d = length of s
    // i = suffix row of pseudo string array suffix grid, d = offset.
    // NOTE we have to check that our offset doesnt overrun the length
    // of the string or this could lead to StackOverflowError as comparisons
    // will keep recursing as the offset cycles around the string
    private static int charAt(String s, int i, int d) {    	
        if (d == s.length()) return -1; // stop condition
        return s.charAt((i+d)%N);
    }

    // 3-way string quicksort a[lo..hi] starting at dth character
    // d is the offset that respresents the pseudo string array
    // we look it up in the index array, we modify exch to s
    private static void sort(String a, int lo, int hi, int d) { 

        // cutoff to insertion sort for small subarrays
        if (hi <= lo + CUTOFF) {
            insertion(a, lo, hi, d);
            return;
        }

        int lt = lo, gt = hi;
        int v = charAt(a, index[lo], d);
        int i = lo + 1;
        while (i <= gt) {
            int t = charAt(a, index[i], d);
            if      (t < v) exch(lt++, i++);
            else if (t > v) exch(i, gt--);
            else              i++;
        }

        // a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi]. 
        sort(a, lo, lt-1, d);
        if (v >= 0) sort(a, lt, gt, d+1);
        sort(a, gt+1, hi, d);
    }

    /* ORIGINAL CUTOFF TO INTERTION
    // sort from a[lo] to a[hi], starting at the dth character
    private static void insertion(String[] a, int lo, int hi, int d) {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a[j], a[j-1], d); j--)
                exch(a, j, j-1);
    }
    */
    private static void insertion(String a, int lo, int hi, int d) {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a, j, j-1, d); j--)
                exch(j, j-1);
    }

    
    /* ORIGINAL EXCHNAGE FOR String[]
    // exchange a[i] and a[j]
    private static void exch(String[] a, int i, int j) {
        String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
    */
    // Since we are not exchaning actual strings and are sorting
    // an imaginary suffix grid of the source string, represented
    // by index, we swap the index values (references).
    // This corresponds to swapping actual strings
    private static void exch(int i, int j) {
        int temp = index[i];
        index[i] = index[j];
        index[j] = temp;
    }
    
    // is v less than w, starting at character d
    // DEPRECATED BECAUSE OF SLOW SUBSTRING EXTRACTION IN JAVA 7
    // private static boolean less(String v, String w, int d) {
    //    assert v.substring(0, d).equals(w.substring(0, d));
    //    return v.substring(d).compareTo(w.substring(d)) < 0; 
    // }

    // is v less than w, starting at character d 
    /* THE ORIGINAL STRING COMPARATOR 
    private static boolean less(String v, String w, int d) {
        assert v.substring(0, d).equals(w.substring(0, d));
        for (int i = d; i < Math.min(v.length(), w.length()); i++) {
            if (v.charAt(i) < w.charAt(i)) return true;
            if (v.charAt(i) > w.charAt(i)) return false;
        }
        return v.length() < w.length();
    }
    */
    
    // MODIFIED STRING COMPARATOR for CIRCULAR OFFSET String
    // NOTE: This method could keep keep recursing when all the chars are the same
    // as is the case for a UNARY alphabet. This could lead to StackOverflowError.
    // So we have to cutout if our offset d runs off the end of the string.
    // charAt() checks for this situation and returns -1 instead of i > 0;
    private static boolean less(String s, int i, int j, int d)
    {
        for (int k = d; k < N; k++)
        {
        	int c1 = charAt(s,index[i],k);
        	int c2 = charAt(s,index[j],k);
            if ( c1 < c2 ) return true;
            else if ( c1 > c2 ) return false;
        }
        return false; // reach here only when the s consists of only one character
    }

    /* no String[] to sort; not needed
    // is the array sorted
    private static boolean isSorted(String[] a) {
        for (int i = 1; i < a.length; i++)
            if (a[i].compareTo(a[i-1]) < 0) return false;
        return true;
    }
    */


    /**
     * Reads in a sequence of fixed-length strings from standard input;
     * 3-way radix quicksorts them;
     * and prints them to standard output in ascending order.
     */
    public static void main(String[] args) {

    	//String s = "ABRACADABRA!";
    	//String s = "AAAAAAAAAAAA"; // insertion cutoff < 15
    	String s = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    	//Test case: StackOverflowError
    	
    	N = s.length();
    	int[] index = new int[N];
    	for(int i=0; i<N; i++) index[i] = i;
    	sort(s,index);

        // print the results
        for (int i = 0; i < N; i++)
            StdOut.println(index[i]);
    }
}

/******************************************************************************
 *  Copyright 2002-2015, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
