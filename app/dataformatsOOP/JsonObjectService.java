package dataformatsOOP;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public interface JsonObjectService{
	
	public JSONObject getJSONObject(LearningResourceType lrt);
	
	public JSONObject getJSONObject(About abt);
	
	public HashMap<String, Object> mapRdfToJson(Map<String, Object> rdf);
	
	public HashMap<String, Object> mapRdfToJson(Map rdf, String lrmiObjName);
	
	//public JSONObject mapRdfToJson(Map<String, Object> rdf);
	
}
