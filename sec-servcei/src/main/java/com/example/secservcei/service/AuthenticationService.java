package com.example.secservcei.service;

import java.util.Map;

public interface AuthenticationService {

    public Map<String,String> generateToken(String username ,boolean withRefreshToken);
}
