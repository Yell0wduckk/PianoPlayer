package org.PianoPlayer.player;

import org.PianoPlayer.bo.NoteBO;

import java.util.ArrayList;
import java.util.List;

import static org.PianoPlayer.util.ReadJsonUtil.readJson;

public class Main {
    public static void main(String[] args){
        String path="notes/春日影.json";
        List<NoteBO> noteBOList = new ArrayList<NoteBO>();
        NoteBO nb1=new NoteBO(0, "C3", 0,1000, 1.0f);
        NoteBO nb2=new NoteBO(1, "D3", 1000,2000, 1.0f);
        noteBOList.add(nb1);
        noteBOList.add(nb2);
        try{
            noteBOList=readJson(path,1);
            for (NoteBO noteBO : noteBOList){
                System.out.println(noteBO);
            }
            AudioPlay.scheduleNotes(noteBOList);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
