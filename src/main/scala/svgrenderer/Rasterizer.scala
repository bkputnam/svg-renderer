package svgrenderer

import java.io.OutputStream
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.apache.batik.transcoder.Transcoder
import org.apache.batik.gvt.renderer.ImageRenderer
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.RenderingHints
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.svg2svg.SVGTranscoder
import org.apache.fop.svg.PDFTranscoder
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import org.apache.batik.transcoder.TranscodingHints
import org.apache.batik.transcoder.SVGAbstractTranscoder
import java.io.InputStream

object Rasterizer {
  
  /**
   * Given a string containing SVG and a target mime type,
   * return a byte array representing the rasterized
   * (rendered to bitmap-like form) image
   */
  def rasterize(svgStr: String, mimeType: String, width: Option[Float]): RasterizerResult = {
    
    // Adapted from: http://stackoverflow.com/questions/11234455/poor-image-quality-when-using-svgconverter-in-batik-to-convert-svg-to-png
    
    val inputStream: InputStream = new ByteArrayInputStream(svgStr.getBytes(StandardCharsets.UTF_8))
    val transcoderInput: TranscoderInput = new TranscoderInput(inputStream)
    transcoderInput.setURI("http://example.com")
    val output: ByteArrayOutputStream = new ByteArrayOutputStream
    val transcoderOutput: TranscoderOutput = new TranscoderOutput(output)
    
    val (transcoder: Transcoder, extension: String) = mimeType match {
      case "image/png" => (getPngTranscoder, "png")
      case "image/jpeg" => (getJpegTranscoder, "jpg")
      case "application/pdf" => (getPdfTranscoder, "pdf")
      case "image/svg+xml" => (getSvgTranscoder, "svg")
      case _ => throw new Exception(s"Unknown mime type: '$mimeType'")
    }
    
    if (!width.isEmpty) {
      transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width.get)
    }
    
    try {
      transcoder.transcode(transcoderInput, transcoderOutput)
    } catch {
      case e: Exception => {
        if(output != null) {
          output.close()
        }
        throw e
      }
    }
    
    if (output != null) {
      output.flush()
      output.close()
    }
    
    return new RasterizerResult(output.toByteArray(), extension)
  }
  
  /**
   * Create a PNG Transcoder
   * 
   * Mostly just a place to configure a PNG Transcoder that's
   * away from the main flow of code
   */
  private def getPngTranscoder: Transcoder = {
    new CustomPngTranscoder
  }
  
  /**
   * Create a JPEG Transcoder
   * 
   * Mostly just a place to configure a JPEG Transcoder that's
   * away from the main flow of code
   */
  private def getJpegTranscoder: Transcoder = {
    // Copied from: http://stackoverflow.com/questions/11234455/poor-image-quality-when-using-svgconverter-in-batik-to-convert-svg-to-png
    val result = new JPEGTranscoder
    val jpegQuality: Float = 0.95f
    result.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, jpegQuality)
    return result
  }
  
  /**
   * Create a PDF Transcoder
   * 
   * Mostly just a place to configure a PDF Transcoder that's
   * away from the main flow of code
   */
  private def getPdfTranscoder: Transcoder = new PDFTranscoder
  
  /**
   * Create a SVG Transcoder
   * 
   * Mostly just a place to configure a SVG Transcoder that's
   * away from the main flow of code
   * 
   * Ps - I have no idea why you'd want to transcode SVG to SVG,
   * but it's an option in Highcharts' PHP rasterizer, so I
   * made sure it was also an option here.
   * http://www.highcharts.com/docs/export-module/setting-up-the-server
   * https://github.com/highslide-software/highcharts.com/blob/master/exporting-server/php/php-batik/index.php
   */
  private def getSvgTranscoder: Transcoder = new SVGTranscoder
  
  /**
   * Quick, private custom PNGTranscoder class, so that we can
   * override the createRenderer method.
   */
  private class CustomPngTranscoder extends PNGTranscoder {
    
    override def createRenderer: ImageRenderer = {
      
      // Copied from: http://stackoverflow.com/questions/11234455/poor-image-quality-when-using-svgconverter-in-batik-to-convert-svg-to-png
      val r: ImageRenderer = super.createRenderer()
      
      val rh: RenderingHints = r.getRenderingHints
      
      rh.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
      rh.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC));
      
      rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON));
      
      rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
        RenderingHints.VALUE_COLOR_RENDER_QUALITY));
      rh.add(new RenderingHints(RenderingHints.KEY_DITHERING,
        RenderingHints.VALUE_DITHER_DISABLE));
      
      rh.add(new RenderingHints(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY));
      
      rh.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,
        RenderingHints.VALUE_STROKE_PURE));
      
      rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON));
      rh.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
      
      r.setRenderingHints(rh);
      return r
    }
  }

}