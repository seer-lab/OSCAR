public class Test {
	public static void main(String[] args) {
		for (int i = 0 ; i < 10000000; i++) {
			doNothing();
		}
	}

	static synchronized void doNothing() {
		return;
	}
}
