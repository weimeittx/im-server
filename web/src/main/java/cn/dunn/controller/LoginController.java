package cn.dunn.controller;

import cn.dunn.constant.Constant;
import cn.dunn.mode.HttpResult;
import cn.dunn.mode.User;
import cn.dunn.mongo.UserRepository;
import cn.dunn.util.MD5Util;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/9/21.
 */
@Controller
@RequestMapping("/user")
public class LoginController {
    @Resource
    private UserRepository userRepository;

    @RequestMapping("/login")
    @ResponseBody
    public HttpResult login(@RequestBody User submitUser, HttpServletRequest request) {
        String username = submitUser.getUsername();
        String password = submitUser.getPassword();
        if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
            User user = userRepository.getByUsernameAndPassword(username, MD5Util.MD5(password));
            if (user != null) {
                request.getSession().setAttribute(Constant.USER_SESSION, user);
                return new HttpResult(user);
            }
        }
        return new HttpResult("密码或者帐号错误!");
    }
}
