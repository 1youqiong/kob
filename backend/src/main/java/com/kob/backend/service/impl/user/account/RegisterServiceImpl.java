package com.kob.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> register(String username, String password, String confirm_password) {

        Map<String,String> map=new HashMap<>();
        if(username==null) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password==null){
            map.put("error_message","密码不能为空");
            return map;
        }
        if(confirm_password==null){
            map.put("error_message","确认密码不能为空");
            return map;
        }
        username=username.trim();
        if(username.length()==0){
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if(password.length()==0){
            map.put("error_message","密码不能为空");
            return map;
        }
        if(confirm_password.length()==0){
            map.put("error_message","确认密码不能为空");
            return map;
        }
        if(username.length()>100){
            map.put("error_message","用户名长度不能超过100");
            return map;
        }
        if(!confirm_password.equals(password)){
            map.put("error_message","两次密码不一致");
            return map;
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("username",username);
        List<User> userList=userMapper.selectList(queryWrapper);
        if(!userList.isEmpty()){
            map.put("error_message","用户名已存在");
            return map;
        }

        String encodePassword=passwordEncoder.encode(password);
        String photo="https://c-ssl.dtstatic.com/uploads/blog/202303/20/20230320145706_07ca5.thumb.400_0.jpeg";
        User user=new User(null,username,encodePassword,photo);
        userMapper.insert(user);
        map.put("error_message","success");
        return map;
    }
}
