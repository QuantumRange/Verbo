package de.quantumrange.verbo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class InviteService {
	
	private final List<String> codes;
	
	public InviteService() {
		this.codes = new ArrayList<>();
	}
	
	public String generateCode() {
		String code = _generate();
		
		codes.add(code);
		
		return code;
	}
	
	private String _generate() {
		final Random rnd = new Random();
		final char[] chars = "0123456789ABCDEF".toCharArray();
		String str;
		
		do {
			str = _ch(rnd, chars, 2) + "-" + _ch(rnd, chars, 4) + "-" + _ch(rnd, chars, 2);
		} while (exist(str));
		
		return str;
	}
	
	private String _ch(Random rnd, char[] chars, int length) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < length; i++) {
			builder.append(_ch(rnd, chars));
		}
		
		return builder.toString();
	}
	
	private char _ch(Random rnd, char[] chars) {
		return chars[rnd.nextInt(chars.length)];
	}
	
	public boolean exist(String code) {
		return codes.contains(code.toUpperCase());
	}
	
	public void invalidate(String code) {
		codes.remove(code);
	}
	
}
