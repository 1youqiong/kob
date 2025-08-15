package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {


    //用来存储所有websocket链接，并于user的id对应
    //静态变量需要用类名.变量名访问,在类内部也可以直接变量名
    public final static ConcurrentHashMap<Integer,WebSocketServer> users=new ConcurrentHashMap<>();
    private final static CopyOnWriteArraySet<User> matchpool=new CopyOnWriteArraySet<>();

    private User user;
    private static UserMapper userMapper;

    private Session session=null;
    private Game game=null;

    public static RecordMapper recordMapper;


    @Autowired
    private void setRecordMapper(RecordMapper recordMapper){
        WebSocketServer.recordMapper=recordMapper;
    }

    private void startMatching(){
        System.out.println("start matching!");
        WebSocketServer.matchpool.add(this.user);

        while(WebSocketServer.matchpool.size()>=2){
            Iterator<User> it=WebSocketServer.matchpool.iterator();
            User a=it.next();
            User b=it.next();
            matchpool.remove(a);
            matchpool.remove(b);

            Game game=new Game(13,14,20,a.getId(),b.getId());
            game.createMap();
            users.get(a.getId()).game=game;
            users.get(b.getId()).game=game;

            game.start(); //另起线程

            JSONObject respGame=new JSONObject();
            respGame.put("a_id",game.getPlayerA().getId());
            respGame.put("a_sx",game.getPlayerA().getSx());
            respGame.put("a_sy",game.getPlayerA().getSy());
            respGame.put("b_id",game.getPlayerB().getId());
            respGame.put("b_sx",game.getPlayerB().getSx());
            respGame.put("b_sy",game.getPlayerB().getSy());
            respGame.put("map",game.getG());

            JSONObject respA=new JSONObject();
            respA.put("event","matched");
            respA.put("opponent_username",b.getUsername());
            respA.put("game",respGame);
            respA.put("opponent_photo",b.getPhoto());
            users.get(a.getId()).sentMessage(respA.toJSONString());

            JSONObject respB=new JSONObject();
            respB.put("event","matched");
            respB.put("opponent_username",a.getUsername());
            respB.put("game",respGame);
            respB.put("opponent_photo",a.getPhoto());
            users.get(b.getId()).sentMessage(respB.toJSONString());
        }
    }

    private void stopMatching(){
        System.out.println("stop matching");
        WebSocketServer.matchpool.remove(this.user);
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper=userMapper;
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session=session;
        System.out.println("connected!");

        Integer userid = JwtAuthentication.getUserId(token);
        this.user=WebSocketServer.userMapper.selectById(userid);
        if(this.user!=null){
            WebSocketServer.users.put(userid,this);
        }else{
            this.session.close();
        }

        System.out.println(WebSocketServer.users);
        System.out.println(this.session);
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
        if(this.user!=null){
            WebSocketServer.users.remove(this.user.getId());
            WebSocketServer.matchpool.remove(this.user);
        }
    }

    private void move(Integer direction){
        if(this.game.getPlayerA().getId().equals(user.getId())){
            this.game.setNextStepA(direction);
        }else if(this.game.getPlayerB().getId().equals(user.getId())){
            this.game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("receive message");
        JSONObject data=JSONObject.parseObject(message);
        String event=data.getString("event");
        if("start-matching".equals(event)){
            this.startMatching();
        }else if("stop-matching".equals(event)){
            this.stopMatching();
        }else if("move".equals(event)){
            this.move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    //发送消息
    public void sentMessage(String message){
        synchronized (this.session){
            try{
                this.session.getBasicRemote().sendText(message);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
