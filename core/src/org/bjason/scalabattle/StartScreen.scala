package org.bjason.scalabattle

import com.badlogic.gdx.{Gdx, Input}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, GL20, Texture}

class StartScreen extends LoopTrait {
  private[scalabattle] var batch: SpriteBatch = null
  lazy val ascii = DrawAscii(normalSize = 64)
  lazy val bern = new Texture(ascii.getPixmapForString("BERNIE", 1624, 350, Color.YELLOW))
  lazy val soft = new Texture(ascii.getPixmapForString("SOFT", 1624, 350, Color.YELLOW))

  val backgroundColour = Color.BLACK //new Color(0,0,0.3f,1)

  lazy val instructions = ascii.getRegularTextAsTexture(
    "         arrow keys rotate, space fire, left shift thrust\n\n        radar shows location of asteroids\n\n        GOOD LUCK!!", 1024, 400,backgroundColour)
  lazy val keys = new Texture(Gdx.files.internal("data/keys.png"))
  var mainTitleShow = 0f
  var mainTitley = 0
  var instructionsy = 0f
  val SHOW_MAIN_TITLE = 0.5f //2
  var delayMoveOn = 0

  override def create(): Unit = {
    batch = new SpriteBatch()
    instructionsy = - instructions.getHeight
  }

  override def firstScreenSetup(): Unit = {
    super.firstScreenSetup()
    mainTitleShow = 0
    mainTitley = 0
    instructionsy =  - instructions.getHeight
    delayMoveOn = 60
  }

  override def render(): Unit = {
    delayMoveOn = delayMoveOn -1
    if ( delayMoveOn < 0 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      this.screenComplete = true
    }
    if ( Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      System.exit(0)
    }
    Gdx.gl.glClearColor(backgroundColour.r, backgroundColour.g, backgroundColour.b, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.begin()
    batch.draw(soft, 0, mainTitley)
    batch.draw(bern, 0, mainTitley + 350)
    batch.draw(keys, Gdx.graphics.getWidth/2 - instructions.getWidth/2 - keys.getWidth, instructionsy)
    batch.draw(instructions, Gdx.graphics.getWidth/2 - instructions.getWidth/2, instructionsy)
    batch.end()

    mainTitleShow = mainTitleShow + Gdx.graphics.getDeltaTime
    if (mainTitleShow > SHOW_MAIN_TITLE) {
      mainTitley = mainTitley + 1
      if (instructionsy <= 0) {
        instructionsy = instructionsy + 1
      }
    }
  }

  override def dispose(): Unit = {
    batch.dispose()
  }
}


