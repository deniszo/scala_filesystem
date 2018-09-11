package org.rtjvm.scala.oop.commands

import org.rtjvm.scala.oop.filesystem.State

trait Command {

  def apply(state: State): State
}

object Command {

  def from(input: String): Command =
    new UnknownCommand
}