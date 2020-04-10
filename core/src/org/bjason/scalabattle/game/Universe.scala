package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}

import scala.collection.mutable.ArrayBuffer

object Universe {
  val size = 512
  lazy val cachedTexture = makeTexture(size, size)
  val universeSize = 4096

  def makeTexture(w: Int, h: Int): Texture = {

    val pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888)
    pixmap.setColor(Color.LIGHT_GRAY)

    for (xx <- 0 to w by w / 20) {
      for (yy <- 0 to h by h / 20) {
        if (Math.random() * 1000 % 20 > 18) {
          pixmap.fillRectangle(xx, yy, 2, 2)
        }
      }
    }

    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }

  val starList = ArrayBuffer[StarTexture]()
  var currentPlayer: Player = null

  def addStars(stage: Stage, p: Player): Unit = {
    starList.clear()
    currentPlayer = p
    for (xx <- -universeSize to universeSize by size) {
      for (yy <- -universeSize to universeSize by size) {
        val star = StarTexture(xx, yy)
        stage.addActor(star)
      }
    }
  }

  def cleanUp(): Unit = {

  }

  case class StarTexture(xx: Int, yy: Int) extends Actor {
    setX(xx)
    setY(yy)

    override def draw(batch: Batch, parentAlpha: Float): Unit = {
      val playerPosition = new Vector2(currentPlayer.getX, currentPlayer.getY)
      if (playerPosition.dst(xx, yy) < Gdx.graphics.getWidth ) {
        batch.draw(cachedTexture, getX, getY)
      }
    }
  }

}
