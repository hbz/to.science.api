package dataformatsOOP;

public class About extends AbstractJsonObject{
	
	About(){
		this.setJsonArrayName("about");
		this.setInScheme("https://w3id.org/kim/hochschulfaechersystematik/scheme");
		this.setPropertiesFile("department-de.properties");
	}
	
}
