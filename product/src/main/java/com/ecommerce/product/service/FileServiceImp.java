package com.ecommerce.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
public class FileServiceImp implements FileService {

    @Value("${project.image}")  // = images/
    private String rootDir;

    @Value("${image.base.url}") // = http://localhost:8082/images
    private String baseUrl;

    @Override
    public String uploadImage(String subFolder, MultipartFile file) throws IOException {
        // Tạo thư mục: images/products/ hoặc images/variants/
        Path dir = Paths.get(rootDir, subFolder);
        Files.createDirectories(dir);

        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : ".jpg";

        String fileName = UUID.randomUUID() + ext;
        Path target = dir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // TRẢ VỀ: chỉ path → "products/abc123.jpg"
        return subFolder + "/" + fileName;
    }

    // Dùng để tạo full URL khi trả DTO
    public String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        return baseUrl + "/" + imagePath;
    }

    // Xóa file cũ (khi update ảnh)
    public void deleteImage(String imagePath) {
        if (imagePath == null) return;
        Path file = Paths.get(rootDir, imagePath);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // log nếu cần
        }
    }
}