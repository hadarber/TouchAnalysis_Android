package com.example.useranalysislibrary;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UserAnalysisService extends Service {
    private final IBinder binder = new LocalBinder();
    private List<TouchData> touchDataList = new ArrayList<>();

    public class LocalBinder extends Binder {
        UserAnalysisService getService() {
            return UserAnalysisService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // התחלת איסוף נתונים או כל אתחול אחר שנדרש
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void addTouchData(float x, float y) {
        touchDataList.add(new TouchData(x, y));
    }

    private class TouchDataCollector {
        void collectTouchData(float x, float y) {
            addTouchData(x, y);
        }
    }

    private class HeatMapGenerator {
        Bitmap generateHeatMap(int width, int height) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();

            for (TouchData touchData : touchDataList) {
                paint.setColor(Color.RED);
                paint.setAlpha(50);  // שקיפות חלקית
                canvas.drawCircle(touchData.x, touchData.y, 30, paint);
            }
            // הוספת כותרת
            paint.setColor(Color.BLACK);
            paint.setTextSize(70);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Heat Map Analysis", width / 2, 150, paint);

            return bitmap;
        }
    }

    private class DataVisualizer {
        public Bitmap generateBarChart(int width, int height) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();

            // הגדרת אזור הציור
            int chartMarginTop = 300;
            int chartMarginBottom = 350;
            int chartMarginLeft = 10;
            int chartMarginRight = 10;
            int chartWidth = width - chartMarginLeft - chartMarginRight;
            int chartHeight = height - chartMarginTop - chartMarginBottom;

            // חישוב רוחב העמודות ורווחים
            int barCount = 4;
            int barWidth = chartWidth / (barCount * 5 / 4);
            int spacing = barWidth / 4;

            int[] values = analyzeTouchData(width, height);
            int maxValue = getMaxValue(values);

            // צבעים לעמודות
            int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};

            // כותרות לעמודות
            String[] labels = {"Top Left", "Top Right", "Bottom Left", "Bottom Right"};

            // ציור העמודות
            for (int i = 0; i < barCount; i++) {
                int left = chartMarginLeft + i * (barWidth + spacing);
                int top = chartMarginTop + chartHeight - (values[i] * chartHeight / maxValue);
                int right = left + barWidth;
                int bottom = height - chartMarginBottom;

                paint.setColor(colors[i]);
                canvas.drawRect(left, top, right, bottom, paint);

                // הוספת ערך מעל העמודה
                paint.setColor(Color.BLACK);
                paint.setTextSize(40);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(String.valueOf(values[i]), left + barWidth / 2, top - 20, paint);

                // הוספת כותרת מתחת לעמודה
                paint.setTextSize(40);
                canvas.drawText(labels[i], left + barWidth / 2, bottom + 50, paint);
            }

            // הוספת כותרת ראשית
            paint.setTextSize(70);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Touch Analysis", width / 2, 150, paint);

            return bitmap;
        }
        private int[] analyzeTouchData(int width, int height) {
            int[] values = new int[4];
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            for (TouchData touch : touchDataList) {
                if (touch.x < halfWidth) {
                    if (touch.y < halfHeight) {
                        values[0]++;  // Top Left
                    } else {
                        values[2]++;  // Bottom Left
                    }
                } else {
                    if (touch.y < halfHeight) {
                        values[1]++;  // Top Right
                    } else {
                        values[3]++;  // Bottom Right
                    }
                }
            }

            return values;
        }

        private int getMaxValue(int[] array) {
            int max = 1;  // מינימום 1 כדי למנוע חלוקה באפס
            for (int value : array) {
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }

    }

    public Bitmap generateHeatMap(int width, int height) {
        return new HeatMapGenerator().generateHeatMap(width, height);
    }

    public Bitmap generateBarChart(int width, int height) {
        return new DataVisualizer().generateBarChart(width, height);
    }
    public void generateDrawingTimelapse(String outputPath, int width, int height) throws IOException {
        MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, screenWidth, screenHeight);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 640, 480);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        }

        Surface inputSurface = encoder.createInputSurface();
        encoder.start();

        MediaMuxer muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        int trackIndex = -1;
        boolean muxerStarted = false;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7);
        paint.setAntiAlias(true);

        Path path = new Path();
        boolean first = true;

        // Scale touch data to fit the video dimensions
        float maxX = 0, maxY = 0;
        for (TouchData touchData : touchDataList) {
            maxX = Math.max(maxX, touchData.x);
            maxY = Math.max(maxY, touchData.y);
        }
        float scaleX = (float) width / maxX;
        float scaleY = (float) height / maxY;

        for (TouchData touchData : touchDataList) {
            float normalizedX = touchData.x * scaleX;
            float normalizedY = touchData.y * scaleY;
            if (first) {
                path.moveTo(normalizedX, normalizedY);
                first = false;
            } else {
                path.lineTo(normalizedX, normalizedY);
            }

            // Lock the canvas, draw the current path, and unlock the canvas
            Canvas canvas = inputSurface.lockCanvas(null);
            try {
                canvas.drawColor(Color.WHITE); // Clear the canvas with white background
                canvas.drawPath(path, paint);  // Draw the path
            } finally {
                inputSurface.unlockCanvasAndPost(canvas); // Ensure the canvas is always unlocked
            }

            // Encode frame
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, -1);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxerStarted) {
                    throw new RuntimeException("Format changed twice");
                }
                MediaFormat newFormat = encoder.getOutputFormat();
                trackIndex = muxer.addTrack(newFormat);
                muxer.start();
                muxerStarted = true;
            } else if (outputBufferIndex >= 0) {
                ByteBuffer encodedData = encoder.getOutputBuffer(outputBufferIndex);
                if (encodedData != null) {
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            }

            try {
                Thread.sleep(33); // Approximately 30 frames per second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Finish up
        encoder.signalEndOfInputStream();
        encoder.stop();
        encoder.release();
        muxer.stop();
        muxer.release();
    }

    private static class TouchData {
        float x, y;

        TouchData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}