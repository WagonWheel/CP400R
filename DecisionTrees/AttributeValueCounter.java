import java.util.HashMap;

public class AttributeValueCounter{
	private int total;
	private Attribute attribute;
	private Attribute classAttribute;
	private HashMap<String,HashMap<String,Integer>> counts;

	public AttributeValueCounter(Attribute attr, Attribute classAttr){
		this.attribute = attr;
		this.classAttribute = classAttr;
		this.total = 0;
		
		String[] classValues = this.classAttribute.getAttributeValues(); 
		this.counts = new HashMap<String,HashMap<String,Integer>>();
		for (String attributeValue: this.attribute.getAttributeValues()){
			HashMap<String,Integer> classCounts = new HashMap<String,Integer>();
			for (String classValue: classValues)
				classCounts.put(classValue, 0);
			this.counts.put(attributeValue, classCounts);
		}
	}

	public void incrementValueCounter(String attributeValue, String classValue){
		attributeValue = this.attribute.getAttributeValue(attributeValue);
		classValue = this.classAttribute.getAttributeValue(classValue);
		
		Integer n = this.counts.get(attributeValue).get(classValue);
		n += 1;
		this.counts.get(attributeValue).put(classValue,n);
	}

	public Integer getCount(String attributeValue, String classValue){
		return counts.get(attributeValue).get(classValue);
	}

	public int getTotal(){
		return this.total;
	}

	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("{\n");
		for (String attrVal : this.counts.keySet()){
			result.append(attrVal + ": {");
			for (String classVal : this.counts.get(attrVal).keySet())
				result.append(" " + classVal + "=" + this.counts.get(attrVal).get(classVal).toString());
			result.append(" }\n");
		}
		result.append("}");
		return result.toString();
	}
}