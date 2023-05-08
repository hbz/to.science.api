package views;

import java.math.BigInteger;

import com.google.common.net.MediaType;

import models.Globals;
import models.Node;

public class ViewerInfo {
	public enum Style {
		EMBEDDED, LINKED
	}

	public enum ViewerType {
		SEC_PDF, VIDEO, AUDIO, DEEPZOOM, IMAGE, UNDEFINED
	}

	public String pid = null;
	public String label = null;
	public String dataLink = null;
	public Style style = null;
	public String thumbnail = "";
	public ViewerType viewertype = null;
	public String mimetype = null;
	public Long filesize = null;
	public String viewerAdress = null;
	public String contentType = null;
	public String accessScheme = null;

	public ViewerInfo(Node n) {
		pid = n.getPid();
		dataLink = "/resource/" + n.getPid() + "/data";
		// Qa: reverted patch for big file sizes. Reverted due to access issues
		// dataLink = Globals.protocol + Globals.server + "/resource/" + n.getPid()
		// + "/data";
		filesize = n.getFileSize();
		viewertype = createViewerType(n);
		viewerAdress = createViewerAdress(n);
		mimetype = n.getMimeType();
		thumbnail = createThumbnailAndSetStyle(n);
		contentType = n.getContentType();
		accessScheme = n.getAccessScheme();
		label = n.getFileLabel();
	}

	private ViewerType createViewerType(Node n) {
		String format = n.getMimeType();
		MediaType mt = MediaType.parse(format);

		if (mt.is(MediaType.ANY_IMAGE_TYPE) || mt.is(MediaType.JPEG)) {
			play.Logger.debug("Filesize is " + filesize);
			if (filesize > 50000l) {
				return ViewerType.DEEPZOOM;
			}
			return ViewerType.IMAGE;
		}
		if (mt.is(MediaType.ANY_VIDEO_TYPE) || mt.is(MediaType.MP4_VIDEO)
				|| mt.is(MediaType.WEBM_VIDEO)) {
			return ViewerType.VIDEO;
		}
		if (mt.is(MediaType.ANY_AUDIO_TYPE)) {
			return ViewerType.AUDIO;
		}
		if ("restricted".equals(n.getAccessScheme()) && mt.is(MediaType.PDF)) {
			return ViewerType.SEC_PDF;
		}
		return ViewerType.UNDEFINED;
	}

	private String createViewerAdress(Node n) {
		if (ViewerType.DEEPZOOM.equals(viewertype)) {
			// return "/viewers/deepzoom/" + n.getPid() + "/data";
		}
		if (ViewerType.VIDEO.equals(viewertype)) {
			return "/viewers/video/" + n.getPid() + "/data";
		}
		if (ViewerType.AUDIO.equals(viewertype)) {
			return "/viewers/audio/" + n.getPid() + "/data";
		}
		if (ViewerType.SEC_PDF.equals(viewertype)) {
			return "/viewers/pdf/" + n.getPid() + "/data";
		}

		return dataLink;
	}

	private String createThumbnailAndSetStyle(Node n) {
		if (ViewerType.UNDEFINED.equals(viewertype)) {
			style = Style.LINKED;
			return thumbyLink(n);
		}
		if (ViewerType.IMAGE.equals(viewertype)) {
			style = Style.LINKED;
			return thumbyLink(n);
		}
		if (ViewerType.DEEPZOOM.equals(viewertype)) {
			style = Style.LINKED;
			return thumbyLink(n);
		}
		if (ViewerType.SEC_PDF.equals(viewertype)) {
			style = Style.LINKED;
			return thumbyLink(n);
		}
		style = Style.EMBEDDED;
		return "";
	}

	private String thumbyLink(Node n) {
		String result = "";
		String size = "&size=250";
		result = Globals.thumbyUrl + "?url=" + Globals.protocol + Globals.server
				+ dataLink + size;
		return result;
	}

	public String getHtml5Element() {
		if (ViewerType.AUDIO.equals(viewertype)) {
			return "audio";
		}
		if (ViewerType.VIDEO.equals(viewertype)) {
			return "video";
		}
		return "video";
	}

	public String getViewerLink() {
		if (ViewerType.SEC_PDF.equals(viewertype)) {
			return Globals.protocol + Globals.server + "/viewers/pdf/" + pid
					+ "/data";
		}
		return Globals.protocol + Globals.server + "/viewers/deepzoom/" + pid
				+ "/data";

	}
}