package dataformatsOOP;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public interface ObjectService{
	
	public JSONObject getJSONObject(LearningResourceType lrt);
	
	public JSONObject getJSONObject(About abt);
	
	public HashMap<String, Object> mapRdfToJson(Map rdf);
	
	public HashMap<String, Object> mapRdfToJson(Map rdf, String lrmiObjName);
	
}
