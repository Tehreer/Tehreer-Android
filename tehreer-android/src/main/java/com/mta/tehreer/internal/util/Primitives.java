package com.mta.tehreer.internal.util;

import com.mta.tehreer.util.ByteList;
import com.mta.tehreer.util.FloatList;
import com.mta.tehreer.util.IntList;
import com.mta.tehreer.util.PointList;
import com.mta.tehreer.util.PrimitiveList;

public class Primitives {

    public static boolean equals(PrimitiveList list, Object obj) {
        if (list == obj) {
            return true;
        }

        if (list == null) {
            return false;
        }

        if (list instanceof ByteList) {
           return equals((ByteList) list, obj);
        }

        if (list instanceof IntList) {
            return equals((IntList) list, obj);
        }

        if (list instanceof FloatList) {
            return equals((FloatList) list, obj);
        }

        if (list instanceof PointList) {
            return equals((PointList) list, obj);
        }

        return false;
    }

    private static boolean equals(ByteList list, Object obj) {
        if (!(obj instanceof ByteList)) {
            return false;
        }

        ByteList bytes = (ByteList) obj;
        int size = bytes.size();

        if (list.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (list.get(i) != bytes.get(i)) {
                return false;
            }
        }

        return true;
    }

    private static boolean equals(IntList list, Object obj) {
        if (!(obj instanceof IntList)) {
            return false;
        }

        IntList ints = (IntList) obj;
        int size = ints.size();

        if (list.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (list.get(i) != ints.get(i)) {
                return false;
            }
        }

        return true;
    }

    private static boolean equals(FloatList list, Object obj) {
        if (!(obj instanceof FloatList)) {
            return false;
        }

        FloatList floats = (FloatList) obj;
        int size = floats.size();

        if (list.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (Float.floatToIntBits(list.get(i)) != Float.floatToIntBits(floats.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean equals(PointList list, Object obj) {
        if (!(obj instanceof PointList)) {
            return false;
        }

        PointList points = (PointList) obj;
        int size = points.size();

        if (list.size() != size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (list.getX(i) != points.getX(i) || list.getY(i) != points.getY(i)) {
                return false;
            }
        }

        return true;
    }

    public static int hashCode(PrimitiveList list) {
        if (list instanceof ByteList) {
            return hashCode((ByteList) list);
        }

        if (list instanceof IntList) {
            return hashCode((IntList) list);
        }

        if (list instanceof FloatList) {
            return hashCode((FloatList) list);
        }

        if (list instanceof PointList) {
            return hashCode((PointList) list);
        }

        return 0;
    }

    private static int hashCode(ByteList list) {
        int size = list.size();
        int result = 1;

        for (int i = 0; i < size; i++) {
            result = 31 * result + list.get(i);
        }

        return result;
    }

    private static int hashCode(IntList list) {
        int size = list.size();
        int result = 1;

        for (int i = 0; i < size; i++) {
            result = 31 * result + list.get(i);
        }

        return result;
    }

    private static int hashCode(FloatList list) {
        int size = list.size();
        int result = 1;

        for (int i = 0; i < size; i++) {
            result = 31 * result + Float.floatToIntBits(list.get(i));
        }

        return result;
    }

    private static int hashCode(PointList list) {
        int size = list.size();
        int result = 1;

        for (int i = 0; i < size; i++) {
            result = 31 * result + Float.floatToIntBits(list.getX(i));
            result = 31 * result + Float.floatToIntBits(list.getY(i));
        }

        return result;
    }

    private Primitives() {
    }
}
