package com.example.finalappandb;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.useranalysislibrary.UserAnalysisHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private ImageButton clearButton, analyzeButton, switchViewButton;
    private ImageButton blackColorButton, redColorButton, purpleColorButton, greenColorButton, blueColorButton;
    private SeekBar strokeWidthSeekBar;
    private ImageButton funAnalysisButton;

    private UserAnalysisHelper userAnalysisHelper;
    private ImageView heatmapImageView, barChartImageView;
    private boolean showingHeatmap = true;
    private boolean isAnalysisMode = false;
    private View drawingControls;

    private ImageButton timelapseButton;
    private String timelapseOutputPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = findViewById(R.id.drawing_view);
        clearButton = findViewById(R.id.clear_button);
        analyzeButton = findViewById(R.id.analyze_button);
        switchViewButton = findViewById(R.id.switch_view_button);
        heatmapImageView = findViewById(R.id.heatmap_image_view);
        barChartImageView = findViewById(R.id.bar_chart_image_view);
        drawingControls = findViewById(R.id.drawing_controls);
        strokeWidthSeekBar = findViewById(R.id.stroke_width_seekbar);
        blackColorButton = findViewById(R.id.black_color_button);
        redColorButton = findViewById(R.id.red_color_button);
        purpleColorButton = findViewById(R.id.purple_color_button);
        greenColorButton = findViewById(R.id.green_color_button);
        blueColorButton = findViewById(R.id.blue_color_button);

        clearButton.setOnClickListener(v -> clearAnalysis());
        analyzeButton.setOnClickListener(v -> startAnalysis());
        switchViewButton.setOnClickListener(v -> switchView());

        blackColorButton.setOnClickListener(v -> setDrawingColor(Color.BLACK));
        redColorButton.setOnClickListener(v -> setDrawingColor(Color.RED));
        purpleColorButton.setOnClickListener(v -> setDrawingColor(Color.MAGENTA));
        greenColorButton.setOnClickListener(v -> setDrawingColor(Color.GREEN));
        blueColorButton.setOnClickListener(v -> setDrawingColor(Color.BLUE));

        funAnalysisButton = findViewById(R.id.fun_analysis_button);
        funAnalysisButton.setOnClickListener(v -> showFunAnalysis());
        funAnalysisButton.setVisibility(View.GONE);

        timelapseButton = findViewById(R.id.timelapse_button);
        timelapseButton.setOnClickListener(v -> createAndShowTimelapse());
        timelapseButton.setVisibility(View.GONE);  // מוסתר בהתחלה

        timelapseOutputPath = getExternalFilesDir(null) + "/drawing_timelapse.mp4";

        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setStrokeWidth(progress + 1); // +1 כדי להימנע מעובי 0
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        userAnalysisHelper = new UserAnalysisHelper(this);

        drawingView.setOnTouchListener((v, event) -> {
            userAnalysisHelper.addTouchData(event.getX(), event.getY());
            return false;
        });
    }
    private void setDrawingColor(int color) {
        drawingView.setColor(color);
    }
    private void startAnalysis() {
        Bitmap heatmap = userAnalysisHelper.getHeatMap(drawingView.getWidth(), drawingView.getHeight());
        Bitmap barChart = userAnalysisHelper.getBarChart(drawingView.getWidth(), drawingView.getHeight());

        heatmapImageView.setImageBitmap(heatmap);
        barChartImageView.setImageBitmap(barChart);

        drawingView.setVisibility(View.GONE);
        drawingControls.setVisibility(View.GONE);
        heatmapImageView.setVisibility(View.VISIBLE);
        barChartImageView.setVisibility(View.GONE);
        switchViewButton.setVisibility(View.VISIBLE);
        funAnalysisButton.setVisibility(View.VISIBLE);
        timelapseButton.setVisibility(View.VISIBLE);  // מציג את הכפתור אחרי הניתוח


        showingHeatmap = true;
        isAnalysisMode = true;

        analyzeButton.setImageResource(R.drawable.save_btn_img);
        analyzeButton.setContentDescription("Save");
        analyzeButton.setOnClickListener(v -> saveCurrentViewAsImage());
    }
    private void createAndShowTimelapse() {
        try {
            userAnalysisHelper.generateDrawingTimelapse(timelapseOutputPath, drawingView.getWidth(), drawingView.getHeight());
            showTimelapse();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating time-lapse", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimelapse() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri videoUri = FileProvider.getUriForFile(this, "com.example.finalappandb.fileprovider", new File(timelapseOutputPath));
        intent.setDataAndType(videoUri, "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
    private void saveCurrentViewAsImage() {
        View viewToSave = showingHeatmap ? heatmapImageView : barChartImageView;
        viewToSave.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(viewToSave.getDrawingCache());
        viewToSave.setDrawingCacheEnabled(false);

        String fileName = "analysis_" + (showingHeatmap ? "heatmap_" : "barchart_") + System.currentTimeMillis() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        Toast.makeText(this, "Image saved: " + fileName, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }


    private void switchView() {
        if (showingHeatmap) {
            heatmapImageView.setVisibility(View.GONE);
            barChartImageView.setVisibility(View.VISIBLE);
            showingHeatmap = false;
        } else {
            heatmapImageView.setVisibility(View.VISIBLE);
            barChartImageView.setVisibility(View.GONE);
            showingHeatmap = true;
        }
    }

    private void clearAnalysis() {
        drawingView.clear();
        drawingView.setVisibility(View.VISIBLE);
        drawingControls.setVisibility(View.VISIBLE);
        heatmapImageView.setVisibility(View.GONE);
        barChartImageView.setVisibility(View.GONE);
        switchViewButton.setVisibility(View.GONE);
        funAnalysisButton.setVisibility(View.GONE);
        timelapseButton.setVisibility(View.GONE);


        isAnalysisMode = false;
        analyzeButton.setImageResource(R.drawable.analyze_btn_img);
        analyzeButton.setContentDescription("Analyze");
        analyzeButton.setOnClickListener(v -> startAnalysis());
    }

    private void showFunAnalysis() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הניתוח הכיפי שלך!")
                .setMessage(generateFunAnalysisMessage())
                .setPositiveButton("מדליק!", null)
                .show();
    }

    private SpannableString generateFunAnalysisMessage() {
        StringBuilder message = new StringBuilder();
        message.append("הניתוח הפסיכולוגי המשעשע שלך! 🎨✨\n\n");

        message.append("הי אמן! בוא נראה מה הקווים שלך אומרים עלייך:\n\n");

        message.append("🕰️ ציור בצד שמאל למעלה:\n");
        message.append("• אולי אתה קצת נוסטלגי? מתגעגע לימי הילדות?\n");
        message.append("• או אולי אתה פשוט אסטרטג מלידה, מתכנן כל צעד!\n\n");

        message.append("🌟 ציור במרכז המסך:\n");
        message.append("• וואו, אתה ממש אוהב להיות במרכז העניינים, הא?\n");
        message.append("• או שאולי אתה פשוט מאוזן להפליא. נמאסטה! 🧘‍♂️\n\n");

        message.append("🚀 ציור בצד ימין למטה:\n");
        message.append("• נראה שאתה צופה לעתיד! מה אתה רואה שם?\n");
        message.append("• אולי אתה פשוט מעדיף לסיים דברים בסטייל!\n\n");

        message.append("🌈 ציור מפוזר על כל המסך:\n");
        message.append("• וואו, איזה מוח יצירתי! או שאולי שתית יותר מדי קפה? ☕\n");
        message.append("• אתה בטח אלוף במולטיטאסקינג!\n\n");

        message.append("📏 ציור בקווים ישרים ומסודרים:\n");
        message.append("• המגירות שלך בטח מסודרות להפליא, נכון?\n");
        message.append("• תגיד, במקרה אתה מהנדס או מתכנת? 💻\n\n");

        message.append("🌊 ציור בקווים מעוגלים או לא סדירים:\n");
        message.append("• אתה ממש זורם עם החיים! נעים להכיר, מר ספונטני!\n");
        message.append("• בטח אתה הנשמה של כל מסיבה! 🎉\n\n");

        message.append("הערה חשובה:\n");
        message.append("זכור, זה הכל בשביל הצחוק! 😉 הניתוח הזה הוא כמו קריאה בקלפי טארוט - מעניין, אבל לא בדיוק מדע מדויק. 🔮\n\n");
        message.append("הציורים שלך יכולים להיות מושפעים מכל מיני דברים: איך ישנת, מה אכלת לארוחת בוקר, או אולי סתם כי ככה התחשק לך! 🍳🖌️\n\n");
        message.append("אז קח את זה בקלות, תיהנה מהיצירה שלך, ואל תשכח לצחוק קצת על עצמך! 🎨😄");

        SpannableString spannableString = new SpannableString(message.toString());

        // הוספת הדגשה לכותרות
        addBoldSpan(spannableString, "הניתוח הפסיכולוגי המשעשע שלך! 🎨✨");
        addBoldSpan(spannableString, "ציור בצד שמאל למעלה:");
        addBoldSpan(spannableString, "ציור במרכז המסך:");
        addBoldSpan(spannableString, "ציור בצד ימין למטה:");
        addBoldSpan(spannableString, "ציור מפוזר על כל המסך:");
        addBoldSpan(spannableString, "ציור בקווים ישרים ומסודרים:");
        addBoldSpan(spannableString, "ציור בקווים מעוגלים או לא סדירים:");
        addBoldSpan(spannableString, "הערה חשובה:");

        return spannableString;
    }

    private void addBoldSpan(SpannableString spannableString, String textToStyle) {
        int start = spannableString.toString().indexOf(textToStyle);
        int end = start + textToStyle.length();
        if (start != -1) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userAnalysisHelper != null) {
            userAnalysisHelper.unbind();
        }
    }
}