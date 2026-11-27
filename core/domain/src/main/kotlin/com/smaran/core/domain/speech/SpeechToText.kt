package com.smaran.core.domain.speech

import kotlinx.coroutines.flow.Flow

/** Turns speech into text. One [listen] call is one utterance; the flow ends after a result or failure. */
interface SpeechToText {
    fun isAvailable(): Boolean

    /** [languageTag] is a BCP 47 tag such as "en-IN" or "hi-IN". */
    fun listen(languageTag: String): Flow<SpeechEvent>
}

sealed interface SpeechEvent {
    data object Listening : SpeechEvent
    data class Partial(val text: String) : SpeechEvent
    data class Result(val text: String) : SpeechEvent
    data class Failed(val error: SpeechError) : SpeechEvent
}

enum class SpeechError { NothingHeard, NoPermission, NotAvailable, NeedsInternet, Busy, Other }
