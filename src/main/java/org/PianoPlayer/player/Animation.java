package org.PianoPlayer.player;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.core.collection.CollUtil;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.PianoPlayer.bo.NoteBO;
import org.PianoPlayer.util.ReadJsonUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Animation extends Application implements Runnable{

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int WHITE_KEY_NUM=52;
    private static final int BLACK_KEY_NUMBER = 36;
    private static final int WHITE_KEY_WIDTH = 22;
    private static final int WHITE_KEY_HEIGHT = 120;
    private static final int BLACK_KEY_WIDTH = 16;
    private static final int BLACK_KEY_HEIGHT = 80;
    private static final int blankWidth=(WINDOW_WIDTH-WHITE_KEY_NUM*WHITE_KEY_WIDTH)/2;

    private static ExecutorService service = Executors.newCachedThreadPool();
    private String songName;
    private static Animation instance;
    private Log log=LogFactory.get();
    private static final URL RES = ResourceUtil.getResource("assets");
    private List<List<NoteBO>> noteList;
    private int speed=7;
    private int delay=40;
    public Animation(){

    }

    public void start(){
        launch();
    }

    public static Animation getInstance(List<List<NoteBO>> noteList){
        instance = new Animation();
        instance.noteList=noteList;
        return instance;
    }

    public Animation getThis(){
        return this;
    }

    private static long findMaxEndTime(List<List<NoteBO>> noteList){
        long maxEndTime=0;
        for(List<NoteBO> noteBOList : noteList){
            for(NoteBO n : noteBOList){
                if(n.getEndTime()>maxEndTime)
                    maxEndTime=n.getEndTime();
            }
        }
        return maxEndTime;
    }

    @Override
    public void start(Stage stage) {
        long startTime=System.currentTimeMillis();
        if (null == instance)
        {
            log.error("实例为空!!");
            System.exit(0);
            return;
        }
        if (CollUtil.isEmpty(instance.noteList))
        {
            log.error("音符集合为空!!");
            System.exit(0);
            return;
        }
        Pane gridPane = instance.createGridPane();
        Pane notePane = instance.createNotePane();
        //Pane pointPane = instance.createPointPane();
        Pane pianoPane = instance.createPianoKey();
        long endTime=findMaxEndTime(instance.noteList)+10000;
        notePane.setLayoutY(-100000-endTime*10);
        final Scene scene = new Scene(new Group(gridPane,notePane,pianoPane), WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        Platform.runLater(() -> {
            //instance.scanNotePane(scene);
        });
        TranslateTransition translateTransition=new TranslateTransition(Duration.millis(endTime),notePane);
        System.out.println(endTime);
        translateTransition.setInterpolator(Interpolator.LINEAR);
        translateTransition.setByY(endTime*speed/10);
        translateTransition.setCycleCount(1);
        translateTransition.setAutoReverse(false);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(instance.songName);
        scene.getStylesheets().add(RES.toExternalForm() + "/css/style.css");
        long currentTime=System.currentTimeMillis();
        notePane.setLayoutY(WINDOW_HEIGHT-WHITE_KEY_HEIGHT+(currentTime-startTime-3000+delay)*speed/10);
        translateTransition.play();
        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });

    }

    /**
    下方钢琴键
    */
    private Pane createPianoKey()
    {
        Pane pianoPane = new Pane();
        //两侧空白
        for (int key = 21; key <= 108; key++)
        {
            if(isWhiteKey(key)){
                Button whiteBtn = new Button();
                if (key % 12 == 0)//对C键进行标识
                {
                    String str = "\n\n\n\n\nC";
                    if (key == 60)
                    {
                        str = "\n\n\n\n·\nC";
                    }
                    Text text = new Text(str + key / 12);
                    whiteBtn.setGraphic(text);
                }

                String note = ReadJsonUtil.generatePitch(key);
                whiteBtn.setId(Integer.toString(key));
                whiteBtn.setPrefHeight(WHITE_KEY_HEIGHT);
                whiteBtn.setPrefWidth(WHITE_KEY_WIDTH);
                whiteBtn.setWrapText(true);
                whiteBtn.setLayoutX(blankWidth+getKeyLocation(key));
                whiteBtn.setLayoutY(WINDOW_HEIGHT-WHITE_KEY_HEIGHT);
                whiteBtn.getStyleClass().add("piano-white-key");
                pianoPane.getChildren().add(whiteBtn);
                whiteBtn.toBack();
                Tooltip tooltipMerge = new Tooltip();
                tooltipMerge.setText(String.format("琴键:%s", note));
                whiteBtn.setTooltip(tooltipMerge);
                whiteBtn.setOnAction(arg0 ->
                        service.submit(()->{
                            new NoteBO(note).play();
                        }));
            }
            else{
                String note = ReadJsonUtil.generatePitch(key);
                Button blackBtn = new Button();
                blackBtn.setId(Integer.toString(key));
                blackBtn.setPrefWidth(BLACK_KEY_WIDTH);
                blackBtn.setPrefHeight(BLACK_KEY_HEIGHT);
                blackBtn.setWrapText(true);
                blackBtn.setLayoutX(blankWidth+getKeyLocation(key)+3);
                blackBtn.setLayoutY(WINDOW_HEIGHT-WHITE_KEY_HEIGHT);
                blackBtn.getStyleClass().add("piano-black-key");
                Tooltip tooltipMerge = new Tooltip();
                tooltipMerge.setText(String.format("琴键:%s", blackBtn));
                blackBtn.setTooltip(tooltipMerge);
                pianoPane.getChildren().add(blackBtn);
                blackBtn.setOnAction(arg0 ->
                service.submit(()->{
                    new NoteBO(note).play();
                }));
            }

        }
        return pianoPane;
    }

    /***
     * 音符
     */
    private Pane createNotePane() {
        Pane notePane = new Pane();
        boolean isFinished = false;
        for (int track = 0; track < noteList.size(); track++) {
            List<NoteBO> list = noteList.get(track);
            for (int i = 0; i < list.size(); i++) {
                NoteBO note = list.get(i);
                int key = note.getPitchInt();
                if (key == 0) {
                    continue;
                }
                long startTime = note.getStartTime();
                Double length = 0d;
                int location = blankWidth+getKeyLocation(key);
                if(isWhiteKey(key)){
                    location+=2;
                }
                else{
                    location+=4;
                }
                long height = (note.getEndTime()-note.getStartTime())*speed/10;
                Button btn = new Button();
                btn.setId(String.valueOf(note.getId()));
                btn.setLayoutX(location);
                btn.setLayoutY((0-startTime) *speed/10-height);
                btn.setMinHeight(height);
                btn.setMaxHeight(height);
                if (isWhiteKey(key)) {
                    btn.setMaxWidth(WHITE_KEY_WIDTH-4);
                    btn.setMinWidth(WHITE_KEY_WIDTH-4);
                } else {
                    btn.setMaxWidth(BLACK_KEY_WIDTH-2);
                    btn.setMinWidth(BLACK_KEY_WIDTH-2);
                }

                btn.setWrapText(true);
                btn.getStyleClass().add("TRACK" + track);
                Tooltip tooltip = new Tooltip();
                tooltip.setText(String.format("音符:%s, 时长:%s", key, note.getId()));
                btn.setTooltip(tooltip);
                notePane.getChildren().add(btn);
            }
        }
        return notePane;
    }




    private boolean isWhiteKey(int key)
    {
        return key % 12 == 0 || key % 12 == 2 || key % 12 == 4 || key % 12 == 5 || key % 12 == 7 || key % 12 == 9
                || key % 12 == 11;
    }


    private int getKeyLocation(int key)
    {
        //黑键本来应该用0.5表示，但是为了避免用小数所以全部乘2了
        int group=((key/12-1)*7-6)*2;
        int index;
        if(key%12<=4)index=key%12+group;//在mi之前
        else index=key%12+1+group;//没有升mi，跨过
        return WHITE_KEY_WIDTH*index/2;
    }

    /***
     * 背景网格
     */
    private Pane createGridPane()
    {
        Pane gridPane = new Pane();
        for (int i = 0; i <WHITE_KEY_NUM+1; i++)
        {
            Line line = new Line(blankWidth+i*WHITE_KEY_WIDTH, 0, blankWidth+i*WHITE_KEY_WIDTH, WINDOW_HEIGHT);
            line.setStroke(Color.rgb(230, 230, 230));
            gridPane.getChildren().add(line);
        }
        return gridPane;
    }


    @Override
    public void run(){
        launch();
    }

    public static void main(String[] args) {
        List<List<NoteBO>> noteList=new ArrayList<>();
        List<NoteBO> noteBOList=new ArrayList<>();
        noteBOList.add(new NoteBO("C2"));
        noteList.add(noteBOList);
        Animation animation= Animation.getInstance(noteList);
        animation.run();
    }
}