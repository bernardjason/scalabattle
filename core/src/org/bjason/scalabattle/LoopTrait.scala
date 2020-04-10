package org.bjason.scalabattle

import com.badlogic.gdx.ApplicationListener

trait LoopTrait extends ApplicationListener {

  protected var currentScreen: LoopTrait = null
  var createCalled = false;
  var screenComplete = false;

  /** every time screen is switched to call this
   *
   * for main screen this could reset score or baseline player to the start
   * not the same as create which is one in game lifetime event
   */
  def firstScreenSetup(): Unit = {

  }

  override def create(): Unit = {
    createCalled=true
    currentScreen.create()
  }

  override def render(): Unit = {
    currentScreen.render()
  }

  override def resume(): Unit = {
    if ( currentScreen != null) currentScreen.resume()
  }

  override def resize(width: Int, height: Int): Unit = {
    if ( currentScreen != null ) currentScreen.resize(width, height)
  }

  override def pause(): Unit = {
    if ( currentScreen != null ) currentScreen.pause()
  }

  override def dispose(): Unit = {
    if ( currentScreen != null ) currentScreen.dispose()
  }
}
