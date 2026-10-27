package com.projects.distributed_lovable.account_service.service.impl;


import com.projects.distributed_lovable.account_service.dto.auth.AuthResponse;
import com.projects.distributed_lovable.account_service.dto.auth.LoginRequest;
import com.projects.distributed_lovable.account_service.dto.auth.SignupRequest;
import com.projects.distributed_lovable.account_service.entity.User;
import com.projects.distributed_lovable.account_service.mapper.UserMapper;
import com.projects.distributed_lovable.account_service.repository.UserRepository;
import com.projects.distributed_lovable.account_service.service.AuthService;
import com.projects.distributed_lovable.common_lib.errors.BadRequestException;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {
    final UserRepository userRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    @Autowired
    public void setAuthenticationManager(@Lazy AuthenticationManager authenticationManager)
    {
       this.authenticationManager = authenticationManager;
    }
    AuthUtil authUtil;
    @Override
    public AuthResponse signup(SignupRequest request) {
        userRepository.findByUsername(request.username())
                .ifPresent(user -> {
                    throw new BadRequestException("User already exists!You cannot signup!");
                });
        User user = userMapper.toUserFromSignUpRequest(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        String token = authUtil.generateAccessToken(userMapper.toUserDto(user));
        return new AuthResponse(token,userMapper.toUserProfileResponse(user));


    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(),request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(userMapper.toUserDto(user));
        return new AuthResponse(token,userMapper.toUserProfileResponse(user));
    }
}
