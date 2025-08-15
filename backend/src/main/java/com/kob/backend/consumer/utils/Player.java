package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Integer id;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;

    private boolean check_tail_increasing(Integer step){
        if(step<=10) return true;
        if(step%3==1) return true;
        return false;
    }

    public List<Cell> getCells(){
        List<Cell> res=new ArrayList<>();

        int dx[]={-1,0,1,0},dy[]={0,1,0,-1};
        int x=sx,y=sy;

        res.add(new Cell(x,y));
        int step=0;
        for(int i:this.steps){
            x+=dx[i];
            y+=dy[i];
            res.add(new Cell(x,y));

            if(!this.check_tail_increasing(++step)){
                res.remove(0);
            }
        }
        return res;
    }
}
