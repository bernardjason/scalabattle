package org.bjason.scalabattle

import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, GL20, Texture}
import org.bjason.scalabattle.game.GameScreen

class EndScreen extends LoopTrait {
  private[scalabattle] var batch:SpriteBatch = null
  lazy val ascii = DrawAscii()

  lazy val score = new Texture(ascii.getPixmapForString("SCORE",1624,350,Color.YELLOW))
  lazy val value = new Texture(ascii.getPixmapForString(""+GameScreen.score,1624,350,Color.YELLOW))
  var delayMoveOn = 0


  override def create(): Unit = {
    batch = new SpriteBatch()
  }

  override def firstScreenSetup(): Unit = {
    super.firstScreenSetup()
    delayMoveOn = 60
  }



  override def render(): Unit = {
    delayMoveOn = delayMoveOn -1
    if ( delayMoveOn < 0 &&  Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      this.screenComplete = true
    }
    if ( Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      System.exit(0)
    }
    Gdx.gl.glClearColor(0.5f, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.begin()
    batch.draw(score, 20, 350)
    batch.draw(value, 20, 0)
    batch.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
  }
}


