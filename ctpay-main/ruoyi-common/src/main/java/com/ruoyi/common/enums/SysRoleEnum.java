package com.ruoyi.common.enums;

import lombok.Getter;

@Getter
public enum SysRoleEnum {

    ADMIN(1, "admin"),
    COMMON(2, "common"),
    AGENT(3,"agent"),
    SHOP(4,"shop"),
    MERCHANT(5,"merchant"),
    ;

    private final long roleId;
    private final String roleKey;

    SysRoleEnum(long roleId, String roleKey){
        this.roleId = roleId;
        this.roleKey = roleKey;
    }

}
