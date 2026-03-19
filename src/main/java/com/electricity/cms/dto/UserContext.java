package com.electricity.cms.dto;

import java.util.UUID;

import com.electricity.cms.model.UserRole;

public record UserContext(
    UUID userId,
    UserRole role,
    UUID regionId,
    String displayName
) {
}
