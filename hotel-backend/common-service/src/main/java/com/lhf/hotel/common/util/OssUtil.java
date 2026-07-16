package com.lhf.hotel.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.lhf.hotel.common.config.OssProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class OssUtil {

    private static final Logger log = LoggerFactory.getLogger(OssUtil.class);

    private final OssProperties properties;

    public OssUtil(OssProperties properties) {
        this.properties = properties;
    }

    /**
     * 上传文件到 OSS，返回完整 HTTPS URL。
     *
     * @param file      上传的文件
     * @param directory OSS 对象 key 前缀，如 "hotel/room-images"
     * @return 上传后的 HTTPS URL
     */
    public String upload(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = directory + "/" + UUID.randomUUID() + extension;

        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());

        try {
            ossClient.putObject(properties.getBucketName(), objectKey, file.getInputStream());
            log.info("OSS upload success: {}", objectKey);
        } catch (IOException e) {
            log.error("OSS upload failed: {}", objectKey, e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            ossClient.shutdown();
        }

        return String.format("https://%s.%s/%s",
                properties.getBucketName(), properties.getEndpoint(), objectKey);
    }

    /**
     * 根据完整 URL 删除 OSS 文件。
     *
     * @param fileUrl upload() 返回的完整 HTTPS URL
     */
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String prefix = String.format("https://%s.%s/",
                properties.getBucketName(), properties.getEndpoint());
        if (!fileUrl.startsWith(prefix)) return;

        String objectKey = fileUrl.substring(prefix.length());

        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());

        try {
            ossClient.deleteObject(properties.getBucketName(), objectKey);
            log.info("OSS delete success: {}", objectKey);
        } catch (Exception e) {
            log.warn("OSS delete failed: {}", objectKey, e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }
}
