package org.openzen.zencode.shared;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.CharacterEntity;
import org.openzen.zencode.shared.StringExpansion;
import stdlib.Result;
import stdlib.Result.Error;
import stdlib.Result.Error;
import stdlib.Result.Ok;
import stdlib.Result.Ok;
import stdlib.Strings;
import zsynthetic.ArrayHelpers;

public final class StringExpansion {
    private StringExpansion() {}
	private static final Map<String, CharacterEntity> NAMED_CHARACTER_ENTITIES;
	
	static  {
	    CharacterEntity[] entities = StringExpansion.getCharacterEntities();
	    NAMED_CHARACTER_ENTITIES = new HashMap<>();
	    for (CharacterEntity entity : entities)
	        StringExpansion.NAMED_CHARACTER_ENTITIES.put(entity.stringValue, entity);
	}
	
	private static CharacterEntity[] getCharacterEntities() {
	    return new CharacterEntity[] {new CharacterEntity("quot", '"'), new CharacterEntity("amp", '&'), new CharacterEntity("apos", '\''), new CharacterEntity("lt", '<'), new CharacterEntity("gt", '>'), new CharacterEntity("nbsp", '\u00a0'), new CharacterEntity("iexcl", '\u00a1'), new CharacterEntity("cent", '\u00a2'), new CharacterEntity("pound", '\u00a3'), new CharacterEntity("curren", '\u00a4'), new CharacterEntity("yen", '\u00a5'), new CharacterEntity("brvbar", '\u00a6'), new CharacterEntity("sect", '\u00a7'), new CharacterEntity("uml", '\u00a8'), new CharacterEntity("copy", '\u00a9'), new CharacterEntity("ordf", '\u00aa'), new CharacterEntity("laquo", '\u00ab'), new CharacterEntity("not", '\u00ac'), new CharacterEntity("shy", '\u00ad'), new CharacterEntity("reg", '\u00ae'), new CharacterEntity("macr", '\u00af'), new CharacterEntity("deg", '\u00b0'), new CharacterEntity("plusmn", '\u00b1'), new CharacterEntity("sup2", '\u00b2'), new CharacterEntity("sup3", '\u00b3'), new CharacterEntity("acute", '\u00b4'), new CharacterEntity("micro", '\u00b5'), new CharacterEntity("para", '\u00b6'), new CharacterEntity("middot", '\u00b7'), new CharacterEntity("cedil", '\u00b8'), new CharacterEntity("sup1", '\u00b9'), new CharacterEntity("ordm", '\u00ba'), new CharacterEntity("raquo", '\u00bb'), new CharacterEntity("frac14", '\u00bc'), new CharacterEntity("frac12", '\u00bd'), new CharacterEntity("frac34", '\u00be'), new CharacterEntity("iquest", '\u00bf'), new CharacterEntity("Agrave", '\u00c0'), new CharacterEntity("Aacute", '\u00c1'), new CharacterEntity("Acirc", '\u00c2'), new CharacterEntity("Atilde", '\u00c3'), new CharacterEntity("Auml", '\u00c4'), new CharacterEntity("Aring", '\u00c5'), new CharacterEntity("AElig", '\u00c6'), new CharacterEntity("Ccedil", '\u00c7'), new CharacterEntity("Egrave", '\u00c8'), new CharacterEntity("Eacute", '\u00c9'), new CharacterEntity("Ecirc", '\u00ca'), new CharacterEntity("Euml", '\u00cb'), new CharacterEntity("lgrave", '\u00cc'), new CharacterEntity("lacute", '\u00cd'), new CharacterEntity("lcirc", '\u00ce'), new CharacterEntity("luml", '\u00cf'), new CharacterEntity("ETH", '\u00d0'), new CharacterEntity("Ntilde", '\u00d1'), new CharacterEntity("Ograve", '\u00d2'), new CharacterEntity("Oacute", '\u00d3'), new CharacterEntity("Ocirc", '\u00d4'), new CharacterEntity("Otilde", '\u00d5'), new CharacterEntity("Ouml", '\u00d6'), new CharacterEntity("times", '\u00d7'), new CharacterEntity("Oslash", '\u00d8'), new CharacterEntity("Ugrave", '\u00d9'), new CharacterEntity("Uacute", '\u00da'), new CharacterEntity("Ucirc", '\u00db'), new CharacterEntity("Uuml", '\u00dc'), new CharacterEntity("Yacute", '\u00dd'), new CharacterEntity("THORN", '\u00de'), new CharacterEntity("szlig", '\u00df'), new CharacterEntity("agrave", '\u00e0'), new CharacterEntity("aacute", '\u00e1'), new CharacterEntity("acirc", '\u00e2'), new CharacterEntity("atilde", '\u00e3'), new CharacterEntity("auml", '\u00e4'), new CharacterEntity("aring", '\u00e5'), new CharacterEntity("aelig", '\u00e6'), new CharacterEntity("ccedil", '\u00e7'), new CharacterEntity("egrave", '\u00e8'), new CharacterEntity("eacute", '\u00e9'), new CharacterEntity("ecirc", '\u00ea'), new CharacterEntity("euml", '\u00eb'), new CharacterEntity("igrave", '\u00ec'), new CharacterEntity("iacute", '\u00ed'), new CharacterEntity("icirc", '\u00ee'), new CharacterEntity("iuml", '\u00ef'), new CharacterEntity("eth", '\u00f0'), new CharacterEntity("ntilde", '\u00f1'), new CharacterEntity("ograve", '\u00f2'), new CharacterEntity("oacute", '\u00f3'), new CharacterEntity("ocirc", '\u00f4'), new CharacterEntity("otilde", '\u00f5'), new CharacterEntity("ouml", '\u00f6'), new CharacterEntity("divide", '\u00f7'), new CharacterEntity("oslash", '\u00f8'), new CharacterEntity("ugrave", '\u00f9'), new CharacterEntity("uacute", '\u00fa'), new CharacterEntity("ucirc", '\u00fb'), new CharacterEntity("uuml", '\u00fc'), new CharacterEntity("yacute", '\u00fd'), new CharacterEntity("thorn", '\u00fe'), new CharacterEntity("yuml", '\u00ff'), new CharacterEntity("OElig", '\u0152'), new CharacterEntity("oelig", '\u0153'), new CharacterEntity("Scaron", '\u0160'), new CharacterEntity("scaron", '\u0161'), new CharacterEntity("Yuml", '\u0178'), new CharacterEntity("fnof", '\u0192'), new CharacterEntity("circ", '\u02c6'), new CharacterEntity("tilde", '\u02dc'), new CharacterEntity("Alpha", '\u0391'), new CharacterEntity("Beta", '\u0392'), new CharacterEntity("Gamma", '\u0393'), new CharacterEntity("Delta", '\u0394'), new CharacterEntity("Epsilon", '\u0395'), new CharacterEntity("Zeta", '\u0396'), new CharacterEntity("Eta", '\u0397'), new CharacterEntity("Theta", '\u0398'), new CharacterEntity("Iota", '\u0399'), new CharacterEntity("Kappa", '\u039a'), new CharacterEntity("Lambda", '\u039b'), new CharacterEntity("Mu", '\u039c'), new CharacterEntity("Nu", '\u039d'), new CharacterEntity("Xi", '\u039e'), new CharacterEntity("Omicron", '\u039f'), new CharacterEntity("Pi", '\u03a0'), new CharacterEntity("Rho", '\u03a1'), new CharacterEntity("Sigma", '\u03a3'), new CharacterEntity("Tau", '\u03a4'), new CharacterEntity("Upsilon", '\u03a5'), new CharacterEntity("Phi", '\u03a6'), new CharacterEntity("Chi", '\u03a7'), new CharacterEntity("Psi", '\u03a8'), new CharacterEntity("Omega", '\u03a9'), new CharacterEntity("alpha", '\u03b1'), new CharacterEntity("beta", '\u03b2'), new CharacterEntity("gamma", '\u03b3'), new CharacterEntity("delta", '\u03b4'), new CharacterEntity("epsilon", '\u03b5'), new CharacterEntity("zeta", '\u03b6'), new CharacterEntity("eta", '\u03b7'), new CharacterEntity("theta", '\u03b8'), new CharacterEntity("iota", '\u03b9'), new CharacterEntity("kappa", '\u03ba'), new CharacterEntity("lambda", '\u03bb'), new CharacterEntity("mu", '\u03bc'), new CharacterEntity("nu", '\u03bd'), new CharacterEntity("xi", '\u03be'), new CharacterEntity("omicron", '\u03bf'), new CharacterEntity("pi", '\u03c0'), new CharacterEntity("rho", '\u03c1'), new CharacterEntity("sigmaf", '\u03c2'), new CharacterEntity("sigma", '\u03c3'), new CharacterEntity("tau", '\u03c4'), new CharacterEntity("upsilon", '\u03c5'), new CharacterEntity("phi", '\u03c6'), new CharacterEntity("chi", '\u03c7'), new CharacterEntity("psi", '\u03c8'), new CharacterEntity("omega", '\u03c9'), new CharacterEntity("thetasym", '\u03d1'), new CharacterEntity("upsih", '\u03d2'), new CharacterEntity("piv", '\u03d6'), new CharacterEntity("ensp", '\u2002'), new CharacterEntity("emsp", '\u2003'), new CharacterEntity("thinsp", '\u2009'), new CharacterEntity("zwnj", '\u200c'), new CharacterEntity("zwj", '\u200d'), new CharacterEntity("lrm", '\u200e'), new CharacterEntity("rlm", '\u200f'), new CharacterEntity("ndash", '\u2013'), new CharacterEntity("mdash", '\u2014'), new CharacterEntity("lsquo", '\u2018'), new CharacterEntity("rsquo", '\u2019'), new CharacterEntity("sbquo", '\u201a'), new CharacterEntity("ldquo", '\u201c'), new CharacterEntity("rdquo", '\u201d'), new CharacterEntity("bdquo", '\u201e'), new CharacterEntity("dagger", '\u2020'), new CharacterEntity("Dagger", '\u2021'), new CharacterEntity("bull", '\u2022'), new CharacterEntity("hellip", '\u2026'), new CharacterEntity("permil", '\u2030'), new CharacterEntity("prime", '\u2032'), new CharacterEntity("Prime", '\u2033'), new CharacterEntity("lsaquo", '\u2039'), new CharacterEntity("rsaquo", '\u203a'), new CharacterEntity("oline", '\u203e'), new CharacterEntity("frasl", '\u2044'), new CharacterEntity("euro", '\u20ac'), new CharacterEntity("image", '\u2111'), new CharacterEntity("weierp", '\u2118'), new CharacterEntity("real", '\u211c'), new CharacterEntity("trade", '\u2122'), new CharacterEntity("alefsym", '\u2135'), new CharacterEntity("larr", '\u2190'), new CharacterEntity("uarr", '\u2191'), new CharacterEntity("rarr", '\u2192'), new CharacterEntity("darr", '\u2193'), new CharacterEntity("harr", '\u2194'), new CharacterEntity("crarr", '\u21b5'), new CharacterEntity("lArr", '\u21d0'), new CharacterEntity("uArr", '\u21d1'), new CharacterEntity("rArr", '\u21d2'), new CharacterEntity("dArr", '\u21d3'), new CharacterEntity("hArr", '\u21d4'), new CharacterEntity("forall", '\u2200'), new CharacterEntity("part", '\u2202'), new CharacterEntity("exist", '\u2203'), new CharacterEntity("empty", '\u2205'), new CharacterEntity("nabla", '\u2207'), new CharacterEntity("isin", '\u2208'), new CharacterEntity("notin", '\u2209'), new CharacterEntity("ni", '\u220b'), new CharacterEntity("prod", '\u220f'), new CharacterEntity("sum", '\u2211'), new CharacterEntity("minus", '\u2212'), new CharacterEntity("lowast", '\u2217'), new CharacterEntity("radic", '\u221a'), new CharacterEntity("prop", '\u221d'), new CharacterEntity("infin", '\u221e'), new CharacterEntity("ang", '\u2220'), new CharacterEntity("and", '\u2227'), new CharacterEntity("or", '\u2228'), new CharacterEntity("cap", '\u2229'), new CharacterEntity("cup", '\u222a'), new CharacterEntity("int", '\u222b'), new CharacterEntity("there4", '\u2234'), new CharacterEntity("sim", '\u223c'), new CharacterEntity("cong", '\u2245'), new CharacterEntity("asymp", '\u2248'), new CharacterEntity("ne", '\u2260'), new CharacterEntity("equiv", '\u2261'), new CharacterEntity("le", '\u2264'), new CharacterEntity("ge", '\u2265'), new CharacterEntity("sub", '\u2282'), new CharacterEntity("sup", '\u2283'), new CharacterEntity("nsub", '\u2284'), new CharacterEntity("sube", '\u2286'), new CharacterEntity("supe", '\u2287'), new CharacterEntity("oplus", '\u2295'), new CharacterEntity("otimes", '\u2297'), new CharacterEntity("perp", '\u22a5'), new CharacterEntity("sdot", '\u22c5'), new CharacterEntity("lceil", '\u2308'), new CharacterEntity("rceil", '\u2309'), new CharacterEntity("lfloor", '\u230a'), new CharacterEntity("rfloor", '\u230b'), new CharacterEntity("lang", '\u2329'), new CharacterEntity("rang", '\u232a'), new CharacterEntity("loz", '\u25ca'), new CharacterEntity("spades", '\u2660'), new CharacterEntity("clubs", '\u22df'), new CharacterEntity("hearts", '\u2665'), new CharacterEntity("diams", '\u2666')};
	}
	
	public static String capitalize(String self) {
	    if (!(self.length() > 0))
	        throw new AssertionError("Cannot capitalize an empty string");
	    return self.substring(0, 1).toUpperCase() + self.substring(1, self.length());
	}
	
	public static Result<String, String> unescape(String self) {
	    if (!(self.charAt(0) == self.charAt(self.length() - 1)))
	        throw new AssertionError("Unbalanced quotes");
	    if (!(self.charAt(0) == '@' && ArrayHelpers.contains(new String[] {"\"", "'"}, Character.toString(self.charAt(1))) || ArrayHelpers.contains(new String[] {"\"", "'"}, Character.toString(self.charAt(0)))))
	        throw new AssertionError("String is not quoted");
	    if (!(self.length() >= 2))
	        throw new AssertionError("String is not quoted");
	    boolean isLiteral = self.charAt(0) == '@';
	    String quoted = self;
	    if (isLiteral)
	        quoted = quoted.substring(1, quoted.length());
	    if (isLiteral)
	        return new Ok(quoted.substring(1, quoted.length() - 1));
	    StringBuilder result = new StringBuilder(self.length() - 2);
	    int i = 1;
	    while (i < quoted.length() - 1) {
	        if (quoted.charAt(i) == '\\') {
	            if (i >= quoted.length() - 1)
	                return new Error("Unfinished escape sequence");
	            switch (quoted.charAt(i + 1)) {
	                case '\\':
	                    i++;
	                    result.append('\\');
	                    break;
	                case '&':
	                    Result<CharacterEntity, String> temp1 = StringExpansion.readCharacterEntity(quoted, i + 1);
	                    if (temp1 instanceof Result.Error)
	                        return new Result.Error<>(((Result.Error<CharacterEntity, String>)temp1).value);
	                    CharacterEntity characterEntity = ((Result.Ok<CharacterEntity, String>)temp1).value;
	                    i = i + ((characterEntity.stringValue).length() + 2);
	                    result.append(characterEntity.charValue);
	                    break;
	                case 't':
	                    i++;
	                    result.append('\t');
	                    break;
	                case 'r':
	                    i++;
	                    result.append('\r');
	                    break;
	                case 'n':
	                    i++;
	                    result.append('\n');
	                    break;
	                case 'b':
	                    i++;
	                    result.append('');
	                    break;
	                case 'f':
	                    i++;
	                    result.append('');
	                    break;
	                case '"':
	                    i++;
	                    result.append('"');
	                    break;
	                case '\'':
	                    i++;
	                    result.append('\'');
	                    break;
	                case 'u':
	                    if (i >= quoted.length() - 5)
	                        return new Error("Unfinished escape sequence");
	                    Result<Integer, String> temp2 = StringExpansion.readHexCharacter(quoted.charAt(i + 2));
	                    if (temp2 instanceof Result.Error)
	                        return new Result.Error<>(((Result.Error<Integer, String>)temp2).value);
	                    int hex0 = ((Result.Ok<Integer, String>)temp2).value;
	                    Result<Integer, String> temp3 = StringExpansion.readHexCharacter(quoted.charAt(i + 3));
	                    if (temp3 instanceof Result.Error)
	                        return new Result.Error<>(((Result.Error<Integer, String>)temp3).value);
	                    int hex1 = ((Result.Ok<Integer, String>)temp3).value;
	                    Result<Integer, String> temp4 = StringExpansion.readHexCharacter(quoted.charAt(i + 4));
	                    if (temp4 instanceof Result.Error)
	                        return new Result.Error<>(((Result.Error<Integer, String>)temp4).value);
	                    int hex2 = ((Result.Ok<Integer, String>)temp4).value;
	                    Result<Integer, String> temp5 = StringExpansion.readHexCharacter(quoted.charAt(i + 5));
	                    if (temp5 instanceof Result.Error)
	                        return new Result.Error<>(((Result.Error<Integer, String>)temp5).value);
	                    int hex3 = ((Result.Ok<Integer, String>)temp5).value;
	                    i = i + 5;
	                    result.append((char)hex0 << 12 | hex1 << 8 | hex2 << 4 | hex3);
	                    break;
	                default:
	                    return new Error("Illegal escape sequence");
	            }
	        }
	        else {
	            result.append(quoted.charAt(i));
	        }
	        i++;
	    }
	    return new Ok(result.toString());
	}
	
	public static String escape(String self, char quote, boolean escapeUnicode) {
	    StringBuilder output = new StringBuilder();
	    output.append(quote);
	    for (char c : self.toCharArray()) {
	        switch (c) {
	            case '\\':
	                output.append("\\\\");
	                break;
	            case '"':
	                if (quote == '"')
	                    output.append("\\\"");
	                else
	                    output.append('"');
	                break;
	            case '\'':
	                if (quote == '\'')
	                    output.append("\\'");
	                else
	                    output.append('\'');
	                break;
	            case '\n':
	                output.append("\\n");
	                break;
	            case '\r':
	                output.append("\\r");
	                break;
	            case '\t':
	                output.append("\\t");
	                break;
	            default:
	                if (escapeUnicode && c > '') {
	                    output.append("\\u").append(Strings.lpad(Integer.toHexString((int)c), 4, '0'));
	                }
	                else {
	                    output.append(c);
	                }
	        }
	    }
	    output.append(quote);
	    return output.toString();
	}
	
	private static Result<Integer, String> readHexCharacter(char hex) {
	    if (hex >= '0' && hex <= '9')
	        return new Ok(hex - '0');
	    if (hex >= 'A' && hex <= 'F')
	        return new Ok((hex - 'A') + 10);
	    if (hex >= 'a' && hex <= 'f')
	        return new Ok((hex - 'a') + 10);
	    return new Error("Illegal hex character: " + Character.toString(hex));
	}
	
	private static Result<CharacterEntity, String> readCharacterEntity(String str, int offset) {
	    if (!(str.charAt(offset) == '&'))
	        throw new AssertionError("Not a proper character entity");
	    if (offset + 3 >= str.length())
	        return new Error("Incomplete character entity");
	    int semi = str.indexOf(';', offset);
	    if (semi < 0)
	        return new Error("Incomplete character entity");
	    String entity = str.substring(offset + 1, semi);
	    if (entity.isEmpty())
	        return new Error("Character entity cannot be empty");
	    if (StringExpansion.NAMED_CHARACTER_ENTITIES.containsKey(entity))
	        return new Ok(StringExpansion.NAMED_CHARACTER_ENTITIES.get(entity));
	    if (entity.charAt(0) == '#') {
	        if (entity.length() < 2)
	            return new Error("Character entity number too short");
	        if (str.charAt(1) == 'x') {
	            if (entity.length() != 7)
	                return new Error("Hexadecimal character entity must have 4 hex digits");
	            int ivalue = Integer.parseInt(entity.substring(2, entity.length()), 16);
	            return new Ok(new CharacterEntity(entity, (char)ivalue));
	        }
	        else {
	            int ivalue = Integer.parseInt(entity.substring(1, entity.length()));
	            return new Ok(new CharacterEntity(entity, (char)ivalue));
	        }
	    }
	    return new Error("Not a valid named character entity");
	}
}