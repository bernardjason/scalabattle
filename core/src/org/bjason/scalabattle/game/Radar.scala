package org.bjason.scalabattle.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import org.bjason.scalabattle.game.GameScreen.asteroids

object Radar {

  def render(shapeRenderer: ShapeRenderer, playerX: Float, playerY: Float, playerRotate: Float): Unit = {
    val radarWidth = (Gdx.graphics.getWidth * 0.2).toInt
    val radarHeight = (Gdx.graphics.getHeight * 0.2).toInt
    val radarX = (Gdx.graphics.getWidth * 0.8).toInt
    val radarY = (Gdx.graphics.getHeight * 0.8).toInt
    val scale = 0.05f
    val pointSize = 3
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    shapeRenderer.setColor(Color.DARK_GRAY)
    shapeRenderer.rect(radarX, radarY, radarWidth, radarHeight)
    shapeRenderer.end()
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
    shapeRenderer.setColor(Color.WHITE)
    shapeRenderer.rect(radarX, radarY, radarWidth - 1, radarHeight - 1)
    shapeRenderer.end()

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
    shapeRenderer.setColor(Color.YELLOW)
    val playerSize = pointSize * 3
    val radarPolygon = new Polygon(
      Array(
        -playerSize, -playerSize,
        0, playerSize,
        playerSize, -playerSize)
    )
    val fromX = radarX + radarWidth / 2
    val fromY = radarY + radarHeight / 2
    radarPolygon.rotate(playerRotate)
    val t = radarPolygon.getTransformedVertices
    for (i <- 0 to t.length - 1 by 2) {
      t(i) = t(i) + fromX
      t(i + 1) = t(i + 1) + fromY
    }
    shapeRenderer.setColor(Color.CYAN)
    shapeRenderer.line(t(0), t(1), t(2), t(3))
    shapeRenderer.line(t(2), t(3), t(4), t(5))
    shapeRenderer.setColor(Color.YELLOW)
    shapeRenderer.line(t(4), t(5), t(0), t(1))
    shapeRenderer.end()

    shapeRenderer.setColor(Color.WHITE)
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
    for (a <- asteroids) {
      val x = (a.getX - playerX) * scale + radarX + radarWidth / 2
      val y = (a.getY - playerY) * scale + radarY + radarHeight / 2
      shapeRenderer.rect(x, y, pointSize, pointSize)
    }
    shapeRenderer.end()
  }
}
