package cn.edu.pku.backend;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;

public class HttpFileHandleAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {
	private String location;

	public HttpFileHandleAdapter(String path) {
		location = path;
	}
HttpFileHandleAdapter
	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_0, status,
				Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private String sanitizeUri(String uri) {
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				throw new Error();
			}
		}

		if (!uri.startsWith("/")) {
			return null;
		}
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".")
				|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
			return null;
		}
		return System.getProperty("user.dir") + File.separator + uri;
	}

	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (request.uri().equals("/favicon.ico")) {
			request.setUri("/favicon.ico");
		}
		String uri = request.uri();
		uri = uri.replaceFirst("/SCIDE", "");
		uri = uri.replaceFirst("\\?.*$", "");

		String path = location + uri;
		System.out.println("[HttpFileHandleAdapter] path:" + path);
		File html = new File(path);

		if (HttpUtil.is100ContinueExpected(request)) {

			send100Continue(ctx);
		}

		if (!html.exists()) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		RandomAccessFile file = new RandomAccessFile(html, "r");
		HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
		response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
		if (path.endsWith(".html")) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		} else if (path.endsWith(".js")) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
		} else if (path.endsWith(".css")) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
		} else if (path.endsWith(".ico")) {
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/x-icon");
		}

		boolean keepAlive = HttpUtil.isKeepAlive(request);
		HttpUtil.setContentLength(response, file.length());
		if (keepAlive) {
			HttpUtil.setKeepAlive(response, true);
		}
		ctx.write(response);
		ctx.write(new ChunkedFile(file, 0, file.length(), 512 * 1024), ctx.newProgressivePromise());

		ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
		file.close();
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.CONTINUE);
		response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
		ctx.writeAndFlush(response);
	}
}