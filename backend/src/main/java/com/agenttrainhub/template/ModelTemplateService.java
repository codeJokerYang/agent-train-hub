package com.agenttrainhub.template;

import com.agenttrainhub.common.BizException;
import com.agenttrainhub.template.dto.ModelTemplateVO;
import com.agenttrainhub.template.entity.ModelTemplate;
import com.agenttrainhub.template.mapper.ModelTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模型模板服务。所有登录用户均可查询；列表仅返回启用的模板。
 */
@Service
public class ModelTemplateService {

    private final ModelTemplateMapper templateMapper;

    public ModelTemplateService(ModelTemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    /** 启用模板列表。 */
    public List<ModelTemplateVO> listEnabled() {
        List<ModelTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<ModelTemplate>()
                        .eq(ModelTemplate::getEnabled, 1)
                        .orderByAsc(ModelTemplate::getId));
        return templates.stream().map(ModelTemplateService::toVO).toList();
    }

    /** 模板详情。 */
    public ModelTemplateVO detail(Long id) {
        ModelTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw BizException.notFound("模型模板不存在");
        }
        return toVO(template);
    }

    /** 供创建任务时校验：模板需存在且启用。 */
    public ModelTemplate requireUsable(Long id) {
        ModelTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw BizException.paramError("模型模板不存在");
        }
        if (!Integer.valueOf(1).equals(template.getEnabled())) {
            throw BizException.paramError("模型模板已停用");
        }
        return template;
    }

    private static ModelTemplateVO toVO(ModelTemplate template) {
        ModelTemplateVO vo = new ModelTemplateVO();
        vo.setId(template.getId());
        vo.setCode(template.getCode());
        vo.setName(template.getName());
        vo.setAlgorithmType(template.getAlgorithmType());
        vo.setDefaultParamsJson(template.getDefaultParamsJson());
        vo.setParamSchemaJson(template.getParamSchemaJson());
        return vo;
    }
}
