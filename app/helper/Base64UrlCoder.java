package helper;

import java.util.Base64;

/**
 * @author aquast Class to encode or decode URLs via Base64. Replaces slashes /
 *         with minus - and plus + with underscore _ and vice versa.
 */
// TODO: Replace class with more robust en- and decoder especially for
// URL-Encoding?
public class Base64UrlCoder {

	public static String encode(String encodeLiteral) {
		return Base64.getEncoder().encodeToString(encodeLiteral.getBytes())
				.replaceAll("/", "-").replaceAll("\\+", "_");
	}

	public static String decode(String decodeLiteral) {
		String base64EncodedName =
				decodeLiteral.replaceAll("-", "/").replaceAll("_", "+");
		return new String(Base64.getDecoder().decode(base64EncodedName));
	}
}
