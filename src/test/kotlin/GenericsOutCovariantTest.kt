import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class `Covariant 'out' modifier test` {

    @Test
    fun `should feed cats and dogs`() {
        val cats = OutGroupOfAnimals<Cat>(Cat(), Cat(), Tiger())
        val dogs = OutGroupOfAnimals<Dog>(Terier())
        /*Without 'out' (OutGroupOfAnimals<out T : Animal>) modifier 'feedAnimals' would raise compilation error
        because it expects only type of OutGroupOfAnimals<Animal> as an input argument. Out makes checks less restrictive
        */
        feedAnimals(cats)
        feedAnimals(dogs)

        assertFalse(cats.anyHungry)
        assertFalse(dogs.anyHungry)
    }

    private fun feedAnimals(animals: OutGroupOfAnimals<Animal>) {
        for (i in 0 until animals.count) {
            //produce animal
            val animal = animals.getAt(i)
            if (animal.hungry) {
                animal.feed(Food())
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun addAnimal(animals: OutGroupOfAnimals<Animal>) {
        /*
        Note that we can not add new animal to group, because OutGroupOfAnimals<Animal>
        may be OutGroupOfAnimals<Cat>, OutGroupOfAnimals<Dog> or anything what extends Animal.
        OutGroupOfAnimals<Animal> is a producer.
        */
        //animals.add(Animal())
    }

    class OutGroupOfAnimals<out T : Animal>(vararg animals: T) {
        private val population = mutableListOf(*animals)

        val count: Int
            get() = population.count()

        fun getAt(position: Int): T {
            return population.get(position)
        }

        /*
        'out' modifier requires from Generic to be 'producer' only - to have no setter of T methods so it is safe.
        Note that 'feedAnimals' gets and feeds only 'animals'. 'cats' and 'dogs' are subtype of animal.
        */
        //fun add(animal: T) = population.add(animal)

        val anyHungry: Boolean
            get() = population.any { it -> it.hungry }
    }
}

