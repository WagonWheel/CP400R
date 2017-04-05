import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.ArrayList;

public class Dataset{
	public static void main(String[] args){
		try{
			Dataset data = new Dataset(new File("Data2.txt"));
			System.out.println(data.toString());
			Dataset b = data.split(data.getAttribute("odor"), "2");
			System.out.println(b);
			Dataset c = b.split(data.getAttribute("color"), "b");
			System.out.println(c.toString());
			c = b.split(data.getAttribute("color"), "w");
			System.out.println(c.toString());
			c = b.split(data.getAttribute("color"), "u");
			System.out.println(c.toString());
			c = b.split(data.getAttribute("color"), "g");
			System.out.println(c.toString());
		
			/*System.out.println("{");
			for(Attribute attr : counts.keySet())
				System.out.println(attr.getName() + ": " + counts.get(attr).toString());
			System.out.println("}");*/
				
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	public static final String AttributeDelim = ",";
	
	private Attribute[] schema; //Array of the dataset attributes
	private String[][] data; //Array of records, Each record is structured by the schema
	private int classAttributeIndex; //Index of class attribute

	//Todo: Docs
	//Constructor takes File with 2 line header n by m csv
	//#1 ClassAttribute
	//#2 Attributes
	//#3 r1_a1,r1_a2...r1_am
	//#4 r2_a1,r2_a2...r2_am
	//#5 rn_a1,rn_a2...rn_am
	public Dataset(File file) throws IllegalArgumentException, FileNotFoundException, IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file)); //Initializer Reader
		String classAttributeInput = reader.readLine().toLowerCase(); //Read in first line as class attriubte
		String line = reader.readLine().toLowerCase(); //Read in second line as schema string
		String[] attributeStrings = line.split(this.AttributeDelim); //Split schema line into string array

		//Convert schema strings to attributes
		this.schema = new Attribute[attributeStrings.length];
		for (int i = 0; i < attributeStrings.length; i++){
			this.schema[i] = new Attribute(attributeStrings[i]);
		}

		//Check schema for duplicates
		for (int i = 0; i < this.schema.length; i++)
			for (int j = i+1; j < this.schema.length; j++)
				if (this.schema[i].getName().equals(this.schema[j].getName()))
					throw new IllegalArgumentException("Schema has duplicate attributes");

		//Find Class Attribute
		this.classAttributeIndex = -1;
		for(int i = 0; i < this.schema.length; i++)
			if (this.schema[i].getName().equals(classAttributeInput))
				this.classAttributeIndex = i;
		if (this.classAttributeIndex == -1)
			throw new IllegalArgumentException("Class Attribute was not found in schema");

		//Read data from file and store into Arrays
		//Also collects all the values of each Attributes
		ArrayList<String[]> dataList = new ArrayList<String[]>();
		ArrayList<String>[] attributeValues = new ArrayList[this.schema.length];
		for (int i = 0; i < this.schema.length; i++)
				attributeValues[i] = new ArrayList<String>();
		while ((line = reader.readLine()) != null){
			String[] record = line.toLowerCase().split(this.AttributeDelim);
			dataList.add(record);
			for (int i = 0; i < this.schema.length; i++)
				attributeValues[i].add(record[i]);
		}
		
		this.data = dataList.toArray(new String[dataList.size()][this.schema.length]);
		for (int i = 0; i < this.schema.length; i++){
			this.schema[i].loadAttributeValues(attributeValues[i].toArray(new String[attributeValues[i].size()]));
		}
	}

	public Dataset(Attribute[] schema, String[][] data, int classAttributeIndex){
		this.schema = schema;
		this.data = data;
		this.classAttributeIndex = classAttributeIndex;
	}
	
/*
	public boolean isEmpty(){
		return (this.data.length <= 0);
	}*/

/*	public Dataset splitOrdinalAttribute(String attribute){
		for (int i = 0; i < this.schema.length; i++){
			if (attribute.toLowerCase().equals(this.schema[i].toLowerCase())){
				
			}
		}
	}*/

	/**Counts the occurance of each attribute value for every attribute and every record.
	 * results are returned in a HashMap which maps the Attribute name (String) to AttributeValueCounters.
	 * <p>
	 * Todo: Dev, Comments, Testing
	 * 
	 * Pre-Conditions - No state affected
	 * Post-Conditions - No state affected
	 * 
	 * @Param type - Nominal, Ordinal, Continuous
	 * @Return HashMap<Attribute, AttributeValueCounter> storing a map from attribute Name
	 * @See Attribute
	 * @See AttributeValueCounter
	 */
	public HashMap<Attribute, AttributeValueCounter> countAttributeValues(){
		//Initialize Attribute Counters
		HashMap<Attribute, AttributeValueCounter> attributeValueCounters = new HashMap<Attribute, AttributeValueCounter>();

		for (Attribute attr : this.schema)
			if (!attr.equals(this.schema[this.classAttributeIndex]))
				attributeValueCounters.put(attr, new AttributeValueCounter(attr, this.schema[this.classAttributeIndex]));
			
		for (String[] record : this.data)
			for (int i = 0; i < this.schema.length; i++)
				if (i != this.classAttributeIndex)
					attributeValueCounters.get(this.schema[i]).incrementValueCounter(record[i], record[this.classAttributeIndex]);
		return attributeValueCounters;
	}

	public Dataset split(Attribute attr, String attrVal){
		int i = 0;
		while (!this.schema[i].equals(attr))
			i++;
		if (i >= this.schema.length)
			throw new IllegalArgumentException("Attribute not found in schema");
		if (i == this.classAttributeIndex)
			throw new IllegalStateException("Cannot split by classAttribute");

		Attribute[] newSchema = new Attribute[this.schema.length - 1];
		for (int j = 0; j < i; j++)
			newSchema[j] = this.schema[j];
		for (int j = i + 1; j < this.schema.length; j++)
			newSchema[j-1] = this.schema[j];

		ArrayList<String[]> newRecords = new ArrayList<String[]>();
		for (String[] record : this.data)
			if(record[i].equals(attrVal)){
				String[] newRecord = new String[this.schema.length - 1];
				for (int j = 0; j < i; j++)
					newRecord[j] = record[j];
				for (int j = i + 1; j < this.schema.length; j++)
					newRecord[j-1] = record[j];
				newRecords.add(newRecord);
			}
		int classAttributeIndex = (i < this.classAttributeIndex)? this.classAttributeIndex - 1 : this.classAttributeIndex;
		return new Dataset(newSchema, newRecords.toArray(new String[newRecords.size()][this.schema.length - 1]), classAttributeIndex);
	}

	public Attribute[] getSchema(){
		return this.schema;
	}

	public Attribute getAttribute(String name){
		for (Attribute attr : this.schema)
			if (attr.getName().equals(name))
				return attr;
		return null;
	}

	/**
     * Creates a visually formatted String representing the Dataset.
	 * Uses String builder to avoid multiple String creation
	 * Assumes schema lengths and values are less than one tab width 
	 * @Return String with visual representation of Dataset
	 */
	public String toString(){
		StringBuilder result = new StringBuilder();//Initialize StringBuilder

		/*for (Attribute attr : this.schema)//Add schema
			result.append(attr.toString() + "\n");*/
		
		for (Attribute attr : this.schema)//Add schema
			result.append(attr.getName() + "\t");
		result.append("\n\n");
		
		for (String[] record : this.data){//Add Records
			for (String attr : record)
				result.append(attr + "\t");
			result.append("\n");
		}
		return result.toString();//Return result
	}
}