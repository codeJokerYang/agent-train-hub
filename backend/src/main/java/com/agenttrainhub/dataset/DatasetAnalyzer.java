package com.agenttrainhub.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 内置数据集分析器（第一阶段：纯 Java，不依赖 Python 进程）。
 *
 * <p>对 zip 统计文件数、推断类型、把目录名当作类别；其余类型给出基础画像。
 * 任何异常都转成 warnings，永不抛出，保证「分析失败不影响上传」。
 * 与 worker/dataset_profile.py 输出结构保持一致，后续可替换为真实 Python 分析。</p>
 */
@Component
public class DatasetAnalyzer {

    private static final Set<String> IMAGE_EXTS =
            Set.of("jpg", "jpeg", "png", "bmp", "gif", "webp");

    private final ObjectMapper objectMapper;

    public DatasetAnalyzer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 返回 profile JSON 字符串。 */
    public String analyze(Path file, String type, long totalSize) {
        Map<String, Object> profile = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        int fileCount = 1;
        String detected = type == null ? "OTHER" : type;
        List<String> classes = new ArrayList<>();

        try {
            if (file.getFileName().toString().toLowerCase().endsWith(".zip")) {
                int files = 0;
                int images = 0;
                Set<String> classSet = new TreeSet<>();
                try (ZipFile zf = new ZipFile(file.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String[] parts = Arrays.stream(entry.getName().split("/"))
                                .filter(s -> !s.isEmpty())
                                .toArray(String[]::new);
                        if (entry.isDirectory()) {
                            if (parts.length >= 1) {
                                classSet.add(parts[parts.length - 1]);
                            }
                            continue;
                        }
                        files++;
                        if (IMAGE_EXTS.contains(extension(entry.getName()))) {
                            images++;
                        }
                        if (parts.length >= 2) {
                            classSet.add(parts[parts.length - 2]);
                        }
                    }
                }
                fileCount = files;
                detected = (images > 0 && images >= files * 0.5) ? "IMAGE" : "ZIP";
                classes = new ArrayList<>(classSet);
            }
            if (classes.isEmpty()) {
                warnings.add("未能从目录结构推断类别，请确认数据集标签组织方式");
            }
        } catch (IOException ex) {
            warnings.add("分析失败: " + ex.getMessage());
        }

        profile.put("fileCount", fileCount);
        profile.put("totalSize", totalSize);
        profile.put("detectedType", detected);
        profile.put("classCount", classes.size());
        profile.put("classes", classes);
        profile.put("warnings", warnings);

        try {
            return objectMapper.writeValueAsString(profile);
        } catch (Exception ex) {
            return "{\"warnings\":[\"serialize failed\"]}";
        }
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
    }
}
