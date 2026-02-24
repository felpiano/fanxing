package com.ruoyi.system.telegram.tgutil;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.domain.business.ShopEntity;
import com.ruoyi.system.telegram.tgDataEntity.TgOrderInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TgUserUtil {
    //tg商户用户管理员
    public static Map<String, String> tgBusinessUserMap = new HashMap<>();
    //tg平台用户管理员
    public static Map<String, String> tgPlateFormUserMap = new HashMap<>();
    //卡密订单和tg消息关联信息
    public static Map<String, List<TgOrderInfo>> messageMap = new HashMap<>();

    public static void setTgUserMap(List<ShopEntity> asList, List<MerchantEntity> pfList) {
        Map<String, String> asMap = asList.stream()
                .filter(entity -> StringUtils.isNotBlank(entity.getTelegramGroup()))
                .collect(Collectors.toMap(
                        entity -> entity.getTelegramGroup().split("/")[0],
                        entity -> {
                            // 将Telegram字符串中的:替换为,并去掉最后的:和,
                            String telegram = entity.getTelegram();
                            if (telegram != null) {
                                return Arrays.stream(telegram.split(","))
                                        .map(tg -> tg.split("/")[0]) // 获取ID部分
                                        .collect(Collectors.joining(",")); // 用逗号连接
                            }
                            return ""; // 如果telegram为null则返回空字符串
                        },
                        (existing, replacement) -> replacement // 覆盖重复的键
                ));
        Map<String, String> pfMap = pfList.stream()
                .filter(entity -> StringUtils.isNotBlank(entity.getTelegramGroup()))
                .collect(Collectors.toMap(
                        entity -> entity.getTelegramGroup().split("/")[0],
                        entity -> {
                            String telegram = entity.getTelegram();
                            if (telegram != null) {
                                return Arrays.stream(telegram.split(","))
                                        .map(tg -> tg.split("/")[0]) // 获取ID部分
                                        .collect(Collectors.joining(",")); // 用逗号连接
                            }
                            return "";
                        },
                        (existing, replacement) -> replacement // 覆盖重复的键
                ));
        tgBusinessUserMap = asMap;
        tgPlateFormUserMap = pfMap;
    }

}
