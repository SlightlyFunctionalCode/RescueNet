package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.ServerSide.Services.GroupService;
import org.estg.ipp.pt.ServerSide.Services.UserService;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public interface ProcessInternalCommands {

    void handleIsReadConfirmation(String payload);

    void handleRegister(String payload, PrintWriter out);

    void handleLogin(String payload, PrintWriter out, Socket clientSocket, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);

    void handleLogout(String username, PrintWriter out);

    void handlePendingRequest(String payload);

    GroupService getGroupService();

    UserService getUserService();
}
