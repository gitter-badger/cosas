/*
## Popping an element from a set

It's like `Lookup`, but it removes the element

*/

package ohnosequences.pointless.ops.record

import ohnosequences.pointless._
import AnyFn._, AnyWrap._, AnyProperty._, AnyTypeSet._, AnyRecord._
import ops.typeSet._

@annotation.implicitNotFound(msg = "Can't transform ${R} to ${Other} with values ${Rest}")
trait Transform[R <: AnyRecord, Other <: AnyRecord, Rest <: AnyTypeSet] 
  extends Fn3[RawOf[R], Other, Rest] with Out[ValueOf[Other]]

object Transform {

  implicit def transform[
      R <: AnyRecord,
      Other <: AnyRecord,
      Rest <: AnyTypeSet, 
      Uni <: AnyTypeSet,
      Missing <: AnyTypeSet
    ](implicit
      missing: (RawOf[Other] \ RawOf[R]) { type Out = Missing },
      allMissing: Rest ~:~ Missing,
      uni: (RawOf[R] ∪ Rest) { type Out = Uni },
      project: Take[Uni, RawOf[Other]]
    ):  Transform[R, Other, Rest] = 
    new Transform[R, Other, Rest] {

      def apply(recRaw: RawOf[R], other: Other, rest: Rest): Out = 
        valueOf[Other](project(uni(recRaw, rest)))
    }

}
