package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Group;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface ExecuteInternalCommands {
    boolean isInternalCommand(String command);

    void handleInternalCommand(String command, String payload, PrintWriter out,
                               Socket clientSocket, List<Group> groupList, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);

    ProcessInternalCommands getProcessInternalCommands();
}
