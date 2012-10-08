/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import actors.Actor
import collection.mutable.Map
import java.nio.channels.AsynchronousSocketChannel


class HttpRequest ( socket: AsynchronousSocketChannel, s: String = "" ) {
    val sock = socket

    var method : String = ""
    var uri : String = ""
    var qs : String = "" // TODO: parse into map
    var ver : String = ""
    var headers : collection.mutable.Map[ String, String ] = Map()
    var query : collection.mutable.Map[ String, String ] = Map()
    var body : String = ""
    var clen = 0L

    var resp = new HttpResponse( sock )

    var ractor = new ReadActor( HttpRequest.this )
    var pactor = new ParseActor( HttpRequest.this )
    var hactor = new HandleActor( HttpRequest.this )
    var reactor = new ResponseActor( HttpRequest.this, resp )
    ractor.start() ; pactor.start() ; hactor.start() ; reactor.start()



    def isDone = headers_done && clen == body.length

    def broadcast( msg: ActorMessages ) {
        for ( x : Actor <- Array( ractor, pactor, hactor, reactor ) ) {
             x ! msg
        }
    }

    private var buf = ""
    private var curr = 0
    private var end = 0
    private var headers_done = false

    add( s )

    def add( s: String ) {
        buf += s
    }

    def parseHeaders() = {
        var continue = true

        if ( !headers_done ) {

            if ( method.length == 0 ) {
                end = buf.indexOf( HttpRequest.word_sep, curr )
                if ( end > 0 ) {
                    method = buf.substring( curr, end )
                    curr = end + HttpRequest.word_sep.length
                }
                else {
                    continue = false
                }
            }

            if ( continue && uri.length == 0 ) {
                end = buf.indexOf( HttpRequest.word_sep, curr )
                if ( end > curr ) {
                    // TODO: Handle query string
                    uri = buf.substring( curr, end )
                    val qmark = uri.indexOf( "?", 0 )
                    if ( qmark > 0 && qmark < uri.length ) {
                        Global.logger ! LogInfo( " method : query " )
                        qs = uri.substring( qmark+1, uri.length-qmark-1 )
                        uri = uri.substring( 0, qmark )
                    }

                    curr = end + HttpRequest.word_sep.length
                }
                else {
                    continue = false
                }
            }

            if( continue && ver.length == 0 ) {
                end = buf.indexOf( HttpRequest.entity_sep, curr )
                if ( end > curr ) {
                    ver = buf.substring( curr, end )
                    curr = end + HttpRequest.entity_sep.length
                }
                else {
                    continue = false
                }
            }

            if ( continue && headers.size == 0 ) {
                end = buf.indexOf( HttpRequest.block_sep, curr )
                if ( end > curr ) {
                    for( x <- buf.substring( curr, end ).split( HttpRequest.entity_sep ) ) {
                        val kv : Array[String] = x.split( ":" )
                        // TODO: Append duplicate headers to existing key
                        //if ( kv.contains( kv(0) ) ) {}
                        headers += kv(0) -> { if ( kv.length > 1 ) kv(1) else "" }
                    }
                    // set the content length
                    if( headers.contains( "Content-Length" ) ) {
                        clen = headers( "Content-Length" ).toLong
                        Global.logger ! LogInfo( "Content-Length: " + clen)
                    }

                    headers_done = true
                    curr = end + HttpRequest.block_sep.length
                }
                else {
                    continue = false
                }
            }

        }

        headers_done && continue
    }

    def parseBody() {
        if ( body.length == 0 &&
             buf.length-curr-HttpRequest.block_sep.length == clen ) {
            val end = buf.indexOf( HttpRequest.block_sep, curr )
            body = buf.substring( curr, end-HttpRequest.block_sep.length )
        }
     }

    def parse() {
        if ( parseHeaders() )
            parseBody()

        Global.logger ! LogInfo( "Method: " + method + " URI: " + uri + " Ver: " + ver )
    }

}

object HttpRequest {
    val word_sep = " "
    val block_sep = "\r\n\r\n"
    val entity_sep = "\r\n"
    val header_kv_sep = ":"

    val qs_kvpair_sep = "&"
    val qs_kv_sep = "="
}
