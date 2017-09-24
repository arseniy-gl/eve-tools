package info.golushkov.eve.tool.akka.utils

import java.time.{LocalDate, LocalDateTime}
import java.util.Date

import scala.language.implicitConversions

object DateConverter {
  implicit def date2DateTime(d: Date): D2LDT = new D2LDT(d)
  implicit def date2Date(d: Date): D2LD = new D2LD(d)
  implicit def dateTime2Date(d: LocalDateTime): LDT2D = new LDT2D(d)
  implicit def date2date(d: LocalDate): LD2D = new LD2D(d)

  class LD2D(d: LocalDate) {
    def toDate: Date = DateUtils.asDate(d)
  }

  class LDT2D(d: LocalDateTime) {
    def toDate: Date = DateUtils.asDate(d)
  }

  class D2LD(d: Date) {
    def toLocalDate: LocalDate = DateUtils.asLocalDate(d)
  }

  class D2LDT(d: Date) {
    def toLocalDateTime: LocalDateTime = DateUtils.asLocalDateTime(d)
  } 
}


import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object DateUtils {
  def asDate(localDate: LocalDate): Date = Date.from(localDate.atStartOfDay.atZone(ZoneId.systemDefault).toInstant)

  def asDate(localDateTime: LocalDateTime): Date = Date.from(localDateTime.atZone(ZoneId.systemDefault).toInstant)

  def asLocalDate(date: Date): LocalDate = Instant.ofEpochMilli(date.getTime).atZone(ZoneId.systemDefault).toLocalDate

  def asLocalDateTime(date: Date): LocalDateTime = Instant.ofEpochMilli(date.getTime).atZone(ZoneId.systemDefault).toLocalDateTime
}