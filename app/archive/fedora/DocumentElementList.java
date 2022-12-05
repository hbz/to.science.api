package archive.fedora;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DocumentElementList {

	private NodeList nodeList = null;
	private int length = 0;

	public DocumentElementList(Document content, String elementName) {
		this.nodeList = content.getElementsByTagName(elementName);
		this.length =
				this.nodeList != null ? this.nodeList.getLength() : this.length;
	}

	public NodeList getNodeList() {
		return nodeList;
	}

	public int getLength() {
		return length;
	}

}
