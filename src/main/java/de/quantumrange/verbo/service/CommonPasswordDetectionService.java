package de.quantumrange.verbo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CommonPasswordDetectionService {
	
	private static final Logger log = LoggerFactory.getLogger(CommonPasswordDetectionService.class);
	private final Set<String> knownPasswords = new HashSet<>();
	
	public CommonPasswordDetectionService() {
		File file = new File("passwords.txt");
		
		if (file.exists()) {
			try {
				List<String> list = Files.readAllLines(file.toPath());
				
				knownPasswords.addAll(list);
			} catch (IOException e) {
				log.error("Can't load common password file, does the file exist?", e);
			}
		}
	}
	
	public boolean isUsedPassword(String password) {
		return knownPasswords.contains(password);
	}
	
}