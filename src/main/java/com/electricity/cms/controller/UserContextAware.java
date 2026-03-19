package com.electricity.cms.controller;

import com.electricity.cms.dto.UserContext;

public interface UserContextAware {
    void setUserContext(UserContext userContext);
}
