/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package models;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToScienceObject implements java.io.Serializable {

	/**
	 * @author jan schnasse
	 * @author Andres Quast
	 *
	 */
	public class Provenience {
		HashMap<String,String> provenience = setInitialProvenience();		

		/**
		 * Setting up HashMap including all provenience variables
		 */
		private HashMap<String,String> setInitialProvenience(){
			HashMap<String,String> initProvenience = new HashMap<>(); 
			initProvenience.put("createdBy", null);
			initProvenience.put("submittedBy", null);
			initProvenience.put("submittedByEmail", null);
			initProvenience.put("importedFrom", null);
			initProvenience.put("legacyId", null);
			initProvenience.put("name", null);
			initProvenience.put("doi", null);
			initProvenience.put("urn", null);
			return initProvenience;
		}
		
			

		/**
		 * @param createdBy
		 */
		public void setCreatedBy(String createdBy) {
			this.provenience.put("createdBy", createdBy);
		}

    /**
     * @param submittedBy
     */
    public void setSubmittedBy(String submittedBy) {
      this.provenience.put("submittedBy", submittedBy);
    }

    /**
     * @param submittedBy
     */
    public void setSubmittedByEmail(String submittedByEmail) {
      this.provenience.put("submittedByEmail", submittedByEmail);
    }

    /**
		 * @return importedFrom
		 */
		public String getImportedFrom() {
			return this.provenience.get("importedFrom");
		}

		/**
		 * @param importedFrom
		 */
		public void setImportedFrom(String importedFrom) {
      this.provenience.put("importedFrom", importedFrom);
		}

		/**
		 * @return legacyId
		 */
		public String getLegacyId() {
			return this.provenience.get("legacyId");
		}

		/**
		 * @param legacyId legacyId
		 */
		public void setLegacyId(String legacyId) {
      this.provenience.put("legacyId", legacyId);
		}

		/**
		 * @return createdBy
		 */
		public String getCreatedBy() {
			return this.provenience.get("createdBy");
		}

    /**
     * @return submittedBy
     */
    public String getSubmittedBy() {
			return this.provenience.get("submittedBy");
    }

    /**
     * @return submittedByEmail
     */
    public String getSubmittedByEmail() {
			return this.provenience.get("submittedByEmail");
    }

		/**
		 * An internal name for the object
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.provenience.put("name", name);
		}

		/**
		 * 
		 * @return an internal name for the object
		 */
		public String getName() {
			return this.provenience.get("name");
		}

		/**
		 * @return the doi as string
		 */
		public String getDoi() {
			return this.provenience.get("doi");
		}

		/**
		 * @param doi
		 */
		public void setDoi(String doi) {
			this.provenience.put("doi",doi);
		}

		/**
		 * @return the urn as string
		 */
		public String getUrn() {
			return this.provenience.get("urn");
		}

		/**
		 * @param urn
		 */
		public void setUrn(String urn) {
			this.provenience.put("urn", urn);
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + (provenience.get("createdBy") != null ? provenience.get("createdBy").hashCode() : 0);
      result = 31 * result + (provenience.get("submittedBy") != null ? provenience.get("submittedBy").hashCode() : 0);
      result = 31 * result + (provenience.get("submittedByEmail") != null ? provenience.get("submittedByEmail").hashCode() : 0);
			result =
					31 * result + (provenience.get("importedFrom") != null ? provenience.get("importedFrom").hashCode() : 0);
			result = 31 * result + (provenience.get("legacyId") != null ? provenience.get("legacyId").hashCode() : 0);
			result = 31 * result + (provenience.get("name") != null ? provenience.get("name").hashCode() : 0);
			result = 31 * result + (provenience.get("doi") != null ? provenience.get("doi").hashCode() : 0);
			result = 31 * result + (provenience.get("urn") != null ? provenience.get("urn").hashCode() : 0);
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof Provenience))
				return false;

			Provenience mt = (Provenience) other;
			
			// create Iterator from HashMap
			Iterator<Map.Entry<String, String>> pit = provenience.entrySet().iterator();
			
			while(pit.hasNext()) {
				Entry<String,String> proEntry = pit.next();
				play.Logger.debug(mt.provenience.get(proEntry.getKey()));
				play.Logger.debug(proEntry.getValue());
				if (!(proEntry.getValue() == null ? mt.provenience.get(proEntry.getKey()) == null : 
					mt.provenience.get(proEntry.getKey()).equals(proEntry.getValue()))) {
					return false;
				};
			}
			
			/*
			* Gebe falsch zurück wenn die interne Prüfung falsch ist: 
			* 1. Prüfe ob createdBy null ist. 
			* 2. Wenn ja, prüfe ob auch mt.createdBy null ist. 
			* Wenn 2 wahr, gebe wahr zurück
			* 3. Wenn 1 falsch, prüfe ob createdBy mt.createdBy entspricht.
			* Wenn 3 wahr, gebe wahr zurück 
			
			if (!(createdBy == null ? mt.createdBy == null
					: createdBy.equals(mt.createdBy)))
				return false;
				
				
				
      if (!(submittedBy == null ? mt.submittedBy == null
          : submittedBy.equals(mt.submittedBy)))
        return false;
      if (!(submittedByEmail == null ? mt.submittedByEmail == null
          : submittedByEmail.equals(mt.submittedByEmail)))
        return false;
			if (!(importedFrom == null ? mt.importedFrom == null
					: importedFrom.equals(mt.importedFrom)))
				return false;
			if (!(legacyId == null ? mt.legacyId == null
					: legacyId.equals(mt.legacyId)))
				return false;
			if (!(doi == null ? mt.doi == null : doi.equals(mt.doi)))
				return false;
			if (!(urn == null ? mt.urn == null : urn.equals(mt.urn)))
				return false;
				
			*/
			return true;
		}
	}

	String contentType = null;
	String parentPid = null;
	List<String> transformer = null;
	List<String> indexes = null;
	String accessScheme = null;
	String publishScheme = null;

	Provenience isDescribedBy = new Provenience();

	/**
	 * Default constructor
	 * 
	 */
	public ToScienceObject() {
		transformer = new Vector<String>();
		indexes = new Vector<String>();
	}

	/**
	 * @return all Transformer-Ids
	 */
	public List<String> getTransformer() {
		return transformer;
	}

	/**
	 * @param t list of Transformer-Ids
	 */
	public void setTransformer(List<String> t) {
		transformer = t;
	}

	/**
	 * @param t a valid type
	 */
	public ToScienceObject(ObjectType t) {
		contentType = t.toString();
	}

	/**
	 * @return the type of the object
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param type the type
	 */
	public void setContentType(String type) {
		this.contentType = type;
	}

	/**
	 * @return the parent
	 */
	public String getParentPid() {
		return parentPid;
	}

	/**
	 * @param parentPid the parent
	 */
	public void setParentPid(String parentPid) {
		this.parentPid = parentPid;
	}

	/**
	 * @return a list of indexes, that are updated on create/modify
	 */
	public List<String> getIndexes() {
		return indexes;
	}

	/**
	 * @param indexes a list of indexes, that are updated on create/modify, valid
	 *          values so far: null, public, private
	 */
	public void setIndexes(List<String> indexes) {
		this.indexes = indexes;
	}

	/**
	 * @return a string that signals who is allowed to access the data node
	 */
	public String getAccessScheme() {
		return accessScheme;
	}

	/**
	 * @param accessScheme a string that signals who is allowed to access the data
	 *          of node
	 */
	public void setAccessScheme(String accessScheme) {
		this.accessScheme = accessScheme;
	}

	/**
	 * @return a string that signals who is allowed to access the metadata of node
	 */
	public String getPublishScheme() {
		return publishScheme;
	}

	/**
	 * @param publishScheme a string that signals who is allowed to access the
	 *          metadata node
	 */
	public void setPublishScheme(String publishScheme) {
		this.publishScheme = publishScheme;
	}

	/**
	 * @return some meta-metadata
	 */
	public Provenience getIsDescribedBy() {
		return isDescribedBy;
	}

	/**
	 * @param describedBy
	 */
	public void setIsDescribedBy(Provenience describedBy) {
		this.isDescribedBy = describedBy;
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		StringWriter w = new StringWriter();
		try {
			mapper.writeValue(w, this);
		} catch (Exception e) {
			return super.toString();
		}
		return w.toString();
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (parentPid != null ? parentPid.hashCode() : 0);
		result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
		result = 31 * result + (accessScheme != null ? accessScheme.hashCode() : 0);
		result = 31 * result + (transformer != null ? transformer.hashCode() : 0);
		result = 31 * result + (indexes != null ? indexes.hashCode() : 0);
		result =
				31 * result + (isDescribedBy != null ? isDescribedBy.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof ToScienceObject))
			return false;
		ToScienceObject mt = (ToScienceObject) other;
		if (!(parentPid == null ? mt.parentPid == null
				: parentPid.equals(mt.parentPid)))
			return false;
		if (!(contentType == null ? mt.contentType == null
				: contentType.equals(mt.contentType)))
			return false;
		if (!(accessScheme == null ? mt.accessScheme == null
				: accessScheme.equals(mt.accessScheme)))
			return false;
		if (!(transformer == null ? mt.transformer == null
				: transformer.equals(mt.transformer)))
			return false;
		if (!(indexes == null ? mt.indexes == null : indexes.equals(mt.indexes)))
			return false;
		if (!(isDescribedBy == null ? mt.isDescribedBy == null
				: isDescribedBy.equals(mt.isDescribedBy)))
			return false;
		return true;
	}
}
