package cn.edu.pku.backend;

import cn.edu.pku.backend.action.ActionExecutor;
import cn.edu.pku.backend.action.BenckmarkActions;
import cn.edu.pku.backend.action.ResultCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Sharable
public class ProxyHandler extends SimpleChannelInboundHandler<HttpObject> {
	private static final String UNSUPPORTEDHTTPMETHOD = "{\"msg\":\"unsupported http method\"}";
	private static final String UNSUPPORTEDACTION = "{\"msg\":\"unsupported action\"}";
	public static ExecutorService executor = Executors.newFixedThreadPool(30);

	private static ActionExecutor<ResultCallback, JsonObject> actionExecutor = new ActionExecutor<>(executor,
			new BenckmarkActions());
	HttpFileHandleAdapter fileServer;

	//静态网页资源的路径
	public ProxyHandler() {
		fileServer = new HttpFileHandleAdapter(new File("./resources/static/").getAbsolutePath());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else {
			System.out.println("[Ignore] " + msg.getClass().getCanonicalName());
		}
	}
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
		if (!msg.getUri().startsWith("/serverless")) {
			try {
				fileServer.channelRead0(ctx, msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		HttpRequest req = (HttpRequest) msg;
		FullHttpResponse fullResponse = null;
		HttpMethod method = req.getMethod();
		if (method.equals(HttpMethod.GET)) {
			QueryStringDecoder decoderQuery = new QueryStringDecoder(req.getUri());
			Map<String, List<String>> parame = decoderQuery.parameters();
			JsonObject transfomedParam = new JsonObject();
			for (String key : parame.keySet()) {
				List<String> val = parame.get(key);
				if (val != null)
					transfomedParam.addProperty(key, val.get(0));
			}
			handleReq(transfomedParam, ctx, req);
			return;
		} else if (method.equals(HttpMethod.POST)) {
			ByteBuf content = msg.content();
			byte[] reqContent = new byte[content.readableBytes()];
			content.readBytes(reqContent);
			String strContent;
			try {
				strContent = new String(reqContent, "UTF-8");
				JsonObject map = new JsonParser().parse(strContent).getAsJsonObject();
				handleReq(map, ctx, req);
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			fullResponse = new DefaultFullHttpResponse(req.protocolVersion(), OK,
					Unpooled.wrappedBuffer(UNSUPPORTEDHTTPMETHOD.getBytes()));
		}
		ChannelFuture f = ctx.write(fullResponse);
		f.addListener(ChannelFutureListener.CLOSE);

	}

	static final String PARAM_ACTION = "action";

	private void handleReq(JsonObject map, ChannelHandlerContext ctx, HttpRequest req) {
		try {
			byte[] ret = null;
			String action = null;
			if (!map.has("action")) {
				ret = UNSUPPORTEDACTION.getBytes();
				DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, OK,
						Unpooled.wrappedBuffer(ret));
				response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
				ChannelFuture f = ctx.write(response);
				f.addListener(ChannelFutureListener.CLOSE);
				return;
			} else
				action = map.get("action").getAsString();
			if (action != null) {
				HttpResultCallback cb;
				if (map.has("callback")) {
					cb = new HttpResultCallback(ctx, map.get("callback").getAsString());
					cb.addHeader("Content-Type", "application/json");
				} else {
					cb = new HttpResultCallback(ctx, null);
					cb.addHeader("Content-Type", "application/json");
				}
				if (map.get("action").getAsString().equals("downloadUUID"))
					cb.addHeader("content-disposition", "attachment;filename=encodeduuid.key");

				actionExecutor.handle(action, map, cb);
			}
		} catch (IllegalArgumentException e) {
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, OK,
					Unpooled.wrappedBuffer(e.getMessage().getBytes()));
			ChannelFuture f = ctx.write(response);
			f.addListener(ChannelFutureListener.CLOSE);
		} catch (Exception e) {
			Map<String, String> ret = new HashMap<String, String>();
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(bo));
			ret.put("msg", bo.toString());
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, OK,
					Unpooled.wrappedBuffer(new Gson().toJson(ret).getBytes()));
			ChannelFuture f = ctx.write(response);
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
