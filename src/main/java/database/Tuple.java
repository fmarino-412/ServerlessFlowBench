package database;

public class Tuple {
	private String stringA;
	private String stringB;
	private String stringC;

	public Tuple(String stringA, String stringB, String stringC) {
		this.stringA = stringA;
		this.stringB = stringB;
		this.stringC = stringC;
	}

	public String getStringA() {
		return stringA;
	}

	public String getStringB() {
		return stringB;
	}

	public String getStringC() {
		return stringC;
	}
}
