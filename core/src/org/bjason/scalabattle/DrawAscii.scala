package org.bjason.scalabattle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.{Color, GL20, Pixmap, Texture}

case class DrawAscii(normalSize:Int=32) {
  lazy val titleFont = fancyFont(48,-5)
  lazy val normalFont = fancyFont(normalSize)
  lazy val spriteBatch = new SpriteBatch
  lazy val fontHeight = (titleFont.getCapHeight * 2).toInt
  lazy val fontWidth = (titleFont.getSpaceWidth * 6).toInt
  val bordery =1
  val borderx = 2

  def getPixmapForString(text: String, width: Int, height: Int,c:Color): Pixmap = {

    val scale = 5
    val scalex = 12
    val workingHeight=1024

    val frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, width, workingHeight, false)
    frameBuffer.begin()

    Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    spriteBatch.begin()
    titleFont.draw(spriteBatch, text, 0, 0)
    spriteBatch.end()

    val fontPixmap = new Pixmap(width, workingHeight, Pixmap.Format.RGB888)
    Gdx.gl.glReadPixels(0, 0, width, workingHeight, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, fontPixmap.getPixels)
    frameBuffer.end()
    frameBuffer.dispose()

    val pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888)

    for (x <- 0 to fontWidth * text.length) {
      for (y <- 0 to height ) {
        val pixel = fontPixmap.getPixel(x, y)
        if (pixel !=255) {
          pixmap.setColor(c)
          pixmap.drawRectangle(borderx + x * scalex,bordery + y *scale, scalex/2,scale/2)
        }
      }
    }
    fontPixmap.dispose()
    pixmap
  }

  def getRegularTextAsTexture(words:String,width:Int,height:Int,colour:Color): Texture = {

    val frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, width, height, false)
    frameBuffer.begin()

    Gdx.gl.glClearColor(colour.r, colour.g, colour.b, 0f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    spriteBatch.begin()
    normalFont.draw(spriteBatch, words, 20, 0)
    spriteBatch.end()

    val pixmap = new Pixmap(width, height, Pixmap.Format.RGB888)
    Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixmap.getPixels)
    frameBuffer.end()
    frameBuffer.dispose()

    val t = new Texture(pixmap)
    t
  }

  def fancyFont(size:Int , lessX:Int = 0): BitmapFont = {

    val fontFile = Gdx.files.internal("data/OpenSans-Italic.ttf")
    val generator = new FreeTypeFontGenerator(fontFile)
    val parameter = new FreeTypeFontParameter()
    parameter.flip = true
    parameter.color = Color.WHITE
    parameter.size = size
    parameter.spaceX = lessX

    val font = generator.generateFont(parameter)
    generator.dispose()
    font
  }

}

