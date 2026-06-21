package com.agenttrainhub.auth.dto;

import lombok.Data;

/**
 * 对外返回的用户信息（不含密码）。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String role;
}
