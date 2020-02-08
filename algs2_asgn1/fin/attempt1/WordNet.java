
/*************************************************************************
 * Name: Steve Dugaro
 * Email: 
 *
 * Compilation:  javac WordNet.java
 * Execution:    java WordNet synsets.txt hypernyms.txt
 * Dependencies: stdlib.jar for StdDraw.java, In.java, and StdOut.java
 *               algs4.jar  for Quick.java
 *               
 * Problem: WordNet groups words into sets of synonyms called synsets and
 * describes semantic relationships between them. One such relationship is 
 * the is-a relationship, which connects a hyponym (more specific synset) 
 * to a hypernym (more general synset). For example, a plant organ is a 
 * hypernym of carrot and plant organ is a hypernym of plant root.     
 * 
 * The WordNet digraph. each vertex v is an integer that represents a synset, 
 * and each directed edge v→w represents that w is a hypernym of v. The wordnet 
 * digraph is a rooted DAG: it is acyclic and has one vertex—the root—that is 
 * an ancestor of every other vertex. However, it is not necessarily a tree 
 * because a synset can have more than one hypernym.
 * 
 * The WordNet input file formats. We now describe the two data files that 
 * you will use to create the wordnet digraph. The files are in CSV format: 
 * each line contains a sequence of fields, separated by commas.
 * List of noun synsets. The file synsets.txt lists all the (noun) synsets 
 * in WordNet. The first field is the synset id (an integer), the second 
 * field is the synonym set (or synset), and the third field is its dictionary 
 * definition (or gloss). For example, the line
        36,AND_circuit AND_gate,a circuit in a computer that fires only when all of its inputs fire
 * means that the synset { AND_circuit, AND_gate } has an id number of 36 
 * and it's gloss is 'a circuit in a computer that fires only when all of 
 * its inputs fire'. The individual nouns that comprise a synset are separated 
 * by spaces (and a synset element is not permitted to contain a space). 
 * The S synset ids are numbered 0 through S − 1; the id numbers will appear 
 * consecutively in the synset file.
 *
 * List of hypernyms. The file hypernyms.txt contains the hypernym relationships: 
 * The first field is a synset id; subsequent fields are the id numbers of the 
 * synset's hypernyms. For example, the following line
        164,21012,56099
 * means that the the synset 164 ("Actifed") has two hypernyms: 
 * 21012 ("antihistamine") and 56099 ("nasal_decongestant"), representing that 
 * Actifed is both an antihistamine and a nasal decongestant. The synsets 
 * are obtained from the corresponding lines in the file synsets.txt.
        164,Actifed,trade name for a drug containing an antihistamine and a decongestant...
        21012,antihistamine,a medicine used to treat allergies...
        56099,nasal_decongestant,a decongestant that provides temporary relief of nasal...
 *   
 * Corner cases: All methods and the constructor shoud throw a 
 * java.lang.NullPointerException if any argument is null. The constructor should throw a 
 * java.lang.IllegalArgumentException if the input does not correspond to a rooted DAG. 
 * The distance() and sap() methods should throw a java.lang.IllegalArgumentException 
 * unless both of the noun arguments are WordNet nouns.
 * 
 * Performance requirements. Your data type should use space linear in the input size 
 * (size of synsets and hypernyms files). The constructor should take time linearithmic 
 * (or better) in the input size. The method isNoun() should run in time logarithmic 
 * (or better) in the number of nouns. The methods distance() and sap() should run in 
 * time linear in the size of the WordNet digraph. For the analysis, assume that the 
 * number of nouns per synset is bounded by a constant.
 * 
 * Measuring the semantic relatedness of two nouns: Semantic relatedness refers to the
 * degree to which two concepts are related. Measuring semantic relatedness is a 
 * challenging problem. For example, most of us agree that George Bush and John Kennedy 
 * (two U.S. presidents) are more related than are George Bush and chimpanzee (two primates). 
 * However, not most of us agree that George Bush and Eric Arthur Blair are related concepts. 
 * But if one is aware that George Bush and Eric Arthur Blair (aka George Orwell) are both 
 * communicators, then it becomes clear that the two concepts might be related.
 * We define the semantic relatedness of two wordnet nouns A and B as follows:
    distance(A, B) = distance is the minimum length of any ancestral path between any 
    synset v of A and any synset w of B.
 * This is the notion of distance that you will use to implement the distance() and sap() 
 * methods in the WordNet data type. 
 * 
 * REFERENCE:
 * 
 ** Read the sysnset/hypernym files once; 
 *  - io is expensive, store it in an good ds
 *  - use readLine from StdIn.In to read one line at a time
 *  - use Java.String.split() to divide a line into fields.
 *  - use Integer.parseInt() to convert string id numbers into integers.
 *  - check input is a rooted DAG not just a DG
 *  - dont store glosses (dictionary defns - not used)
 *  - a noun (2nd field) can appear in more than one synset (line in synsets.txt)
 *  - a synset can consist of exactly one noun and sever different synsets can
 *    consist of that same sungle noun.
 ** must use Digraph.java
 *
 ** A noun can appear in more than one synset 
 *  It will appear once for each meaning that the noun has. 
 *  For example, here are all of the entries in synsets.txt that include the noun word.
    35532,discussion give-and-take word,an exchange of views on some topic; "we had a good discussion"; "we had a word or two about it"
    56587,news intelligence tidings word,new information about specific and timely events; "they awaited news of the outcome"
    59267,parole word word_of_honor,a promise; "he gave his word"
    59465,password watchword word parole countersign,a secret word or phrase known only to a restricted group; "he forgot the password"
    81575,word,a string of bits stored in computer memory; "large computers use words up to 64 bits long"
    81576,word,a verbal command for action; "when I give the word, charge!"
    81577,word,a brief statement; "he didn't say a word about it"
    81578,word,a unit of language that native speakers can identify; "words are the blocks
 ** There can also be several different synsets that consist of the same nound
    62,Aberdeen,a town in western Washington  
    63,Aberdeen,a town in northeastern South Dakota  
    64,Aberdeen,a town in northeastern Maryland  
    65,Aberdeen,a city in northeastern Scotland on the North Sea
 ** This means we need to hash a list of ids on the key noun
 *   
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class WordNet {
    
    // immutable data type
    private final Map<Integer,String> synsetHash;
    private final Map<String,List<Integer>> nounHash; 
    private final SAP sap; // maintains digraph internally
    private final int numSynsets;
    
    private void printHash(Map<String,List<Integer>> h) {
        Object[] keys = h.keySet().toArray();
        Arrays.sort(keys);
        
        System.out.println("--------------["+h.size()+"]");
        for (Object key: keys) {
            List<Integer> ids = h.get(key);
            System.out.print(key + " : ");
            for (Integer i: ids) System.out.print(i + " ");
            System.out.println();
        }
    }
    
    /* read a synset file into our hash mapping keyed on nouns 
     * with one or more id representations for the noun.
     * synsets.txt lists all the nouns in the wordnet. each line
     * contains a unique id and a set of synonyms that correspond 
     * to that id. the same noun can have multiple ids representing 
     * it. The number of synsets corresponds to the number of
     * verticies in the graph. ie a meaning of a noun
     * 
     */
    private int importSynsets(String file) {
        int numSynsets = 0;
        In fd = new In(file);
        while (fd.hasNextLine()) {
            String line = fd.readLine();
            String[] lSplit = line.split(",");
            int id = Integer.parseInt(lSplit[0]);
            String synset = lSplit[1];
            String[] nouns = synset.split(" ");
            // for each unique id add it to the list of each noun key
            for (String n: nouns) {
                List<Integer> nids;
                if (!nounHash.containsKey(n)) {
                    nids = new ArrayList<Integer>();
                } else nids = nounHash.get(n);
                nids.add(id);
                nounHash.put(n,nids);
            }
            synsetHash.put(id, synset);
            numSynsets++;
        }
        fd.close();
        //printHash(nounHash);
        return numSynsets;
    }
    
    /* read a hypernym file into a Digraph connecting edges.
     * hypernyms.txt contains hypernym relationships where
     * the first field is a synset id, and subsequent fields are
     * the id numbers of the synset hypernyms; or graph edges.
     * 
     * If we cannot ensure that the digraph is a ROOTED DAG,
     * raise java.lang.IllegalArgumentException. A DAG cannot
     * contain any cycles, this is determined by running dfs
     * on the graph, specifically with a topological sort:
     * NO DIRECTED CYCLE (DAG) <=> DIGRAPH HAS A TOPILOGICAL ORDER
     * ROOTED means there is a single vertex (the root) that
     * is an ancestor of every other vertex; it cannot have
     * an outgoing edge; there are no vertices adjacent to it.
     * 
     */
    private void importHypernyms(String file, Digraph dg) {
        int singletons = 0;
        In fd = new In(file);
        while (fd.hasNextLine()) {
            String[] lSplit = fd.readLine().split(",");
            int v = Integer.parseInt(lSplit[0]);
            for (int i = 1; i<lSplit.length; i++) {
                int w = Integer.parseInt(lSplit[i]);
                dg.addEdge(v, w);
            }
            // check for a single root vertex (out degree 0)
            if (lSplit.length == 1) {
                singletons++;
                //StdOut.println("FOUND ROOT: v="+v + " " + synsetHash.get(v));
            }
        }
        fd.close();
        
        if (singletons != 1) {
            throw new java.lang.IllegalArgumentException("Unable to find root.");
        }
        Topological topological = new Topological(dg);
        if (!topological.hasOrder()) {
            throw new java.lang.IllegalArgumentException("Input is not a DAG");
        }
        /* root will be at the end of this list; not sure if i can reverse it
        for (Integer i: topological.order()) {
            StdOut.println(synsetHash.get(i));
        }
        */
    }

    /* constructor takes the name of the two input files
     * synsets are all the verticies of the form:  
     35532,discussion give-and-take word,the glossary definition
     * hypernyms is the list of directed edges connecting synset nouns by id:
     164,21012,56099
     * the nounds are not unique, but their ids are, which is how the edges
     * are connected.
     */
    public WordNet(String synsets, String hypernyms) {
        
        nounHash = new HashMap<String,List<Integer>>();
        synsetHash = new HashMap<Integer,String>();
        numSynsets = importSynsets(synsets);
        Digraph dg = new Digraph(numSynsets);
        importHypernyms(hypernyms,dg); // throws exception if not rooted DAG.
        sap = new SAP(dg);
        //sap.printGraph();
    }

    /* returns all WordNet nouns
     * keys in nounHash
     */
    public Iterable<String> nouns() {
        return nounHash.keySet();
    }

    /* is the word a WordNet noun?
     * yes, if its in the nounHash
     */
    public boolean isNoun(String word) {
        return nounHash.containsKey(word);
    }

    /* distance between nounA and nounB 
     * distance(A,B) = the minimum length of any ancestral
     * path between any synset v of A and any synset w of B
     * sap.length(Iterable<Integer> v, Iterable<Integer> w)
     * each noun maintains a list of synset integer ids.
     */
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new java.lang.IllegalArgumentException("nounA is not a noun.");
        if (!isNoun(nounB)) throw new java.lang.IllegalArgumentException("nounB is not a noun.");
        return sap.length(nounHash.get(nounA),nounHash.get(nounB)); 
    }

    /* a synset (second field of synsets.txt) 
     * that is the common ancestor of nounA and nounB
     * in a shortest ancestral path (defined below)
     */
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new java.lang.IllegalArgumentException("nounA is not a noun.");
        if (!isNoun(nounB)) throw new java.lang.IllegalArgumentException("nounB is not a noun.");
        int ancestor = sap.ancestor(nounHash.get(nounA), nounHash.get(nounB));
        return synsetHash.get(ancestor); // translate id back to synset
    }

    // do unit testing of this class
    public static void main(String[] args) {
        
        WordNet wn = new WordNet(args[0],args[1]);
        StdOut.println("white_marlin,milage [23]:" + wn.distance("white_marlin","milage"));
        StdOut.println("Black_Plague,black_marlin [33]:" + wn.distance("Black_Plague", "black_marlin"));
        StdOut.println("American_water_spaniel,histology [27]:" + wn.distance("American_water_spaniel", "histology"));   
        StdOut.println("Brown_Swiss,barrel_roll [29]:" + wn.distance("Brown_Swiss","barrel_roll"));
    }
    
}
