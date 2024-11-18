package org.estg.ipp.pt.Classes.Enum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegexPatterns {
    DIRECTED_MESSAGE("^USER: (\\w+)\\s(.*)"), // Match "USER: <username> <message>"
    NOTIFICATION("^NOTIFICAÇÃO:(.*)"),       // Match "NOTIFICAÇÃO:<message>"
    COMMAND("^(MASS_EVACUATION|EMERGENCY_COMM|RESOURCE_DISTRIBUTION|APPROVE.*|REJECT.*)$"), // Match valid commands
    SERVER_PENDING("^PENDENTE"),            // Match server response "PENDENTE"
    SERVER_SUCCESS("^SUCESSO"),             // Match server response "SUCESSO"
    SERVER_ERROR("^ERRO:(.*)"),             // Match server response "ERRO:<error_message>"
    SERVER_APPROVE("^APPROVE"),             // Match server response "APPROVE"
    SERVER_REJECT("^REJECT"),               // Match server response "REJECT"
    SEND_TO("^SEND TO:(.*)"),               // Match "SEND TO:<username>"
    EMAIL("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"),        // Email validation
    LOGIN_SUCCESS("^SUCESSO\\s.*Grupo: ([\\d\\.]+):(\\d+)$"), // Match "SUCESSO Grupo: <address>:<port>"
    LOGIN_FAILED("^FAILED"),                             // Match "FAILED" response
    GENERIC_RESPONSE("^(SUCESSO|FAILED|ERROR):.*");      // Generic server response validation

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
