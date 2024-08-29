package com.example.useranalysislibrary;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;

import java.io.IOException;

public class UserAnalysisHelper {
    private Context context;
    private UserAnalysisService userAnalysisService;
    private boolean isBound = false;

    public UserAnalysisHelper(Context context) {
        this.context = context;
        Intent intent = new Intent(context, UserAnalysisService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            UserAnalysisService.LocalBinder binder = (UserAnalysisService.LocalBinder) service;
            userAnalysisService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    public void addTouchData(float x, float y) {
        if (isBound && userAnalysisService != null) {
            userAnalysisService.addTouchData(x, y);
        }
    }

    public void unbind() {
        if (isBound) {
            context.unbindService(connection);
            isBound = false;
        }
    }
    public Bitmap getHeatMap(int width, int height) {
        if (isBound && userAnalysisService != null) {
            return userAnalysisService.generateHeatMap(width, height);
        }
        return null;
    }

    public Bitmap getBarChart(int width, int height) {
        if (isBound && userAnalysisService != null) {
            return userAnalysisService.generateBarChart(width, height);
        }
        return null;
    }
    public void generateDrawingTimelapse(String outputPath, int width, int height) throws IOException {
        if (isBound && userAnalysisService != null) {
            userAnalysisService.generateDrawingTimelapse(outputPath, width, height);
        }
    }


}