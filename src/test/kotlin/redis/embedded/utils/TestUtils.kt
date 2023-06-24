package redis.embedded.utils

import kotlin.random.Random

fun generateRandomPort() = Random.nextInt(0, 65535)
