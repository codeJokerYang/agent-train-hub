package com.agenttrainhub.job;

import com.agenttrainhub.common.BizException;
import com.agenttrainhub.job.entity.TrainingJob;
import com.agenttrainhub.job.mapper.TrainingJobMapper;
import com.agenttrainhub.security.SecurityUtils;
import com.agenttrainhub.security.UserPrincipal;
import org.springframework.stereotype.Component;

/**
 * 训练任务数据权限校验：ADMIN 与 TEACHER 可访问全部任务，STUDENT 仅自己的。
 * 抽成独立组件供 {@code TrainingJobService} 与 {@code ArtifactService} 复用，避免循环依赖。
 */
@Component
public class JobAccessGuard {

    private final TrainingJobMapper jobMapper;

    public JobAccessGuard(TrainingJobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    public UserPrincipal currentUser() {
        return SecurityUtils.currentUser()
                .orElseThrow(() -> BizException.unauthorized("未登录"));
    }

    /** 取任务并校验访问权限。 */
    public TrainingJob requireAccessible(Long jobId) {
        UserPrincipal me = currentUser();
        TrainingJob job = jobMapper.selectById(jobId);
        if (job == null) {
            throw BizException.notFound("训练任务不存在");
        }
        if (!me.canAccessAllData() && !me.id().equals(job.getOwnerId())) {
            throw BizException.forbidden("无权访问该任务");
        }
        return job;
    }
}
