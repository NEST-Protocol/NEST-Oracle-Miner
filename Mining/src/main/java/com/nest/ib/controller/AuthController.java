package com.nest.ib.controller;

import com.nest.ib.service.MiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wll
 * @date 2020/7/24 12:52
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Value("${nest.user.name}")
    private String userName;

    @Value("${nest.user.password}")
    private String passwd;


    @RequestMapping(value = "/login")
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/verify")
    public String verify(@RequestParam String userName, String passwd, HttpServletRequest request, ModelMap mmp) {
        if (userName.equalsIgnoreCase(this.userName) && passwd.equalsIgnoreCase(this.passwd)) {

            HttpSession session = request.getSession();
            Map<String, Object> user = new HashMap<>();
            user.put("userName", userName);
            user.put("passwd", passwd);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(3600);

            return "redirect:/offer/miningData";
        }

        return "login";
    }

}
