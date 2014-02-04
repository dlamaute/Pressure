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
	private IOIO ioio_;
	
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
	class Vibration extends Thread{

		
		private final IOIO ioio_;
		
		public Vibration(IOIO ioio){
			Log.i (TAG, ":) Inside Thread");
			ioio_=ioio;
		}
		
		private DigitalOutput led_;
		
		public void run(){
			
			while(true){
				rate = rate/_volts*10;
				try{
					led_ = ioio_.openDigitalOutput(0, true);
					Log.i (TAG, "Rate= "+rate);
					led_.write(false);
					sleep((long) (1000));
					led_.write(true);
					sleep((long) (1000));
				}catch (ConnectionLostException e){
				}catch (Exception e){
					Log.e(TAG, ":( Unexpected exception caught", e);
					ioio_.disconnect();
					break;
				}
			}
		}
	}
	
	class Looper extends BaseIOIOLooper {
		Vibration thread_ = new Vibration(ioio_);
		
		@Override
		protected void setup() throws ConnectionLostException {
			
			_pressureRead = ioio_.openAnalogInput(_pressurePin);
			//_pressureRead.setBuffer(200);
			out = ioio_.openDigitalOutput(23, true);
			thread_.start();
		}

		@Override
		public void loop() throws ConnectionLostException {
			//led_.write(!button_.isChecked());
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
				thread_.join();
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
	
}