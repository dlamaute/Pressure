package mit.edu.obmg.pressuresensing;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

public class PressureSensingMain extends IOIOActivity {
	private final String TAG = "PressureSensing";
	private ToggleButton button_;
	
	private Thread Vibration;
	Thread thread = new Thread(Vibration);
	
	//Pressure Sensing
	int _pressurePin = 41;
	private AnalogInput _pressureRead;
	float _volts = 1;
	
	//Vibration
	float rate = 1000;
	DigitalOutput out;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pressure_sensing_main);
		
		button_ = (ToggleButton) findViewById(R.id.button);

		
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
			
			try {

				MyThread thread_ = new MyThread(ioio_);
				thread_.start();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Vibration = new Vibration(ioio_);
			//thread.start();
		}

		@Override
		public void loop() throws ConnectionLostException {
			try {
				_volts = _pressureRead.getVoltage();
				Log.i(TAG, "Volts= "+_volts);
				Thread.sleep(100);
				//_volts = _pressureRead.getVoltageBuffered();
			} catch (InterruptedException e) {
			}
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
	
	class MyThread extends Thread{
    	private DigitalOutput led;
    	
    	private IOIO ioio_;
    	
    	public MyThread(IOIO ioio)throws InterruptedException{
    		ioio_ = ioio;
    	}
    	
    	public void run(){
    		super.run();
			while (true) {
				try {
					led = ioio_.openDigitalOutput(0, true);
					out = ioio_.openDigitalOutput(23, true);
					while (true) {
						rate = 10/_volts*10;
						Log.i (TAG, "Rate= "+ rate);
						led.write(true);
						out.write(true);
						sleep((long) rate/2);
						led.write(false);
						out.write(false);
						sleep((long) rate/2);
					}
				} catch (ConnectionLostException e) {
				} catch (Exception e) {
					Log.e("HelloIOIOPower", "Unexpected exception caught", e);
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
}