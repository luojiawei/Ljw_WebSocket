# websocket
## 1. 使用场景简介

原有项目是服务端（设备端）与APP通过TCP加上自定义协议接收和收发数据，由于项目所需，需要将之前原生态的APP修改为加载HTML的混合开发APP，因为HTML不能“直接支持”TCP协议，需要使用websocket协议，所以做为服务端，也要增加对websocket协议的支持。
## 2. websocket简介
websocket是html5提出的一个协议规范，参考rfc6455。

websocket约定了一个通信的规范，通过一个握手的机制，客户端（浏览器）和服务器（webserver）之间能建立一个类似tcp的连接，从而方便c－s之间的通信。在websocket出现之前，web交互一般是基于http协议的短连接或者长连接。

websocket是为解决客户端与服务端实时通信而产生的技术。websocket协议本质上是一个基于tcp的协议，是先通过HTTP/HTTPS协议发起一条特殊的http请求进行握手后创建一个用于交换数据的TCP连接，此后服务端与客户端通过此TCP连接进行实时通信。

注意：此时不再需要原HTTP协议的参与了。
## 3. websocket优点
以前web server实现推送技术或者即时通讯，用的都是轮询（polling），在特点的时间间隔（比如1秒钟）由浏览器自动发出请求，将服务器的消息主动的拉回来，在这种情况下，我们需要不断的向服务器发送请求，然而HTTP request 的header是非常长的，里面包含的数据可能只是一个很小的值，这样会占用很多的带宽和服务器资源。

而最比较新的技术去做轮询的效果是Comet – 用了AJAX。但这种技术虽然可达到全双工通信，但依然需要发出请求(reuqest)。

WebSocket API最伟大之处在于服务器和客户端可以在给定的时间范围内的任意时刻，相互推送信息。 浏览器和服务器只需要要做一个握手的动作，在建立连接之后，服务器可以主动传送数据给客户端，客户端也可以随时向服务器发送数据。 此外，服务器与客户端之间交换的标头信息很小。

WebSocket并不限于以Ajax(或XHR)方式通信，因为Ajax技术需要客户端发起请求，而WebSocket服务器和客户端可以彼此相互推送信息；

> 因此从服务器角度来说，websocket有以下好处：
> 1. 节省每次请求的header,http的header一般有几十字节。
> 2. Server Push ,服务器可以主动传送数据给客户端。

## 5. websocket协议
与http协议不同的请求/响应模式不同，Websocket在建立连接之前有一个Handshake（Opening Handshake）过程，在关闭连接前也有一个Handshake（Closing Handshake）过程，建立连接之后，双方即可双向通信。

WebSocket 协议流程如下图：

![image](https://note.youdao.com/yws/public/resource/d5afd19abb45d26f2be9f28694a34bf3/xmlnote/47E9389391974B29A3D2959A87412DA6/2443)

客户端http请求


```HTTP
GET / HTTP/1.1
Host: localhost:1984
Connection: Upgrade
Pragma: no-cache
Cache-Control: no-cache
Upgrade: websocket
Origin: file://
Sec-WebSocket-Version: 13
User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36
Accept-Encoding: gzip, deflate, sdch, br
Accept-Language: zh-CN,zh;q=0.8,en;q=0.6
Sec-WebSocket-Key: CdFAfl2vqePUEHQ9lkUzFg==
Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
```


服务端http回复


```HTTP
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: FU4VfF/Onl1fCIZD9QIqfGWTJEM=
```

- Upgrade：WebSocket

    表示这是一个特殊的 HTTP 请求，请求的目的就是要将客户端和服务器端的通讯协议从 HTTP 协议升级到 WebSocket 协议。

- Sec-WebSocket-Key

    是一段浏览器base64加密的密钥，server端收到后需要提取Sec-WebSocket-Key 信息，然后加密。

- Sec-WebSocket-Accept

    服务器端在接收到的Sec-WebSocket-Key密钥后追加一段神奇字符串“258EAFA5-E914-47DA-95CA-C5AB0DC85B11”，并将结果进行sha-1哈希，然后再进行base64加密返回给客户端（就是Sec-WebSocket-Key）。 比如：
    
    
```
    function encry($req)
    {
        $key = $this->getKey($req);
        $mask = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"; 
        # 将 SHA-1 加密后的字符串再进行一次 base64 加密
        return base64_encode(sha1($key . '258EAFA5-E914-47DA-95CA-C5AB0DC85B11', true));
    }
```

如果加密算法错误，客户端在进行校检的时候会直接报错。如果握手成功，则客户端侧会出发onopen事件。
- Sec-WebSocket-Protocol

    表示客户端请求提供的可供选择的子协议，及服务器端选中的支持的子协议，“Origin”服务器端用于区分未授权的websocket浏览器
- Sec-WebSocket-Version: 13

    客户端在握手时的请求中携带，这样的版本标识，表示这个是一个升级版本，现在的浏览器都是使用的这个版本。
- HTTP/1.1 101 Switching Protocols

    101为服务器返回的状态码，所有非101的状态码都表示handshake并未完成。

## 5. websocket使用

**5.1 下载Java-WebSocket-1.3.6.jar**

在网上搜索相关资料，很多人用Java-WebSocket库，找到其github地址
    https://github.com/TooTallNate/Java-WebSocket
    
下载发现没有已经编译好的jar，不想去折腾编译的问题，用Android Stuido在build.gradle文件dependencies中加入
    
```
compile "org.java-websocket:Java-WebSocket:1.3.6"
```

Android Stuido自动将Java-WebSocket-1.3.6.jar包下载下来，在目录
    
```
C:\Users\用户名\.gradle\caches\modules-2\files-2.1\org.java-websocket\Java-WebSocket\1.3.6
```


**5.2 android websocket服务端**


    
```java
public class WebsocketServer extends WebSocketServer {

	public String TAG = "WebsocketServer";
	
	public WebSocket mClientSession = null;
	private final int RECEIVE_MSG = 1;
	private ReceiveHandler mHandler;
	
	public WebsocketServer(int port, ReceiveHandler handler) {
		super(new InetSocketAddress(port));
		
		mHandler = handler;
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake arg1) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Server onOpen getHostAddress:" + conn.getRemoteSocketAddress().getAddress().getHostAddress());
		mClientSession = conn;
	}
	
	@Override
	public void onMessage(WebSocket conn, String arg1) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Server onMessage getHostAddress:" + conn.getRemoteSocketAddress().getAddress().getHostAddress());
		Log.d(TAG, "Server onMessage msg:" + arg1);
		
		Message msg = mHandler.obtainMessage();
		msg.what = RECEIVE_MSG;
		msg.obj = arg1;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onClose(WebSocket conn, int arg1, String arg2, boolean arg3) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Server onClose getHostAddress:" + conn.getRemoteSocketAddress().getAddress().getHostAddress());
		
		mClientSession = null;
	}

	@Override
	public void onError(WebSocket conn, Exception arg1) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Server client onError:" + arg1);
		
		mClientSession = null;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.d(TAG, "Server client onStart");
	}
}
```
调用启动服务端

```java

private void bindServer() {
    int port = 9000; //端口
    mServer = new WebsocketServer(port, mHandler);
    mServer.setReuseAddr(true);
    mServer.start();
}
```

    
**5.3 android websocket设备端**


```java
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
```

**5.4 web(html) websocket客户端**

用android服务器和web 客户端调试，把手机usb连接电脑后，进入“设置”打开“USB网络共享”，在adb shell中ifconfig查看usb网口的ip地址，打开服务端，再启动web客户端，将localhost改为对应的ip地址，1984改为对应的端口号，即可连接和发送消息。
    
web客户端界面如下：
![image](https://note.youdao.com/yws/public/resource/d5afd19abb45d26f2be9f28694a34bf3/xmlnote/DF55FD9B3E134A98A09BB4D4B0F96E6F/2544)
    
    
##     6.参考资料
https://github.com/TooTallNate/Java-WebSocket

[WebScoket支持safari+chrome+firefox的规范和协议](http://www.cnblogs.com/pctzhang/archive/2012/02/19/2358496.html)


[Websocket协议的学习、调研和实现](https://www.cnblogs.com/lizhenghn/p/5155933.html)