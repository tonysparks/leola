/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm.util;

import leola.vm.asm.Outer;
import leola.vm.asm.OuterDesc;
import leola.vm.types.LeoObject;

/**
 * @author Tony
 *
 */
public final class ArrayUtil {

	public static final LeoObject[] EMPTY_LEOOBJECTS = {};
	public static final Outer[] EMPTY_OUTERS = {};
	
	public static String[] resize(String[] array, int len) {
		if ( array.length > len) {
			throw new IllegalArgumentException("Original array length is greater than: " + len);
		}
		
		String[] newarray = new String[len];
		System.arraycopy(array, 0, newarray, 0, array.length);
		return newarray;
	}
	
	public static LeoObject[] resize(LeoObject[] array, int len) {
		if ( array.length > len) {
			throw new IllegalArgumentException("Original array length is greater than: " + len);
		}
		
		LeoObject[] newarray = new LeoObject[len];
		System.arraycopy(array, 0, newarray, 0, array.length);
		return newarray;
	}
		
	public static OuterDesc[] resize(OuterDesc[] array, int len) {
		if ( array.length > len) {
			throw new IllegalArgumentException("Original array length is greater than: " + len);
		}
		
		OuterDesc[] newarray = new OuterDesc[len];
		System.arraycopy(array, 0, newarray, 0, array.length);
		return newarray;
	}

	public static Outer[] resize(Outer[] array, int len) {
		if ( array.length > len) {
			throw new IllegalArgumentException("Original array length is greater than: " + len);
		}
		
		Outer[] newarray = new Outer[len];
		System.arraycopy(array, 0, newarray, 0, array.length);
		return newarray;
	}
	
	public static Outer[] newOuterArray() {
		return new Outer[10];
	}
	
	public static OuterDesc[] newOuterDescArray() {
		return new OuterDesc[10];
	}
	
	public static LeoObject[] newLeoObjectArray() {
		return new LeoObject[10];
	}
	
	public static String[] newStringArray() {
		return new String[10];
	}
	
	  /**
     * Tuning parameter: list size at or below which insertion sort will be
     * used in preference to mergesort or quicksort.
     */
    private static final int INSERTIONSORT_THRESHOLD = 7;

    public static <T> void sort(T[] src) {
    	 mergeSort(src, src, 0, src.length, 0);
    }
    
    public static <T> void sort(T[] src, int offset, int len) {
    	mergeSort(src, src, offset, len, offset);
    }
    
	/**
	 * Src is the source array that starts at index 0 Dest is the (possibly
	 * larger) array destination with a possible offset low is the index in dest
	 * to start sorting high is the end index in dest to end sorting off is the
	 * offset to generate corresponding low, high in src
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mergeSort(Object[] src, Object[] dest, int low,
			int high, int off) {
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low
						&& ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low += off;
		high += off;
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid
					&& ((Comparable) src[p]).compareTo(src[q]) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	/**
	 * Swaps x[a] with x[b].
	 */
	private static void swap(Object[] x, int a, int b) {
		Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

}

