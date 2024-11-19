package org.estg.ipp.pt.Services;

import org.estg.ipp.pt.Classes.Enum.Permissions;

public record Operation(String name, Permissions requiredPermission) {
}
