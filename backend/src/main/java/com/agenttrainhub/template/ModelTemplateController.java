package com.agenttrainhub.template;

import com.agenttrainhub.common.Result;
import com.agenttrainhub.template.dto.ModelTemplateVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模型模板接口。所有登录用户可访问。
 */
@RestController
@RequestMapping("/api/model-templates")
public class ModelTemplateController {

    private final ModelTemplateService templateService;

    public ModelTemplateController(ModelTemplateService templateService) {
        this.templateService = templateService;
    }

    /** 可用模板列表（仅 enabled=true）。 */
    @GetMapping
    public Result<List<ModelTemplateVO>> list() {
        return Result.ok(templateService.listEnabled());
    }

    /** 模板详情。 */
    @GetMapping("/{id}")
    public Result<ModelTemplateVO> detail(@PathVariable Long id) {
        return Result.ok(templateService.detail(id));
    }
}
