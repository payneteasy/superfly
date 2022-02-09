package com.payneteasy.superfly.password;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;

import java.util.Arrays;
import java.util.List;

/**
 * Generates passwords
 */
public class PasswordGeneratorImpl implements PasswordGenerator {

    private static final String SPECIAL_CHARS = "~`!@#$%^&*+-=_|\\/()[]{}<>,.;:?\"'";
    private static final List<CharacterRule> RULES = Arrays.asList(
            new CharacterRule(EnglishCharacterData.LowerCase, 2),
            new CharacterRule(EnglishCharacterData.UpperCase, 2),
            new CharacterRule(EnglishCharacterData.Digit, 2),
            new CharacterRule(
                    new CharacterData() {
                        public String getErrorCode() {
                            return "SPECIAL_CHARS";
                        }

                        public String getCharacters() {
                            return SPECIAL_CHARS;
                        }
                    }, 1
            )
    );

    @Override
    public String generate() {
        return new org.passay.PasswordGenerator().generatePassword(8, RULES);
    }

}
