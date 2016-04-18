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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

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

                Log.i("SENSORS GRAVITY_ACC", "x: "+linear_acceleration[0]);
                Log.i("SENSORS GRAVITY_ACC", "y: "+linear_acceleration[1]);
                Log.i("SENSORS GRAVITY_ACC", "z: "+linear_acceleration[2]);
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

                Log.i("SENSORS LINEAR_ACC", "x: "+linear_acceleration[0]);
                Log.i("SENSORS LINEAR_ACC", "y: "+linear_acceleration[1]);
                Log.i("SENSROS LINEAR_ACC", "z: "+linear_acceleration[2]);
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
}
