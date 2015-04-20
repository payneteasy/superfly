package com.payneteasy.superfly.password;

import java.security.SecureRandom;

/**
 * Generates passwords using a {@link SecureRandom}.
 *
 * @author Roman Puchkovskiy
 */
public class PasswordGeneratorImpl implements PasswordGenerator {
	
	private SecureRandom random = new SecureRandom();
	
	private int passwordLength = 6;

	public void setPasswordLength(int length) {
		passwordLength = length;
	}

	public String generate() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < passwordLength; i++) {
			char ch;
			do {
				int n = random.nextInt(62);
				if (n < 10) {
					ch = (char) (n + '0');
				} else if (n < 36) {
					ch = (char) (n - 10 + 'a');
				} else {
					ch = (char) (n - 36 + 'A');
				}
			} while (ch == 'o' || ch == 'O' || ch == 'l' || ch == 'I');
			buf.append(ch);
		}
		return buf.toString();
	}

}
