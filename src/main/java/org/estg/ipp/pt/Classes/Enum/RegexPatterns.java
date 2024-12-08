package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatterns {
    REGISTER("^(?<username>.+),(?<email>.+),(?<password>.+)$"),
    LOGIN("^(?<username>.+),(?<password>.+)$"),
    READY("^(?<username>.+)$"),
    CONFIRM_READ("^CONFIRM_READ:(?<id>\\d+)$");

    private final Pattern pattern;

    RegexPatterns(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
