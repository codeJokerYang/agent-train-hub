package com.agenttrainhub.dataset.mapper;

import com.agenttrainhub.dataset.entity.Dataset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据集 Mapper。
 */
@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {
}
