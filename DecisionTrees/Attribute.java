import java.util.TreeSet;
import java.util.Set;
public class Attribute{
	private static final String AttributeStringDelim = ":";
	private AttributeType type;
	private String name;
	private Set<String> attributeValues;
		
	public Attribute(String attributeString){
		String[] attributeTypePair = attributeString.split(AttributeStringDelim);
		this.type = AttributeType.valueOf(attributeTypePair[1].toUpperCase());
		this.name = attributeTypePair[0];
		this.attributeValues = null;
	}

	public String getName(){
		return this.name;
	}

	public AttributeType getType(){
		return this.type;
	}

	public void loadAttributeValues(String[] attributeValues) throws IllegalStateException{
		this.attributeValues = new TreeSet();
		switch(this.type){
			case NOMINAL:
			case ORDINAL:
				for (String attributeValue : attributeValues)
					if (this.type.validateAttributeValue(attributeValue))
						this.attributeValues.add(attributeValue);
				break;
			case CONTINUOUS:
				throw new IllegalStateException("CONTINUOUS values are not yet implemented");
		}
		return;
	}

	public String getAttributeValue(String value) throws IllegalStateException{
		if (this.attributeValues == null)
			throw new IllegalStateException("Attribute Values were not loaded (Loading happens when a dataset is read from file)");
		switch(this.type){
			case NOMINAL:
			case ORDINAL:
				if (this.attributeValues.contains(value))
					return value;
				break;
			case CONTINUOUS:
				throw new IllegalStateException("CONTINUOUS values are not yet implemented");
		}
		System.out.println(this.name);
		System.out.println(this.attributeValues.toString());
		throw new IllegalStateException("Attribute Value \"" + value + "\" was not located in loaded attribute values (Loading happens when a dataset is read from file)");
	}
	
	public String[] getAttributeValues() throws IllegalStateException{ 
		if (this.attributeValues == null)
			throw new IllegalStateException("AttributeValues not yet loaded");
		else
			return this.attributeValues.toArray(new String[this.attributeValues.size()]);
	}
		
	public boolean equals(Object o){
		if (o instanceof Attribute)
			return this.equals((Attribute)o);
		return false;
	}

	public boolean equals(Attribute o){
		return (o.name.toLowerCase().equals(this.name.toLowerCase()) && o.type.equals(this.type));
	}

	public String toString(){
		return this.name + ": " + this.type.name() + "\n" + ((this.attributeValues == null)? "{}" : this.attributeValues.toString());
	}
}