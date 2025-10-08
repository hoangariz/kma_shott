package com.example.kma_shot.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import com.example.kma_shot.R

class PauseMenuDialog : DialogFragment() {

    interface PauseMenuListener {
        fun onContinue()
        fun onRestart()
        fun onMainMenu()
        fun onBackgroundMusicToggle(isEnabled: Boolean)
        fun onSoundEffectsToggle(isEnabled: Boolean)
    }

    private var listener: PauseMenuListener? = null
    private var isBackgroundMusicEnabled = true
    private var isSoundEffectsEnabled = true

    companion object {
        fun newInstance(
            backgroundMusicEnabled: Boolean = true,
            soundEffectsEnabled: Boolean = true
        ): PauseMenuDialog {
            val dialog = PauseMenuDialog()
            val args = Bundle()
            args.putBoolean("background_music_enabled", backgroundMusicEnabled)
            args.putBoolean("sound_effects_enabled", soundEffectsEnabled)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isBackgroundMusicEnabled = it.getBoolean("background_music_enabled", true)
            isSoundEffectsEnabled = it.getBoolean("sound_effects_enabled", true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pause_menu_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinue = view.findViewById<Button>(R.id.btnContinue)
        val btnRestart = view.findViewById<Button>(R.id.btnRestart)
        val btnMainMenu = view.findViewById<Button>(R.id.btnMainMenu)
        val switchBackgroundMusic = view.findViewById<Switch>(R.id.switchBackgroundMusic)
        val switchSoundEffects = view.findViewById<Switch>(R.id.switchSoundEffects)

        // Set initial states
        switchBackgroundMusic.isChecked = isBackgroundMusicEnabled
        switchSoundEffects.isChecked = isSoundEffectsEnabled

        btnContinue.setOnClickListener {
            listener?.onContinue()
            dismiss()
        }

        btnRestart.setOnClickListener {
            listener?.onRestart()
            dismiss()
        }

        btnMainMenu.setOnClickListener {
            // Cleanup trước khi quay lại main menu
            try {
                // Dừng tất cả audio nếu context không null
                context?.let { ctx ->
                    val audioManager = com.example.kma_shot.core.AudioManager.getInstance(ctx)
                    audioManager.stopBackgroundMusic()
                }
                
                // Dismiss dialog trước
                dismiss()
                
                // Gọi callback sau khi dismiss
                listener?.onMainMenu()
            } catch (e: Exception) {
                // Nếu có lỗi, vẫn dismiss và gọi callback
                dismiss()
                listener?.onMainMenu()
            }
        }

        switchBackgroundMusic.setOnCheckedChangeListener { _, isChecked ->
            listener?.onBackgroundMusicToggle(isChecked)
        }

        switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            listener?.onSoundEffectsToggle(isChecked)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun setPauseMenuListener(listener: PauseMenuListener) {
        this.listener = listener
    }
}
