package com.example.kma_shot.systems

import com.example.kma_shot.items.PowerUp
import com.example.kma_shot.items.PowerUpFactory
import kotlin.random.Random

class DropTableSystem {

    // Drop rates cho từng mode
    private var dropRate = 0.3f

    // Drop table weights
    private val dropWeights = mutableMapOf<PowerUp.PowerUpType, Int>(
        PowerUp.PowerUpType.BULLET to 30,
        PowerUp.PowerUpType.HEALTH to 20,
        PowerUp.PowerUpType.ENERGY to 25,
        PowerUp.PowerUpType.MULTI_BALL to 25
    )

    // TODO: Rơi items theo tỉ lệ được config theo mode

    fun setDropRate(rate: Float) {
        dropRate = rate.coerceIn(0f, 1f)
    }

    fun setDropWeight(type: PowerUp.PowerUpType, weight: Int) {
        dropWeights[type] = weight
    }

    fun rollDrop(x: Float, y: Float): PowerUp? {
        // TODO: Random drop with configured rate
        if (Random.nextFloat() < dropRate) {
            return createWeightedRandomPowerUp(x, y)
        }
        return null
    }

    private fun createWeightedRandomPowerUp(x: Float, y: Float): PowerUp {
        // TODO: Weighted random selection
        val totalWeight = dropWeights.values.sum()
        var randomValue = Random.nextInt(totalWeight)

        for ((type, weight) in dropWeights) {
            randomValue -= weight
            if (randomValue <= 0) {
                return PowerUp(x, y, type = type)
            }
        }

        return PowerUp(x, y, type = PowerUp.PowerUpType.MULTI_BALL)
    }
}
