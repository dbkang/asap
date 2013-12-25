package asapstack

import java.net.{URL, URLDecoder}
import java.util.jar.JarFile
import scala.collection.JavaConversions.enumerationAsScalaIterator
import scala.io.Source


object Resources {
  private def getJarEntries(path: String) = {
    val root = getClass.getResource(path)
    val jarPath = root.getPath.substring(5, root.getPath.indexOf("!"))
    val jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
    val entries = jar.entries
    entries.toSeq.map(e => e.getName)
  }

  def load(path: String) = loadBuffer(path).map(x => x.mkString)

  def loadBuffer(path: String) = {
    val res = getClass.getResource(path)
    Option(res).map(r => Source.fromURL(r))
  }

  def loadLines(path: String) = loadBuffer(path).map(x => x.getLines.toSeq)

}
