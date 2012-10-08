/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import actors._
import java.nio.{CharBuffer, ByteBuffer}
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset

class WriteHandler( req: HttpRequest, resp: HttpResponse ) extends CompletionHandler[ java.lang.Integer, ByteBuffer] {

    def completed( sz: java.lang.Integer, buf: ByteBuffer ) {
        if ( sz != resp.toString.length ) {
            Global.logger ! LogWarn( " Write does not match expected size " + sz + " bytes" )
            req.broadcast( Error() )
        }
        else {
            Global.logger ! LogInfo( " Write completed " + sz + " bytes")
            req.broadcast( Done() )
        }
    }

    def failed( exc: Throwable, buf: ByteBuffer ){
        Global.logger ! LogWarn( "Write failed " + exc.getMessage )
        req.broadcast( Error() )
    }
}

class ResponseActor( req: HttpRequest, resp: HttpResponse ) extends Actor {
    val encoder = Charset.forName("US-ASCII").newEncoder()

    def act() {
        loop {
            react {
                case WriteReady() =>
                    Global.logger ! LogInfo( "Ready for write " + resp.toString().length + " bytes " )
                    val buf = encoder.encode( CharBuffer.wrap( resp.toString() ) )

                    resp.sock.write( buf, buf, new WriteHandler( req, resp ) )
                    encoder.reset()
                case Done() =>
                    exit()
                case Error() =>
                    exit()
                case Disconnect() =>
                    exit()
                case _ =>
                    Global.logger ! LogSevere( "Default response" )
            }
        }
    }
}
