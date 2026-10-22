package com.tenagrim.meshgrouptestassg.security;


import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.service.UserLookupService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserLookupService userLookupService;

    public UserDetailsService(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userLookupService.findByLogin(login);

        return org.springframework.security.core.userdetails.User
                .withUsername(String.valueOf(user.getId()))
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
