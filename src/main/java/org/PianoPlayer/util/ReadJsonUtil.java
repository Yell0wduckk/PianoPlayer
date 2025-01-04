package org.PianoPlayer.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.PianoPlayer.bo.NoteBO;
import org.PianoPlayer.util.MidiToJsonUtil.Note;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReadJsonUtil {
    public static String generatePitch(int note)//根据整数来对应相应的音高 范围21到108
    {
        String[] notes = { "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B" };
        String n;
        String note_ = notes[note % 12];
        switch (note_)
        {
            case "C#/Db":
                n = "C#";
                break;
            case "D#/Eb":
                n = "D#";
                break;
            case "F#/Gb":
                n = "F#";
                break;
            case "G#/Ab":
                n = "G#";
                break;
            case "A#/Bb":
                n = "A#";
                break;
            default:
                n = note_;
                break;
        }
        if (note == 0)
        {
            return "R";
        }
        return n + note / 12;

    }
    public static List<NoteBO> readJson(String path,float volume) throws IOException {
        List <NoteBO> noteBOList = new ArrayList<NoteBO>();
        Gson gson = new Gson();

        // 读取 JSON 文件内容到字符串
        try (FileReader reader = new FileReader(path)) {
            // 使用 TypeToken 来指定泛型类型的类型
            Type listType = new TypeToken<List<Note>>(){}.getType();

            // 反序列化 JSON 文件为 Java 对象
            List<Note> notesList = gson.fromJson(reader, listType);

            // 打印读取的信息以验证结果
            if (notesList != null) {
                for (Note note : notesList) {
                    NoteBO tempNoteBO=new NoteBO(note.index,generatePitch(note.pitch), note.pitch, note.startTime,note.endTime,volume);
                    noteBOList.add(tempNoteBO);
                }
            } else {
                System.out.println("JSON 文件为空或格式不正确");
            }
        }
        return noteBOList;
    }

    public static void main(String[] args){
        String path="notes/春日影.json";
        List <NoteBO> noteBOList = List.of();
        try{
            noteBOList=readJson(path,1);
            for (NoteBO noteBO : noteBOList){
                System.out.println(noteBO);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
