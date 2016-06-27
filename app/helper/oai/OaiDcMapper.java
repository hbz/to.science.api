package helper.oai;

import java.util.List;

import models.DublinCoreData;
import models.Link;
import models.Node;

/**
 * @author Jan Schnasse
 *
 */
public class OaiDcMapper {
	Node node = null;

	/**
	 * @param node the node will be mapped by getData to DublinCore
	 */
	public OaiDcMapper(Node node) {
		this.node = node;
	}

	/**
	 * @return DublinCore object of node
	 **/
	/*-doc-type:preprint
	
	doc-type:workingPaper
	
	doc-type:article
	
	doc-type:contributionToPeriodical
	
	doc-type:PeriodicalPart
	
	doc-type:Periodical
	
	doc-type:book
	
	doc-type:bookPart
	
	doc-type:Manuscript
	
	doc-type:StudyThesis
	
	doc-type:bachelorThesis
	
	doc-type:masterThesis
	
	doc-type:doctoralThesis
	
	doc-type:conferenceObject
	
	doc-type:lecture
	
	doc-type:review
	
	doc-type:annotation
	
	doc-type:patent
	
	doc-type:report
	
	doc-type:MusicalNotation
	
	doc-type:Sound
	
	doc-type:Image
	
	doc-type:MovingImage
	
	doc-type:StillImage
	
	doc-type:CourseMaterial
	
	doc-type:Website
	
	doc-type:Software
	
	doc-type:CarthographicMaterial
	
	doc-type:ResearchData
	
	doc-type:Other
	
	doc-type:Text
	 */
	public DublinCoreData getData() {

		DublinCoreData data = new DublinCoreData();
		if (node == null)
			return data;
		List<Link> ld = node.getLinks();
		for (Link l : ld) {
			if ("http://purl.org/dc/terms/title".equals(l.getPredicate())) {
				data.addTitle(l.getObject());
			} else if ("http://purl.org/dc/terms/creator".equals(l.getPredicate())) {
				data.addCreator(l.getObjectLabel());
			} else if ("http://purl.org/dc/terms/issued".equals(l.getPredicate())) {
				data.addDate(l.getObject());
			} else if ("http://purl.org/dc/terms/contributor"
					.equals(l.getPredicate())) {
				data.addContributor(l.getObjectLabel());
			} else if ("http://purl.org/dc/terms/creator".equals(l.getPredicate())) {
				data.addCreator(l.getObjectLabel());
			} else if ("http://purl.org/lobid/lv#urn".equals(l.getPredicate())) {
				data.addIdentifier(l.getObject(), "hbz-urn");
			} else if ("http://purl.org/lobid/lv#hbzID".equals(l.getPredicate())) {
				data.addIdentifier(l.getObject(), "hbz-alephId");
			} else if ("http://purl.org/ontology/bibo/isbn"
					.equals(l.getPredicate())) {
				data.addIdentifier(l.getObject(), "hbz-isbn");
			} else if ("http://purl.org/dc/terms/subject".equals(l.getPredicate())) {
				data.addSubject(l.getObjectLabel());
			} else if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
					.equals(l.getPredicate())) {
				data.addType(l.getObject());
				if ("http://purl.org/ontology/bibo/Periodical".equals(l.getObject())) {
					data.addType("doc-type:Periodical");
				} else if ("http://purl.org/ontology/bibo/Book".equals(l.getObject())) {
					data.addType("doc-type:book");
				} else if ("http://purl.org/lobid/lv#ArchivedWebPage"
						.equals(l.getObject())) {
					data.addType("doc-type:Website");
				} else if ("http://purl.org/ontology/bibo/Thesis"
						.equals(l.getObject())) {
					data.addType("doc-type:doctoralThesis");
				}
			}
		}
		data.addIdentifier(node.getAggregationUri(), "hbz-edowebId");
		if (node.hasUrn())
			data.addIdentifier(node.getUrn(), "hbz-urn");
		if (node.hasDoi())
			data.addIdentifier(node.getDoi(), "hbz-doi");
		data.addType("regal:" + node.getContentType());
		return data;
	}
}
