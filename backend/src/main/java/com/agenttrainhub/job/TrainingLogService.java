package com.agenttrainhub.job;

import com.agenttrainhub.common.PageResult;
import com.agenttrainhub.job.dto.LogVO;
import com.agenttrainhub.job.entity.TrainingLog;
import com.agenttrainhub.job.mapper.TrainingLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 训练日志服务。
 */
@Service
public class TrainingLogService {

    private final TrainingLogMapper logMapper;

    public TrainingLogService(TrainingLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    public void add(Long jobId, String level, String message) {
        TrainingLog log = new TrainingLog();
        log.setJobId(jobId);
        log.setLevel(level);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    public PageResult<LogVO> page(Long jobId, long pageNum, long pageSize) {
        Page<TrainingLog> result = logMapper.selectPage(
                new Page<>(Math.max(1, pageNum), Math.min(500, Math.max(1, pageSize))),
                new LambdaQueryWrapper<TrainingLog>()
                        .eq(TrainingLog::getJobId, jobId)
                        .orderByAsc(TrainingLog::getId));
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(),
                result.getRecords().stream().map(TrainingLogService::toVO).toList());
    }

    public void deleteByJob(Long jobId) {
        logMapper.delete(new LambdaQueryWrapper<TrainingLog>().eq(TrainingLog::getJobId, jobId));
    }

    private static LogVO toVO(TrainingLog log) {
        LogVO vo = new LogVO();
        vo.setId(log.getId());
        vo.setLevel(log.getLevel());
        vo.setMessage(log.getMessage());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
