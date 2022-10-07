package dataformatsOOP;

abstract class AbstractJsonObject{
	
	private String JsonArrayName; //variabel 						
	private String InScheme; // variabel
	private String type = "Concept"; // bei allen Unterklassen gleich 
	private String prefLabel; // variabel
	private String iD; // variabel
	
	public String getJsonArrayName() {
		return JsonArrayName;
	}
	
	public void setJsonArrayName(String jsonArrayName) {
		JsonArrayName = jsonArrayName;
	}
	
	public String getInScheme() {
		return InScheme;
	}
	
	public void setInScheme(String inScheme) {
		InScheme = inScheme;
	}
	
	public String getPrefLabel() {
		return prefLabel;
	}
	
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}
	
	public String getiD() {
		return iD;
	}
	
	public void setiD(String iD) {
		this.iD = iD;
	}
	
	public String getType() {
		return type;
	}
	
}
