package com.example.kma_shot.core

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import com.example.kma_shot.R
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class AudioManager private constructor(private val context: Context) {

    companion object {
        @Volatile private var instance: AudioManager? = null
        fun getInstance(context: Context): AudioManager {
            return instance ?: synchronized(this) {
                instance ?: AudioManager(context.applicationContext).also { instance = it }
            }
        }
        private const val TAG = "AudioManager"
    }

    // ===== Background Music (MediaPlayer) =====
    private var bgm: MediaPlayer? = null
    private var musicVolume: Float = 0.7f
    private var isMusicEnabled: Boolean = true

    // ===== Sound Effects (SoundPool) =====
    private var soundPool: SoundPool? = null
    private var isSoundEnabled: Boolean = true
    private var soundVolume: Float = 1.0f

    // Map resourceId -> soundId
    private val soundIdMap = ConcurrentHashMap<Int, Int>()
    // set loaded soundIds
    private val loadedSoundIds = ConcurrentHashMap<Int, Boolean>()
    // pending plays while a sound is still loading
    private val pendingPlays = ConcurrentHashMap<Int, CopyOnWriteArrayList<PlayRequest>>()

    private data class PlayRequest(
        val resourceId: Int,
        val loop: Boolean,
        val rate: Float,
        val volumeL: Float,
        val volumeR: Float
    )

    init {
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(20) // tăng để không drop khi burst/va chạm
            .setAudioAttributes(attr)
            .build()

        soundPool?.setOnLoadCompleteListener { sp, soundId, status ->
            if (status == 0) {
                loadedSoundIds[soundId] = true
                // phát các yêu cầu đang đợi
                pendingPlays[soundId]?.let { queue ->
                    queue.forEach { r ->
                        val loopMode = if (r.loop) -1 else 0
                        sp.play(soundId, r.volumeL, r.volumeR, 1, loopMode, r.rate)
                    }
                    queue.clear()
                }
            } else {
                Log.w(TAG, "Sound load failed: soundId=$soundId")
            }
        }
    }

    // ===================== BGM =====================

    fun startBackgroundMusic() = playBackgroundMusic(R.raw.bg_music)

    fun playBackgroundMusic(resourceId: Int) {
        try {
            stopBackgroundMusic()
            if (!isMusicEnabled) return
            bgm = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "playBackgroundMusic error", e)
        }
    }

    fun pauseBackgroundMusic() {
        bgm?.takeIf { it.isPlaying }?.pause()
    }

    fun resumeBackgroundMusic() {
        if (!isMusicEnabled) return
        bgm?.takeIf { !it.isPlaying }?.start()
    }

    fun stopBackgroundMusic() {
        try {
            bgm?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) { }
        bgm = null
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) pauseBackgroundMusic() else resumeBackgroundMusic()
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        bgm?.setVolume(musicVolume, musicVolume)
    }

    fun isMusicEnabled() = isMusicEnabled
    fun getMusicVolume() = musicVolume

    // ===================== SFX =====================

    /**
     * Nên gọi ở màn Splash/Menu/Game init để preload toàn bộ SFX.
     * Bạn đã đổi mp3 -> wav (giữ tên) trừ bg, nên các ID dưới vẫn đúng.
     */
    fun preloadAll() {
        loadSound(R.raw.paddlehit)
        loadSound(R.raw.brick_destroy)
        loadSound(R.raw.paddleshot)
        loadSound(R.raw.alert_ball)
        loadSound(R.raw.collect_item)   // nhớ thêm file wav vào res/raw
        // Nếu có: loadSound(R.raw.win); loadSound(R.raw.lose)
    }

    /**
     * Hâm nóng SFX để giảm latency lần đầu (âm lượng cực nhỏ, không nghe thấy).
     */
    fun warmUp() {
        val ids = listOf(
            R.raw.paddlehit, R.raw.brick_destroy, R.raw.paddleshot, R.raw.alert_ball, R.raw.collect_item
        )
        ids.forEach { res ->
            val sid = soundIdMap[res] ?: return@forEach
            soundPool?.play(sid, 0.001f, 0.001f, 0, 0, 1f)
        }
    }

    fun setSoundEnabled(enabled: Boolean) { isSoundEnabled = enabled }
    fun isSoundEnabled() = isSoundEnabled

    fun setSoundVolume(volume: Float) { soundVolume = volume.coerceIn(0f, 1f) }
    fun getSoundVolume() = soundVolume

    /**
     * Load SFX (idempotent). Trả về soundId, hoặc -1 nếu lỗi.
     */
    fun loadSound(resourceId: Int): Int {
        val sp = soundPool ?: return -1
        // đã có soundId?
        soundIdMap[resourceId]?.let { return it }
        return try {
            val soundId = sp.load(context, resourceId, 1)
            soundIdMap[resourceId] = soundId
            soundId
        } catch (e: Exception) {
            Log.e(TAG, "loadSound error: $resourceId", e)
            -1
        }
    }

    /**
     * Phát SFX với queue “đợi load xong”.
     * @param rate tốc độ phát (1f mặc định). Mình sẽ random nhẹ để đỡ “đồng pha”.
     */
    fun playSound(resourceId: Int, loop: Boolean = false, rate: Float = 1f, volume: Float = soundVolume) {
        if (!isSoundEnabled) return
        val sp = soundPool ?: return

        val sid = soundIdMap[resourceId] ?: loadSound(resourceId)
        if (sid == -1) return

        val actualRate = if (Build.VERSION.SDK_INT >= 21) {
            // ngẫu nhiên nhẹ 0.98..1.02 để tự nhiên hơn
            (0.98f + (Math.random().toFloat() * 0.04f)) * rate
        } else rate

        if (loadedSoundIds[sid] == true) {
            val loopMode = if (loop) -1 else 0
            sp.play(sid, volume, volume, 1, loopMode, actualRate)
        } else {
            // hàng đợi đến khi load xong
            val req = PlayRequest(resourceId, loop, actualRate, volume, volume)
            pendingPlays.getOrPut(sid) { CopyOnWriteArrayList() }.add(req)
        }
    }

    /**
     * Dừng một SFX đang phát (cần streamId – khác soundId).
     * Nếu bạn cần stop stream cụ thể, hãy lưu lại giá trị trả về của SoundPool.play(...)
     * ở nơi gọi. Ở đây giữ API tối giản như bản cũ nên không trả streamId.
     */

    // ===== Game-specific wrappers =====

    fun playBallPaddleSound()  = playSound(R.raw.paddlehit)
    fun playBallBrickSound()   = playSound(R.raw.brick_destroy)
    fun playBrickBreakSound()  = playSound(R.raw.brick_destroy)
    fun playPaddleShotSound()  = playSound(R.raw.paddleshot)
    fun playAlertBallSound()   = playSound(R.raw.alert_ball)

    fun playPowerUpSound()     = playSound(R.raw.collect_item)
    fun powerUpPickSound()     = playSound(R.raw.item_pickup)

    fun playWinSound()  { /* playSound(R.raw.win)  */ }
    fun playLoseSound() { /* playSound(R.raw.lose) */ }

    // ===================== Cleanup =====================

    fun release() {
        stopBackgroundMusic()
        try {
            soundPool?.release()
        } catch (_: Exception) { }
        soundPool = null
        soundIdMap.clear()
        loadedSoundIds.clear()
        pendingPlays.clear()
    }
}
