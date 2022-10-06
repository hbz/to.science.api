package dataformatsOOP;

public class LearningResourceType extends AbstractLrmiObject{
	
	private String JsonArrayName = "learningResourceType";
	private String InScheme = "https://w3id.org/kim/hcrt/scheme";
	private String type = "Concept";
	private String prefLabel; // variabel
	private String lrtID; // variabel
	
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
	
	public String getLrtID() {
		return lrtID;
	}
	
	public void setLrtID(String lrtID) {
		this.lrtID = lrtID;
	}
	
}
