package com.example.kma_shot.engine

class GameLoop : Runnable {

    private var running = false
    private var gameThread: Thread? = null

    fun start() {
        running = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    fun stop() {
        running = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        // TODO: Implement game loop with fixed time step
        while (running) {
            // Update game logic
            // Render graphics
        }
    }
}
