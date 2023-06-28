package redis.embedded.utils

import kotlin.random.Random

fun generateRandomPort() = Random.nextInt(0, 65535)

private val CHAR_RANGE = 'a'..'z'

fun generateRandomString(minLengthInclusive: Int, maxLengthExclusive: Int): String {
    val length = Random.nextInt(minLengthInclusive, maxLengthExclusive)
    return StringBuilder(length).apply {
        repeat(length) {
            this.append(CHAR_RANGE.random())
        }
    }.toString()
}
