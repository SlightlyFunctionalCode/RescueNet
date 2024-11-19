package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatterns {
    REQUEST("^(?<command>\\w+)(?:\\s+(?<requester>\\w+))?:(?<payload>.*)?$"), // Command, requester (optional), payload (optional)
    REGISTER("^REGISTER:(?<username>.+),(?<email>.+),(?<password>.+)$"),    // Register-specific regex
    LOGIN("^LOGIN:(?<username>.+),(?<password>.+)$"),                       // Login-specific regex
    APPROVE("^APPROVE\\s(?<username>.+)$"),                                 // Approve-specific regex
    REJECT("^REJECT\\s(?<username>.+)$"),                                   // Reject-specific regex
    DIRECTED_MESSAGE("^USER: (?<username>\\w+)\\s(?<message>.*)$"),         // Directed message: username and message
    NOTIFICATION("^NOTIFICAÇÃO:(?<message>.*)$"),                           // Notification: message
    COMMAND("^(?<command>MASS_EVACUATION|EMERGENCY_COMM|RESOURCE_DISTRIBUTION|APPROVE.*|REJECT.*)$"), // Command group
    SERVER_PENDING("^PENDENTE$"),                                          // Server response: "PENDENTE"
    SERVER_SUCCESS("^SUCESSO$"),                                           // Server response: "SUCESSO"
    SERVER_ERROR("^ERRO:(?<errorMessage>.*)$"),                            // Server error with errorMessage
    SERVER_APPROVE("^APPROVE (?<requester>.+):(?<username>.+)$"),                                           // Server response: "APPROVE"
    SERVER_REJECT("^REJECT (?<requester>.+):(?<username>.+)$"),                                             // Server response: "REJECT"
    SEND_TO("^SEND TO:(?<recipient>.*)$"),                                 // Send to a specific recipient
    EMAIL("^(?<email>[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,})$"),               // Email validation
    LOGIN_SUCCESS("^SUCESSO:.*Grupo:\\s(?<address>[\\d\\.]+):(?<port>\\d+)$"), // Login success with address and port
    LOGIN_FAILED("^FAILED$"),                                              // Login failed
    GENERIC_RESPONSE("^(?<status>SUCESSO|FAILED|ERROR):.*$");      // Generic server response validation

    private final Pattern pattern;

    RegexPatterns(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
