package org.PianoPlayer.player;

import org.PianoPlayer.bo.NoteBO;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioPlay {
    // 创建两个独立的ScheduledExecutorService，一个用于play()，另一个用于stop()
    private static final ScheduledExecutorService playScheduler = Executors.newScheduledThreadPool(20);
    private static final ScheduledExecutorService stopScheduler = Executors.newScheduledThreadPool(20);

    /**
     * 安排一系列NoteBO对象的播放和停止。
     *
     * @param notes 要安排的NoteBO对象列表
     */
    public static void scheduleNotes(List<NoteBO> notes) {
        // 根据startTime升序排序，如果startTime相同，则根据endTime升序排序
        //notes.sort(Comparator.comparingLong(NoteBO::getStartTime).thenComparingLong(NoteBO::getEndTime));

        // 获取当前时间戳作为参考点
        long referenceTime = 0;

        // 安排每个NoteBO的play和stop动作
        for (NoteBO note : notes) {
            long playDelay = Math.max(0, note.getStartTime() - referenceTime);
            long stopDelay = Math.max(0, note.getEndTime() - referenceTime+2);
            /*System.out.println(note.getStartTime() - referenceTime);
            System.out.println(note.getEndTime() - referenceTime);
            System.out.println(playDelay);
            System.out.println(stopDelay);*/
            // 使用独立的线程池来安排play和stop任务
            playScheduler.schedule(() -> {
                try {
                    note.play();
                    //System.out.println(note.getId()+"play");// 播放音频
                } catch (Exception e) {
                    // 记录错误日志等
                    e.printStackTrace();
                }
            }, playDelay, TimeUnit.MILLISECONDS);

            stopScheduler.schedule(() -> {
                try {
                    note.stop(); // 终止播放音频
                    //System.out.println(note.getId()+"stop");
                } catch (Exception e) {
                    // 记录错误日志等
                    e.printStackTrace();
                }
            }, stopDelay, TimeUnit.MILLISECONDS);
        }
        System.out.println(1);
        // 注意：通常不需要在这里关闭线程池，因为它们是长期运行的。
        // 如果需要关闭，请确保所有任务都已经完成。
        // playScheduler.shutdown();
        // stopScheduler.shutdown();
    }


}
