package com.agenttrainhub.dataset;

import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.PageQuery;
import com.agenttrainhub.common.DownloadFile;
import com.agenttrainhub.common.PageResult;
import com.agenttrainhub.dataset.dto.DatasetVO;
import com.agenttrainhub.dataset.entity.Dataset;
import com.agenttrainhub.dataset.mapper.DatasetMapper;
import com.agenttrainhub.security.SecurityUtils;
import com.agenttrainhub.security.UserPrincipal;
import com.agenttrainhub.storage.StorageService;
import com.agenttrainhub.storage.StoredFile;
import com.agenttrainhub.user.UserService;
import com.agenttrainhub.user.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据集服务。数据权限规则（技术文档第 13 节，校验放在 Service 层而非仅靠前端隐藏）：
 * ADMIN 与 TEACHER 可访问全部数据集，STUDENT 仅能访问自己上传的。
 */
@Service
public class DatasetService {

    private final DatasetMapper datasetMapper;
    private final StorageService storageService;
    private final DatasetAnalyzer analyzer;
    private final UserService userService;

    public DatasetService(DatasetMapper datasetMapper,
                          StorageService storageService,
                          DatasetAnalyzer analyzer,
                          UserService userService) {
        this.datasetMapper = datasetMapper;
        this.storageService = storageService;
        this.analyzer = analyzer;
        this.userService = userService;
    }

    public PageResult<DatasetVO> page(PageQuery query) {
        UserPrincipal me = currentUser();
        long pageNum = Math.max(1, query.getPageNum());
        long pageSize = Math.min(100, Math.max(1, query.getPageSize()));

        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        if (!me.canAccessAllData()) {
            wrapper.eq(Dataset::getOwnerId, me.id());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Dataset::getName, query.getKeyword());
        }
        wrapper.orderByDesc(Dataset::getId);

        Page<Dataset> result = datasetMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(),
                toVOList(result.getRecords()));
    }

    public DatasetVO upload(MultipartFile file, String name) {
        if (file == null || file.isEmpty()) {
            throw BizException.paramError("上传文件不能为空");
        }
        UserPrincipal me = currentUser();
        String original = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "dataset";
        String ext = extensionWithDot(original);

        Dataset dataset = new Dataset();
        dataset.setOwnerId(me.id());
        dataset.setName(StringUtils.hasText(name) ? name : original);
        dataset.setType(detectType(ext));
        dataset.setStatus("READY");
        dataset.setFileSize(0L);
        dataset.setCreatedAt(LocalDateTime.now());
        datasetMapper.insert(dataset);

        String relativeDir = "datasets/" + dataset.getId();
        StoredFile stored = storageService.store(file, relativeDir, "original" + ext);
        dataset.setStoragePath(stored.storagePath());
        dataset.setFileSize(stored.size());
        dataset.setFileHash(stored.sha256());
        datasetMapper.updateById(dataset);

        return toVO(dataset, displayName(userService.getById(me.id())));
    }

    public DatasetVO detail(Long id) {
        return toVO(getOwned(id), null);
    }

    public DownloadFile download(Long id) {
        Dataset dataset = getOwned(id);
        if (!StringUtils.hasText(dataset.getStoragePath())) {
            throw BizException.notFound("数据集文件缺失");
        }
        Resource resource = storageService.loadAsResource(dataset.getStoragePath());
        return new DownloadFile(resource, downloadName(dataset), dataset.getFileSize());
    }

    public DatasetVO analyze(Long id) {
        Dataset dataset = getOwned(id);
        if (!StringUtils.hasText(dataset.getStoragePath())) {
            throw BizException.conflict("数据集文件缺失，无法分析");
        }
        Path path = storageService.resolve(dataset.getStoragePath());
        try {
            long size = dataset.getFileSize() == null ? 0L : dataset.getFileSize();
            dataset.setProfileJson(analyzer.analyze(path, dataset.getType(), size));
            dataset.setStatus("READY");
        } catch (Exception ex) {
            dataset.setProfileJson("{\"warnings\":[\"analyze error\"]}");
            dataset.setStatus("ANALYZE_FAILED");
        }
        datasetMapper.updateById(dataset);
        return toVO(dataset, null);
    }

    public void delete(Long id) {
        Dataset dataset = getOwned(id);
        if (StringUtils.hasText(dataset.getStoragePath())) {
            storageService.delete(dataset.getStoragePath());
        }
        datasetMapper.deleteById(id);
    }

    /** 供其它模块（如创建训练任务）复用的数据集访问校验。 */
    public Dataset requireAccessible(Long id) {
        return getOwned(id);
    }

    /* ----------------------- 内部辅助 ----------------------- */

    /** 取数据集并做数据权限校验。 */
    private Dataset getOwned(Long id) {
        UserPrincipal me = currentUser();
        Dataset dataset = datasetMapper.selectById(id);
        if (dataset == null) {
            throw BizException.notFound("数据集不存在");
        }
        if (!me.canAccessAllData() && !me.id().equals(dataset.getOwnerId())) {
            throw BizException.forbidden("无权访问该数据集");
        }
        return dataset;
    }

    private UserPrincipal currentUser() {
        return SecurityUtils.currentUser()
                .orElseThrow(() -> BizException.unauthorized("未登录"));
    }

    private List<DatasetVO> toVOList(List<Dataset> datasets) {
        if (datasets.isEmpty()) {
            return List.of();
        }
        Set<Long> ownerIds = datasets.stream().map(Dataset::getOwnerId).collect(Collectors.toSet());
        Map<Long, User> owners = userService.mapByIds(ownerIds);
        return datasets.stream()
                .map(ds -> toVO(ds, displayName(owners.get(ds.getOwnerId()))))
                .toList();
    }

    private DatasetVO toVO(Dataset dataset, String ownerName) {
        DatasetVO vo = new DatasetVO();
        vo.setId(dataset.getId());
        vo.setName(dataset.getName());
        vo.setType(dataset.getType());
        vo.setFileSize(dataset.getFileSize());
        vo.setFileHash(dataset.getFileHash());
        vo.setStatus(dataset.getStatus());
        vo.setOwnerId(dataset.getOwnerId());
        vo.setProfileJson(dataset.getProfileJson());
        vo.setCreatedAt(dataset.getCreatedAt());
        vo.setOwnerName(ownerName != null ? ownerName
                : displayName(userService.getById(dataset.getOwnerId())));
        return vo;
    }

    private String displayName(User user) {
        if (user == null) {
            return null;
        }
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
    }

    private String detectType(String extWithDot) {
        String ext = extWithDot.startsWith(".") ? extWithDot.substring(1).toLowerCase() : extWithDot.toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "bmp", "gif", "webp" -> "IMAGE";
            case "csv", "tsv", "xlsx", "xls" -> "TABULAR";
            case "txt", "json" -> "TEXT";
            case "zip" -> "ZIP";
            default -> "OTHER";
        };
    }

    private String extensionWithDot(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private String downloadName(Dataset dataset) {
        String storedName = Paths.get(dataset.getStoragePath()).getFileName().toString();
        String ext = extensionWithDot(storedName);
        String base = StringUtils.hasText(dataset.getName()) ? dataset.getName() : ("dataset-" + dataset.getId());
        return base.toLowerCase().endsWith(ext.toLowerCase()) ? base : base + ext;
    }
}
