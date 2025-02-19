package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.exceptions.OpenFeatureError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EvaluationContextExtTest {
    @Test
    fun toBKTUser_withValidData_returnsCorrectBKTUser() {
        val evaluationContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val bktUser = evaluationContext.toBKTUser()
        assertEquals("user1", bktUser.id)
        assertEquals(mapOf("attr1" to "value1"), bktUser.attributes)
    }

    @Test
    fun toBKTUser_withNullAttributes_returnsBKTUserWithEmptyAttributes() {
        val evaluationContext = ImmutableContext(targetingKey = "user2", attributes = emptyMap())
        val bktUser = evaluationContext.toBKTUser()
        assertEquals("user2", bktUser.id)
        assertEquals(emptyMap<String, String>(), bktUser.attributes)
    }

    @Test
    fun toBKTUser_withMissingTargetingKey_throwsTargetingKeyMissingError() {
        val evaluationContext =
            ImmutableContext(
                targetingKey = "",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val exception =
            assertThrows(OpenFeatureError.TargetingKeyMissingError::class.java) {
                evaluationContext.toBKTUser()
            }
        assertEquals("missing targeting key", exception.message)
    }

    @Test
    fun toBKTUser_withNullTargetingKey_throwsTargetingKeyMissingError() {
        val evaluationContext =
            ImmutableContext(
                targetingKey = "",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val exception =
            assertThrows(OpenFeatureError.TargetingKeyMissingError::class.java) {
                evaluationContext.toBKTUser()
            }
        assertEquals("missing targeting key", exception.message)
    }
}
