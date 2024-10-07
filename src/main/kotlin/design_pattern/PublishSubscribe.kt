package design_pattern

object EventBroker {
    private val subscribers = mutableMapOf<String, MutableList<(String)-> Unit>>()

    fun subscribe(event: String, action: (String)-> Unit) {
        subscribers.computeIfAbsent(event) { mutableListOf() }.add(action)
    }

    fun publish(event: String, message: String) {
        subscribers[event]?.forEach {
            it.invoke(message)
        }
    }
}

fun main() {
    EventBroker.subscribe("news") {
        println("received $it #1")
    }

    EventBroker.subscribe("news") {
        println("received $it #2")
    }

    EventBroker.publish("news", "Hello World to All the Subscribers!!")
}