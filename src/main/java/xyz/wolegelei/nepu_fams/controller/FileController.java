package xyz.wolegelei.nepu_fams.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String suffix = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            suffix = originalName.substring(dotIndex);
        }

        String datePath = LocalDate.now().toString();
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", datePath);
        Files.createDirectories(uploadDir);

        String fileName = UUID.randomUUID() + suffix;
        Path target = uploadDir.resolve(fileName);
        file.transferTo(target.toFile());

        String url = "/uploads/" + datePath + "/" + fileName;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        data.put("name", originalName);
        return Result.success(data);
    }
}
