package com.ruoyi.common.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import sun.misc.BASE64Encoder;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class QrCodeUtils {
    public static String createdQrCodeBase64(String text,int width,int height,String urlstr) throws MalformedURLException {
        String base64 = "";
        QrConfig qrConfig = new QrConfig(width,height);
        //设置边距
        qrConfig.setMargin(0);
        //设置二维码颜色
        qrConfig.setForeColor(Color.BLACK.getRGB());
        //设置背景色
        qrConfig.setBackColor(Color.WHITE.getRGB());
        //二维码中间图片
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(urlstr)) {
            URL url = new URL(urlstr);
            Image image = ImgUtil.getImage(url);
            qrConfig.setImg(image);
            qrConfig.setRatio(5);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QrCodeUtil.generate(text, qrConfig, ImgUtil.IMAGE_TYPE_PNG, out);
        byte[] data = out.toByteArray();
        BASE64Encoder base64Encoder = new BASE64Encoder();
        base64 = base64Encoder.encode(data);
        return "data:image/png;base64,"+base64;
    }


}
