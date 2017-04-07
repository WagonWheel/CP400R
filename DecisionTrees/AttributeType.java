public enum AttributeType{
	NOMINAL,ORDINAL,CONTINUOUS;
	
/*	public String orderValue(String value1, String value2) throws IllegalArgumentException { 
	}*/
	
	public boolean validateAttributeValue(String s){
		switch(this){
			case NOMINAL:
				return true;
			case ORDINAL:
				for (int i = 0; i < s.length(); i++)
					if (!Character.isDigit(s.charAt(i)) || (i == 0 && s.charAt(0) == '-'))
						return false;
				return true;
			case CONTINUOUS:
				if (s.charAt(0) != '<')
					return false;
				for (int i = 1; i < s.length(); i++)
					if (!Character.isDigit(s.charAt(i)) || (i == 1 && s.charAt(1) == '-'))
						return false;
				return true;
			default:
				throw new IllegalStateException("Cannot Validate AttributeType");
		}
	}
/*
	public String bucketContinuousValue(){
	}*/
}