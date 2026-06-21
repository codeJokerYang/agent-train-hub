package com.agenttrainhub.job;

import com.agenttrainhub.job.dto.MetricVO;
import com.agenttrainhub.job.entity.TrainingMetric;
import com.agenttrainhub.job.mapper.TrainingMetricMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练指标服务。
 */
@Service
public class TrainingMetricService {

    private final TrainingMetricMapper metricMapper;

    public TrainingMetricService(TrainingMetricMapper metricMapper) {
        this.metricMapper = metricMapper;
    }

    public void add(Long jobId, Integer epoch, String metricName, Double value) {
        TrainingMetric metric = new TrainingMetric();
        metric.setJobId(jobId);
        metric.setEpoch(epoch);
        metric.setMetricName(metricName);
        metric.setMetricValue(value);
        metric.setCreatedAt(LocalDateTime.now());
        metricMapper.insert(metric);
    }

    public List<MetricVO> listByJob(Long jobId) {
        List<TrainingMetric> metrics = metricMapper.selectList(
                new LambdaQueryWrapper<TrainingMetric>()
                        .eq(TrainingMetric::getJobId, jobId)
                        .orderByAsc(TrainingMetric::getEpoch)
                        .orderByAsc(TrainingMetric::getId));
        return metrics.stream().map(TrainingMetricService::toVO).toList();
    }

    public void deleteByJob(Long jobId) {
        metricMapper.delete(new LambdaQueryWrapper<TrainingMetric>().eq(TrainingMetric::getJobId, jobId));
    }

    private static MetricVO toVO(TrainingMetric metric) {
        MetricVO vo = new MetricVO();
        vo.setEpoch(metric.getEpoch());
        vo.setStep(metric.getStep());
        vo.setMetricName(metric.getMetricName());
        vo.setMetricValue(metric.getMetricValue());
        vo.setCreatedAt(metric.getCreatedAt());
        return vo;
    }
}
