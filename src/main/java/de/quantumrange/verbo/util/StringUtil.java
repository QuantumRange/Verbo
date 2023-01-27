package de.quantumrange.verbo.util;

import org.jetbrains.annotations.NotNull;

public class StringUtil {
	
	public static int damerauLevenshteinDistance(@NotNull String str1, @NotNull String str2) {
		int[][] substitutionMatrix = new int[str2.length() + 1][str1.length() + 1];
		
		for (int i = 0; i < substitutionMatrix.length; i++) {
			substitutionMatrix[i][0] = i;
		}
		
		for (int i = 0; i < substitutionMatrix[0].length; i++) {
			substitutionMatrix[0][i] = i;
		}
		
		for (int y = 1; y < str2.length() + 1; y++) {
			for (int x = 1; x < str1.length() + 1; x++) {
				substitutionMatrix[y][x] = min(
						substitutionMatrix[y][x - 1] + 1,
						substitutionMatrix[y - 1][x] + 1,
						substitutionMatrix[y - 1][x - 1] + (str2.charAt(y - 1) == str1.charAt(x - 1) ? 0 : 1)
				);
				if (y > 1 && x > 1 && str2.charAt(y - 1) == str1.charAt(x - 2) && str2.charAt(y - 2) == str1.charAt(x - 1)) {
					substitutionMatrix[y][x] = Math.min(substitutionMatrix[y][x], substitutionMatrix[y - 2][x - 2] + 1);
				}
			}
		}
		
		return substitutionMatrix[str2.length()][str1.length()];
	}
	
	private static int min(int i1, int i2, int i3) {
		return Math.min(i1, Math.min(i2, i3));
	}
	
}
