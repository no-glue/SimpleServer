import java.nio.channels.AsynchronousSocketChannel

/**
 * Package   : 
 * Created By: matt
 * Date      : 9/18/12
 * Copyright : Matt Taylor 2012
 */

import collection.mutable.Map

class HttpResponse(  socket: AsynchronousSocketChannel, s: String = "" ) {
    val sock = socket
    var ver = "HTTP/1.0"
    var status = "200 OK"
    var headers : collection.mutable.Map[ String, String ] = Map()
    var body = ""

    def addHeader( k: String, v: String ) {
        headers += k -> v
    }

    override def toString = {
        var rval =  ver + HttpResponse.word_sep + status

        headers += "Content-Length" -> body.length.toString
        for( ( k,v ) <- headers ) {
            rval += HttpResponse.entity_sep + k + HttpResponse.header_kv_sep + v
        }

        rval + HttpResponse.block_sep + body
    }
}

object HttpResponse {
    val word_sep = " "
    val block_sep = "\r\n\r\n"
    val entity_sep = "\r\n"
    val header_kv_sep = ":"
}
