package helper;

import java.util.Base64;

/**
 * 
 */
public class MyURLEncoding {

	/**
	 * @param encodeMe literal to encode
	 * @return encoded value as literal
	 */
	public static String encode(String encodeMe) {
		play.Logger.trace("encodeMe=" + encodeMe);
		String base64EncodedName =
				Base64.getUrlEncoder().encodeToString(encodeMe.getBytes());
		// .replaceAll("/", "-").replaceAll("\\+", "_");
		play.Logger.trace("base64EncodedName=" + base64EncodedName);
		return base64EncodedName;
	}

	/**
	 * @param decodeMe value to encode
	 * @return decoded value as literal
	 */
	public static String decode(String decodeMe) {
		play.Logger.info("decodeMe=" + decodeMe);
		String base64EncodedName = decodeMe; // .replaceAll("-",
																					// "/").replaceAll("_", "+");
		play.Logger.info("base64EncodedName=" + base64EncodedName);
		/*
		 * if (base64EncodedName.endsWith(",")) {
		 * 
		 * base64EncodedName = base64EncodedName.substring(0,
		 * base64EncodedName.length() - 1); }
		 */
		play.Logger.info("base64EncodedName=" + base64EncodedName);
		play.Logger.info(
				"decoded String=" + Base64.getUrlDecoder().decode(base64EncodedName));
		return new String(Base64.getUrlDecoder().decode(base64EncodedName));
	}
}
