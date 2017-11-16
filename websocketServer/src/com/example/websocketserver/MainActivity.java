package com.example.websocketserver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Context mContext;
	private Button mSendButton;
	private EditText mHexEditText;
	
	private final int RECEIVE_MSG = 1;
	private ReceiveHandler mHandler;
	private WebsocketServer mServer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		mHandler = new ReceiveHandler();
		initView();
		bindServer();
	}
	
	private void initView() {
		mSendButton = (Button)this.findViewById(R.id.id_bt_send);
		mHexEditText = (EditText)this.findViewById(R.id.id_et_hex);
		
		mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mServer.mClientSession) {
                	mServer.mClientSession.send(mHexEditText.getText().toString());
                }
                else {
                	Toast.makeText(mContext, "not connect", Toast.LENGTH_LONG).show();
                }
            }
        });
	}
	
	private void bindServer() {
		int port = 9000; //端口
		mServer = new WebsocketServer(port, mHandler);
		mServer.setReuseAddr(true);
		mServer.start();
	}
	
	class ReceiveHandler extends Handler {
        @Override 
        public void handleMessage(Message msg) { 
            // TODO Auto-generated method stub 
        	switch (msg.what) {
	        	case RECEIVE_MSG:
	        		String receive = (String)msg.obj;
	        		Toast.makeText(mContext, "服务端收到消息:" + receive, Toast.LENGTH_SHORT).show();
	        		break;
        		default:
        			break;
        	}
        }
	};
}
