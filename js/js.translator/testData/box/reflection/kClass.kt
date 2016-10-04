package foo

import kotlin.reflect.KClass

fun <T : Any> check(x: KClass<T>, y: KClass<T>, shouldBeEquals: Boolean = true) {
    assertNotEquals(null, x)
    assertNotEquals(null, y)
    if (shouldBeEquals)
        assertEquals(x, y)
    else
        assertNotEquals(x, y)
}

fun box(): String {
    check(A::class, A()::class)
    check(B::class, B()::class)
    check(O::class, (O)::class)
    assertNotEquals(null, I::class)
    check(E::class, E.X::class)
    check(E::class, E.Y::class, shouldBeEquals = false)
// TODO uncomment after KT-13338 is fixed
//    check(E::class, E.Z::class)

    return "OK"
}
