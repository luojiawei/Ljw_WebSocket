## Js Websocket 发送二进制

此demo只能发送string数据，如果发送二进制数据可参考如下示例：

```JS
var a = new ArrayBuffer(1);
var b = new Uint8Array(a);
b[0] = 0xfc;
ws.send(a); // ws为websocket对象
```
