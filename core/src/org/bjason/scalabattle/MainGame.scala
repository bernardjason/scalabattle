package org.bjason.scalabattle

import com.badlogic.gdx.{Gdx, Input}
import org.bjason.scalabattle.game.GameScreen

class MainGame() extends LoopTrait {

  val screens = List(new StartScreen(),new GameScreen(),new EndScreen() )
  var screenIndex = 0

  currentScreen = screens(screenIndex)

  def nextScreen(): Unit = {
    screenIndex = screenIndex +1
    if ( screenIndex >= screens.length ) {
      screenIndex = 0
    }
    currentScreen = screens(screenIndex)
    currentScreen.screenComplete = false
    currentScreen.firstScreenSetup()
  }

  override def render(): Unit = {
    super.render()
    //moveToNextScreenBodge
    if ( currentScreen.screenComplete ) {
      nextScreen()
    }
    for (s <- screens) {
      if (!s.createCalled) {
        s.create()
        s.createCalled = true
      }
    }
  }

  var notTooQuick=0
  private def moveToNextScreenBodge = {
    notTooQuick = notTooQuick + 1
    if (notTooQuick > 0 && Gdx.input.isKeyPressed(Input.Keys.X)) {
      nextScreen()
      notTooQuick = -30
    }

  }
}
