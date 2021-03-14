import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class `Contrvariant 'in' modifier test` {
    @Test
    fun `should add Tiger to each group`() {

        val animals = InGroupOfAnimals<Animal>()
        val cats = InGroupOfAnimals<Cat>()
        val tigers = InGroupOfAnimals<Tiger>()

        /*Without 'in' (InGroupOfAnimals<in T : Animal>) modifier 'addTigerTo' would raise compilation error
        because it expects only type of InGroupOfAnimals<Animal> as an input argument. In makes checks less restrictive
        */

        addTigerTo(animals)//you can add tiger to animals
        addTigerTo(cats)//you can add tiger to cats
        addTigerTo(tigers)//you can add tiger to tigers
        //addTigerTo(siberianTigers)//you can not add tiger to siberian tigers

        assertEquals(1, animals.count)
        assertEquals(1, cats.count)
        assertEquals(1, tigers.count)
    }

    private fun feedTigers(tigers: InGroupOfAnimals<Tiger>) {
        /*
        Note that we can not get tiger from a group, because InGroupOfAnimals<Tiger>
        may be InGroupOfAnimals<Cat>, InGroupOfAnimals<Animal> or anything what Tiger extends.
        InGroupOfAnimals<Tiger> is a consumer.
        */
        for (i in 0 until tigers.count) {
            /*
            val tiger = tigers.getAt(i)
            if (tiger.hungry) {
                tiger.feed(Food())
            }*/
        }
    }

    fun addTigerTo(tigers: InGroupOfAnimals<Tiger>) {
        //Even InGroupOfAnimals<Animal>, InGroupOfAnimals<Cat> can be passed as an argument and consume Tiger
        tigers.put(Tiger())
    }

    class InGroupOfAnimals<in T : Animal> {
        private val population = mutableListOf<T>()

        val count: Int
            get() = population.count()

        fun put(animal: T) {
            population.add(animal)
        }
        /*
        'in' modifier requires from Generic to be 'consumer' only - to have no getter of T methods so it is safe.
        */
        //fun getAt(position: Int): T = population.get(position)
    }
}

