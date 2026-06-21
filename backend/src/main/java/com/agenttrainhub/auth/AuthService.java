package com.agenttrainhub.auth;

import com.agenttrainhub.auth.dto.LoginRequest;
import com.agenttrainhub.auth.dto.LoginResponse;
import com.agenttrainhub.auth.dto.UserVO;
import com.agenttrainhub.common.BizException;
import com.agenttrainhub.common.ErrorCode;
import com.agenttrainhub.security.JwtTokenProvider;
import com.agenttrainhub.security.SecurityUtils;
import com.agenttrainhub.security.UserPrincipal;
import com.agenttrainhub.user.UserService;
import com.agenttrainhub.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：登录校验、签发 token、查询当前用户。
 */
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserService userService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        String token = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, toVO(user));
    }

    public UserVO currentUser() {
        UserPrincipal principal = SecurityUtils.currentUser()
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED, "未登录"));
        User user = userService.getById(principal.id());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }
}
