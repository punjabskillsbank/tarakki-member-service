package com.tarakki.member.service;

import com.tarakki.member.dto.LoginRequestDTO;
import com.tarakki.member.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
