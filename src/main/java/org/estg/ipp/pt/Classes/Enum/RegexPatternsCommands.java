package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatternsCommands {
    APPROVE("^/approve\\s(?<requester>.+):(?<username>.+)$"),                                 // Approve-specific regex
    REJECT("^/reject\\s(?<requester>.+):(?<username>.+)$"),
    EXPORT_HELP("^/export\\s(?<help>-h):(?<username>.+)$"),                                 // Approve-specific regex
    EXPORT_TAG("^/export\\s(?<tag>.+):(?<username>.+)$"),                                 // Approve-specific regex
    EXPORT_DATE("^/export\\s(?<startDate>.+)\\s(?<endDate>.+):(?<username>.+)$"),                                 // Approve-specific regex
    EXPORT_DATE_TAG("^/export\\s(?<startDate>.+)\\s(?<endDate>.+)\\s(?<tag>.+):(?<username>.+)$"),                                 // Approve-specific regex
    REQUEST("^(?<command>/\\w+|\\w+)(?:\\s+(?<requester>(?:[^\\s:]+(?:\\s[^\\s:]+)*)))?:(?<payload>.*)$"),
    JOIN("^/join\\s(?<name>.+):(?<requester>.+)$");

    private final Pattern pattern;

    RegexPatternsCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
