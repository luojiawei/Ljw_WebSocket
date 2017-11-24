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

## 4. websocket协议
与http协议不同的请求/响应模式不同，Websocket在建立连接之前有一个Handshake（Opening Handshake）过程，在关闭连接前也有一个Handshake（Closing Handshake）过程，建立连接之后，双方即可双向通信。

WebSocket 协议流程如下图：

![image](https://note.youdao.com/yws/public/resource/d5afd19abb45d26f2be9f28694a34bf3/xmlnote/47E9389391974B29A3D2959A87412DA6/2443)

**4.1 握手**


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

**4.2 数据帧**

Websocket协议通过序列化的数据帧传输数据。数据封包协议中定义了opcode、payload length、Payload data等字段。其中要求：

1. 客户端向服务器传输的数据帧必须进行掩码处理：服务器若接收到未经过掩码处理的数据帧，则必须主动关闭连接。
2. 服务器向客户端传输的数据帧一定不能进行掩码处理。客户端若接收到经过掩码处理的数据帧，则必须主动关闭连接。


针对上情况，发现错误的一方可向对方发送close帧（状态码是1002，表示协议错误），以关闭连接。

具体数据帧格式如下图所示：

![image](https://note.youdao.com/yws/public/resource/d5afd19abb45d26f2be9f28694a34bf3/xmlnote/79B9391F40DA48349EEF26E60172FCE5/2622)

- FIN

    标识是否为此消息的最后一个数据包，占 1 bit
- RSV1, RSV2, RSV3: 用于扩展协议，一般为0，各占1bit
- Opcode
数据包类型（frame type），占4bits

    0x0：标识一个中间数据包
    
    0x1：标识一个text类型数据包
    
    0x2：标识一个binary类型数据包
    
    0x3-7：保留
    
    0x8：标识一个断开连接类型数据包
    
    0x9：标识一个ping类型数据包
    
    0xA：表示一个pong类型数据包
    
    0xB-F：保留

- MASK：占1bits

    用于标识PayloadData是否经过掩码处理。如果是1，Masking-key域的数据即是掩码密钥，用于解码PayloadData。客户端发出的数据帧需要进行掩码处理，所以此位是1。

- Payload length

    Payload data的长度，占7bits，7+16bits，7+64bits：

    1. 如果其值在0-125，则是payload的真实长度。
    2. 如果值是126，则后面2个字节形成的16bits无符号整型数的值是payload的真实长度。注意，网络字节序，需要转换。
    3. 如果值是127，则后面8个字节形成的64bits无符号整型数的值是payload的真实长度。注意，网络字节序，需要转换。
    
    这里的长度表示遵循一个原则，用最少的字节表示长度（尽量减少不必要的传输）。举例说，payload真实长度是124，在0-125之间，必须用前7位表示；不允许长度1是126或127，然后长度2是124，这样违反原则。

- Payload data
应用层数据

    server解析client端的数据
    
    接收到客户端数据后的解析规则如下：

- 1byte

    1. 1bit: frame-fin，x0表示该message后续还有frame；x1表示是message的最后一个frame
    2. 3bit: 分别是frame-rsv1、frame-rsv2和frame-rsv3，通常都是x0
    
    3. 4bit: frame-opcode，x0表示是延续frame；x1表示文本frame；x2表示二进制frame；x3-7保留给非控制frame；x8表示关 闭连接；x9表示ping；xA表示pong；xB-F保留给控制frame
- 2byte

    1. 1bit: Mask，1表示该frame包含掩码；0表示无掩码
    2. 7bit、7bit+2byte、7bit+8byte: 7bit取整数值，若在0-125之间，则是负载数据长度；若是126表示，后两个byte取无符号16位整数值，是负载长度；127表示后8个 byte，取64位无符号整数值，是负载长度
    3. 3-6byte: 这里假定负载长度在0-125之间，并且Mask为1，则这4个byte是掩码
    4. 7-end byte: 长度是上面取出的负载长度，包括扩展数据和应用数据两部分，通常没有扩展数据；若Mask为1，则此数据需要解码，解码规则为- 1-4byte掩码循环和数据byte做异或操作。

**4.3 心跳ping帧**

Opcode中的值代表着这个帧的作用(0-7:数据帧 8-F:控制帧)，0x9代表ping帧。


发送ping帧，例如：


```
89 80 A4 E9 5C 70
```


默认一分钟发送一次，可通过接口setConnectionLostTimeout( int connectionLostTimeout )去修改发送间隔时间（单位是秒）

当接收到ping帧的时候,应该返回一个pong帧,而且,ping帧可能带有数据,那么pong帧也需要带上ping过来的数据并返回。

**4.4 关闭帧**

Opcode中的值代表着这个帧的作用(0-7:数据帧 8-F:控制帧)，0x8代表关闭帧。

发送一个关闭帧，例如：


```
88 82 79 01 D1 A3 7A E9
```


- 当接收到关闭帧这个控制帧后,应该 尽快吧没有发送完毕的数据发送完(例如分片),然后再响应一个关闭帧.
- 关闭帧内可能会有数据,可以用来说明关闭的理由等等,但是没有规定是人类可读语言,所以不一定是字符串

当连接不需要继续存在时,就可以结束了
基本流程是:

1. 一端发送一个 关闭帧
2. 另外一端再响应一个关闭帧
3. 断开TCP

完成这三步即可,但是,存在特殊情况

- 有一端的程序关闭了,TCP连接直接关闭,并没有发送 关闭帧
- 有一端的程序发送关闭帧以后,马上断开了TCP,另外一端发送关闭帧的时候,报错了

我就被以上的坑坑过,所以要注意一下,当TCP连接出错时,直接当成已经关闭即可
如果 浏览器发送关闭帧,服务器没有响应的话,大概会在30-60秒左右会断开TCP,所以不需要怕发了关闭帧缺没有断开TCP(但如果是自己实现的客户端就要注意了!!!)

经过 **TCP连接 → 握手协议 → 数据传输 → 连接结束** 就基本走完一个websocket流程了。

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
    

如果需要发送二进制数据，可参考[《Js Websocket 发送二进制》](https://github.com/luojiawei/WebSocket/tree/master/webClient)
    
##     6.参考资料
https://github.com/TooTallNate/Java-WebSocket

[WebScoket支持safari+chrome+firefox的规范和协议](http://www.cnblogs.com/pctzhang/archive/2012/02/19/2358496.html)


[Websocket协议的学习、调研和实现](https://www.cnblogs.com/lizhenghn/p/5155933.html)