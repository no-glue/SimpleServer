/**
 * Package   : 
 * Created By: matt
 * Date      : 9/16/12
 * Copyright : Matt Taylor 2012
 */
object SimpleWeb {
    def main( args: Array[String] ) {
        var log = new DailyServerLog( "/tmp", "sw" )
        log.setLevel( java.util.logging.Level.ALL )
        log.fine( "Started Up" )

        var port = if ( args.length > 0 ) args(0).toInt else 8181

        val server = new SimpleServer( port );


    }
}
