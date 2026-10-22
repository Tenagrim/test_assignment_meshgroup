package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.cache.CacheNames;
import com.tenagrim.meshgrouptestassg.dto.request.UserSearchRequest;
import com.tenagrim.meshgrouptestassg.dto.response.PageResponse;
import com.tenagrim.meshgrouptestassg.dto.response.UserResponse;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.exception.UserNotFoundException;
import com.tenagrim.meshgrouptestassg.mapper.UserMapper;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import com.tenagrim.meshgrouptestassg.repository.UserSpecifications;
import com.tenagrim.meshgrouptestassg.util.Emails;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Cacheable(cacheNames = CacheNames.USERS, key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    @Cacheable(cacheNames = CacheNames.USER_SEARCH, key = "#filter.cacheKey(#page, #size)")
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(UserSearchRequest filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<UserResponse> result = userRepository
                .findAll(UserSpecifications.withFilters(
                        filter.getDateOfBirth(),
                        filter.getPhone(),
                        filter.getName(),
                        Emails.normalize(filter.getEmail())
                ), pageable)
                .map(userMapper::toResponse);

        return PageResponse.from(result);
    }
}