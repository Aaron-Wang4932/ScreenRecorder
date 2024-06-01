package org.example;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.example.recording.CaptureScheduler;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class Main extends JFrame {
    CaptureScheduler cs;
    JButton btn;
    Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("screen record");

        btn = new JButton("record");
        btn.setPreferredSize(new Dimension(200, 50));
        btn.addActionListener(e -> {
            if(btn.getText().equals("record")) {
                record();
            } else if(btn.getText().equals("stop")) {
                try {
                    stop();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                System.out.println("wait.");
            }
        });

        add(btn);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String[] args) {
        new Main();
    }

    private void record() {
        cs = new CaptureScheduler();
        try {
            cs.init();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        btn.setText("stop");
    }

    private void stop() throws IOException {
        cs.stop();

        BufferedImage[] frames = {};
        frames = cs.getStore().toArray(frames);
        System.out.println(frames.length);

        File dir = new File("src/main/resources/" + System.currentTimeMillis());
        File output = new File(dir.getPath() + "/output.mp4");
        dir.mkdirs();
        output.createNewFile();

        int width = frames[0].getWidth(),
            height = frames[0].getHeight();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        recorder.setFrameRate(30);

        Java2DFrameConverter converter = new Java2DFrameConverter();

        recorder.start();

        for(BufferedImage i : frames) recorder.record(converter.convert(i));

        recorder.stop();
        recorder.release();






//        for(int i = 0; i < frames.length; i++) {
//            output = new File(dir.getPath() + "/" + i + ".png");
//            if(output.createNewFile()) ImageIO.write(frames[i], "png", output);
//        }
        btn.setText("record");
    }

    Mat imageToMat(BufferedImage i) {

        DataBufferByte imgData = (DataBufferByte) i.getRaster().getDataBuffer();
        byte[] pixels = imgData.getData();

        Mat mat = new Mat(i.getWidth(), i.getHeight(), i.getType());
        mat.put(0, 0, pixels);

        return mat;
    }
}