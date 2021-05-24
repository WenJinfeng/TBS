package cn.edu.pku.backend;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ProxyServer {
	private final int port;
	final String PATH = "/";

	private ProxyServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws Exception {
		start(6161);

	}

	public static void start(int port) throws Exception {
		System.out.println("[ProxyServer] start at:" + port);
		new ProxyServer(port).start();
	}

	private void start() throws Exception {
		// EpollEventLoopGroup
		// EpollServerSocketChannel
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {

			final ProxyHandler serverHandler = new ProxyHandler();
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).localAddress(port)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel arg0) throws Exception {
							arg0.pipeline().addLast(new HttpServerCodec()).addLast(new HttpObjectAggregator(65536))
									.addLast(new ChunkedWriteHandler()).addLast(serverHandler);
						}
					});
			Channel ch = b.bind(port).sync().channel();
			ch.closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
