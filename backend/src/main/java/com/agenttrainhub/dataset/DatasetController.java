package com.agenttrainhub.dataset;

import com.agenttrainhub.common.PageQuery;
import com.agenttrainhub.common.PageResult;
import com.agenttrainhub.common.DownloadFile;
import com.agenttrainhub.common.Result;
import com.agenttrainhub.dataset.dto.DatasetVO;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 数据集接口。所有接口需登录；数据权限在 {@link DatasetService} 内按 ownerId 校验。
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    /** 上传数据集（multipart/form-data）。 */
    @PostMapping
    public Result<DatasetVO> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "name", required = false) String name) {
        return Result.ok(datasetService.upload(file, name));
    }

    /** 分页列表。 */
    @GetMapping
    public Result<PageResult<DatasetVO>> page(PageQuery query) {
        return Result.ok(datasetService.page(query));
    }

    /** 详情。 */
    @GetMapping("/{id}")
    public Result<DatasetVO> detail(@PathVariable Long id) {
        return Result.ok(datasetService.detail(id));
    }

    /** 重新分析。 */
    @PostMapping("/{id}/analyze")
    public Result<DatasetVO> analyze(@PathVariable Long id) {
        return Result.ok(datasetService.analyze(id));
    }

    /** 删除。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        datasetService.delete(id);
        return Result.ok();
    }

    /** 下载原始文件。 */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DownloadFile file = datasetService.download(id);
        String encoded = URLEncoder.encode(file.filename(), StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + file.filename() + "\"; filename*=UTF-8''" + encoded;
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition);
        if (file.size() != null) {
            builder.contentLength(file.size());
        }
        return builder.body(file.resource());
    }
}
