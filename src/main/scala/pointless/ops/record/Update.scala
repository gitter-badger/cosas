/*
## Popping an element from a set

It's like `Lookup`, but it removes the element

*/

package ohnosequences.pointless.ops.record

import ohnosequences.pointless._
import AnyFn._, AnyType._, AnyProperty._, AnyTypeSet._, AnyRecord._
import ops.typeSet._

@annotation.implicitNotFound(msg = "Can't update record ${R} with property values ${Ps}")
trait Update[R <: AnyRecord, Ps <: AnyTypeSet]
  extends Fn2[RawOf[R], Ps] with Out[ValueOf[R]]

object Update {

  implicit def update[R <: AnyRecord, Ps <: AnyTypeSet]
    (implicit 
      check: Ps ⊂ RawOf[R],
      replace: Replace[RawOf[R], Ps]
    ):  Update[R, Ps] =
    new Update[R, Ps] {

      def apply(recEntry: RawOf[R], propReps: Ps): Out = 

        valueOf[R](replace(recEntry, propReps))
    }

}
