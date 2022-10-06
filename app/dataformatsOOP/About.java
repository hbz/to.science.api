package dataformatsOOP;

public class About extends AbstractLrmiObject{
	
	private String JsonArrayName = "about";
	private String InScheme = "https://w3id.org/kim/hochschulfaechersystematik/scheme";
	private String type = "Concept";
	private String prefLabel; // variabel
	private String abtID; // variabel
	
	public String getJsonArrayName() {
		return JsonArrayName;
	}
	
	public String getInScheme() {
		return InScheme;
	}
	
	public String getType() {
		return type;
	}
	
	public String getPrefLabel() {
		return prefLabel;
	}
	
	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}
	
	public String getAbtID() {
		return abtID;
	}
	
	public void setAbtID(String abtID) {
		this.abtID = abtID;
	}
	
}
