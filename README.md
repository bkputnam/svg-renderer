# SVG Renderer

This project is a simple svg renderer. It accepts svg via post request and returns an image.
It is intended to mimic almost exactly the functionality of Highcharts' PHP-and-Batik
image rasterizer.

[Highcharts Export Server](http://www.highcharts.com/docs/export-module/setting-up-the-server)

[Highcharts PHP Source (github)](https://github.com/highslide-software/highcharts.com/blob/master/exporting-server/php/php-batik/index.php)

This server is configured to listen at http://[host]/highcharts-export-web

It expects a post request using `enctype="multipart/form-data"` with the following parameters:

<table>
	<tr>
		<td>svg</td>
		<td>The svg string (file) to be rasterized</td>
	</tr>
	<tr>
		<td>type</td>
		<td>The mime type of the returned file. Accepted values are: image/png, image/jpeg, application/pdf, image/svg+xml</td>
	</tr>
	<tr>
		<td>filename</td>
		<td>The name of the returned file (defaults to "chart.suffix")</td>
	</tr>
	<tr>
		<td>width</td>
		<td>The width of the returned image (optional, pulls default from svg)</td>
	</tr>
</table>