package redis.embedded

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.assertThrows
import redis.clients.jedis.Jedis
import redis.embedded.utils.generateRandomString
import kotlin.test.assertNull

abstract class AbstractJedisTest {
    private val logger = KotlinLogging.logger {}

    private lateinit var key1: String
    private lateinit var key2: String
    private lateinit var key3: String

    private lateinit var value1: String
    private lateinit var value2: String
    private lateinit var value3: String

    private fun initializeKeys() {
        key1 = generateRandomString(3, 50)
        key2 = generateRandomString(3, 50)
        key3 = generateRandomString(3, 50)
    }

    private fun initializeValues() {
        value1 = generateRandomString(3, 50)
        value2 = generateRandomString(3, 50)
        value3 = generateRandomString(3, 50)
    }

    private fun msetSuccessOnlyKey1Value1Key2Value2(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        jedis.mset(key1, value1, key2, value2)
    }

    private fun setSuccessOnlyKey1Value1Key2Value2(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        jedis.set(key1, value1)
        jedis.set(key2, value2)
    }

    private fun mgetSuccessWithValue1Value2AndValue3IsNull(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        val newValue1 = jedis.mget(key1)[0]
        val newValue2 = jedis.mget(key2)[0]
        val newValue3 = jedis.mget(key3)[0]

        logger.info("key1: $key1, newValue1: $value1")
        logger.info("key2: $key2, newValue2: $value2")
        logger.info("key3: $key3, newValue3: $value3")

        require(value1 == newValue1)
        require(value2 == newValue2)
        requireNotNull(newValue3)
    }

    private fun getSuccessWithValue1Value2AndValue3IsNull(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        val newValue1 = jedis[key1]
        val newValue2 = jedis[key2]
        val newValue3 = jedis[key3]

        logger.info("key1: $key1, newValue1: $value1")
        logger.info("key2: $key2, newValue2: $value2")
        logger.info("key3: $key3, newValue3: $value3")

        require(value1 == newValue1)
        require(value2 == newValue2)
        requireNotNull(newValue3)
    }

    fun writeAndReadSuccess(jedis: Jedis) {
        initializeKeys()
        initializeValues()

        msetSuccessOnlyKey1Value1Key2Value2(jedis)
        setSuccessOnlyKey1Value1Key2Value2(jedis)
        mgetSuccessWithValue1Value2AndValue3IsNull(jedis)
        getSuccessWithValue1Value2AndValue3IsNull(jedis)
    }

    fun msetFail(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        assertThrows<Exception> {
            jedis.mset(key1, value1, key2, value2, key3, value3)
        }
    }

    fun setFail(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        assertThrows<Exception> {
            jedis.set(key1, value1)
            jedis.set(key2, value2)
            jedis.set(key3, value3)
        }
    }

    fun mgetFail(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        assertThrows<Exception> {
            jedis.mget(key1)
            jedis.mget(key2)
            jedis.mget(key3)
        }
    }

    fun mgetNothing(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        val newValue1 = jedis.mget(key1)[0]
        val newValue2 = jedis.mget(key2)[0]
        val newValue3 = jedis.mget(key3)[0]

        logger.info("key1: $key1, newValue1: $value1")
        logger.info("key2: $key2, newValue2: $value2")
        logger.info("key3: $key3, newValue3: $value3")

        assertNull(newValue1)
        assertNull(newValue2)
        assertNull(newValue3)
    }

    fun getFail(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        assertThrows<Exception> {
            jedis[key1]
            jedis[key2]
            jedis[key3]
        }
    }

    fun getNothing(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        val newValue1 = jedis[key1]
        val newValue2 = jedis[key2]
        val newValue3 = jedis[key3]

        logger.info("key1: $key1, newValue1: $value1")
        logger.info("key2: $key2, newValue2: $value2")
        logger.info("key3: $key3, newValue3: $value3")

        assertNull(newValue1)
        assertNull(newValue2)
        assertNull(newValue3)
    }
}
