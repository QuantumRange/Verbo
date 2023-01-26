package de.quantumrange.verbo.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class CommonPasswordDetectionService {

	private final Set<String> knownPasswords = new HashSet<>();

	public CommonPasswordDetectionService() {
		File file = new File("passwords.txt");

		if (file.exists()) {
			try {
				List<String> list = Files.readAllLines(file.toPath());

				knownPasswords.addAll(list);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// TODO: Relocate
	@Deprecated(forRemoval = true)
	public String generateCode() {
		char[] allowed = "0123456789ABDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < 9; i++) {
			if (i == 4) builder.append('-');
			else builder.append(allowed[new Random().nextInt(allowed.length)]);
		}

		return builder.toString();
	}

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

	public boolean isUsedPassword(String password) {
		return knownPasswords.contains(password);
	}

}