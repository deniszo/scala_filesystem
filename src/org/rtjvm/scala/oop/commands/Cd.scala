package org.rtjvm.scala.oop.commands
import org.rtjvm.scala.oop.files.{DirEntry, Directory}
import org.rtjvm.scala.oop.filesystem.State

class Cd(dir: String) extends Command {
  override def apply(state: State): State = {
    /*
      absolute path: cd /something/somethingElse/../
      relative path: cd a/b/c
     */

    // 1. find root
    val root = state.root
    val wd = state.wd
    // 2. find the absolute path of the directory I want to cd to
    val absolutePath =
      if (dir.startsWith(Directory.SEPARATOR)) dir
      else if (wd.isRoot) wd.path + dir
      else wd.path + Directory.SEPARATOR + dir
    // 3. find the directory to cd to, to given path
    val destination = doFindEntry(root, absolutePath)
    // 4. change the state given new directory
    if (destination == null || !destination.isDirectory) state.setMessage(dir + ": no such directory")
    else State(root, destination.asDirectory)
  }

  def doFindEntry(root: Directory, path: String): DirEntry = {

    def findEntryHelper(currentDirectory: Directory, path: List[String]): DirEntry = {
      if (path.isEmpty || path.head.isEmpty) currentDirectory
      else if (path.tail.isEmpty) currentDirectory.findEntry(path.head)
      else {
        val nextDir = currentDirectory.findEntry(path.head)
        if (nextDir == null || !nextDir.isDirectory) null
        else findEntryHelper(nextDir.asDirectory, path.tail)
      }
    }

    def collapseRelativeTokens(path: List[String], result: List[String]): List[String] = {
      if (path.isEmpty) result
      else if (".".equals(path.head)) collapseRelativeTokens(path.tail, result)
      else if ("..".equals(path.head)) {
        if (result.isEmpty) null
        else collapseRelativeTokens(path.tail, result.init)
      } else collapseRelativeTokens(path.tail, result :+ path.head)
    }

    // 1. get tokens
    val tokens: List[String] = path.substring(1).split(Directory.SEPARATOR).toList
    // 1.5 eliminate/collapse relative tokens
    /*

      /a/. => ["a", "."] => ["a"]
      /a/b/./. => ["a", "b", ".", "."] => ["a", "b"]

      /a/../ => ["a", ".."] => []
      /a/b/.. => ["a". "b", ".."] => ["a"]

     */

    val collapsedTokens = collapseRelativeTokens(tokens, List())

    // 2. navigate to the correct entry
    if (collapsedTokens == null) null
    else findEntryHelper(root, collapsedTokens)
  }
}
