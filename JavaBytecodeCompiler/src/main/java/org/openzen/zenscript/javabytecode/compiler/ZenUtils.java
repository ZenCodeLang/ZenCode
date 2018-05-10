package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.CompareType;

import java.util.Objects;

public class ZenUtils {

    // ###############
    // ### Compare ###
    // ###############
    public static boolean compare(Object a, Object b, CompareType type) {
        if (type == CompareType.SAME || type == CompareType.NOTSAME) {
            final boolean same = a == b;
            return (type == CompareType.SAME) == same;
        }

        //TODO how to compare them?
        if (a instanceof Comparable)
            return checkCompareReturn(((Comparable) a).compareTo(b), type);
        return false;
    }


    public static boolean compare(boolean a, boolean b, CompareType compareType) {
        return checkCompareReturn(Boolean.compare(a, b), compareType);
    }

    public static boolean compare(int a, int b, CompareType compareType) {
        return checkCompareReturn(Integer.compare(a, b), compareType);
    }

    public static boolean compare(char a, char b, CompareType compareType) {
        return checkCompareReturn(Character.compare(a, b), compareType);
    }

    public static boolean compare(byte a, byte b, CompareType compareType) {
        return checkCompareReturn(Byte.compare(a, b), compareType);
    }

    public static boolean compare(short a, short b, CompareType compareType) {
        return checkCompareReturn(Short.compare(a, b), compareType);
    }

    public static boolean compare(long a, long b, CompareType compareType) {
        return checkCompareReturn(Long.compare(a, b), compareType);
    }

    public static boolean compare(float a, float b, CompareType compareType) {
        return checkCompareReturn(Float.compare(a, b), compareType);
    }

    public static boolean compare(double a, double b, CompareType compareType) {
        return checkCompareReturn(Double.compare(a, b), compareType);
    }

    private static boolean checkCompareReturn(int compareResult, CompareType type) {
        switch (type) {
            case LT:
                return compareResult < 0;
            case GT:
                return compareResult > 0;
            case EQ:
            case SAME:
                return compareResult == 0;
            case NOTSAME:
            case NE:
                return compareResult != 0;
            case LE:
                return compareResult <= 0;
            case GE:
                return compareResult >= 0;
            default:
                return false;
        }
    }
}
