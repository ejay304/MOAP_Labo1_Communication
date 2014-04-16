/**
 * File     : ClientCommunication.java
 * Project  : MoapLab
 * Author   : Markus Jaton 24 November 2011
 *            IICT / HEIG-VD
 *                                       
 * mailto:markus.jaton@heig-vd.ch
 * 
 * Client-side communications. This code is intended to run on Android clients, although
 * it is in non way specific to Android, and should run on any JVM-compliant device as well
 * 
 * Implements a SYNCHRONOUS relationship between a client and a server;
 * This version (due to Carlo Criniti) uses a standard HTTP POST request,
 * allowing to treat the server side with an HTTP servlet rather than a
 * generic servlet. It is slightly less performant than the original version 
 * using a raw POST and a GenericServlet.
 * To make this code asynchronous with respect to the application, use a
 * thread to isolate execution from the main thread
 * 
 * Adapted from Carlo Criniti and Stephen Badan
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ch.heig.moap.labo1_communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class ClientCommunication {
	
	public static final int RESENDINGTRIES = 2;
	public static final int BUFFERSIZE = 8192;
	public int resendingTries = RESENDINGTRIES;
	public int CONNECTION_TIMEOUT = 3000;
	public int SOCKET_TIMEOUT = 5000;
	
	public void setResendingTries(int tries) {
		resendingTries = tries;
	}
	
	/**
	 * Enables the user to send a request (apdu) to the server
	 * @param request Request code (APDU)
	 * @param link Link to the server
	 * @return Response form the server (APDU)
	 * @throws Exception Problem that occured during the communication
	 */
	public String sendRequest(String request, String link) throws Exception {
		
		//StringBuffer to save the response returned by the server
        StringBuffer response = null;
        
        //Variable which indicates how many attempts have been made to send the
        // request to the server
        int numberOfTries = 0;
        //Variable to know if the request was sent with success
        boolean sendingIsSuccesful = false;
        
        //Loops while the request has not been sent correctly
        while(!sendingIsSuccesful){ 
            
            //Counts the number of tries to send the request
            numberOfTries ++;
            
            //StringBuffer to save the response returned by the server
            response = new StringBuffer();
            // System.out.println("REQUEST : "+request);
            try{
            	// Prepare the HTTP request
            	HttpParams httpParameters = new BasicHttpParams();
            	// Set the timeout in milliseconds until a connection is established.
            	// The default value is zero, that means the timeout is not used. 
            	int timeoutConnection = CONNECTION_TIMEOUT;
            	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            	// Set the default socket timeout (SO_TIMEOUT) 
            	// in milliseconds which is the timeout for waiting for data.
            	int timeoutSocket = SOCKET_TIMEOUT;
            	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                
                HttpPost httpPost = new HttpPost(link);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("request", request));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                HttpClient httpClient = new DefaultHttpClient(httpParameters);
                // Execute the request and get response
                HttpResponse resp = httpClient.execute(httpPost);
                
                // Read response as a string
                BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()),
                					ClientCommunication.BUFFERSIZE);
                response = new StringBuffer();
                String line;
                while((line = br.readLine()) != null) {
                	response.append(line + "\n");
                }
                
                //Indicates that the request has been sent correctly
                sendingIsSuccesful = true;
                
            } catch(Exception e) {
                //An error occurred while sending the request
                System.out.println("SAFE : Connection to server failed "+e);
                // Checks if the limit of trials has been reached
                if (numberOfTries >= resendingTries){
                    //The limit of trial count has been reached and the connection
                    // failed each time -> throwing an exception
                    throw new Exception("MOAP : Communication with server impossible :" + e.toString());
                }
            }
        }
        
        if (response.length() == 0) {
        	// Flag a transmission error :
        	return null;
        }
        // System.out.println("Client Comm response "+response.toString());
        // Returns the response XML file
        return response.toString();
	}

	/**
	 * Get a resource file (like a radiography) directly
	 * @param link URL to the resource file to get
	 * @return The byte array of the resource file
	 * @throws Exception Problem that occurred during the communication
	 */
	public byte[] getResource(String link) throws Exception {
		try {
			// Open the URL
            URL url = new URL(link);	            
            URLConnection connection = url.openConnection();
            connection.connect();
            
            // Get data size and stream
            int length = connection.getContentLength();
            InputStream is = url.openStream();
            
            // Read the data
            byte data[] = new byte[length];
            int offset = 0;
            int numRead = 0;
            while (offset < data.length && (numRead=is.read(data, offset, Math.min(data.length-offset, 1024))) >= 0) {
                offset += numRead;
            }
            is.close();
            
            return data;
	    } catch (IOException e) {
	        System.out.println("SAFE: Unable to download file: " + e);
	        return null;
	    }
	}
	

}
