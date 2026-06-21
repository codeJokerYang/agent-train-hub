package com.agenttrainhub.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 文件存储抽象。第一阶段为本地实现，后续可切换 MinIO。
 */
public interface StorageService {

    /**
     * 保存上传文件，同时计算大小与 SHA-256。
     *
     * @param file        上传文件
     * @param relativeDir 相对目录，如 {@code datasets/1}
     * @param fileName    目标文件名，如 {@code original.zip}
     */
    StoredFile store(MultipartFile file, String relativeDir, String fileName);

    /**
     * 写入生成的内容（如训练产物），同时计算大小与 SHA-256。
     *
     * @param content     文件内容
     * @param relativeDir 相对目录，如 {@code artifacts/jobs/1}
     * @param fileName    目标文件名，如 {@code model-demo.txt}
     */
    StoredFile writeBytes(byte[] content, String relativeDir, String fileName);

    /** 按相对存储路径读取为 Resource（供下载）。 */
    Resource loadAsResource(String storagePath);

    /** 删除文件（失败不抛出）。 */
    void delete(String storagePath);

    /** 解析相对存储路径为绝对路径。 */
    Path resolve(String storagePath);
}
