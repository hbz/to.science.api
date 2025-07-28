package helper;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 
 */
public class MyURLEncoding {

	/**
	 * @param encodeMe literal to encode
	 * @return encoded value as literal
	 */
	public static String encode(String encodeMe) {
		return Base64.getEncoder()
				.encodeToString(encodeMe.getBytes(StandardCharsets.UTF_8))
				.replaceAll("/", "-").replaceAll("\\+", "_");
	}

	/**
	 * @param decodeMe value to encode
	 * @return decoded value as literal
	 */
	public static String decode(String decodeMe) {
		String modifiedInput = decodeMe.replaceAll("-", "/").replaceAll("_", "+");
		return new String(Base64.getDecoder().decode(modifiedInput),
				StandardCharsets.UTF_8);
	}
}
