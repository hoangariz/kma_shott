package com.example.kma_shot.items

import kotlin.random.Random

class PowerUpFactory {

    companion object {
        // TODO: Factory pattern for creating power-ups

        fun createRandomPowerUp(x: Float, y: Float): PowerUp? {
            // TODO: Random power-up generation logic
            val chance = Random.nextFloat()
            
            return if (chance < 0.3f) { // 30% chance to drop power-up
                val types = PowerUp.PowerUpType.values()
                val randomType = types[Random.nextInt(types.size)]
                PowerUp(x, y, type = randomType)
            } else {
                null
            }
        }
    }
}
