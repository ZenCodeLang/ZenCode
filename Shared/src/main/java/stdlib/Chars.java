package stdlib;

public final class Chars {
    private Chars() {}
	public static String times(char self, int number) {
	    char[] temp1 = new char[number];
	    for (int temp2 = 0; temp2 < temp1.length; temp2++)
	        temp1[temp2] = self;
	    return new String(temp1);
	}
}