package foo

import kotlin.reflect.KClass

inline fun <reified T : Any> foo(b: Boolean = false): KClass<T> {
    if (b) {
        val T = 1
    }
    return T::class
}

fun box(): String {
    assertEquals(A::class, foo<A>())
    assertEquals(B::class, foo<B>())
    assertEquals(O::class, foo<O>())
    assertEquals(E::class, foo<E>())

    return "OK"
}
