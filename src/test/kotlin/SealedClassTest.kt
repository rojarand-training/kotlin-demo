import org.junit.Test

sealed class TrafficLight
data class GreenLight(val active: Boolean): TrafficLight()
data class YellowLight(val active: Boolean): TrafficLight()
data class RedLight(val active: Boolean): TrafficLight()

class SealedClassTest{

    @Test
    fun `Should not generate warning when all 'TrafficLights' are used inside when expression`(){

        /*
        when(trafficLight){
           is GreenLight -> print("Can't go")
        }
        'when' expression on sealed classes is recommended to be exhaustive,
        add 'is YellowLight', 'is RedLight' branches or 'else' branch instead
        */
        when(val trafficLight:TrafficLight = GreenLight(active = false)){
            is GreenLight -> if(trafficLight.active) print("Can go")
            is YellowLight -> if(trafficLight.active) print("Prepare for change")
            is RedLight -> if(trafficLight.active) print("Can't go")
        }
    }
}
