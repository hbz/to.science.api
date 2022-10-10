package dataformatsOOP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import helper.GenericPropertiesLoader;
import helper.LRMIMapper;
import java.util.Iterator;

// import de.hbz.lobid.helper.JsonConverter;

public class JsonObjectServiceImpl implements JsonObjectService{
	
	/**
	 * method converts a LearningResourceType-Object to json-object
	 * 
	 * @param an about-object
	 * @return a Json-Object (json2)
	 */
	public JSONObject getJSONObject(LearningResourceType lrt) {
		
		JSONObject JsonLrtBody = new JSONObject();
		JSONObject retJsonLrt = new JSONObject();
		
		JsonLrtBody.put("@id", lrt.getLrtID());
		JsonLrtBody.put("prefLabel", lrt.getPrefLabel());
		// Lrt in Json = medium
		retJsonLrt.put("medium", JsonLrtBody);
		
		return retJsonLrt;
	}
	
	/**
	 * method converts an about-object to json-object
	 * 
	 * @param an about-object
	 * @return a Json-Object (json2)
	 */
	public JSONObject getJSONObject(About abt) {
		
		JSONObject JsonAboutBody = new JSONObject();
		JSONObject retJsonAbout = new JSONObject();
		
		JsonAboutBody.put("@id", abt.getiD());
		JsonAboutBody.put("prefLabel", abt.getPrefLabel());
		// About in Json = department
		retJsonAbout.put("department", JsonAboutBody);
		
		return retJsonAbout;
	}
	
	/**
	 * method convert rdf to json, the method is created for about (Fachbereich) and
	 * LearningResourceType(Art der Datei)
	 * 
	 * @param rdf map is given in the gui (forms)
	 * @return a hashMap with objects of About
	 */
	public HashMap<String, Object> mapRdfToJson(Map<String, Object> rdf) {
		
		LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
		LRMIMapper lrmiMapper = new LRMIMapper();
		About abt = null;
		HashMap retHash = null;
		Map<String, Object> map = null;
		
		genPropMap.putAll(new GenericPropertiesLoader().loadVocabMap("department-de.properties"));
		Iterator itr = lrmiMapper.getLobid2Iterator(rdf.get("department"));
		
		while (itr.hasNext()) {
			map = (Map<String, Object>) itr.next();
			abt = new About();
			abt.setiD(String.valueOf(map.get("@id"))); // id ist ein Link
			// abt.setPrefLabel(genPropMap.get(map.get("@id"))); // PrefLabel z.B.Chemie
			abt.setPrefLabel(genPropMap.get(abt.getiD()));
			//genPropMap.putAll(new GenericPropertiesLoader().loadVocabMap(abt.getPropertiesFile()))
			retHash.put("about", abt); // department ??
			
			// ein About-Json hier zurueckzugeben ist nicht noetig, denn es wurde JSONObject getJSONObject(About abt) implementiert 
		}
		
		return retHash;
	}
	
	public HashMap<String, Object> mapRdfToJson(Map rdf, String lrmiObjName) {
		
		LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
		LRMIMapper lrmiMapper = new LRMIMapper();
		About abt = null;
		HashMap retHash = null;
		Map<String, Object> map = null;
		
		genPropMap.putAll(new GenericPropertiesLoader().loadVocabMap("department-de.properties"));
		Iterator itr = lrmiMapper.getLobid2Iterator(rdf.get("about"));
		
		while (itr.hasNext()) {
			map = (Map<String, Object>) itr.next();
			abt = new About();
			abt.setiD(String.valueOf(map.get("@id"))); // id ist ein Link
			// abt.setPrefLabel(genPropMap.get(map.get("@id"))); // PrefLabel z.B.Chemie
			abt.setPrefLabel(genPropMap.get(abt.getiD()));
			//genPropMap.putAll(new GenericPropertiesLoader().loadVocabMap(abt.getPropertiesFile()))
			
			retHash.put("about", abt); // department ??
			
		}
		
		return retHash;
		
	}
	
}
