package com.example.websocketclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public String TAG = "websocketClient";
	private Context mContext;
	private WebSocketClient client;
	private Button mSendButton;
	private EditText mHexEditText;
	
	//private String mDefaultServer = "ws://192.168.42.129:9000";
	private String mDefaultServer = "ws://192.168.43.1:9000";
	
	private final int RECEIVE_MSG = 1;
	private ReceiveHandler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		mHandler = new ReceiveHandler();
		initView();
		connectServer();
	}
	
	private void initView() {
		mSendButton = (Button)this.findViewById(R.id.id_bt_send);
		mHexEditText = (EditText)this.findViewById(R.id.id_et_hex);
		
		mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != client) {
                	client.send(mHexEditText.getText().toString());
                }
                else {
                	Toast.makeText(mContext, "not connect", Toast.LENGTH_LONG).show();
                }
            }
        });
	}
	
	private void connectServer() {
		URI uri = null;
		try {
			// 默认连接地址和端口
			uri = new URI(mDefaultServer);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.d(TAG, "URI printStackTrace:" + e1.toString());
		}
		
		client = new WebSocketClient(uri) {
			
			@Override
			public void onOpen(ServerHandshake arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "client onOpen getHttpStatus:" + arg0.getHttpStatus());
			}
			
			@Override
			public void onMessage(String arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "client onMessage:" + arg0);
				Message msg = mHandler.obtainMessage();
				msg.what = RECEIVE_MSG;
				msg.obj = arg0;
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onError(Exception arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "client onError:" + arg0);
				client = null;
			}
			
			@Override
			public void onClose(int arg0, String arg1, boolean arg2) {
				// TODO Auto-generated method stub
				Log.d(TAG, "client onClose:" + arg0);
				client = null;
			}
		};
		
		try {
			boolean con = client.connectBlocking();
			if (con) {
				Toast.makeText(this, "connect success", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(this, "connect fail", Toast.LENGTH_LONG).show();
				client = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "client client.connectBlocking() InterruptedException:" + e.toString());
		}
	}
	
	class ReceiveHandler extends Handler {
        @Override 
        public void handleMessage(Message msg) { 
            // TODO Auto-generated method stub 
        	switch (msg.what) {
	        	case RECEIVE_MSG:
	        		String receive = (String)msg.obj;
	        		Toast.makeText(mContext, "客户端收到消息:" + receive, Toast.LENGTH_SHORT).show();
	        		break;
        		default:
        			break;
        	}
        }
	};
}