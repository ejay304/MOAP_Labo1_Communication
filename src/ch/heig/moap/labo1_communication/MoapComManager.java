package ch.heig.moap.labo1_communication;

import java.util.LinkedList;

import android.os.AsyncTask;

public class MoapComManager {
	
	private LinkedList<CommunicationEventListener> eventListner;
	
	public MoapComManager(){
		eventListner = new LinkedList<CommunicationEventListener>();
	}
	
	public void sendRequest(String request, String link){
		
		new RequestTask().execute(request, link);
	}

	
	public void addCommunicationEventListener(CommunicationEventListener listener){
		eventListner.add(listener);
		
	}

	public class RequestTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			
			ClientCommunication connection = new ClientCommunication();
			
			String response = "";
			
			try {
				response = connection.sendRequest(params[0], params[1]);
				
				for(CommunicationEventListener c : eventListner)
					c.handleServerResponse(response);
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
