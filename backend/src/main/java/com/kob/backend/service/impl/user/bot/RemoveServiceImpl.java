package com.kob.backend.service.impl.user.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> Remove(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authentication=
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser=(UserDetailsImpl) authentication.getPrincipal();
        User user=loginUser.getUser();
        int id=Integer.parseInt(data.get("id"));
        QueryWrapper<Bot> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("id",id);
        Bot bot=botMapper.selectOne(queryWrapper);
        Map<String,String> map=new HashMap<>();
        if(bot==null){
            map.put("error_message","bot不存在或者已删除");
            return map;
        }
        if(user.getId()!=bot.getUserId()){
            map.put("error_message","您没有权限删除该bot！");
            return map;
        }
        botMapper.delete(queryWrapper);
        map.put("error_message","success");
        return map;
    }
}
