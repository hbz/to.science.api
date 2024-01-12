package helper;

import java.util.Base64;

public class MyURLEncoding {
	public static String encode(String encodeMe) {
		play.Logger.trace("encodeMe=" + encodeMe);
		String base64EncodedName =
				Base64.getEncoder().encodeToString(encodeMe.getBytes())
						.replaceAll("/", "-").replaceAll("\\+", "_");
		play.Logger.trace("base64EncodedName=" + base64EncodedName);
		return base64EncodedName;
	}

	public static String decode(String decodeMe) {
		play.Logger.trace("decodeMe=" + decodeMe);
		String base64EncodedName =
				decodeMe.replaceAll("-", "/").replaceAll("_", "+");
		play.Logger.trace("base64EncodedName=" + base64EncodedName);
		if( base64EncodedName.endsWith(",") ) {
			base64EncodedName = base64EncodedName.substring(0,base64EncodedName.length()-1);
		}
		play.Logger.trace("base64EncodedName=" + base64EncodedName);
		return new String(Base64.getDecoder().decode(base64EncodedName));
	}
}
