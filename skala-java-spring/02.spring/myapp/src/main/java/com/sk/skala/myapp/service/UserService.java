package com.sk.skala.myapp.service;

import org.springframework.stereotype.Service;

import com.sk.skala.myapp.domain.User;
import com.sk.skala.myapp.repository.UserRepository;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // 모든 사용자 조회
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // 특정 사용자 조회
    public Optional<User> getUserById(long id){
        return userRepository.findById(id);
    }

    // 사용자 추가
    public User createUser(User user){
        log.debug("사용자 저장: {}", user);
        return userRepository.save(user);
    }


    // 사용자 삭제
    public void deleteUser(long id){
        userRepository.deleteById(id);
    }


    // 사용자 정보 수정
    // 새로운 사용자이면 insert, 이미 존재하는 사용자이면 update를 수행
    public Optional<User> updateUser(long id, User updatedUser){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            return Optional.empty();
        }

        User user = optionalUser.get();
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        User savedUser = userRepository.save(user);
        
        return Optional.of(savedUser);
    }
    
}
