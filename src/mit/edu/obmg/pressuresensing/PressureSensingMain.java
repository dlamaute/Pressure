package mit.edu.obmg.pressuresensing;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

public class PressureSensingMain extends IOIOActivity {
	private final String TAG = "PressureSensing";
	private ToggleButton button_;
	private DigitalOutput led_;
	
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
			led_ = ioio_.openDigitalOutput(0, true);
			
			_pressureRead = ioio_.openAnalogInput(_pressurePin);
			//_pressureRead.setBuffer(200);
			out = ioio_.openDigitalOutput(23, true);
			Vibration();
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
	}
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	
	public void Vibration(){
		new Thread(new Runnable(){
			public void run(){
				Log.d (TAG, ":) Inside the thread");
				try {
					while(true){
						rate = rate/_volts*10;
						Log.i (TAG, "Rate= "+rate);
						led_.write(false);
						Thread.sleep((long) (rate/2));
						led_.write(true);
						Thread.sleep((long) (rate/2));
					}
					
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
	}
}