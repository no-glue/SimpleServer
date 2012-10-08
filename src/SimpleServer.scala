/**
 * Package   : 
 * Created By: matt
 * Date      : 9/16/12
 * Copyright : Matt Taylor 2012
 */

import java.util.concurrent.TimeUnit
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

class SimpleServer( port: Int ) {
    Global.logger ! LogInfo( "Starting" )
    var as = new AcceptHandler( port )
    as.group.awaitTermination( Long.MaxValue, TimeUnit.SECONDS )

}
