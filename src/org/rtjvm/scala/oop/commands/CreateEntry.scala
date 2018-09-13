package org.rtjvm.scala.oop.commands

import org.rtjvm.scala.oop.files.{DirEntry, Directory}
import org.rtjvm.scala.oop.filesystem.State

abstract class CreateEntry(name: String) extends Command {
  override def apply(state: State): State = {
    val wd = state.wd
    if (wd.hasEntry(name)) {
      state.setMessage("Entry " + name + " already exists1")
    } else if (name.contains(Directory.SEPARATOR)) {
      state.setMessage(name + "must not contain separators!")
    } else if (checkIllegal(name)) {
      state.setMessage(name + ": illegal entry name1")
    } else {
      doCreateEntry(state, name)
    }
  }

  def checkIllegal(name: String): Boolean =
    name.contains(".")

  def doCreateEntry(state: State, name: String): State = {
    def updateStructure(currentDirectory: Directory, path: List[String], newEntry: DirEntry): Directory = {
      if (path.isEmpty) currentDirectory.addEntry(newEntry)
      else {
        val oldEntry = currentDirectory.findEntry(path.head).asDirectory
        currentDirectory.replaceEntry(oldEntry.name, updateStructure(oldEntry, path.tail, newEntry))
      }
    }

    val wd = state.wd
    val fullPath = wd.path

    // 1. all the directories in the full path
    val allDirsInPath = wd.getAllFoldersInPath

    // 2. create new entry in the wd
    val newEntry: DirEntry = createSpecificEntry(state)

    // 3. update the whole directory structure starting from the root
    // (the directory structure is IMMUTABLE)
    val newRoot = updateStructure(state.root, allDirsInPath, newEntry)

    // 4. find new working directory INSTANCE given wd's full path, in the NEW directory structure
    val newWd = newRoot.findDescendant(allDirsInPath)

    State(newRoot, newWd)
  }

  def createSpecificEntry(state: State): DirEntry
}
