import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Created by tseyunghuang on 2017/5/17.
 */
public class SimpleHttpServer {
    // Initializer will be passed in to the EventLoop which will then
    // initialize channel and server's handler to handle requests
    static class Initializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
            pipeline.addLast("handler", new Handler());
        }
    }
    public static void main(String[] args) throws Exception {
        final SslContext sslCtx=null;
        EventLoopGroup parent = new NioEventLoopGroup();
        EventLoopGroup child = new NioEventLoopGroup();

        Integer port = 8000;
        try {
            ServerBootstrap starter = new ServerBootstrap();
            starter.group(parent, child)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpStaticFileServerInitializer(sslCtx));

            starter.bind(port).sync().channel().closeFuture().sync();
            Channel ch = starter.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {

            parent.shutdownGracefully();
            child.shutdownGracefully();
        }
    }
}
