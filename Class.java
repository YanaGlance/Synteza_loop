
public class Class {
	public static void main(String[] args) {
		System.out.println("Synteza");
		method1();
		method2();
		method3();
		method4();
	}

	public static void method1() {
		int k = 30;
		FOR(j, k, 15, ">", "-");
	}

	public static void method2() {
		FOR(i, 0, 10, "<", "+");
	}

	public static void method3() {
		int k = 30;
		FOR(j, k, -15, ">=", "-");
	}

	public static void method4() {
		FOR(i, -3, 10, "<=", "+");
	}
}
