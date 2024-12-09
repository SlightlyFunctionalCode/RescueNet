package org.estg.ipp.pt.ServerSide.Interfaces;

import org.estg.ipp.pt.Classes.Enum.Permissions;
import org.estg.ipp.pt.Classes.Enum.TagType;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface ProcessCommandsInterface {

    void processExport(String request, PrintWriter out);

    void processExportByDateRangeCommand(LocalDateTime startDate, LocalDateTime endDate, String username, PrintWriter out);

    void processExportByDateRangeAndTagCommand(LocalDateTime startDate, LocalDateTime endDate, TagType tagType, String username, PrintWriter out);

    void processExportByTagCommand(TagType tagType, String username, PrintWriter out);

    void processExportURL(String url, String username, PrintWriter out);

    void processJoinCommand(String username, String name, PrintWriter out);

    void processChangePermissionCommand(String username, String name, Permissions permission, PrintWriter out);

    void processCreateGroupCommand(String username, String name, String publicOrPrivate, PrintWriter out);

    void processOperationCommand(String username, String command, PrintWriter out, ConcurrentHashMap<String, Permissions> usersWithPermissionsOnline);

    void handleApprovalCommand(String action, long id, String username, String requester, PrintWriter out);

    void handleCommandHelper(String username, PrintWriter out);

    void handleAddToGroup(String username, String requester, String group, PrintWriter out);

    void handleListGroups(String username, PrintWriter out);

    void handleLeaveGroup(String username, String group, PrintWriter out);
}
