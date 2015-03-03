package svgrenderer

import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

class WebServer extends HttpServlet {
  
  override def init() {
    /* maybe do stuff in the future? */
  }
  
  override def doGet(request: HttpServletRequest, response: HttpServletResponse) {
    response.getOutputStream.print("hello world")
  }
}