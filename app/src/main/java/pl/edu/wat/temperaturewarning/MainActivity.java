package pl.edu.wat.temperaturewarning;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static pl.edu.wat.temperaturewarning.R.id.textView;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private MqttClient client;
    private MqttMessage status;
    private MemoryPersistence persistence;
    private View screen;
    private TextView tempText;
    private TextView alarmValue;
    private SeekBar alarmValueBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        screen = this.getWindow().getDecorView();
        screen.setBackgroundColor(Color.WHITE);
        tempText = screen.findViewById(textView);
        alarmValueBar = screen.findViewById(R.id.seekBar);
        alarmValue = screen.findViewById(R.id.textView2);

        alarmValueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                value = progresValue;
                alarmValue.setText(String.valueOf(progresValue));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                alarmValue.setText(String.valueOf(value));
            }
        });
        try{
            initMosquito();
        }
        catch (MqttException e){
            e.printStackTrace();
        }
    }

    private void initMosquito() throws MqttException{
        persistence = new MemoryPersistence();
        client = new MqttClient("tcp://192.168.0.25:1883", MqttClient.generateClientId(), persistence);
        client.setCallback(this);
        client.connect();
        client.subscribe("temp");
    }

    public void connectionLost(Throwable throwable) {
        System.out.println("Connection to MQTT broker lost!");
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        double temperature = Double.parseDouble(new String(mqttMessage.getPayload()));

        System.out.println(new String(mqttMessage.getPayload()));
        if(temperature < Double.parseDouble(alarmValue.getText().toString())){
            status = new MqttMessage("warning".getBytes());
            client.publish("status", status);
        }
        setUI(temperature);
    }

    private void setUI(double temperature){
        if(temperature <= 0)
            setActivityUI(Color.BLUE, "Lodowato");
        if(temperature > 0 && temperature < 10.0)
            setActivityUI(Color.CYAN, "Zimno");
        if(temperature >= 10.0 && temperature < 20.0)
            setActivityUI(Color.GRAY, "Chłodno");
        if(temperature >= 20.0 && temperature < 30.0)
            setActivityUI(Color.YELLOW, "Ciepło");
        if(temperature >= 40.0)
            setActivityUI(Color.RED, "Gorąco");
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

    public void setActivityUI(final int color, final String message) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                screen.setBackgroundColor(color);
                tempText.setText(message);
            }
        });
    }
}

