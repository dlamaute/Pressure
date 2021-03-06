package mit.edu.obmg.pressuresensing;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PressureSensingMain extends IOIOActivity implements OnClickListener{
	private final String TAG = "PressureSensing";
	private ToggleButton button_;
	
	//MultiThreading
	private Thread Vibration;
	Thread thread = new Thread(Vibration);
	
	//Pressure Sensing
	int _pressurePin = 41;
	private AnalogInput _pressureRead;
	float _volts = 1;
	String _roundVolts;
	
	//Vibration
	float rate = 1000;
	float initialRate = 500;
	DigitalOutput out;
	private int sensitivityFactor = 1;
	
	//UI
	private TextView mVoltValue, mRateValue, mSensitivityValue;
	private Button ButtonPlus, ButtonMinus;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pressure_sensing_main);
		
		button_ = (ToggleButton) findViewById(R.id.button);
		ButtonPlus = (Button) findViewById(R.id.ButtonPlus);
		ButtonPlus.setOnClickListener(this);
		ButtonMinus = (Button) findViewById(R.id.ButtonMinus);
		ButtonMinus.setOnClickListener(this);
		
		mVoltValue = (TextView)findViewById(R.id.Volt);
		mRateValue = (TextView)findViewById(R.id.Rate); 
		mSensitivityValue = (TextView)findViewById(R.id.Sensitivity);		
	}
	
	protected void onStart(){
		super.onStart();
	}

	protected void onStop(){
		super.onStop();
		//_pressureRead.close();
	}
	
	class Looper extends BaseIOIOLooper {
		
		@Override
		protected void setup() throws ConnectionLostException {
			
			_pressureRead = ioio_.openAnalogInput(_pressurePin);
			//_pressureRead.setBuffer(200);
			//Log.d(TAG, "Inside Looper");
			
			try {
				Vibration thread_ = new Vibration(ioio_);
				thread_.start();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void loop() throws ConnectionLostException {
			try {
				_volts = _pressureRead.read()*100;
				//DecimalFormat numberFormat = new DecimalFormat("#.00");
				
				//_roundVolts = numberFormat.format(_volts);
				
				//Log.i(TAG, "Volts= "+_volts);
				
				Thread.sleep(100);
				//_volts = _pressureRead.getVoltageBuffered();
			} catch (InterruptedException e) {
			}
			mVoltValue.post(new Runnable() {
				public void run() {
					mVoltValue.setText("Volts: "+_volts);
				}
			});
		}
		
		@Override
		public void disconnected() {
			Log.i(TAG, "IOIO disconnected");
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	class Vibration extends Thread{
    	private DigitalOutput led;
    	
    	private IOIO ioio_;
    	
    	public Vibration(IOIO ioio)throws InterruptedException{
    		ioio_ = ioio;
    	}
    	
    	public void run(){
    		super.run();
			while (true) {
				try {
					led = ioio_.openDigitalOutput(0, true);
					out = ioio_.openDigitalOutput(13,false);
					while (true) {
						if (_volts == 0){
							rate = initialRate - sensitivityFactor;
						}else{
							rate = map(_volts, (float) 0.0, (float) 2.0, (float) rate, (float) 20.0);
						}
						
						mRateValue.post(new Runnable() {
							public void run() {
								mRateValue.setText("Rate: "+ rate);
								mSensitivityValue.setText("Sensitivity: "+ sensitivityFactor);
							}
						});
						
						led.write(true);
						out.write(true);
						sleep((long) rate);
						led.write(false);
						out.write(false);
						sleep((long) rate);
					}
				} catch (ConnectionLostException e) {
				} catch (Exception e) {
					Log.e(TAG, "Unexpected exception caught", e);
					ioio_.disconnect();
					break;
				} finally {
					try {
						ioio_.waitForDisconnect();
					} catch (InterruptedException e) {
					}
				}
			}
    	}
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.ButtonPlus:
			sensitivityFactor = sensitivityFactor + 10;
			break;

		case R.id.ButtonMinus:
			sensitivityFactor = sensitivityFactor - 10;
			break;
		}
		
	}
	
	float map(float x, float in_min, float in_max, float out_min, float out_max)
	{
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
}