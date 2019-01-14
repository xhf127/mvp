package qed.mvp.controller;

import io.swagger.annotations.ApiOperation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class BaseController {

    @GetMapping(value = "/")
    public String index() {
        return "login";
    }

    @ApiOperation(value = "用户登录页面", notes = "定向到登录页面")
    @GetMapping(value = "/login")
    public String login() {
        return "login";
    }

    @ApiOperation(value = "显示页面", notes = "定向到影像浏览页面")
    @GetMapping(value = "/view")
    public String view() {
        return "view";
    }


}
