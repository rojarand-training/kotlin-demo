import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction2

data class Person(val age: Int, val name: String){
    fun introduceYourself(){
        println("Hello, my name is $name, I am $age years old")
    }
}

fun sum(arg1: Int, arg2: Int):Int = arg1+arg2

class ReflectionTest {

    @Test
    fun `Access kotlin kClass and its members`(){
        val john = Person(age = 38, name = "John")
        val kClass: KClass<Person> = john.javaClass.kotlin
        println("Person class simple name: ${kClass.simpleName}")
        kClass.members.forEach {
            println("kClass member: ${it.name}")
        }
    }

    @Test
    fun `Access function via reflection`(){
        ///Note 'import kotlin.reflect.KFunction2' needed
        val fn2: KFunction2<Int, Int, Int> = ::sum
        fn2.call(1, 2)//vararg
        fn2.invoke(1,2)
        println("Fn2 name: ${fn2.name}")
    }

    @Test
    fun `Access method via reflection`(){
        val person = Person(age = 38, name = "John")
        val fn2: KFunction<Unit> = person::introduceYourself
        fn2.call()//vararg
        //fn2.invoke(person)
        println("Fn2 name: ${fn2.name}")
    }
}