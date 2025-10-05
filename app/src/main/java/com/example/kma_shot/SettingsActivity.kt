package com.example.kma_shot

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.kma_shot.core.AudioManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchBackgroundMusic: Switch
    private lateinit var switchSoundEffects: Switch
    private lateinit var btnBack: AppCompatButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var audioManager: AudioManager

    companion object {
        private const val PREFS_NAME = "GameSettings"
        private const val KEY_BACKGROUND_MUSIC = "background_music_enabled"
        private const val KEY_SOUND_EFFECTS = "sound_effects_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContentView(R.layout.activity_settings)
        
        // Initialize SharedPreferences and AudioManager
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        audioManager = AudioManager.getInstance(this)
        
        // Initialize views
        switchBackgroundMusic = findViewById(R.id.switchBackgroundMusic)
        switchSoundEffects = findViewById(R.id.switchSoundEffects)
        btnBack = findViewById(R.id.btnBack)
        
        // Load saved settings
        loadSettings()
        
        // Set up listeners
        setupListeners()
    }
    
    private fun loadSettings() {
        val isBackgroundMusicEnabled = sharedPreferences.getBoolean(KEY_BACKGROUND_MUSIC, true)
        val isSoundEffectsEnabled = sharedPreferences.getBoolean(KEY_SOUND_EFFECTS, true)
        
        switchBackgroundMusic.isChecked = isBackgroundMusicEnabled
        switchSoundEffects.isChecked = isSoundEffectsEnabled
    }
    
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        switchBackgroundMusic.setOnCheckedChangeListener { _, isChecked ->
            saveBackgroundMusicSetting(isChecked)
            onBackgroundMusicToggle(isChecked)
        }
        
        switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            saveSoundEffectsSetting(isChecked)
            onSoundEffectsToggle(isChecked)
        }
    }
    
    private fun saveBackgroundMusicSetting(isEnabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BACKGROUND_MUSIC, isEnabled)
            .apply()
    }
    
    private fun saveSoundEffectsSetting(isEnabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SOUND_EFFECTS, isEnabled)
            .apply()
    }
    
    private fun onBackgroundMusicToggle(isEnabled: Boolean) {
        // Apply setting to AudioManager
        audioManager.setMusicEnabled(isEnabled)
        println("Background music: ${if (isEnabled) "ON" else "OFF"}")
    }
    
    private fun onSoundEffectsToggle(isEnabled: Boolean) {
        // Apply setting to AudioManager
        audioManager.setSoundEnabled(isEnabled)
        println("Sound effects: ${if (isEnabled) "ON" else "OFF"}")
    }
    
    // Public methods to get current settings (can be used by other activities)
    fun isBackgroundMusicEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BACKGROUND_MUSIC, true)
    }
    
    fun isSoundEffectsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SOUND_EFFECTS, true)
    }
}
