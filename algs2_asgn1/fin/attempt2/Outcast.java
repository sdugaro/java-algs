
/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac Outcast.java
 * Execution:    java Outcast circle10.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *               
 *
 * Outcast detection: Given a list of wordnet nouns A1, A2, ..., An, 
 * which noun is the least related to the others? To identify an outcast, 
 * compute the sum of the distances between each noun and every other one:
 * di   =   dist(Ai, A1)   +   dist(Ai, A2)   +   ...   +   dist(Ai, An)
 * and return a noun At for which dt is maximum. 
 */

public class Outcast {
    
    // immutable
    private final WordNet WN;

    /* constructor takes a WordNet object
     * 
     */
    public Outcast(WordNet wordnet) {
        WN = wordnet; 
    }
    
    /* given an array of WordNet nouns, return an outcast
     */
    public String outcast(String[] nouns) {
        int maxDist = 0;
        String maxNoun = "";
        
        for (int i=0; i<nouns.length; i++) {
            int sum = 0;
            String Ai = nouns[i];
            for (int j = 0; j < nouns.length; j++) {
                if ( i != j ) {
                    sum += WN.distance(Ai,nouns[j]);
                }
            }
            if ( sum > maxDist) {
                maxNoun = Ai;
                maxDist = sum;
            }
        }
        return maxNoun;
    }
    
    /* test client
     * 
     */
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
        
}
