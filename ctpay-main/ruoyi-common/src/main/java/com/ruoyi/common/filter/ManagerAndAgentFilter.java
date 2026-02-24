package com.ruoyi.common.filter;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class ManagerAndAgentFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String url = req.getRequestURL().toString();
        if (!StrUtil.endWith( url, "/login") && !StrUtil.contains(url, "/order/") && !StrUtil.endWith(url, "/getValidCode")
                && !StrUtil.contains(url, "/onebuyApi")) {
            try {
                LoginUser loginUser = SecurityUtils.getLoginUser();
                if (loginUser != null) {
                    if (loginUser.getUser().getIdentity() == 0 || loginUser.getUser().getIdentity() == 1) {
                        if (StrUtil.isNotEmpty(loginUser.getUser().getAllowLoginIp()) && !IpUtils.isMatchedIp(loginUser.getUser().getAllowLoginIp(), IpUtils.getIpAddr())) {
                            log.error("登录IP为：{}", IpUtils.getIpAddr());
                            res.sendError(400, "not white list ip");
                            return;
                        }
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {

    }

    public static void main(String[] args) {
        System.out.println(SecurityUtils.encryptPassword("$2a$10$ryzK5031IEJmlUMSYXWn6.MSf6J41ew75TOembwWh0REcNgwmMFsS"));
    }
}
