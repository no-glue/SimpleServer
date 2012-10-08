/**
 * Package   : 
 * Created By: matt
 * Date      : 9/17/12
 * Copyright : Matt Taylor 2012
 */

import java.util.logging.Level

sealed abstract class ActorMessages

case object ReadReady()   extends  ActorMessages
case object WriteReady()  extends  ActorMessages
case object ParseData()   extends  ActorMessages
case object Handle()      extends  ActorMessages
case object Disconnect()  extends  ActorMessages
case object Done()        extends  ActorMessages
case object Error()       extends  ActorMessages

case class LogInfo( s: String )   extends ActorMessages
case class LogWarn( s: String )   extends ActorMessages
case class LogSevere( s: String ) extends ActorMessages
case class LogFine( s: String )   extends ActorMessages
case class LogMsg( level: Level, s: String ) extends ActorMessages
case object LogStop()     extends  ActorMessages
