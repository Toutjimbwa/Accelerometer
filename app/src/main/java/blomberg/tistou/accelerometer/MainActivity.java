package blomberg.tistou.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    private static final int MAX_DATAPOINTS = 1000;
    private static final double SHAKE_THRESH = 2f;

    TextView mainTextViewX;
    TextView mainTextViewY;
    TextView mainTextViewZ;
    GraphView mainGraphViewX;
    GraphView mainGraphViewY;
    GraphView mainGraphViewZ;

    LineGraphSeries<DataPointInterface> accDataX;
    LineGraphSeries<DataPointInterface> accDataY;
    LineGraphSeries<DataPointInterface> accDataZ;
    int graphStep = 0;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener mAcceleratorListener;
    private Sensor mGravitysensor;
    private SensorEventListener mGravityListener;
    private double[] gravity = new double[]{0,0,0};
    private double[] linear_acceleration = new double[]{0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainTextViewX = (TextView)findViewById(R.id.MAIN_TEXTVIEW_X);
        mainTextViewY = (TextView)findViewById(R.id.MAIN_TEXTVIEW_Y);
        mainTextViewZ = (TextView)findViewById(R.id.MAIN_TEXTVIEW_Z);

        mainGraphViewX = (GraphView)findViewById(R.id.MAIN_GRAPHVIEW_X);
        mainGraphViewY = (GraphView)findViewById(R.id.MAIN_GRAPHVIEW_Y);
        mainGraphViewZ = (GraphView)findViewById(R.id.MAIN_GRAPHVIEW_Z);
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

        Sensor mGravitysensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(mGravityListener, mGravitysensor, SensorManager.SENSOR_DELAY_NORMAL);
        if( mGravitysensor != null){
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

                addDataPoints(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
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
