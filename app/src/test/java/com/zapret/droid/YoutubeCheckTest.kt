package com.zapret.droid

import org.junit.Assert.assertTrue
import org.junit.Test

class YoutubeCheckTest {
    @Test
    fun acceptedHttpStatusesMarkYoutubeAsReachable() {
        assertTrue(formatYoutubeResponse(200, 120).startsWith("доступен"))
        assertTrue(formatYoutubeResponse(302, 120).startsWith("доступен"))
    }

    @Test
    fun blockedAndClientRateLimitStatusesAreReportedAsSiteResponse() {
        assertTrue(formatYoutubeResponse(403, 120).startsWith("ответ сайта получен"))
        assertTrue(formatYoutubeResponse(429, 120).startsWith("ответ сайта получен"))
    }

    @Test
    fun serverErrorsAreReportedAsUnavailable() {
        assertTrue(formatYoutubeResponse(500, 120).startsWith("недоступен"))
    }
}
