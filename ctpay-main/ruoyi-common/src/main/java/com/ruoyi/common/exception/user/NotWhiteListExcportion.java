package com.ruoyi.common.exception.user;

public class NotWhiteListExcportion extends UserException{
    private static final long serialVersionUID = 1L;

    public NotWhiteListExcportion()
    {
        super("login.notWhiteList", null);
    }
}
