package com.ruoyi.web.core.config;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
@Component
public class S3UploadAndGetUrl {

    private static final String accessKeyId = "AKIA2UC3CHCZXWBEKXGG";
    private static final String secretAccessKey = "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";
    private static final String region = "ap-east-1";
    private static final String bucketName = "qzzfzfb";
    private static S3Client s3Client;

    static {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public static URL uploadObjectAndGetUrl( String keyName, MultipartFile file) {
        try {
            // 上传文件
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(S3UploadAndGetUrl.bucketName)
                    .key(keyName)
                    .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 获取上传文件的 URL
            GetUrlRequest request = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();
            URL url = s3Client.utilities().getUrl(request);
            return url;
        } catch (Exception e) {
            System.err.println("上传对象或获取 URL 时出错: " + e.getMessage());
            return null;
        }
    }

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    public static void main(String[] args) {
//        String keyName = "test";
//        File file = new File("/Users/zhangjie/IdeaProjects/ctpay/sql/ma.jpg");
////        URL url = uploadObjectAndGetUrl(keyName, file);
//        if (url != null) {
//            System.out.println("对象 URL: " + url.toString());
//        }
    }
}
