package com.smaran.core.domain.speech

/** The language the person uses in the app. Speech recognition and phrase parsing follow it. */
interface AppLanguage {
    /** BCP 47 tag for speech recognition, e.g. "en-IN". */
    val speechLanguageTag: String
}
