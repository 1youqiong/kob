package com.kob.backend.controller.pk;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class Indexcontroller {
    @RequestMapping("")
    public String index(){
        return "/index.html";
    }
    @RequestMapping("pk/index/")
    public String pk_index(){
        return "/pk/index.html";
    }
}
