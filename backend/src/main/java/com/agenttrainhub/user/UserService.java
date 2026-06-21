package com.agenttrainhub.user;

import com.agenttrainhub.user.entity.User;
import com.agenttrainhub.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /** 批量按 ID 取用户，用于列表填充上传人姓名，避免 N+1。 */
    public Map<Long, User> mapByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, ids))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
