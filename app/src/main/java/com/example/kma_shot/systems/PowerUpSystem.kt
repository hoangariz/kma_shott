package com.example.kma_shot.systems

import com.example.kma_shot.items.PowerUp

class PowerUpSystem {

    private val activePowerUps = mutableListOf<PowerUp>()
    private val activatedEffects = mutableMapOf<PowerUp.PowerUpType, Float>()

    // TODO: Quản lý power-ups đang rơi và đang active

    fun update(deltaTime: Float) {
        // TODO: Update power-up positions, check collection, update effect timers
    }

    fun addPowerUp(powerUp: PowerUp) {
        activePowerUps.add(powerUp)
    }

    fun activatePowerUp(type: PowerUp.PowerUpType, duration: Float = 10f) {
        // TODO: Activate power-up effect
        activatedEffects[type] = duration
    }

    fun isEffectActive(type: PowerUp.PowerUpType): Boolean {
        return activatedEffects.containsKey(type) && activatedEffects[type]!! > 0
    }

    fun getActivePowerUps() = activePowerUps

    fun clear() {
        activePowerUps.clear()
        activatedEffects.clear()
    }
}
