/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import actors._
import java.nio.ByteBuffer
import java.nio.channels.CompletionHandler
import java.nio.channels.AsynchronousFileChannel

import java.nio.charset.Charset
import java.nio.file.{NoSuchFileException, Path, Paths}

class HandleHandler( req: HttpRequest ) extends CompletionHandler[ java.lang.Integer, ByteBuffer] {
    val decoder = Charset.forName("US-ASCII").newDecoder()

    def completed(  sz: java.lang.Integer, buf: ByteBuffer ) {
        buf.flip()
        var content = decoder.decode( buf ).toString
        Global.logger ! LogInfo( "Read file: " + req.uri )
        req.resp.body = content
        req.reactor ! WriteReady()
    }

    def failed( exc: Throwable, buf: ByteBuffer ) {
        Global.logger ! LogWarn( exc.getMessage() )
        req.broadcast( Error() )
    }
}

    class HandleActor( req: HttpRequest ) extends Actor {
    val root = "~/www"
    def act() {
        loop {
            react {
                case Handle() =>
                    val path = Paths.get( root + "/" + req.uri + { if ( req.uri.length == 0 || req.uri.last == '/' ) "index.html" else "" } )

                    try {
                        val fchan = AsynchronousFileChannel.open( path )
                        var bbuf = ByteBuffer.allocate( 1024 )
                        fchan.read( bbuf, 0, bbuf, new HandleHandler( req ) )
                    }
                    catch {
                        case e: NoSuchFileException =>
                            req.resp.status = "404 File Not Found"
                            req.resp.body = "<html><head><title>404 FNF</title></head><body><h1>We could not find '" + req.uri + "'</h1></body></html>\r\n"
                            req.reactor ! WriteReady()
                    }
                case Done() =>
                    exit()
                case Error() =>
                    exit()
                case Disconnect() =>
                    exit()

                case _ =>
                    Global.logger ! LogSevere( "Default handle action")

            }
        }
    }

}
