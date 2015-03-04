package svgrenderer

import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }
import javax.servlet.annotation.MultipartConfig
import scala.util.matching.Regex
import org.apache.batik.script.rhino.BatikSecurityController
import java.io.ByteArrayOutputStream
import java.io.DataInputStream

@MultipartConfig(location="/tmp",
    fileSizeThreshold=20*1024*1024, // 20 MB - The size threshold after which the file will be written to disk
    maxFileSize=10*1024*1024, // 10 MB - The maximum size allowed for uploaded files
    maxRequestSize=20*1024*1024) // 20 MB - The maximum size allowed for multipart/form-data requests
class WebServer extends HttpServlet {
  
  private val companion = WebServer
  
  /**
   * Handle every request coming into this server. This server
   * will take in SVG and return a rasterized (rendered to
   * a bitmap-style image) version of it.
   * 
   * This code delegates to Apache Batik under the hood
   * 
   * This code was ported from Highcharts' PHP script that does
   * the same:
   * https://github.com/highslide-software/highcharts.com/blob/master/exporting-server/php/php-batik/index.php 
   */
  override def doPost(request: HttpServletRequest, response: HttpServletResponse) {
    
    val requestedMimeType = request.getParameter("type")
    
    // turning an uploaded file into a string is harder than it should be...
    val svgPart = request.getPart("svg")
    val numBytes: Int = svgPart.getSize.toInt
    val svgBytes = new Array[Byte](numBytes)
    (new DataInputStream(svgPart.getInputStream)).readFully(svgBytes)
    val svg: String = new String(svgBytes)
    
    val filename = request.getParameter("filename") match {
      case companion.FILENAME_REGEX(fname) => fname
      case _ => "chart"
    }
    val width: Option[Int] = try { Some(request.getParameter("width").toInt) } catch { case _: Throwable => None }
    
    // check for malicious attack in SVG
    if(svg.indexOfSlice("<!ENTITY") > -1 || svg.indexOfSlice("<!DOCTYPE") > -1) {
      throw new Exception("Exception is stopped, the posted SVG could contain code for a malicious attack")
    }
    
    val rasterizedImage: Array[Byte] = Rasterizer.rasterize(svg, requestedMimeType, width)
    
    response.setContentType(requestedMimeType)
    response.getOutputStream.write(rasterizedImage)
  }
}

object WebServer {
  private val FILENAME_REGEX: Regex = """(^[A-Za-z0-9\-_ ]+$)""".r
}