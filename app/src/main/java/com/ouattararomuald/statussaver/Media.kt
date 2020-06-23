package com.ouattararomuald.statussaver

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class Media(val file: File, val mediaType: MediaType): Parcelable
