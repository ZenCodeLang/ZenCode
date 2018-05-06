package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.CompareType;

import java.util.Objects;

public class ZenUtils {

    // ###############
    // ### Compare ###
    // ###############

    //TODO how to compare them?
    public static boolean compare(Object a, Object b, String compareType) {
        CompareType type = CompareType.valueOf(compareType);
        if (type == CompareType.SAME || type == CompareType.NOTSAME) {
            final boolean same = a == b;
            return (type == CompareType.SAME) == same;
        }

        if (a instanceof Comparable)
            return checkCompareReturn(((Comparable) a).compareTo(b), type);
        return false;
    }


    public static boolean compare(boolean a, boolean b, String compareType) {
        return checkCompareReturn(Boolean.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(int a, int b, String compareType) {
        return checkCompareReturn(Integer.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(char a, char b, String compareType) {
        return checkCompareReturn(Character.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(byte a, byte b, String compareType) {
        return checkCompareReturn(Byte.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(short a, short b, String compareType) {
        return checkCompareReturn(Short.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(long a, long b, String compareType) {
        return checkCompareReturn(Long.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(float a, float b, String compareType) {
        return checkCompareReturn(Float.compare(a, b), CompareType.valueOf(compareType));
    }

    public static boolean compare(double a, double b, String compareType) {
        return checkCompareReturn(Double.compare(a, b), CompareType.valueOf(compareType));
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
