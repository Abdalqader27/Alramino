package com.abdalqader27.myalarm.data.preference

import com.abdalqader27.myalarm.R
import com.abdalqader27.myalarm.data.PreferenceData
import com.abdalqader27.myalarm.data.SoundData
import com.abdalqader27.myalarm.dialogs.SoundChooserDialog

/**
 * Allows the user to select from a set of
 * ringtone sounds (preference is a string
 * that can be recreated into a SoundData
 * object).
 */
class RingtonePreferenceData(private val preference: PreferenceData, name: Int) : CustomPreferenceData(name) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String {
        return preference.getValue(holder.context, "")?.let{ sound ->
            if (!sound.isEmpty())
                SoundData.fromString(sound)?.name ?: holder.context.getString(R.string.title_sound_none)
            else null
        } ?: holder.context.getString(R.string.title_sound_none)
    }

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        holder.alarmio?.fragmentManager?.let { manager ->
            val dialog = SoundChooserDialog()
            dialog.setListener { sound ->
                preference.setValue(holder.context, sound?.toString())
                bindViewHolder(holder)
            }
            dialog.show(manager, null)
        }
    }
}
