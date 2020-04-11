package org.bjason.scalabattle.game

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}

object AlienExplode{
  val texures = for( c <- List(Color.RED,Color.GREEN,Color.ORANGE,Color.GREEN) ) yield {
    val pixmap = new Pixmap(4,4,Pixmap.Format.RGB888)
    pixmap.setColor(c)
    pixmap.fill()
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }
  var currentTexure = 0
  def nextTexure = {
    currentTexure=currentTexure+1
    if ( currentTexure >= texures.length) currentTexure = 0
    texures(currentTexure)
  }

  case class Explode(startx:Int,starty:Int) extends Actor {
    var ttl = 60f
    setPosition(startx,starty)
    val texture = nextTexure

    def plusMinus = {
      val decide = (Math.random() * 256).toInt % 2
      if ( decide == 0 ) -1 else 1
    }

    val dir = new Vector2(Math.random().toFloat / 2, Math.random().toFloat / 2)
    var dirX = plusMinus
    var dirY = plusMinus

    dir.scl(dirX * 8, dirY * 8)

    override def draw(batch: Batch, parentAlpha: Float): Unit = {
      batch.draw(texture,getX,getY)
      ttl = ttl - parentAlpha
      if ( ttl <= 0 ) {
        getParent.removeActor(this)
      }
      setX(getX + dir.x)
      setY(getY + dir.y)
    }
  }

  def makeExplosion(startx:Int,starty:Int,stage:Stage) {
    for (xx <- startx - 20 to startx + 20 by 4) {
      for (yy <- starty - 20 to starty + 20 by 4) {
        val e = Explode(xx, yy)
        stage.addActor(e)
      }
    }
  }
}
