open class Food

@Suppress("UNUSED_PARAMETER")
open class Animal(var hungry: Boolean){
    fun feed(food: Food) {hungry = false}
}

open class Dog: Animal(hungry = true)
class Terier: Dog()
open class Cat: Animal(hungry = true)
open class Tiger: Cat()
class SiberianTiger: Tiger()