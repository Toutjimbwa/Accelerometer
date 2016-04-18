package blomberg.tistou.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    private static final double ARROW_SENSITIVITY = 200;
    private static final double ARROW_SMOOTHNESS = 5;
    private static final int MAX_DATAPOINTS = 1000;
    private static final double SHAKE_THRESH = 2f;

    @Bind(R.id.MAIN_TEXTVIEW_X)TextView mainTextViewX;
    @Bind(R.id.MAIN_TEXTVIEW_Y)TextView mainTextViewY;
    @Bind(R.id.MAIN_TEXTVIEW_Z)TextView mainTextViewZ;
    @Bind(R.id.MAIN_GRAPHVIEW_X)GraphView mainGraphViewX;
    @Bind(R.id.MAIN_GRAPHVIEW_Y)GraphView mainGraphViewY;
    @Bind(R.id.MAIN_GRAPHVIEW_Z)GraphView mainGraphViewZ;
    @Bind(R.id.MAIN_IMAGEVIEW_UP)ImageView mainImageView_up;
    @Bind(R.id.MAIN_IMAGEVIEW_DOWN)ImageView mainImageView_down;
    @Bind(R.id.MAIN_IMAGEVIEW_LEFT)ImageView mainImageView_left;
    @Bind(R.id.MAIN_IMAGEVIEW_RIGHT)ImageView mainImageView_right;
    @Bind(R.id.MAIN_IMAGEVIEW_COME)ImageView mainImageView_come;
    @Bind(R.id.MAIN_IMAGEVIEW_LEAVE)ImageView mainImageView_leave;
    @Bind(R.id.MAIN_BTN_SWITCH)Button mainButtonSwitch;
    @Bind(R.id.MAIN_LINLAY_GRAPHS)LinearLayout mainLinlayGraphs;
    @Bind(R.id.MAIN_RELLAY_ARROWS)RelativeLayout mainRellayArrows;

    private boolean arrowsVisible;

    LineGraphSeries<DataPointInterface> accDataX;
    LineGraphSeries<DataPointInterface> accDataY;
    LineGraphSeries<DataPointInterface> accDataZ;
    int graphStep = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener mAcceleratorListener;
    private Sensor mGravitySensor;
    private SensorEventListener mGravityListener;
    private double[] gravity = new double[]{0,0,0};
    private double[] linear_acceleration = new double[]{0,0,0};
    private double ARROW_MIN_SIZE = 10f;
    private double ARROW_MAX_SIZE = 400f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupButton();
        setupGraphData();
    }

    private void setupButton() {
        mainButtonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchGraphArrows();
            }
        });
    }

    private void switchGraphArrows() {
        if(arrowsVisible){
            mainButtonSwitch.setText("arrows");
            mainRellayArrows.setVisibility(View.GONE);
            mainLinlayGraphs.setVisibility(View.VISIBLE);
            arrowsVisible=false;
        }else {
            mainButtonSwitch.setText("graphs");
            mainRellayArrows.setVisibility(View.VISIBLE);
            mainLinlayGraphs.setVisibility(View.GONE);
            arrowsVisible=true;
        }
    }

    private void setupGraphData() {
        accDataX = new LineGraphSeries<>(new DataPointInterface[]{});
        accDataY = new LineGraphSeries<>(new DataPointInterface[]{});
        accDataZ = new LineGraphSeries<>(new DataPointInterface[]{});
        mainGraphViewX.addSeries(accDataX);
        mainGraphViewY.addSeries(accDataY);
        mainGraphViewZ.addSeries(accDataZ);
    }

    private void addDataPoints(double x, double y, double z){
        graphStep++;
        accDataX.appendData(new com.jjoe64.graphview.series.DataPoint(graphStep, x), false, MAX_DATAPOINTS);
        accDataY.appendData(new com.jjoe64.graphview.series.DataPoint(graphStep, y), false, MAX_DATAPOINTS);
        accDataZ.appendData(new com.jjoe64.graphview.series.DataPoint(graphStep, z), false, MAX_DATAPOINTS);
    }

    private void setupAccelerometer() {

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        mGravityListener = new SensorEventListener(){
            @Override
            public void onSensorChanged(SensorEvent event) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.

                final float alpha = 0.8f;

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = event.values[0];
                gravity[1] = event.values[1];
                gravity[2] = event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(mGravityListener, mGravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        if( mGravityListener != null){
            toast("gravity sensor initialised");
        }else{
            toast("gravity sensor NULL");
        }

        mAcceleratorListener = new SensorEventListener(){
            @Override
            public void onSensorChanged(SensorEvent event) {
                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.

                final float alpha = 0.8f;
                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];

                addDataPoints(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);

                if(arrowsVisible){
                    setImageSizes(accDataX, accDataY, accDataZ);
                }else{
                    String x = String.format("X: %.1f", linear_acceleration[0]);
                    String y = String.format("Y: %.1f", linear_acceleration[1]);
                    String z = String.format("Z: %.1f", linear_acceleration[2]);
                    mainTextViewX.setText(x);
                    mainTextViewY.setText(y);
                    mainTextViewZ.setText(z);
                    setTextColor(mainTextViewX, linear_acceleration[0]);
                    setTextColor(mainTextViewY, linear_acceleration[1]);
                    setTextColor(mainTextViewZ, linear_acceleration[2]);

                    setGraphColor(mainGraphViewX, linear_acceleration[0]);
                    setGraphColor(mainGraphViewY, linear_acceleration[1]);
                    setGraphColor(mainGraphViewZ, linear_acceleration[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mAcceleratorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if( mAccelerometer != null){
            toast("accelerometer initialised");
        }else{
            toast("accelerometer NULL");
        }
    }

    private void setImageSizes(LineGraphSeries x, LineGraphSeries y, LineGraphSeries z) {
        double size_up = calculateImageSizeA(y);
        double size_down = calculateImageSizeB(y);
        double size_right = calculateImageSizeA(x);
        double size_left = calculateImageSizeB(x);
        double size_come = calculateImageSizeA(z);
        double size_leave = calculateImageSizeB(z);
        setSize(mainImageView_up, size_up);
        setSize(mainImageView_down, size_down);
        setSize(mainImageView_left, size_left);
        setSize(mainImageView_right, size_right);
        setSize(mainImageView_come, size_come);
        setSize(mainImageView_leave, size_leave);
    }

    private double calculateImageSizeB(LineGraphSeries x) {
        Iterator<DataPoint> iterator = new Iterator<DataPoint>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public DataPoint next() {
                return null;
            }

            @Override
            public void remove() {

            }
        };
        try {
            iterator = x.getValues((double)graphStep- ARROW_SMOOTHNESS, (double)graphStep);
        }catch (Exception e){
            e.printStackTrace();
        }

        double mean = 0;
        if(graphStep > ARROW_SMOOTHNESS) {
            while (iterator.hasNext()) {
                DataPointInterface dpi = iterator.next();
                Double d = dpi.getY();
                mean += d;
            }
        }
        mean = mean/ ARROW_SMOOTHNESS;
        double size = 200 - mean*ARROW_SENSITIVITY;
        if(size < ARROW_MIN_SIZE){
            return ARROW_MIN_SIZE;
        }else if (size > ARROW_MAX_SIZE){
            return ARROW_MAX_SIZE;
        }else{
            return size;
        }
    }

    private double calculateImageSizeA(LineGraphSeries x) {
        Iterator<DataPoint> iterator = new Iterator<DataPoint>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public DataPoint next() {
                return null;
            }

            @Override
            public void remove() {

            }
        };
        try {
            iterator = x.getValues((double)graphStep- ARROW_SMOOTHNESS, (double)graphStep);
        }catch (Exception e){
            e.printStackTrace();
        }

        double mean = 0;
        if(graphStep > ARROW_SMOOTHNESS) {
            while (iterator.hasNext()) {
                DataPointInterface dpi = iterator.next();
                Double d = dpi.getY();
                mean += d;
            }
        }
        mean = mean/ ARROW_SMOOTHNESS;
        double size = 200 + mean*ARROW_SENSITIVITY;
        if(size < ARROW_MIN_SIZE){
            return ARROW_MIN_SIZE;
        }else if (size > ARROW_MAX_SIZE){
            return ARROW_MAX_SIZE;
        }else{
            return size;
        }

    }

    private void setSize(ImageView imageView, double i) {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) imageView.getLayoutParams();
        params.width = (int)i;
        params.height = (int)i;
        imageView.setLayoutParams(params);
    }

    private void setTextColor(TextView textView, double acc) {
        if(didShake(acc)){
            textView.setTextColor(getResources().getColor(R.color.idle));
            textView.setTextSize(getResources().getDimension(R.dimen.threshhold));
        }else{
            textView.setTextColor(getResources().getColor(R.color.threshhold));
            textView.setTextSize(getResources().getDimension(R.dimen.idle));
        }
    }

    private void setGraphColor(GraphView graphView, double acc) {
        if(didShake(acc)) {
            graphView.setBackgroundColor(getResources().getColor(R.color.threshhold));
        }else {
            graphView.setBackgroundColor(getResources().getColor(R.color.idle));
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAccelerometer();
    }

    public boolean didShake(double d){
        if( (d > SHAKE_THRESH) || (d < -SHAKE_THRESH)){
            return true;
        }else {
            return false;
        }
    }
}
