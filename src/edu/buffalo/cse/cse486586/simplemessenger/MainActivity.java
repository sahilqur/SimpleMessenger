package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private boolean connected=false;
	private TextView txt_serv;
	private EditText txt;
	private Button send;
	private String tx="";
	public static String serv_ip= "10.0.2.2";
	public final int server_port=10000;
	public int port;
	private Handler hand=new Handler(); 
	private ServerSocket serv_sock;
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.e("jgsfjsg","akjdh");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        if(portStr.equals("5554")) {
        	port=11112;        
        }
        else {
        	port=11108;
        }     
        txt=(EditText) findViewById(R.id.editText1);
        send=(Button) findViewById(R.id.mybutton);
        txt_serv=(TextView) findViewById(R.id.textView1);
         
        Thread serv = new Thread(new TCPserver());
        serv.start();
        send.setOnClickListener(new Button.OnClickListener() {
   			public void onClick(View v) {
				tx=txt.getText().toString();
				txt.setText("");
        		if(!tx.equals("")) {
        			Thread client = new Thread(new TCPclient());
        			client.start();
			    }
   		    }
        });
    }
        
        
    public class TCPserver implements Runnable {
    	
    	private String msg1;

		public void run() {
    		try {
    			if(serv_ip!=null) {
    				hand.post(new Runnable() {
    					public void run() {
    						txt_serv.setText("Listening for messages");    				
    					}
    				});
    			    serv_sock = new ServerSocket(server_port);
    			    while (true) {
    			    	Socket cl = serv_sock.accept();
    			    	hand.post(new Runnable() {
    			    		public void run() {
    			    			txt_serv.setText("Connected.");
    			    		}
    			    	});
    			    	
    			    	try {
    			    		BufferedReader in = new BufferedReader(new InputStreamReader(cl.getInputStream()));
    			    	    msg1 = null;
    			    		while ((msg1 = in.readLine()) != null) {
    			    			hand.post(new Runnable() {
    			    				public void run() {
    			    					txt_serv.setText(msg1);
    			    				}
    			    			});
    			    		}
    			    		break;
    			    	} catch(Exception e) {
    			    		hand.post(new Runnable() {
    			    			public void run() {
    			    				txt_serv.setText("Connection broken"); 
    			    			}
    			    	    });
    			    		e.printStackTrace();
    			    	}
    			    }
           		} else {
           			hand.post(new Runnable() {
           				public void run() {
           					txt_serv.setText("Null Server ip");
           				}

           			});

           		}
    		} catch(Exception e) {
    			hand.post(new Runnable() {
    				public void run() {
    					txt_serv.setText("Error");
    				}
    			});
    			e.printStackTrace();
    		}
    	}
    }
    
    protected void onStop() {
    	super.onStop();
    	try {
    		serv_sock.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public class TCPclient implements Runnable {
    	
    	public void run() {
    		try {
    			Socket socket=new Socket(serv_ip,port);
    			connected=true;
    			while(connected) {
    				try {
    					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter (socket.getOutputStream())), true);
    					out.println(tx);
        				} catch (Exception e) {
        					Log.e("ClientActivity", "S: Error", e);
        				}
         		}
    			socket.close();
    			Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
            	Log.e("ClientActivity", "C: Error", e);
            	connected=false;
            }
        }
    }
    
    
        

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
