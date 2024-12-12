package org.estg.ipp.pt.ServerSide.Classes;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;
import org.estg.ipp.pt.ServerSide.Interfaces.ExecuteInternalCommands;
import org.estg.ipp.pt.ServerSide.Interfaces.ProcessInternalCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExecuteInternalCommandsImpl implements ExecuteInternalCommands {

    @Autowired
    ProcessInternalCommands processInternalCommands;

    public boolean isInternalCommand(String command) {
        return command.equals("REGISTER") || command.equals("LOGIN") || command.equals("LOGOUT") || command.equals("READY") || command.equals("CONFIRM_READ");
    }

    public void handleInternalCommand(String command, String payload, PrintWriter out, Socket clientSocket, List<Group> groupList, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) {
        switch (command) {
            case "REGISTER" -> processInternalCommands.handleRegister(payload, out);
            case "LOGIN" -> processInternalCommands.handleLogin(payload, out, clientSocket, usersWithPermissionsOnline);
            case "LOGOUT" -> processInternalCommands.handleLogout(payload, out);
            case "READY" -> processInternalCommands.handlePendingRequest(payload);
            case "CONFIRM_READ" -> new Thread(() -> processInternalCommands.handleIsReadConfirmation(command + ":" + payload)).start();

            default -> out.println("ERRO: Comando interno inválido");
        }
    }

    public ProcessInternalCommands getProcessInternalCommands() {
        return processInternalCommands;
    }
}


