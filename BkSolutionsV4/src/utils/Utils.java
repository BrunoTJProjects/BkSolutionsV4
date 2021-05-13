package utils;

import java.util.UUID;

public class Utils {

	public static String radomString() {

		UUID uuid = UUID.randomUUID();
		String myRandom = uuid.toString();
		return myRandom.substring(0, 8);

	}

}
