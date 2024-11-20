package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatternsCommands {
    APPROVE("^/approve\\s(?<requester>.+):(?<username>.+)$"),                                 // Approve-specific regex
    REJECT("^/reject\\s(?<requester>.+):(?<username>.+)$"),
    EXPORT("^/export\\s(?<startDate>.+)\\s(?<endDate>.+)\\s(?<filepath>.+):(?<username>.+)$"),                                 // Approve-specific regex
    REQUEST("^(?<command>/\\w+|\\w+)(?:\\s+(?<requester>(?:[^\\s:]+(?:\\s[^\\s:]+)*)))?:(?<payload>.*)$");

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
