package org.PianoPlayer.bo;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.File;
import java.io.InputStream;

import static java.lang.Thread.sleep;


public class NoteBO extends PlaybackListener {
    /** 标识位*/
    private int id;
    /** 音高*/
    private String pitch;
    /** 起始时间(ms)*/
    private long startTime;
    /** 持续时间(ms)*/
    private long endTime;
    /** 响度*/
    private float volume;
    /** 播放器*/
    private Player player;

    private InputStream is;
    private static final Log log = LogFactory.get();
    
    public int getId(){return this.id;}
    public void setId(int id){this.id=id;}
    public String getPitch(){return this.pitch;}
    public void setPitch(String pitch){this.pitch = pitch;}
    public long getStartTime(){return this.startTime;}
    public void setStartTime(int startTime){this.startTime=startTime;}
    public long getEndTime(){return this.endTime;}
    public void setEndTime(int endTime){this.endTime=endTime;}
    public float getVolume(){return this.volume;}
    public void setVolume(float v){this.volume=v;}

    public NoteBO(int id, String n,long st, long et,float v){
        this.id = id;
        this.pitch = n;
        this.startTime = st;
        this.endTime = et;
        this.volume=v;
        String path = ResourceUtil.getResource("pianoKey").getPath() + File.separator;
        path = path + pitch + ".mp3";
        is = ResourceUtil.getStream(path);
        try
        {
            player=new Player(is,this.volume);
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    public void play(){
        try
        {
            player.play();
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    public void stop(){
        try {
            player.close();
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    @Override
    public String toString(){
        return "id:"+this.id+",音高:"+this.pitch+",起始时间:"+this.startTime+",结束时间:"+this.endTime+",音量:"+this.volume;
    }

    public static void main(String[] args) throws InterruptedException {
        NoteBO noteBO = new NoteBO(1, "C2", 0,100, 1.0f);
        noteBO.play();
        System.out.println(0);
        sleep(1000);
        System.out.println(1);
        noteBO.stop();
    }

}
