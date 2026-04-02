package com.neighbor.controller.api;

import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping("/images")
    public ApiResponse<List<ImageUploadResult>> uploadImages(
            @RequestParam("files") MultipartFile[] files) {

        List<ImageUploadResult> results = new ArrayList<>();

        for (MultipartFile file : files) {
            // 1. 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedImageType(contentType)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "只支持jpg、png、webp格式的图片");
            }

            // 2. 校验文件大小（最大10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件大小不能超过10MB");
            }

            try {
                // 3. 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;

                // 4. 保存到指定目录
                String relativePath = "items/" + uniqueFilename;
                Path filePath = Paths.get(uploadPath, relativePath);

                // 确保目录存在
                Files.createDirectories(filePath.getParent());

                // 保存文件
                file.transferTo(filePath);

                // 5. 返回相对URL路径
                results.add(new ImageUploadResult(relativePath, uniqueFilename));

            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
            }
        }

        return ApiResponse.ok(results);
    }

    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp");
    }

    public record ImageUploadResult(
            String url,  // 相对路径
            String filename
    ) {}
}
