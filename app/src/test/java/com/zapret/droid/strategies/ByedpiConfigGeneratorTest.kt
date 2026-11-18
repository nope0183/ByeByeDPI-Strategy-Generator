package com.zapret.droid.strategies

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ByedpiConfigGeneratorTest {
    @Test
    fun generatedCandidatesAreDeterministicAndUnlimitedInBatchSize() {
        val first = ByedpiConfigGenerator.generate(0, 0, 250)
        val repeated = ByedpiConfigGenerator.generate(0, 0, 250)

        assertEquals(250, first.size)
        assertEquals(first.map { it.arguments }, repeated.map { it.arguments })
        assertTrue(first.all { it.arguments.isNotBlank() })
        assertTrue(first.any { it.arguments.contains("-s") })
    }

    @Test
    fun differentSeedsProduceDifferentCandidates() {
        val first = ByedpiConfigGenerator.generate(0, 0, 10)
        val second = ByedpiConfigGenerator.generate(1, 0, 10)

        assertNotEquals(first.map { it.arguments }, second.map { it.arguments })
    }

    @Test
    fun exportContainsEveryCandidate() {
        val candidates = ByedpiConfigGenerator.generate(7, 0, 3)
        val exported = ByedpiConfigGenerator.export(candidates, 7)

        candidates.forEach { candidate ->
            assertTrue(exported.contains(candidate.arguments))
        }
    }
}
