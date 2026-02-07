package com.cmci.cr.application.dto.command;

import com.cmci.cr.domain.valueobject.Role;
import lombok.Builder;
import lombok.Value;

/**
 * Command pour assigner un rôle à un utilisateur
 */
@Value
@Builder
public class AssignRoleCommand {
    String userId;
    Role newRole;
    String assignedByUserId;
}
