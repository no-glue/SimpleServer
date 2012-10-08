/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import java.nio.channels.{CompletionHandler, AsynchronousSocketChannel}
import java.nio.charset.Charset
import java.nio.{ByteBuffer, CharBuffer}
import java.util.Arrays
import actors._

// TODO: separate decode actor
class ReadHandler( req: HttpRequest ) extends CompletionHandler[ java.lang.Integer, ByteBuffer] {

    val decoder = Charset.forName("US-ASCII").newDecoder()

    def completed( sz: java.lang.Integer, buf: ByteBuffer ) {
        buf.flip()
        var content = decoder.decode( buf ).toString
        if ( sz > 0 ) {
            req.add( content )
            req.pactor ! ParseData
        }
        else {
            Global.logger ! LogWarn( "Write size is 0 " )
            req.ractor ! Disconnect
        }
        decoder.reset()
    }

    def failed( exc: Throwable, buf: ByteBuffer ) {
        Global.logger ! LogSevere( "Read failed: " + exc.getMessage )
        req.broadcast( Error() )
    }
}

class ReadActor( req: HttpRequest ) extends actors.Actor {
    def act() {
        loop {
            react {
                case ReadReady() =>
                    val buf = ByteBuffer.allocate( 1024 )
                    req.sock.read( buf, buf, new ReadHandler( req ) )
                case Done() =>
                    exit()
                case Error() =>
                    exit()
                case Disconnect() =>
                    exit()

                case _ =>
                    Global.logger ! LogSevere( "Default read action" )
            }
        }
    }
}
