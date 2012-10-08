/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.CompletionHandler
import java.util.concurrent.Executors
import javax.naming.InsufficientResourcesException

class AcceptHandler( bind_port: Int ) extends CompletionHandler[ java.nio.channels.AsynchronousSocketChannel, Void ] {
    val port = bind_port
    val group = java.nio.channels.AsynchronousChannelGroup.withThreadPool( Executors.newSingleThreadExecutor() )

    val listener = java.nio.channels.AsynchronousServerSocketChannel.open( group )

    listener.setOption( java.net.StandardSocketOptions.SO_REUSEADDR, Boolean.box( true ) )
    listener.setOption( java.net.StandardSocketOptions.SO_RCVBUF, Int.box( 256 * 1024 ) )
    if ( listener.getOption( java.net.StandardSocketOptions.SO_RCVBUF ) < 256*1024 )
        throw new InsufficientResourcesException( "Could not get at least 256kb for recv buffer" )

    listener.bind( new InetSocketAddress( port ) )

    // start in accept
    listener.accept( null, AcceptHandler.this );

    override def completed( sock: AsynchronousSocketChannel, att: Void ) {
        Global.logger ! LogInfo( "accepted" )
        var req = new HttpRequest( sock )
        req.ractor ! ReadReady()

        listener.accept( att, AcceptHandler.this );
    }

    override def failed( exc: Throwable, att: Void ) {
        listener.accept( att, this )
    }

}
