package com.ruoyi.web.controller.business;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.RedisKeys;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.telegram.SFTelegramBot;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/loginValid")
@Api("登录二次验证码")
public class LoginValidController {
    @Resource
    private SFTelegramBot kmsfTelegramBot;
    @Autowired
    private RedisCache redisCache;
    @Resource
    private ISysUserService userService;

    @GetMapping("getValidCode")
    public AjaxResult getValidCode(@RequestParam(value = "userName", required = false)String userName) {
        if (StrUtil.isNotEmpty(userName)) {
            SysUser user = userService.selectUserByUserName(userName);
            if (ObjectUtil.isNull(user)) {
                throw new ServiceException("请确认用户名是否正确");
            }
            if (user.getIdentity() == 1 || (user.getIdentity() == 3 && user.getUserId() == 10053)) {
                return getCode(userName);
            }
        }
        return AjaxResult.success();
    }

    private AjaxResult getCode(String userName) {
        AjaxResult ajax = AjaxResult.success();
        SysUser sysUser = userService.selectUserById(1L);
        String botList = sysUser.getBotList();
        String validCode = "" + RandomUtil.randomInt(100000, 999999);
        if (!redisCache.hasKey(RedisKeys.validCode + userName)) {
            redisCache.setCacheObject(RedisKeys.validCode + userName, validCode, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
            if (botList != null && !botList.isEmpty()) {
                String[] bots = botList.split(";");
                for (String bot : bots) {
                    kmsfTelegramBot.sendReply(Long.parseLong(bot), "繁星验证码：" + validCode + ",有效期2分钟。");
                }
            }
        }
        ajax.put("uuid", userName);
        return ajax;
    }
}
