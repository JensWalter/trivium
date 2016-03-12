##description

Bindings, like Tasks, carry code. The difference lies in the lifecycle of the object. Tasks are instantiated and destroyed on demand.
Bindings on the other hand are kind of durable. Once started, a binding should be active all the time. This activity can be interrupted by failures, but then the system ensures that the binding becomes available in another environment.
Further to that, bindings can emit as much objects as required during there lifecycle.
