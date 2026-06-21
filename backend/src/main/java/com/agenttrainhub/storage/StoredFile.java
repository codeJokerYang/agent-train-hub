package com.agenttrainhub.storage;

/**
 * 文件落地结果。
 *
 * @param storagePath 相对存储路径（相对 base-path），如 {@code datasets/1/original.zip}
 * @param size        文件大小（字节）
 * @param sha256      SHA-256 十六进制
 */
public record StoredFile(String storagePath, long size, String sha256) {
}
