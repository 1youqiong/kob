package com.kob.backend.controller.pk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pk/bot/")

public class BotInfocontroller {
    @RequestMapping("getbotinfo/")
    public String getbotinfo(){
        return "hhhh";
    }


    @RequestMapping("getlistinfo/")
    public List<String> getlistinfo(){
        List<String> l1=new ArrayList<>();
        l1.add("sword");
        l1.add("apple");
        l1.add("tiger");
        return l1;
    }

    @RequestMapping("getmapinfo/")
    public Map<String,String> getmapinfo(){
        Map<String,String> map=new HashMap<>();
        map.put("name","tiger");
        map.put("rating","1800");
        return map;
    }
}
