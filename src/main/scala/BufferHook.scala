package hooks

import Imports._

case class Buffer[T, P <: BufferProfile[T, P]](parts: Vector[Either[T, Buffer[T, P]]], profile: P) {
  def this(profile: P) = this(Vector.empty, profile)
  def +(part: T): Buffer[T, P] = Buffer(parts :+ Left(part), profile)
  def +(nest: Buffer[T, P]): Buffer[T, P] = Buffer(parts :+ Right(nest), profile)
  def apply(): T = profile.collate(this)
}

trait BufferProfile[T, P <: BufferProfile[T, P]] {
  def collate(items: Buffer[T, P]): T
}

case class StringProfile (
    start: Option[String], end: Option[String], delim: Option[String],
    before: Option[String], after: Option[String],
    wrap: Option[String => String], conv: Option[String => String]
  ) extends BufferProfile[String, StringProfile] {
  
  def collate(buffer: Buffer[String, StringProfile]): String = {
    val into = new StringBuilder
    _write(buffer, into)
    into.toString
  }

  def _opt(value: Option[String], into: StringBuilder) {
    value match {
      case Some(v) => into.append(v)
      case _ =>
    }
  }

  def _write(buffer: Buffer[String, StringProfile], into: StringBuilder) {
    _opt(start, into)
    
    var first = true
    for (part <- buffer.parts) {
      if (!first) _opt(delim, into)
      part match {
        case Left(v) => into.append(v)
        case Right(buf2) =>
          //_opt(buf2.profile.before, into)
          buf2.profile._write(buf2, into)
          //_opt(buf2.profile.after, into)
      }
      first = false
    }
    
    _opt(end, into)
  }
}

/*
case class BufferProfile[T](
    start: Option[T], end: Option[T], delim: Option[T],
    before: Option[T], after: Option[T],
    wrap: Option[T => T], conv: Option[T => T]
  )(implicit collate: Buffer[T] => T)

implicit val stringCollate(buffer: Buffer[String]): String = {
  import java.util.StringBuilder
  val into = new StringBuilder
  
  def write(buffer: Buffer[String]) = {
    val profile = buffer.profile
    for (start <- profile.start) buffer.append(start)
    for (part <- buffer.parts) {
      if (conv == null) buffer.append(part)
      else buffer.append(conv(part))
    }
    for (end <- profile.end) buffer.append(end)
  }
}
*/

/*
implicit val xmlCollate(buffer: Buffer[Node]): String = {
  
}
*/

/*
class BufferHook[T](profile: BufferProfile[T]) extends Hook[=> T] {
  
  def add(f: => T): Unit = _register(f)
  def add(b: BufferHook[T]): Unit = _register(b.apply)
  
  def pieces = _get
  def apply() = {
    
  }
}

class StandaloneBufferHook[T](base: BufferHook[T]) extends StandaloneHook(base) {
  //val earlyFilters = new StandaloneFilterHook(base.earlyFilters)
  //val lateFilters = new StandaloneFilterHook(base.lateFilters)

  def add(f: => T) = standalone { base.add(f) }
  def add(nested: BufferHook[_]) = standalone { base.add(nested) }
  def fragments = standalone { base.fragments }
  def apply = standalone { base.apply }  
}
*/
