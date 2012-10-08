/**
 * Package   : 
 * Created By: matt
 * Date      : 9/16/12
 * Copyright : Matt Taylor 2012
 */

import java.util.Calendar
import java.util.logging.Logger
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import java.util.logging.Level

/*
 * Log file that rolls every roll_every seconds
 */

class RollableServerLog(file_path: String, filename_prefix: String, roll_every: Long = 24 * 60 * 60) {
    val rollEvery = roll_every
    val path = file_path

    private var logger = Logger.getLogger("RollableServer")
    private val prefix = filename_prefix
    private var nextRollSec = 0L
    protected val cal = Calendar.getInstance()
    private val date_format = new java.text.SimpleDateFormat("'" + prefix + "'yyyyMMddHHmmss'.log'")


    def period(time: Long = cal.getTimeInMillis / 1000L): Long = {
        time - ( time % rollEvery )
    }

    def periodInMillis( time: Long = cal.getTimeInMillis / 1000L ) : Long = {
        period() * 1000L
    }

    def filename(per: Long = period()) = {
        path + "/" + date_format.format(per * 1000L)
    }

    private def open() = {
        val per = period()
        nextRollSec += rollEvery
        var handler = new FileHandler( filename(), true );
        handler.setFormatter( new SimpleFormatter() );
        logger.addHandler( handler );
        logger.fine( "New log" )
    }

    def rollIfNeeded() {
        val per = period()
        if ( per >= nextRollSec ) {
            close()
            open()
        }
    }

    private def close() {
        for ( h <- logger.getHandlers() ) {
            logger.removeHandler(h)
        }
    }

    def log( level: Level, s: String ) {
        rollIfNeeded()
        logger.log( level, s )
    }

    def setLevel(level: Level) {
        logger.setLevel(level)
    }

    def info(s: String) {
        log(Level.INFO, s)
    }

    def warn(s: String) {
        log(Level.WARNING, s)
    }

    def severe(s: String) {
        log(Level.SEVERE, s)
    }

    def fine(s: String) {
        log(Level.FINE, s)
    }
}

/*
 * Log file that rolls at the same time every day
 */

class DailyServerLog( file_path: String, filename_prefix: String, roll_at: Int = 0 )
    extends RollableServerLog( file_path, filename_prefix, 24*60*60 ) {

    private val p_rollat = roll_at
    def rollAt = p_rollat

    val per_offset = {
        val per = DailyServerLog.super.period()

        val c = java.util.Calendar.getInstance()
        c.setTimeInMillis( DailyServerLog.super.period() * 1000L )
        c.set( java.util.Calendar.HOUR_OF_DAY , 0 )
        c.set( java.util.Calendar.MINUTE, 0)
        c.set( java.util.Calendar.SECOND, 0)
        c.getTimeZone.getOffset( c.getTimeInMillis ) / 1000L
    }

    override def period( time: Long = cal.getTimeInMillis / 1000L ): Long = {
        time - ( time % rollEvery ) + rollAt - per_offset
    }

}
