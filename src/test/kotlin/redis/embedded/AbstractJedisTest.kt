package redis.embedded

import io.github.oshai.kotlinlogging.KotlinLogging
import redis.clients.jedis.Jedis
import redis.embedded.utils.generateRandomString
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractJedisTest {
    private val logger = KotlinLogging.logger {}

    private lateinit var key1: String
    private lateinit var key2: String
    private lateinit var key3: String

    private lateinit var key4: String
    private lateinit var key5: String
    private lateinit var key6: String

    private lateinit var value1: String
    private lateinit var value2: String
    private lateinit var value3: String

    private lateinit var value4: String
    private lateinit var value5: String
    private lateinit var value6: String

    private fun initializeKeys() {
        key1 = generateRandomString(3, 50)
        key2 = generateRandomString(3, 50)
        key3 = generateRandomString(3, 50)

        key4 = generateRandomString(3, 50)
        key5 = generateRandomString(3, 50)
        key6 = generateRandomString(3, 50)
    }

    private fun initializeValues() {
        value1 = generateRandomString(3, 50)
        value2 = generateRandomString(3, 50)
        value3 = generateRandomString(3, 50)

        value4 = generateRandomString(3, 50)
        value5 = generateRandomString(3, 50)
        value6 = generateRandomString(3, 50)
    }

    private fun msetOnlyKey1Value1Key2Value2(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        jedis.mset(key1, value1, key2, value2)
    }

    private fun setOnlyKey4Value4Key5Value5(jedis: Jedis) {
        logger.info("key4: $key4, value4: $value4")
        logger.info("key5: $key5, value5: $value5")
        logger.info("key6: $key6, value6: $value6")

        jedis.set(key4, value4)
        jedis.set(key5, value5)
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

        assertEquals(value1, newValue1)
        assertEquals(value2, newValue2)
        assertNull(newValue3)
    }

    private fun getSuccessWithValue4Value5AndValue6IsNull(jedis: Jedis) {
        logger.info("key4: $key4, value4: $value4")
        logger.info("key5: $key5, value5: $value5")
        logger.info("key6: $key6, value6: $value6")

        val newValue4 = jedis[key4]
        val newValue5 = jedis[key5]
        val newValue6 = jedis[key6]

        logger.info("key4: $key4, newValue4: $value4")
        logger.info("key5: $key5, newValue5: $value5")
        logger.info("key6: $key6, newValue6: $value6")

        assertEquals(value4, newValue4)
        assertEquals(value5, newValue5)
        assertNull(newValue6)
    }

    fun writeAndReadSuccess(jedis: Jedis) {
        initializeKeys()
        initializeValues()

        msetOnlyKey1Value1Key2Value2(jedis)
        setOnlyKey4Value4Key5Value5(jedis)

        mgetSuccessWithValue1Value2AndValue3IsNull(jedis)
        getSuccessWithValue4Value5AndValue6IsNull(jedis)
    }

    fun msetAll(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        jedis.mset(key1, value1, key2, value2, key3, value3)
    }

    fun setAll(jedis: Jedis) {
        logger.info("key1: $key1, value1: $value1")
        logger.info("key2: $key2, value2: $value2")
        logger.info("key3: $key3, value3: $value3")

        jedis.set(key1, value1)
        jedis.set(key2, value2)
        jedis.set(key3, value3)
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

    fun getNothing(jedis: Jedis) {
        logger.info("key4: $key4, value4: $value4")
        logger.info("key5: $key5, value5: $value5")
        logger.info("key6: $key6, value6: $value6")

        val newValue4 = jedis[key4]
        val newValue5 = jedis[key5]
        val newValue6 = jedis[key6]

        logger.info("key4: $key4, newValue4: $value4")
        logger.info("key5: $key5, newValue5: $value5")
        logger.info("key6: $key6, newValue6: $value6")

        assertNull(newValue4)
        assertNull(newValue5)
        assertNull(newValue6)
    }
}
