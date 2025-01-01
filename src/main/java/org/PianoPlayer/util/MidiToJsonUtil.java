package org.PianoPlayer.util;
import javax.sound.midi.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MidiToJsonUtil {

    public static List<Note> ToJson(String Path){
        List<Note> notes = List.of();
        try {
            File midiFile = new File("Crychic.mid");
            Sequence sequence = MidiSystem.getSequence(midiFile);

            notes = parseNotes(sequence);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //System.out.println(notesJson);
        return notes;

    }

    public static Pair<Integer, List<MidiEvent>> extractResolutionAndTempos(Sequence sequence) {
        int ticksPerQuarterNote = sequence.getResolution(); // Resolution is in ticks per quarter note

        List<MidiEvent> tempoEvents = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage) event.getMessage();
                    if (metaMessage.getType() == 0x51 && metaMessage.getData().length == 3) { // Tempo event
                        tempoEvents.add(event);
                    }
                }
            }
        }

        return new Pair<>(ticksPerQuarterNote, tempoEvents);
    }

    // Helper class to hold a pair of values
    public static class Pair<T1, T2> {
        public final T1 first;
        public final T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    public static long ticksToMilliseconds(long tick, int ppq, double bpm) {
        // Calculate the duration of one quarter note in milliseconds
        double quarterNoteDurationMs = 60000.0 / bpm;

        // Convert from ticks to quarters, then quarters to milliseconds
        return Math.round(tick / (double) ppq * quarterNoteDurationMs);
    }
    private static List<Note> parseNotes(Sequence sequence) {
        Pair<Integer, List<MidiEvent>> result = extractResolutionAndTempos(sequence);
        int ppq = result.first;
        List<MidiEvent> tempoEvents = result.second;

        // 初始化 BPM 为默认值
        double bpm = 240.0;
        if (!tempoEvents.isEmpty()) {
            MetaMessage firstTempoEvent = (MetaMessage) tempoEvents.get(0).getMessage();
            int microsecondsPerQuarterNote = ((firstTempoEvent.getData()[0] & 0xFF) << 16) |
                    ((firstTempoEvent.getData()[1] & 0xFF) << 8) |
                    (firstTempoEvent.getData()[2] & 0xFF);
            bpm = 60_000_000.0 / microsecondsPerQuarterNote;
        }

        List<Note> notes = new ArrayList<>();
        int noteIndex = 0;

        for (Track track : sequence.getTracks()) {
            Map<Integer, MidiEvent> activeNotes = new HashMap<>(); // 存储正在播放的音符及其开始时间

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage message = (ShortMessage) event.getMessage();
                    int command = message.getCommand();
                    int pitch = message.getData1();
                    int velocity = message.getData2();

                    if (command == ShortMessage.NOTE_ON && velocity > 0) {
                        // 记录音符开始时间
                        activeNotes.put(pitch, event);
                    } else if ((command == ShortMessage.NOTE_OFF || (command == ShortMessage.NOTE_ON && velocity == 0)) && activeNotes.containsKey(pitch)) {
                        // 找到对应的 NOTE_ON 事件并创建 Note 对象
                        MidiEvent onEvent = activeNotes.remove(pitch);
                        long startTime = ticksToMilliseconds(onEvent.getTick(), ppq, bpm);
                        long endTime = ticksToMilliseconds(event.getTick(), ppq, bpm);
                        long duration = endTime - startTime;

                        Note note = new Note(
                                noteIndex++,
                                pitch,
                                startTime,
                                endTime,
                                duration
                        );
                        notes.add(note);
                    }
                } else if (event.getMessage() instanceof MetaMessage) {
                    // 更新 BPM 如果遇到 Set Tempo 元事件
                    MetaMessage metaMessage = (MetaMessage) event.getMessage();
                    if (metaMessage.getType() == 0x51 && metaMessage.getData().length == 3) {
                        int microsecondsPerQuarterNote = (metaMessage.getData()[0] & 0xFF) << 16 |
                                (metaMessage.getData()[1] & 0xFF) << 8 |
                                (metaMessage.getData()[2] & 0xFF);
                        bpm = 60_000_000.0 / microsecondsPerQuarterNote;
                    }
                }
            }

            // 处理任何未关闭的音符（例如文件结尾处仍在播放的音符）
            for (Map.Entry<Integer, MidiEvent> entry : activeNotes.entrySet()) {
                int pitch = entry.getKey();
                MidiEvent onEvent = entry.getValue();
                long startTime = ticksToMilliseconds(onEvent.getTick(), ppq, bpm);
                System.out.printf("Warning: Note %d started at time %.2f ms was never turned off.%n", pitch, startTime);
            }
        }

        return notes;
    }
    /*private static List<Note> parseNotes(Sequence sequence) {
        Pair<Integer, List<MidiEvent>> result = extractResolutionAndTempos(sequence);
        System.out.println("Ticks per quarter note: " + result.first);
        int ppq=result.first;
        double bpm=1;
        for (MidiEvent event : result.second) {
            byte[] data = ((MetaMessage) event.getMessage()).getData();
            int microsecondsPerQuarterNote = (data[0] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[2] & 0xFF);
            bpm = 60_000_000.0 / microsecondsPerQuarterNote;
            System.out.printf("Tick %d: Tempo %.2f BPM%n", event.getTick(), bpm);
        }
        List<Note> notes = new ArrayList<>();
        int noteIndex = 0;
        for (Track track : sequence.getTracks()) {
            List<MidiEvent> events = new ArrayList<>();
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage message = (ShortMessage) event.getMessage();
                    int command= message.getCommand();
                    if (command== ShortMessage.NOTE_ON) {
                        events.add(event);
                    }
                    if(command==ShortMessage.NOTE_OFF){
                        events.add(event);
                    }
                }
            }
            // Pair up NOTE_ON and NOTE_OFF events to create notes.
            // This is a simplified example and may not work correctly for all MIDI files.
            for (int i = 0; i < events.size(); i += 2) {
                MidiEvent onEvent = events.get(i);
                MidiEvent offEvent = events.get(i + 1);
                ShortMessage onMessage = (ShortMessage) onEvent.getMessage();
                ShortMessage offMessage = (ShortMessage) offEvent.getMessage();

                Note note = new Note(
                        noteIndex++,
                        onMessage.getData1(), // pitch
                        ticksToMilliseconds(onEvent.getTick(),ppq,bpm), // start time
                        ticksToMilliseconds(offEvent.getTick(),ppq,bpm) // end time
                );
                notes.add(note);
            }
        }
        return notes;
    }
*/
    static class Note {
        int index;
        int pitch;
        long startTime;
        long endTime;

        Note(int index, int pitch, long startTime, long endTime, long time) {
            this.index = index;
            this.pitch = pitch;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName(); // 获取文件名（不包括路径）

        // 如果文件名中没有'.'，直接返回原文件名
        if (fileName.lastIndexOf('.') == -1) {
            return fileName;
        }

        // 获取最后一个'.'之前的所有内容
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static void writeJsonToFile(String inputFilePath,String resourcesPath, Object jsonObject) throws IOException {
        // 获取输入文件名和路径
        File inputFile = new File(inputFilePath);
        String fileNameWithoutExtension = getFileNameWithoutExtension(inputFilePath);
        String jsonFileName = fileNameWithoutExtension + ".json";

        // 创建完整的输出路径
        Path outputPath = Paths.get(resourcesPath, jsonFileName);
        File outputDir = outputPath.getParent().toFile();

        // 如果目标目录不存在，则创建它
        if (!outputDir.exists()) {
            Files.createDirectories(outputDir.toPath());
        }

        // 检查是否有权限写入
        if (!outputDir.canWrite()) {
            throw new IOException("没有权限写入目标目录: " + outputDir.getAbsolutePath());
        }

        // 使用Gson将Java对象转换为JSON字符串
        //Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(jsonObject);
        System.out.println(jsonString);

        // 写入JSON字符串到文件
        try (FileWriter writer = new FileWriter(outputPath.toString())) {
            writer.write(jsonString);
        }

        System.out.println("JSON 文件已成功保存至: " + outputPath);
    }

    public static void main(String[] args) {
        try {
            // 示例调用，假设有一个名为"春日影.mid"的MIDI文件
            String midiFilePath = "春日影.mid";
            // 假设我们有一个包含音符信息的对象 notesObject
            Object notes =ToJson(midiFilePath);
            System.out.println(notes);
            writeJsonToFile(midiFilePath, "notes",notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}