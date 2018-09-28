package org.rtjvm.scala.oop.commands
import org.rtjvm.scala.oop.filesystem.State

class Cat(fileName: String) extends Command {
  override def apply(state: State): State = {
    val wd = state.wd
    val dirEnty = wd.findEntry(fileName)

    if (dirEnty == null || !dirEnty.isFile)
      state.setMessage(fileName + ": no such file")
    else
      state.setMessage(dirEnty.asFile.contents)
  }
}
