package com.kob.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    UserMapper userMapper;
    @GetMapping("/user/all/")
    public List<User> getAll(){
        return userMapper.selectList(null);
    }

    @GetMapping("/user/query/{userId}/")
    public List<User> queryuser(@PathVariable int userId){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("id",userId);
        return userMapper.selectList(queryWrapper);
    }

//    @GetMapping("/user/add/{useId}/{username}/{password}/")
//    public String Adduser(@PathVariable int useId,
//                          @PathVariable String username,
//                          @PathVariable String password){
//        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
//        String passwordencode=bCryptPasswordEncoder.encode(password);
//        User user=new User(useId,username,passwordencode);
//        userMapper.insert(user);
//        return "Add User Successfully";
//    }

    @GetMapping("/user/delete/{userId}/")
    public String delete(@PathVariable int userId){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("id",userId);
        userMapper.delete(queryWrapper);
        return "delete id=%d successfully".formatted(userId);
    }

}
