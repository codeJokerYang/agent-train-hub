package com.agenttrainhub.common;

import org.springframework.core.io.Resource;

/**
 * 下载文件载体，供数据集与模型产物下载复用。
 */
public record DownloadFile(Resource resource, String filename, Long size) {
}
