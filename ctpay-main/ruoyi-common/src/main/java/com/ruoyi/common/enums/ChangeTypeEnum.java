package com.ruoyi.common.enums;

/**
 * @author admin
 * @Date 2024/9/15 0:58
 */
public enum ChangeTypeEnum {
    BALANCE(1),
    FREEZE(2),
    PREPAMENT(3),
    ;
    private int value;

    ChangeTypeEnum(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
