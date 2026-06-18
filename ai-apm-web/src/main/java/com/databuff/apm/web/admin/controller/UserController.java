package com.databuff.apm.web.admin.controller;

import com.databuff.apm.web.admin.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        return userService.login(body);
    }

    @PostMapping("/loginOut")
    public Map<String, Object> loginOut() {
        return userService.logout();
    }

    @PostMapping("/imgcapt")
    public Map<String, Object> imgcapt() {
        return userService.imgcapt();
    }

    @PostMapping("/getMenuByAccount")
    public Map<String, Object> getMenuByAccount(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return userService.menus(authorization);
    }

    @GetMapping("/getUserInfo")
    public Map<String, Object> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return userService.userInfo(authorization);
    }

    @GetMapping("/findRoleGroupByUser")
    public Map<String, Object> findRoleGroupByUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return userService.findRoleGroupByUser(authorization);
    }

    @GetMapping("/product/version")
    public Map<String, Object> productVersion() {
        return userService.productVersion();
    }

    @GetMapping("/getAuthLangs")
    public Map<String, Object> getAuthLangs() {
        return userService.authLangs();
    }

    /** 0 = no license gate (open-source); see portal AuthBuilder status codes. */
    @PostMapping("/isActivate")
    public Map<String, Object> isActivate() {
        return userService.isActivate();
    }
}
