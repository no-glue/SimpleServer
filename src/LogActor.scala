/**
 * Package   : 
 * Created By: matt
 * Date      : 9/18/12
 * Copyright : Matt Taylor 2012
 */

import actors._
import java.util.logging.Level

class LogActor( file_path: String, filename_prefix: String ) extends Actor {
    val log = new DailyServerLog( file_path, filename_prefix )

    def act() {
        loop {
            react {
                case LogWarn( s: String ) =>
                    log.warn( s )
                case LogInfo( s: String ) =>
                    log.info( s )
                case LogSevere( s: String ) =>
                    log.severe( s )
                case LogFine( s: String ) =>
                    log.fine( s )
                case LogMsg ( level: Level, s: String ) =>
                    log.log( level, s )
                case LogStop() =>
                    exit()
            }
        }
    }

}

object Global {
    val logger = new LogActor( "/tmp", "server" )
    logger.start()

}
