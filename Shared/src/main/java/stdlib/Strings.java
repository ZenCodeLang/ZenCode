package stdlib;

import java.util.ArrayList;
import java.util.List;

public final class Strings {
    private Strings() {}
	public static String[] split(String self, char delimiter) {
	    List<String> result = new ArrayList<String>();
	    int start = 0;
	    int limitForI = self.length();
	    for (int i = 0; i < limitForI; i++) {
	        if (self.charAt(i) == delimiter) {
	            result.add(self.substring(start, i));
	            start = i + 1;
	        }
	    }
	    result.add(self.substring(start, self.length()));
	    return result.toArray(new String[result.size()]);
	}
	
	public static String lpad(String self, int length, char c) {
	    return self.length() >= length ? self : Chars.times(c, length - self.length()) + self;
	}
	
	public static String rpad(String self, int length, char c) {
	    return self.length() >= length ? self : self + Chars.times(c, length - self.length());
	}
}