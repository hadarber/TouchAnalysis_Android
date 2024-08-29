# User Analysis Library

## Overview

The User Analysis Library is a powerful tool designed to analyze and visualize user interactions within Android applications. It provides developers with insights into how users interact with their app's interface, offering features such as heat maps, bar charts, and time-lapse videos of user interactions.

### Why Use This Library?

- **Gain Valuable Insights**: Understand how users interact with your app's UI.
- **Visualize User Behavior**: Generate heat maps and bar charts for easy interpretation of user data.
- **Time-lapse Feature**: Create videos showing the progression of user interactions over time.

## Demo Application

To showcase the capabilities of the User Analysis Library, I've created a demo application. This app features a drawing interface where users can create artwork using various colors and brush sizes. The app then utilizes the library to analyze and visualize how users interacted with the canvas.

### Features of the Demo App:

1. Interactive drawing canvas
2. Multiple color options and brush size control
3. Analysis button to generate heat maps and bar charts
4. Switch view functionality to toggle between different visualizations
5. Fun analysis feature for entertaining insights
6. Time-lapse generation of the drawing process

## How to Use the Library

### Installation

1. Clone this repository:
   ```
   git clone https://github.com/yourusername/user-analysis-library.git
   ```

2. Add the library to your project's `settings.gradle`:
   ```gradle
   include ':app', ':useranalysislibrary'
   ```

3. Add the dependency in your app's `build.gradle`:
   ```gradle
   dependencies {
       implementation project(':useranalysislibrary')
   }
   ```

4. Sync your project with Gradle files.

### Usage in Your Application

1. Initialize the UserAnalysisHelper in your activity:

```java
private UserAnalysisHelper userAnalysisHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    userAnalysisHelper = new UserAnalysisHelper(this);
}
```

2. Add touch data to the helper:

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    userAnalysisHelper.addTouchData(event.getX(), event.getY());
    return super.onTouchEvent(event);
}
```

3. Generate visualizations:

```java
// Generate heat map
Bitmap heatmap = userAnalysisHelper.getHeatMap(width, height);

// Generate bar chart
Bitmap barChart = userAnalysisHelper.getBarChart(width, height);

// Generate time-lapse video
String outputPath = getExternalFilesDir(null) + "/drawing_timelapse.mp4";
userAnalysisHelper.generateDrawingTimelapse(outputPath, width, height);
```

4. Don't forget to unbind the service in onDestroy:

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (userAnalysisHelper != null) {
        userAnalysisHelper.unbind();
    }
}
```

## Key Functions in the Library

### UserAnalysisHelper

- `addTouchData(float x, float y)`: Adds a touch point to the analysis.
- `getHeatMap(int width, int height)`: Generates a heat map visualization.
- `getBarChart(int width, int height)`: Generates a bar chart visualization.
- `generateDrawingTimelapse(String outputPath, int width, int height)`: Creates a time-lapse video of user interactions.

### UserAnalysisService

This service runs in the background to collect and process user interaction data. It's responsible for generating the visualizations and the time-lapse video.

## Screenshots

The main drawing interface

<img src="https://github.com/user-attachments/assets/8a8d283b-df04-4321-8f4b-3a4d81c2890a" width="300" alt="Main drawing interface">

The heat map visualization

<img src="https://github.com/user-attachments/assets/4b03f165-a78c-4ee3-8b7f-815e4f8ca129" width="300" alt="Heat map visualization">

The bar chart visualization

<img src="https://github.com/user-attachments/assets/f6c72837-1b45-424d-8ccb-34c9ea3463aa" width="300" alt="Bar chart visualization">

The fun analysis pop-up

<img src="https://github.com/user-attachments/assets/42ea7158-8a28-4a51-bda8-e36c3dcc15af" width="300" alt="Fun analysis pop-up">


## Contributing

I welcome contributions to improve the User Analysis Library! Please feel free to submit issues, fork the repository and send pull requests.

## License

[Insert your chosen license here]

---

I hope you find the User Analysis Library useful for gaining insights into user behavior in your Android applications. If you have any questions or feedback, please don't hesitate to reach out!
