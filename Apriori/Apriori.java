import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

class Apriori{
	public static void main(String[] argv){
		try{
			Apriori m = new Apriori(argv[0], Integer.valueOf(argv[1]), Double.valueOf(argv[2]), Double.valueOf(argv[3]));
		}catch(FileNotFoundException e){
			System.out.println(e.getMessage());
		}catch(IOException e){
			System.out.println(e.getMessage());
		}catch(Exception e){

		}
	}

	public static final String RECORD_DELIM = ",";
	public static final boolean CASE = false;
	public static final boolean DATA_VERBOSE = false;
	public static final boolean LARGE_VERBOSE = true;
	public static final boolean CANDIDATE_VERBOSE = false;
	public static final boolean AR_VERBOSE = true;
	
	private AprioriDataset dataset;
	private ArrayList<HashMap<HashSet<String>, Double>> largeSets;
	private HashMap<HashSet<String>, HashMap<HashSet<String>, Double>> assosiationRules;
	private double minSupport;
	private double minConfidence;
	private int k;

	public Apriori(String file, int k, double minS, double minC) throws InterruptedException, FileNotFoundException, IOException{	
		long startTime = System.nanoTime();
		readFile(file);
		this.k = k;
		minSupport = minS;
		minConfidence = minC;

		System.out.println("Number of records: " + String.valueOf(dataset.getSize()) + "\n");

		largeSets = new ArrayList<HashMap<HashSet<String>, Double>>();
		assosiationRules = new HashMap<HashSet<String>, HashMap<HashSet<String>,Double>>();
		largeSets.add(dataset.getInitialCandidateSets());
		collectLargeSets();
		collectAssosiationRules();
		System.out.println(String.valueOf((double)(System.nanoTime() - startTime)/1000) + "us");
	}

	private void readFile(String file) throws FileNotFoundException, IOException{
		File inputFile = new File(file);
		FileReader inputReader = new FileReader(inputFile);
		BufferedReader inputBuffer = new BufferedReader(inputReader);
		dataset = new AprioriDataset(inputBuffer, RECORD_DELIM, CASE);
		inputBuffer.close();
		inputReader.close();
	}

	private void collectAssosiationRules(){
		for (HashMap<HashSet<String>, Double> workingLargeSets : largeSets){
			for (HashSet<String> largeSet : workingLargeSets.keySet()){
				for (HashSet<String> subset : subsets(largeSet)){
					HashSet<String> lhs = cloneItemSet(largeSet);
					HashSet<String> rhs = cloneItemSet(largeSet);
					lhs.removeAll(subset);
					rhs.removeAll(lhs);
					if (!assosiationRules.containsKey(lhs))
						assosiationRules.put(lhs, new HashMap<HashSet<String>, Double>());
				 	if (!assosiationRules.get(lhs).containsKey(rhs)){
				 		double confidence = workingLargeSets.get(largeSet)/largeSets.get(lhs.size()-1).get(lhs);
				 		if (confidence >= minConfidence)
				 			assosiationRules.get(lhs).put(rhs, confidence);
					}
				}
			}
		}
		if (AR_VERBOSE)for (HashSet<String> lhs : assosiationRules.keySet())for (HashSet<String> rhs : assosiationRules.get(lhs).keySet())System.out.println(lhs.toString() + " => " + rhs.toString() + ": " + assosiationRules.get(lhs).get(rhs));		
	}

	private void collectLargeSets(){
		if (DATA_VERBOSE){System.out.println("-----Dataset-----");System.out.println(dataset.toString());}
		int i = 0;
		while(i < k){
			if (CANDIDATE_VERBOSE){System.out.println("\n-----|" + String.valueOf(i+1) + "|-Candidates-----");printItemSets(largeSets.get(i));}
			prune(i);
			if (largeSets.get(i).size() == 0)
				break;
			if (LARGE_VERBOSE){System.out.println("\n-----|" + String.valueOf(i+1) + "|-Large-----");printItemSets(largeSets.get(i));}
			if (i != k-1)
				grow(i);
			i++;
		}
		if (CANDIDATE_VERBOSE || LARGE_VERBOSE ||DATA_VERBOSE){System.out.println("-----------------\n");}
	}

	private void prune(int i){
		HashMap<HashSet<String>, Double> candidateSets = largeSets.get(i);
		ArrayList<HashSet<String>> smallSets = new ArrayList<HashSet<String>>();
		for (HashSet<String> candidateSet : candidateSets.keySet())
			if (candidateSets.get(candidateSet) < minSupport)
				smallSets.add(candidateSet);
			
		for(HashSet<String> smallSet : smallSets)
			candidateSets.remove(smallSet);
	}

	private void grow(int i){
		HashMap<HashSet<String>, Double> candidateSets = new HashMap<HashSet<String>, Double>();
		Set<HashSet<String>> prevLargeSets = largeSets.get(i).keySet();
		Set<HashSet<String>> oneLargeSets = largeSets.get(0).keySet();

		for(HashSet<String> prevLargeSet : prevLargeSets){
			//System.out.println("Prev LargeSet: " + prevLargeSet.toString());
			HashSet<String> candidateSet = cloneItemSet(prevLargeSet);

			for(HashSet<String> oneLargeSet : oneLargeSets){
				String newItem = null;
				for (String item : oneLargeSet)
					newItem = item;
				//System.out.println("Large Item: " + newItem);
				if (!prevLargeSet.contains(newItem)){
					candidateSet.add(newItem);
					if (!candidateSets.containsKey(candidateSet)){
						boolean downwardClosure = true;
						HashSet<String> closureSet = cloneItemSet(candidateSet);
						for(String item : candidateSet){
							closureSet.remove(item);
							if (!prevLargeSets.contains(closureSet)){
								downwardClosure = false;
								closureSet.add(item);
								break;
							}
							closureSet.add(item);
						}
						if (downwardClosure)
							candidateSets.put(cloneItemSet(candidateSet), (double)dataset.getSupport(candidateSet)/(double)dataset.getSize());
						//System.out.println("Candidate Set: " + candidateSet.toString());
					}
					candidateSet.remove(newItem);
				}
			}
		}
		//System.out.println(candidateSets);
		largeSets.add(candidateSets);
	}

	private ArrayList<HashSet<String>> subsets(HashSet<String> itemSet){
		ArrayList<HashSet<String>> subsetList = new ArrayList<HashSet<String>>();
		ArrayList<String> itemList = new ArrayList<String>(itemSet.size());
		for (String item : itemSet)
			itemList.add(item);

		for (int i = 1; i < (1<<itemSet.size()) - 1; i++){
			HashSet<String> subset = new HashSet<String>();
			for (int j = 0; j < itemSet.size(); j++){
				if (((i>>j) & 1) == 1)
					subset.add(itemList.get(j));
			}
			subsetList.add(subset);
		}
		return subsetList;
	} 

	private HashSet<String> cloneItemSet(HashSet<String> itemSet){
		HashSet<String> cloneSet = new HashSet<String>();
		for(String item : itemSet)
			cloneSet.add(item);
		return cloneSet;
	}
	
	private void printItemSets(HashMap<HashSet<String>, Double> itemSets){
		for (HashSet<String> itemSet : itemSets.keySet()){
			System.out.println(itemSet.toString() + ": " + String.valueOf(itemSets.get(itemSet)));
		}
	}
}