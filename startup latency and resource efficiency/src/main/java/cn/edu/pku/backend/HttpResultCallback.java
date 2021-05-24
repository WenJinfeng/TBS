package cn.edu.pku.backend;

import cn.edu.pku.backend.action.ResultCallback;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpResultCallback implements ResultCallback, Runnable {
	public HttpResultCallback(ChannelHandlerContext ctx, String jsonCallback) {
		ctxField = ctx;
		this.jsonCallback = jsonCallback;
	}

	protected ChannelHandlerContext ctxField;
	String jsonCallback;
	Map<String, String> extraHeaders = new HashMap<String, String>();
	DefaultFullHttpResponse response;

	@Override
	public void onResult(String ret) {
		if (jsonCallback != null) {
			ret = (jsonCallback + "(" + ret + ")");
		}
		if (ret != null && !ctxField.isRemoved()) {
			byte[] bytes = null;

			bytes = ret.getBytes();
			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, OK, Unpooled.wrappedBuffer(bytes));
			response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
			for (String key : extraHeaders.keySet())
				response.headers().add(key, extraHeaders.get(key));
			ctxField.channel().eventLoop().execute(this);
		} else {
			// Just ignore
		}
	}

	public void addHeader(String key, String val) {
		extraHeaders.put(key, val);
	}

	@Override
	public void run() {

		ctxField.writeAndFlush(response);
		ctxField.close();
	}

}
