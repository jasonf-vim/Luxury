package com.jasonf.auth.service;

import com.jasonf.auth.util.AuthToken;

public interface AuthService {
    AuthToken login(String username, String password, String clientId, String clientSecret);
}
