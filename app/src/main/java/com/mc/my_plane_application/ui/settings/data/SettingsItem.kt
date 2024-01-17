package com.mc.my_plane_application.ui.settings.data

// Définition de la Class qui sera dans notre RecyclerView
data class SettingsItem(val name: String, val icon: Int, val onClick: (() -> Unit))
