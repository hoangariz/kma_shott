package com.example.kma_shot.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

class AudioManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AudioManager? = null

        fun getInstance(context: Context): AudioManager {
            return instance ?: synchronized(this) {
                instance ?: AudioManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var backgroundMusic: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val soundMap = HashMap<Int, Int>()

    private var musicVolume: Float = 0.7f
    private var soundVolume: Float = 1.0f
    private var isMusicEnabled: Boolean = true
    private var isSoundEnabled: Boolean = true

    init {
        // Initialize SoundPool for sound effects
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    /**
     * Play background music
     * @param resourceId Resource ID of the music file in res/raw
     */
    fun playBackgroundMusic(resourceId: Int) {
        try {
            stopBackgroundMusic()
            
            if (!isMusicEnabled) return

            backgroundMusic = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
            
            Log.d("AudioManager", "Background music started")
        } catch (e: Exception) {
            Log.e("AudioManager", "Error playing background music", e)
        }
    }

    /**
     * Stop background music
     */
    fun stopBackgroundMusic() {
        backgroundMusic?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        backgroundMusic = null
    }

    /**
     * Pause background music
     */
    fun pauseBackgroundMusic() {
        backgroundMusic?.apply {
            if (isPlaying) {
                pause()
            }
        }
    }

    /**
     * Resume background music
     */
    fun resumeBackgroundMusic() {
        if (!isMusicEnabled) return
        
        backgroundMusic?.apply {
            if (!isPlaying) {
                start()
            }
        }
    }

    /**
     * Set music volume
     * @param volume Float between 0.0 and 1.0
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(musicVolume, musicVolume)
    }

    /**
     * Set sound effects volume
     * @param volume Float between 0.0 and 1.0
     */
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Enable or disable background music
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            pauseBackgroundMusic()
        } else {
            resumeBackgroundMusic()
        }
    }

    /**
     * Enable or disable sound effects
     */
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    /**
     * Load a sound effect
     * @param resourceId Resource ID of the sound file in res/raw
     * @return Sound ID that can be used to play the sound
     */
    fun loadSound(resourceId: Int): Int {
        soundPool?.let { pool ->
            if (!soundMap.containsKey(resourceId)) {
                val soundId = pool.load(context, resourceId, 1)
                soundMap[resourceId] = soundId
                return soundId
            }
            return soundMap[resourceId] ?: -1
        }
        return -1
    }

    /**
     * Play a sound effect
     * @param resourceId Resource ID of the sound file
     * @param loop If true, the sound will loop until stopped
     */
    fun playSound(resourceId: Int, loop: Boolean = false) {
        if (!isSoundEnabled) return

        soundPool?.let { pool ->
            val soundId = soundMap[resourceId] ?: loadSound(resourceId)
            if (soundId != -1) {
                val loopMode = if (loop) -1 else 0
                pool.play(soundId, soundVolume, soundVolume, 1, loopMode, 1f)
            }
        }
    }

    /**
     * Stop a specific sound
     * @param soundId The sound ID returned from loadSound()
     */
    fun stopSound(soundId: Int) {
        soundPool?.stop(soundId)
    }

    /**
     * Get current music volume
     */
    fun getMusicVolume(): Float = musicVolume

    /**
     * Get current sound volume
     */
    fun getSoundVolume(): Float = soundVolume

    /**
     * Check if music is enabled
     */
    fun isMusicEnabled(): Boolean = isMusicEnabled

    /**
     * Check if sound effects are enabled
     */
    fun isSoundEnabled(): Boolean = isSoundEnabled

    /**
     * Release all resources
     */
    fun release() {
        stopBackgroundMusic()
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}
