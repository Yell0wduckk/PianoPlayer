package org.PianoPlayer.player;

import org.PianoPlayer.bo.NoteBO;
import org.PianoPlayer.util.ReadDirUtil;

import java.util.ArrayList;
import java.util.List;

import static org.PianoPlayer.util.ReadJsonUtil.readJson;

public class Main {
    public static void main(String[] args){
        String dirPath="notes/Crychic";
        List<List<NoteBO>> noteList = new ArrayList<>();
        List<String> allJson= ReadDirUtil.findJsonFiles(dirPath);
        /*NoteBO nb1=new NoteBO(0, "C3", 0,1000, 1.0f);
        NoteBO nb2=new NoteBO(1, "D3", 1000,2000, 1.0f);
        noteBOList.add(nb1);
        noteBOList.add(nb2);*///test
        try{
            for (String path:allJson) {
                List<NoteBO> noteBOList = readJson(path, 1);
                for (NoteBO noteBO : noteBOList) {
                    System.out.println(noteBO);
                }
                noteList.add(noteBOList);
            }
            Animation animation=Animation.getInstance(noteList).getThis();
            List<NoteBO> noteAllList=new ArrayList<>();
            for (List<NoteBO> noteBOList:noteList) {
                noteAllList.addAll(noteBOList);
            }
            new Thread(animation).start();
            AudioPlay.scheduleNotes(noteAllList);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
