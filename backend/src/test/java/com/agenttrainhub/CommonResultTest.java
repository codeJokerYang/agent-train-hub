package com.agenttrainhub;

import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.ErrorCode;
import com.agenttrainhub.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 不依赖 Spring 上下文 / 数据库的纯单元测试，验证统一响应与业务异常。
 */
class CommonResultTest {

    @Test
    void ok_shouldUseSuccessCode() {
        Result<String> result = Result.ok("hello");
        assertEquals(0, result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    void fail_shouldCarryErrorCode() {
        Result<Void> result = Result.fail(ErrorCode.NOT_FOUND);
        assertEquals(404, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void bizException_shouldExposeCustomCode() {
        BizException ex = new BizException(40901, 409, "当前任务状态为 SUCCESS，不能执行停止操作");
        assertEquals(40901, ex.getCode());
        assertEquals(409, ex.getHttpStatus());
    }
}
