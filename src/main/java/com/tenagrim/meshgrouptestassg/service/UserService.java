package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.dto.response.UserResponse;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.exception.UserNotFoundException;
import com.tenagrim.meshgrouptestassg.mapper.UserMapper;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }
}