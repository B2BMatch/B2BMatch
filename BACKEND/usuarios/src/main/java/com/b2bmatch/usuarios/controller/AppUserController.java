package com.b2bmatch.usuarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.usuarios.model.AppUser;
import com.b2bmatch.usuarios.service.AppUserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestBody AppUser appUser){
        AppUser saved = appUserService.register(appUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }



}
