package org.estg.ipp.pt.ClientSide.Classes.Enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ServerResponseRegex {
    SERVER_PENDING("^PENDENTE$"),
    SERVER_SUCCESS("^SUCESSO$"),
    SERVER_ERROR("^ERRO:(?<errorMessage>.*)$"),
    SERVER_APPROVE("^APPROVE$"),
    SERVER_REJECT("^REJECT$"),
    LOGIN_SUCCESS("^SUCESSO:.*Grupo:\\s(?<address>[\\d\\.]+):(?<port>\\d+):(?<name>.+)$"),
    LOGIN_FAILED("^FAILED$"),
    GENERIC_RESPONSE("^(?<status>SUCESSO|FAILED|ERROR):.*$"),
    EMAIL("^(?<email>[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,})$"),
    MESSAGE("^PRIVATE:(?<id>/\\d+/).+$"),
    SERVER_CHAT_GROUP("^CHAT_GROUP:(?<address>.+):(?<port>.+)");

    private final Pattern pattern;

    ServerResponseRegex(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
