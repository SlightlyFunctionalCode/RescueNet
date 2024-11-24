package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatternsCommands {
    APPROVE("^/approve(?:\\s(?<help>-h))?(?:\\s(?<requester>.+))?:(?<username>.+)$"),                                 // Approve-specific regex
    REJECT("^/reject(?:\\s(?<help>-h))?(?:\\s(?<requester>.+))?:(?<username>.+)$"),
    EXPORT("^/export(?:\\s(?<help>-h))?(?:\\s(?<startDate>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}))?(?:\\s(?<endDate>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}))?(?:\\s(?<tag>[^\\s:]+))?:(?<username>.+)$"),
    REQUEST("^(?<command>/\\w+|\\w+)(?:\\s+(?<requester>(?:[^\\s:]+(?:\\s[^\\s:]+)*)))?:(?<payload>.*)$"),
    JOIN("^/join(?:\\s(?<help>-h))?(?:\\s(?<name>.+))?:(?<requester>.+)$"),
    CHANGE_PERMISSIONS("^/change_permission(?:\\s(?<help>-h))?(?:\\s(?<name>.+)\\s(?<permission>.+))?:(?<requester>.+)$"),
    CREATE_GROUP("^/create_group(?:\\s(?<help>-h))?(?:\\s(?<name>.+)\\s(?<publicOrPrivate>.+))?:(?<requester>.+)$"),
    CHAT("^/chat(?:\\s(?<targetUsername>\\S+))?:(?<username>.+)$");

    private final Pattern pattern;

    RegexPatternsCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }
}
