package com.agenttrainhub.storage;

import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 本地文件存储实现。文件落在 {@code base-path}（默认 ./data）下。
 */
@Service
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(@Value("${agenttrainhub.storage.base-path:./data}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file, String relativeDir, String fileName) {
        try {
            Path dir = resolveContained(relativeDir);
            Files.createDirectories(dir);
            Path target = resolveContained(relativeDir, fileName);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream();
                 DigestInputStream din = new DigestInputStream(in, digest);
                 OutputStream out = Files.newOutputStream(target)) {
                din.transferTo(out);
            }
            String hash = HexFormat.of().formatHex(digest.digest());
            String storagePath = (relativeDir + "/" + fileName).replace("\\", "/");
            return new StoredFile(storagePath, Files.size(target), hash);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件保存失败: " + ex.getMessage());
        }
    }

    @Override
    public StoredFile writeBytes(byte[] content, String relativeDir, String fileName) {
        try {
            Path dir = resolveContained(relativeDir);
            Files.createDirectories(dir);
            Path target = resolveContained(relativeDir, fileName);
            Files.write(target, content);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String hash = HexFormat.of().formatHex(digest.digest(content));
            String storagePath = (relativeDir + "/" + fileName).replace("\\", "/");
            return new StoredFile(storagePath, content.length, hash);
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件写入失败: " + ex.getMessage());
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path path = resolve(storagePath);
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BizException(ErrorCode.NOT_FOUND, "文件不存在或不可读");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件读取失败");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolve(storagePath));
        } catch (IOException ignored) {
            // 删除物理文件失败不阻断业务
        }
    }

    @Override
    public Path resolve(String storagePath) {
        return resolveContained(storagePath);
    }

    private Path resolveContained(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw BizException.paramError("存储路径不能为空");
        }
        Path supplied = Paths.get(relativePath);
        if (supplied.isAbsolute()) {
            throw BizException.paramError("存储路径必须是相对路径");
        }
        Path resolved = basePath.resolve(supplied).normalize();
        if (!resolved.startsWith(basePath)) {
            throw BizException.paramError("存储路径越界");
        }
        return resolved;
    }

    private Path resolveContained(String relativeDir, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw BizException.paramError("文件名不能为空");
        }
        Path suppliedName = Paths.get(fileName);
        if (suppliedName.isAbsolute() || suppliedName.getNameCount() != 1
                || ".".equals(fileName) || "..".equals(fileName)) {
            throw BizException.paramError("文件名不能包含路径");
        }
        Path dir = resolveContained(relativeDir);
        Path resolved = dir.resolve(suppliedName).normalize();
        if (!resolved.startsWith(dir) || !resolved.startsWith(basePath)) {
            throw BizException.paramError("文件路径越界");
        }
        return resolved;
    }
}
