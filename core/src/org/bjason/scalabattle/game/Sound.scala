package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx

object Sound {

  lazy private val warn = Gdx.audio.newMusic(Gdx.files.internal("data/warning.wav"))
  lazy private val fire = Gdx.audio.newMusic(Gdx.files.internal("data/fire.wav"))
  lazy private val explode = Gdx.audio.newMusic(Gdx.files.internal("data/explode.wav"))

  def playFire  {
    fire.play()
  }
  def playExplode {
    explode.play()
  }
  def playWarning: Unit = {
    warn.play()
  }
}


