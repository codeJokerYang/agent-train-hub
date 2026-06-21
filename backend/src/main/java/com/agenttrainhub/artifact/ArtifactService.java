package com.agenttrainhub.artifact;

import com.agenttrainhub.artifact.dto.ArtifactVO;
import com.agenttrainhub.artifact.entity.ModelArtifact;
import com.agenttrainhub.artifact.mapper.ModelArtifactMapper;
import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.DownloadFile;
import com.agenttrainhub.job.JobAccessGuard;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.storage.StorageService;
import com.agenttrainhub.storage.StoredFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型产物服务：训练成功后生成 model-demo.txt 与 training-report.json，并支持鉴权下载。
 */
@Service
public class ArtifactService {

    private final ModelArtifactMapper artifactMapper;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final JobAccessGuard jobAccessGuard;

    public ArtifactService(ModelArtifactMapper artifactMapper,
                           StorageService storageService,
                           ObjectMapper objectMapper,
                           JobAccessGuard jobAccessGuard) {
        this.artifactMapper = artifactMapper;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.jobAccessGuard = jobAccessGuard;
    }

    /** 训练成功后生成模型文件与训练报告。 */
    public void generateForSuccess(TrainingJob job, double finalLoss, double finalAccuracy) {
        String dir = "artifacts/jobs/" + job.getId();

        String model = "AgentTrainHub demo model\n"
                + "jobId=" + job.getId() + "\n"
                + "taskName=" + job.getTaskName() + "\n"
                + "status=SUCCESS\n"
                + "totalEpoch=" + job.getTotalEpoch() + "\n"
                + "finalLoss=" + round(finalLoss) + "\n"
                + "finalAccuracy=" + round(finalAccuracy) + "\n"
                + "generatedAt=" + LocalDateTime.now() + "\n";
        StoredFile modelFile = storageService.writeBytes(
                model.getBytes(StandardCharsets.UTF_8), dir, "model-demo.txt");
        save(job.getId(), "MODEL", "model-demo.txt", modelFile);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("jobId", job.getId());
        report.put("taskName", job.getTaskName());
        report.put("status", "SUCCESS");
        report.put("totalEpoch", job.getTotalEpoch());
        report.put("finalLoss", round(finalLoss));
        report.put("finalAccuracy", round(finalAccuracy));
        report.put("params", parseParams(job.getParamsJson()));
        report.put("startedAt", job.getStartedAt() == null ? null : job.getStartedAt().toString());
        report.put("finishedAt", LocalDateTime.now().toString());

        byte[] reportBytes;
        try {
            reportBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report);
        } catch (Exception ex) {
            reportBytes = "{}".getBytes(StandardCharsets.UTF_8);
        }
        StoredFile reportFile = storageService.writeBytes(reportBytes, dir, "training-report.json");
        save(job.getId(), "REPORT", "training-report.json", reportFile);
    }

    public List<ArtifactVO> listByJob(Long jobId) {
        return artifactMapper.selectList(new LambdaQueryWrapper<ModelArtifact>()
                        .eq(ModelArtifact::getJobId, jobId)
                        .orderByAsc(ModelArtifact::getId))
                .stream().map(ArtifactService::toVO).toList();
    }

    public void deleteByJob(Long jobId) {
        List<ModelArtifact> artifacts = artifactMapper.selectList(
                new LambdaQueryWrapper<ModelArtifact>().eq(ModelArtifact::getJobId, jobId));
        for (ModelArtifact artifact : artifacts) {
            if (artifact.getStoragePath() != null) {
                storageService.delete(artifact.getStoragePath());
            }
        }
        artifactMapper.delete(new LambdaQueryWrapper<ModelArtifact>().eq(ModelArtifact::getJobId, jobId));
    }

    /** 下载产物：先校验所属任务的访问权限。 */
    public DownloadFile download(Long artifactId) {
        ModelArtifact artifact = artifactMapper.selectById(artifactId);
        if (artifact == null) {
            throw BizException.notFound("产物不存在");
        }
        jobAccessGuard.requireAccessible(artifact.getJobId());
        Resource resource = storageService.loadAsResource(artifact.getStoragePath());
        return new DownloadFile(resource, artifact.getFileName(), artifact.getFileSize());
    }

    private void save(Long jobId, String type, String fileName, StoredFile stored) {
        ModelArtifact artifact = new ModelArtifact();
        artifact.setJobId(jobId);
        artifact.setArtifactType(type);
        artifact.setFileName(fileName);
        artifact.setStoragePath(stored.storagePath());
        artifact.setFileSize(stored.size());
        artifact.setCreatedAt(LocalDateTime.now());
        artifactMapper.insert(artifact);
    }

    private JsonNode parseParams(String paramsJson) {
        if (paramsJson == null || paramsJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(paramsJson);
        } catch (Exception ex) {
            return null;
        }
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static ArtifactVO toVO(ModelArtifact artifact) {
        ArtifactVO vo = new ArtifactVO();
        vo.setId(artifact.getId());
        vo.setArtifactType(artifact.getArtifactType());
        vo.setFileName(artifact.getFileName());
        vo.setFileSize(artifact.getFileSize());
        vo.setCreatedAt(artifact.getCreatedAt());
        return vo;
    }
}
