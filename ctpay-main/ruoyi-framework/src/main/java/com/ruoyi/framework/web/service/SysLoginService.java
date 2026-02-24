package com.ruoyi.framework.web.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.*;
import com.ruoyi.common.utils.*;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.domain.business.MerchantEntity;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.business.AgentService;
import com.ruoyi.system.service.business.MerchantService;
import com.ruoyi.system.service.business.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Slf4j(topic = "ct-business")
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysConfigService configService;
    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid, String validCode, String address)
    {
/*        // 验证码校验
        validateCaptcha(username, code, uuid);*/

        Object rc = redisCache.getCacheObject(CacheConstants.PWD_ERR_CNT_KEY + username);
        if (rc == null){
            rc = 0;
        }
        int retryCount = Integer.parseInt(rc.toString());
        if (retryCount >= maxRetryCount)
        {
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }
        //谷歌验证码
        SysUser sysUser = userService.selectUserByUserName(username);
        //用户校验
        if (ObjectUtil.isNull(sysUser)){
            log.error("用户登录用户名错误:{}", username);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "404"));
            throw new ServiceException("404");
        }
//        //地址校验
//        if (!StrUtil.equals(address, sysUser.getUid()) && !StrUtil.equals(address, "mmobile") && !StrUtil.equals(uuid, "mmobile")){
//            log.error("用户登录地址错误:{}", uuid);
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "404"));
//            throw new ServiceException("404");
//        }
        //码商校验
//        if (sysUser.getIdentity() == 5) {
//            MerchantEntity merchant = merchantService.getById(sysUser.getUserId());
//            if (merchant == null || merchant.getStatus() != 0) {
//                log.error("登录用户：{} 已被停用.", username);
//                throw new ServiceException(MessageUtils.message("user.blocked"));
//            }
//        }
        //如果是码商，则只能一个地方登录，踢出另外一个登录
/*        if (sysUser.getIdentity() == 5) {
            Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
            for (String key : keys) {
                LoginUser user = JSONUtil.toBean(redisCache.getCacheObject(key).toString(), LoginUser.class);
                if (user.getUserId().equals(sysUser.getUserId())) {
                    redisCache.deleteObject(key);
                    break;
                }
            }
        }*/
        //谷歌验证码
        if (sysUser.getGoogleSecretFlag() == 0 && StringUtils.isNotEmpty(sysUser.getGoogleSecret())) {
            try {
                Integer.parseInt(code);
            }catch (NumberFormatException e) {
                log.error("验证码错误:{}", username);
                retryCount = retryCount + 1;
                redisCache.setCacheObject(CacheConstants.PWD_ERR_CNT_KEY + username, retryCount + "", lockTime, TimeUnit.MINUTES);
                if (retryCount >= maxRetryCount){
                    throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
                }
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
            //判断谷歌验证码是否正确
            GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
            googleAuthenticator.setWindowSize(5);
            if (StrUtil.isEmpty(code) || !googleAuthenticator.check_code(sysUser.getGoogleSecret(), Integer.parseInt(code), System.currentTimeMillis())) {
                log.error("验证码错误:{}", username);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                retryCount = retryCount + 1;
                redisCache.setCacheObject(CacheConstants.PWD_ERR_CNT_KEY + username, retryCount + "", lockTime, TimeUnit.MINUTES);
                if (retryCount >= maxRetryCount){
                    throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
                }
                throw new CaptchaException();
            } else {
                if (redisCache.hasKey(CacheConstants.PWD_ERR_CNT_KEY + username)){
                    redisCache.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + username);
                }
            }
        }
        //管理员白名单
        if (sysUser.getIdentity() == 1 || (sysUser.getIdentity() == 3 && sysUser.getUserId() == 10053)) {
            //地址校验
            if (!StrUtil.equals(address, sysUser.getUid()) && !StrUtil.equals(address, "mmobile") && !StrUtil.equals(uuid, "mmobile")){
                log.error("用户登录地址错误:{}", address);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "404"));
                throw new ServiceException("404");
            }
            if (StrUtil.isEmpty(sysUser.getAllowLoginIp()) && !IpUtils.isMatchedIp(sysUser.getAllowLoginIp(), IpUtils.getIpAddr()))
            {
                log.error("登录IP为：{}", IpUtils.getIpAddr());
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "非白名单IP"));
                throw new NotWhiteListExcportion();
            }
            //校验二级码
            String verifyKey = RedisKeys.validCode + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (StrUtil.isEmpty(validCode) || !StrUtil.equals(captcha, validCode)) {
                retryCount = retryCount + 1;
                redisCache.setCacheObject(CacheConstants.PWD_ERR_CNT_KEY + username, retryCount + "", lockTime, TimeUnit.MINUTES);
                if (retryCount >= maxRetryCount){
                    throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
                }
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "二级验证码错误"));
                throw new CaptchaExpireException();
            } else {
                if (redisCache.hasKey(CacheConstants.PWD_ERR_CNT_KEY + username)){
                    redisCache.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + username);
                }
            }
        } else if (sysUser.getIdentity() == 1 || sysUser.getIdentity() == 3){
            //地址校验
            if (!StrUtil.equals(address, sysUser.getUid()) && !StrUtil.equals(address, "mmobile") && !StrUtil.equals(uuid, "mmobile")){
                log.error("用户登录地址错误:{}", address);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "404"));
                throw new ServiceException("404");
            }
        }
        //白名单校验
        if (StrUtil.isNotEmpty(sysUser.getAllowLoginIp()) && !IpUtils.isMatchedIp(sysUser.getAllowLoginIp(), IpUtils.getIpAddr()))
        {
            log.error("登录IP非白名单，登录IP为：{}，用户:{}", IpUtils.getIpAddr(), username);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "非白名单IP"));
            throw new NotWhiteListExcportion();
        }
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//        if (StringUtils.isEmpty(sysUser.getGoogleSecret())) {
//            loginUser.setShowGoogle(true);
//        }
        recordLoginInfo(sysUser);
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param user 用户ID
     */
    public boolean recordLoginInfo(SysUser user)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(user.getUserId());
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
        return false;
    }
}
