package com.campus_platform.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class UploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 【新增】：常见真实图片的二进制文件头（魔数）
    private static final String[] IMAGE_MAGIC_NUMBERS = {
            "FFD8FF", // JPG/JPEG
            "89504E47", // PNG
            "47494638"  // GIF
    };

    // 【新增】：读取文件前4个字节并转为十六进制字符串
    private String getFileHeader(MultipartFile file) throws IOException {
        byte[] bytes = new byte[4];
        file.getInputStream().read(bytes, 0, bytes.length);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString().toUpperCase();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // ====== 【终极安全防线：魔数探伤校验】 ======
        String header = getFileHeader(file);
        boolean isImage = false;
        for (String magic : IMAGE_MAGIC_NUMBERS) {
            if (header.startsWith(magic)) {
                isImage = true;
                break;
            }
        }
        if (!isImage) {
            throw new RuntimeException("安全拦截：禁止上传非图片格式的伪装文件！");
        }
        // ===========================================

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return null;
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;
        Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(targetLocation)) {
            Files.createDirectories(targetLocation);
        }

        Path targetFile = targetLocation.resolve(fileName);
        file.transferTo(targetFile.toFile());

        return "/upload/" + fileName;
    }
}