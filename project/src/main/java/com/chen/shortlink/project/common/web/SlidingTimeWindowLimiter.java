package com.chen.shortlink.project.common.web;

import java.util.LinkedList;

public class SlidingTimeWindowLimiter {
    private int reqCount;
    private LinkedList<Integer> slots=new LinkedList<>();
    private int limitNum=100;
    private int windowNum=10;
    private long windowLength=100L;

    public synchronized Boolean limit(){
        if((reqCount+1)>limitNum){
            return true;
        }
        slots.set(slots.size()+1, slots.peekLast());
        reqCount++;
        return false;
    }

    public SlidingTimeWindowLimiter(){
        slots.addLast(0);
        new Thread(()->{
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                slots.addLast(0);
                if(slots.size()>windowNum){
                    reqCount-=slots.peekFirst();
                    slots.removeFirst();
                }
            }
        }).start();
    }
}
