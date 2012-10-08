/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import actors._

class ParseActor( req: HttpRequest ) extends Actor {

    def act() {
        loop {
            react {
                case ParseData =>
                    Global.logger ! LogInfo( "Parsing data" )
                    req.parse()
                    if( !req.isDone ) {
                        Global.logger ! LogInfo( "Need to read more" )
                        req.ractor ! ReadReady
                    }
                    else {
                        Global.logger ! LogInfo( "Ready to handle request" )
                        req.hactor ! Handle()
                    }
                case Done() =>
                    exit()
                case Disconnect() =>
                    exit()
                case Error() =>
                    exit()

            }
        }

    }
}