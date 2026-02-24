package com.ruoyi.web.controller.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.*;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.web.core.config.MinioUtil;
import com.ruoyi.web.core.config.S3UploadAndGetUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private S3UploadAndGetUrl s3UploadAndGetUrl;
    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传并返回新文件名称
            String url = s3UploadAndGetUrl.uploadObjectAndGetUrl( IdUtils.simpleUUID(),file).toString();
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", file.getName());
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/getSecret")
    public AjaxResult getSecret(){
        try {
            String secret = DESUtil.createSecret();
            return AjaxResult.success(secret);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    @GetMapping("/createQrCode")
    public R<String> createQrCode(@RequestParam("text")String text, @RequestParam("weight")int weight,
                                  @RequestParam("height")int height, @RequestParam(value = "imageUrl", required = false)String imageUrl){
        try {
            return R.ok(QrCodeUtils.createdQrCodeBase64(text, weight, height, imageUrl));
        } catch (Exception ignored) {
            log.error("生成二维码失败,{}", ignored.getMessage());
        }
        return R.fail();
    }

    @PostMapping("/qrcodeAnalysis")
    public R<String> qrcodeAnalysis(@RequestParam("file") MultipartFile file){
        try {
            String analysis = QrCodeUtil.decode(file.getInputStream());
            return R.ok(analysis);
        } catch (IOException e) {
            log.error("解析二维码失败{}", e.getMessage());
        }
        return R.ok();
    }

    @GetMapping("/getGoogleSecret")
    public R<Map<String,String>> getGoogleSecret(){
        Map<String,String> result = new HashMap<String,String>();
        String googleScret = GoogleAuthenticator.generateSecretKey();
        result.put("googleScret", googleScret);
        try {
            String qrStr = "otpauth://totp/"+ SecurityUtils.getUsername() +"?secret=" + googleScret;
            result.put("googleScretCode", QrCodeUtils.createdQrCodeBase64(qrStr, 200, 200, null));
        } catch (MalformedURLException e) {

        }
        return R.ok(result);
    }

}
