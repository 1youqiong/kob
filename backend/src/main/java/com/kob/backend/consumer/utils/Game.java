package com.kob.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_wall_count;
    private final int[][] g;
    private final int[] dx={-1,0,1,0},dy={1,0,-1,0};
    private final Player playerA,playerB;
    private Integer nextStepA=null,nextStepB=null;
    private ReentrantLock lock=new ReentrantLock();
    private String status="playing";  //playing -> finished
    private String loser=""; //all,a,b

    public Game(Integer rows,Integer cols,Integer inner_wall_count,Integer playerA_id,Integer playerB_id){
        this.rows=rows;
        this.cols=cols;
        this.inner_wall_count=inner_wall_count;
        this.g=new int[rows][cols];
        this.playerA=new Player(playerA_id,rows-2,1,new ArrayList<>());
        this.playerB=new Player(playerB_id,1,cols-2,new ArrayList<>());
    }

    public void setNextStepA(Integer nextStepA){
        lock.lock();
        try{
            this.nextStepA=nextStepA;
        }finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB){
        lock.lock();
        try{
            this.nextStepB=nextStepB;
        }finally {
            lock.unlock();
        }
    }

    public Player getPlayerA(){
        return this.playerA;
    }

    public Player getPlayerB(){
        return this.playerB;
    }

    private boolean check_connectivity(int sx,int sy,int tx,int ty){
        if(sx==tx && sy==ty) return true;

        this.g[sx][sy]=1;
        for(int i=0;i<4;i++){
            int x=sx+dx[i],y=sy+dy[i];
            if(x>0 && x<this.rows && y>0 && y<this.cols && this.g[x][y]==0){
                if(this.check_connectivity(x,y,tx,ty)) return true;
            }
        }
        this.g[sx][sy]=0;
        return false;
    }

    public int[][] getG(){
        return this.g;
    }

    private boolean draw(){
        for(int i=0;i<this.rows;i++){
            for(int j=0;j<this.cols;j++){
                this.g[i][j]=0;
            }
        }

        //给四周加墙
        for(int r=0;r<this.rows;r++){
            this.g[r][0]=1;
            this.g[r][this.cols-1]=1;
        }

        for(int c=0;c<this.cols;c++){
            this.g[0][c]=1;
            this.g[this.rows-1][c]=1;
        }

        Random random=new Random();
        for(int i=0;i<this.inner_wall_count/2;i++){
            for(int j=0;j<1000;j++){
                int r=random.nextInt(this.rows);
                int c=random.nextInt(this.cols);
                if(this.g[r][c]==1 || this.g[this.rows-1-r][this.cols-1-c]==1) continue;
                if(r==this.rows-2 && c==1 || c==this.cols-2 && r==1) continue;
                this.g[r][c]=this.g[this.rows-1-r][this.cols-1-c]=1;
                break;
            }
        }
        return check_connectivity(this.rows-2,1,1,this.cols-2);
    }

    public void createMap(){
        for(int i=0;i<1000;i++){
            if(this.draw()) break;
        }
    }

    public boolean nextStep(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(int i=0;i<50;i++){
            try {
                Thread.sleep(100);
                lock.lock();
                try{
                    if(this.nextStepA!=null && this.nextStepB!=null){
                        this.playerA.getSteps().add(nextStepA);
                        this.playerB.getSteps().add(nextStepB);
                        return true;
                    }
                }finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public void sendAllMessage(String message){
        WebSocketServer.users.get(this.playerA.getId()).sentMessage(message);
        WebSocketServer.users.get(this.playerB.getId()).sentMessage(message);
    }

    private String getSteps(List<Integer> steps){
        StringBuilder res=new StringBuilder();
        for(int d:steps){
            res.append(d);
        }
        return res.toString();
    }

    private String getMap(){
        StringBuilder res=new StringBuilder();

        for(int i=0;i<this.rows;i++){
            for(int j=0;j<this.cols;j++){
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void saveToDatabase(){
        Record record=new Record(
                null,
                this.playerA.getId(),
                this.playerA.getSx(),
                this.playerA.getSy(),
                this.playerB.getId(),
                this.playerB.getSx(),
                this.playerB.getSy(),
                this.getSteps(this.playerA.getSteps()),
                this.getSteps(this.playerB.getSteps()),
                this.getMap(),
                this.loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }
    private void sendResult(){
        JSONObject resp=new JSONObject();
        resp.put("event","result");
        resp.put("loser",this.loser);
        this.sendAllMessage(resp.toJSONString());

        this.saveToDatabase();
    }

    private void sendMove(){
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", this.nextStepA);
            resp.put("b_direction", this.nextStepB);
            this.nextStepA=this.nextStepB=null;
            this.sendAllMessage(resp.toJSONString());
        }finally {
            lock.unlock();
        }
    }

    private boolean check_valid(List<Cell> cellsA,List<Cell> cellsB){
        int n=cellsA.size();
        Cell cell=cellsA.get(n-1);
        if(g[cell.getX()][cell.getY()]==1) return false;

        for(int i=0;i<n-1;i++){
            Cell cellA=cellsA.get(i);
            if(cell.getX()==cellA.getX() && cell.getY()==cellA.getY()) return false;
        }

        for(int i=0;i<n-1;i++){
            Cell cellB=cellsB.get(i);
            if(cell.getX()==cellB.getX() && cell.getY()==cellB.getY()) return false;
        }

        return true;
    }

    private void judge(){
        List<Cell> cellsA=this.playerA.getCells();
        List<Cell> cellsB=this.playerB.getCells();

        boolean validA=this.check_valid(cellsA,cellsB);
        boolean validB=this.check_valid(cellsB,cellsA);

        if(!validA || !validB){
            this.status="finished";
            if(!validA && !validB){
                this.loser="all";
            }else if(!validA){
                this.loser="a";
            }else if(!validB){
                this.loser="b";
            }
        }
    }

    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            if(nextStep()){
                this.judge();
                if(this.status.equals("playing")){
                    this.sendMove();
                }else{
                    sendResult();
                    break;
                }
            }else {
                this.status="finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        this.loser = "all";
                    } else if (nextStepA == null) {
                        this.loser = "a";
                    } else {
                        loser = "b";
                    }
                    sendResult();
                    break;
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
