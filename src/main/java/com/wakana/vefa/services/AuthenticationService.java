package com.wakana.vefa.services;

import com.wakana.vefa.dto.ChangePasswordRequest;
import com.wakana.vefa.dto.JwtAuthenticationResponse;
import com.wakana.vefa.dto.RefreshTokenRequest;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.SigninRequest;
import com.wakana.vefa.model.User;

public interface AuthenticationService {
    User signUp(SignUpRequest signUpRequest);
    User   saveReservataire(SignUpRequest signUpRequest,Long id);

    User create(SignUpRequest signUpRequest);

    JwtAuthenticationResponse signin(SigninRequest signinRequest);

    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    User changePassword(ChangePasswordRequest changePassword);
    User changePassword(ChangePasswordRequest changePassword,long id);
    User resetPassword(ChangePasswordRequest changePassword);
}
