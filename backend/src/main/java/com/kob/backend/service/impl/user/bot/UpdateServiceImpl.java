package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.service.user.bot.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateServiceImpl implements UpdateService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> Update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authentication=
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser=(UserDetailsImpl) authentication.getPrincipal();
        User user=loginUser.getUser();

        int id=Integer.parseInt(data.get("id"));
        String title=data.get("title");
        String description=data.get("description");
        String content=data.get("content");

        Map<String,String> map=new HashMap<>();

        if(title==null || title.length()==0){
            map.put("error_message","标题不能为空");
            return map;
        }
        if(title!=null && title.length()>300){
            map.put("error_message","标题长度不能超过300");
            return map;
        }

        if(description==null || description.length()==0){
            description="这个人很懒，什么也没留下~";
        }

        if(content==null || content.length()==0){
            map.put("error_message","代码不能为空");
            return map;
        }
        if(content!=null && content.length()>10000){
            map.put("error_message","代码长度不能超过10000");
            return map;
        }

        Bot bot=botMapper.selectById(id);

        if(bot==null){
            map.put("error_message","该bot不存在或者已删除");
            return map;
        }

        if(bot.getUserId()!=user.getId()){
            map.put("error_message","抱歉，您没有该bot的修改权限！");
            return map;
        }

        Bot new_bot=new Bot(id,bot.getUserId(),title,description,content,bot.getRating(),bot.getCreatetime(),new Date());
        botMapper.updateById(new_bot);

        map.put("error_message","success");
        return map;
    }
}
