package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

public interface ExecuteUserCommands {
    void handleUserCommand(String command, String request, String requester, String payload, PrintWriter out,
                           ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline) throws IOException;
}
