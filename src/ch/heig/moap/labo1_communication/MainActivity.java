package ch.heig.moap.labo1_communication;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private Button btnAction    = null;
	private EditText txtMessage 	= null;
	private TextView lblResponse = null;
	private static final String URL = "http://moap.iict.ch:8080/Moap/Basic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnAction = (Button) findViewById(R.id.btnAction);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        lblResponse = (TextView) findViewById(R.id.lblResponse);
        
        btnAction.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				MoapComManager mcm = new MoapComManager() ;
				
				mcm.addCommunicationEventListener(
				new CommunicationEventListener(){
					public boolean handleServerResponse(String response) {
					
						lblResponse.setText(response);
						return true;
						
					}
				});
				
				mcm.sendRequest("lulu",URL);
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
