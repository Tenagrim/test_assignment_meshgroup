package com.tenagrim.meshgrouptestassg.mapper;

import com.tenagrim.meshgrouptestassg.dto.response.UserResponse;
import com.tenagrim.meshgrouptestassg.entity.EmailData;
import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import com.tenagrim.meshgrouptestassg.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        List<String> emails = user.getEmails().stream()
                .map(EmailData::getEmail)
                .collect(Collectors.toList());
        List<String> phones = user.getPhones().stream()
                .map(PhoneData::getPhone)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getDateOfBirth(),
                emails,
                phones
        );
    }
}
