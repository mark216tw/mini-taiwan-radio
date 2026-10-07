package com.mark216tw.minitaiwanradio

data class Station(
    val id: String,
    val name: String,
    val network: String,
    val frequency: String,
    val region: String,
    val streamUrl: String,
    val websiteUrl: String,
    val logoUrl: String,
    val enabled: Boolean,
)
