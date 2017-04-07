import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;

class AprioriDataset{
	private HashSet<String> items;
	private ArrayList<HashSet<String>> itemSets;
	private HashMap<HashSet<String>, Double> itemSupports;
	private int size;
	
	public AprioriDataset(BufferedReader input, String recordDelim, boolean caseSensitive) throws IOException{
		items = new HashSet<String>();
		itemSets = new ArrayList<HashSet<String>>();
		itemSupports = new HashMap<HashSet<String>, Double>();
		String line;
		while ((line = input.readLine()) != null){
			String[] itemStrings = line.split(recordDelim);
			addItemSet(itemStrings, caseSensitive);
		}
		size = itemSets.size();
		for (HashSet<String> itemSet : itemSupports.keySet())
			itemSupports.put(itemSet, itemSupports.get(itemSet)/(double)size);
	}

	private void addItemSet(String[] itemStrings, boolean caseSensitive){
		HashSet<String> itemSet = new HashSet<String>();
		for (int i = 0; i < itemStrings.length; i++){
			if (caseSensitive)
				itemStrings[i] = itemStrings[i].toLowerCase();

			if (!itemSet.contains(itemStrings[i])){
				itemSet.add(itemStrings[i]);
				incrementItemCount(new HashSet<String>(Arrays.asList(itemStrings[i])));
				items.add(itemStrings[i]);
			}
		}
		itemSets.add(itemSet);
	}
	
	private void incrementItemCount(HashSet<String> key){
		if (itemSupports.containsKey(key))
			itemSupports.put(key, itemSupports.get(key) + 1);
		else
			itemSupports.put(key, (double)1);
	}

	public HashMap<HashSet<String>,Double> getInitialCandidateSets(){
		return itemSupports;
	}

	public int getSupport(HashSet<String> keySet){
		int count = 0;
		for(HashSet<String> itemSet : itemSets){
			boolean found = true;
			for (String key : keySet){
				if (!itemSet.contains(key)){
					found = false;
					break;
				}
			}
			if(found)
				count++;
		}
		return count;
	}

	public int getSize(){
		return size;
	}

	public String toString(){
		StringBuilder builder = new StringBuilder();
		for (HashSet<String> itemSet : itemSets)
			builder.append(itemSet.toString() + ",\n");
		return builder.toString();
	}
}