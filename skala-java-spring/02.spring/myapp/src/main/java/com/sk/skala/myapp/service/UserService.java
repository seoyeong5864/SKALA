package com.sk.skala.myapp.service;

import org.springframework.stereotype.Service;

import com.sk.skala.myapp.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // 모든 사용자 조회
    
    
}
