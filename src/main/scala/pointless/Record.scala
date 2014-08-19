package ohnosequences.pointless

import ohnosequences.pointless._, typeSet._, representable._, property._

object record {

  trait AnyRecord extends AnyRepresentable {

    type Properties <: AnyTypeSet
    val  properties: Properties
    // should be provided implicitly:
    val  propertiesBound: Properties isBoundedBy AnyProperty

    type Raw <: AnyTypeSet
    // should be provided implicitly:
    val representedProperties: Properties isRepresentedBy Raw
  }

  // extractors
  type withProperties[Ps <: AnyTypeSet] = AnyRecord { type Properties = Ps }

  // accessors
  type PropertiesOf[R <: AnyRecord] = R#Properties

  class Record[Props <: AnyTypeSet, Vals <: AnyTypeSet](val properties: Props)(implicit 
    val propertiesBound: Props isBoundedBy AnyProperty,
    val representedProperties: Props isRepresentedBy Vals
  ) extends AnyRecord {

    val label = this.toString

    type Properties = Props
    type Raw = Vals
  }

  /*
    Ops 
  */
  implicit def recordOps[R <: AnyRecord](rec: R): RecordOps[R] = new RecordOps(rec)
  class RecordOps[R <: AnyRecord](val rec: R) extends RepresentableOps(rec) {

    /* Same as just tagging with `=>>`, but you can pass fields in any order */
    def fields[Vs <: AnyTypeSet](values: Vs)
      (implicit 
        p: Vs As RawOf[R]
      ): RepOf[R] = rec =>> p(values)
  }

  implicit def recordRepOps[R <: AnyRecord](recEntry: RepOf[R]): RepOps[R] = new RepOps(recEntry)
  class RepOps[R <: AnyRecord](val recEntry: RepOf[R]) {

    def get[P <: AnyProperty](p: P)
      (implicit 
        isThere: P ∈ PropertiesOf[R],
        lookup: Lookup[RawOf[R], RepOf[P]]
      ): RepOf[P] = lookup(recEntry)


    def update[P <: AnyProperty, S <: AnyTypeSet](propRep: RepOf[P])
      (implicit 
        isThere: P ∈ PropertiesOf[R],
        replace: Replace[RepOf[R], RepOf[P] :~: ∅]
      ): RepOf[R] = replace(recEntry, propRep :~: ∅)

    def update[Ps <: AnyTypeSet, S <: AnyTypeSet](propReps: Ps)
      (implicit 
        check: Ps ⊂ RepOf[R],
        replace: Replace[RepOf[R], Ps]
      ): RepOf[R] = replace(recEntry, propReps)


    def as[Other <: AnyRecord](other: Other)
      (implicit
        project: Take[RawOf[R], RawOf[Other]]
      ): RepOf[Other] = other =>> project(recEntry)

    def as[
        Other <: AnyRecord,
        Rest <: AnyTypeSet, 
        Uni <: AnyTypeSet,
        Missing <: AnyTypeSet
      ](other: Other, rest: Rest)
      (implicit
        missing: (RawOf[Other] \ RawOf[R]) { type Out = Missing },
        allMissing: Rest ~:~ Missing,
        uni: (RawOf[R] ∪ Rest) { type Out = Uni },
        project: Take[Uni, RawOf[Other]]
      ): RepOf[Other] = other =>> project(uni(recEntry, rest))

  }

}

// trait ListLike[L] {
//   type E // elements type

//   val nil: L
//   def cons(h: E, t: L): L

//   def head(l: L): E
//   def tail(l: L): L
// }

// object ListLike {
//   type Of[L, T] = ListLike[L] { type E = T }
// }

// /* Transforms a representation of item to something else */
// trait FromProperties[
//     A <: TypeSet, // set of properties
//     Out           // what we want to get
//   ] {

//   type Reps <: TypeSet            // representation of properties
//   type Fun <: Singleton with Poly // transformation function

//   def apply(r: Reps): Out
// }

// object FromProperties {
  
//   def apply[A <: TypeSet, Reps <: TypeSet, F <: Singleton with Poly, Out](implicit tr: FromProperties.Aux[A, Reps, F, Out]):
//     FromProperties.Aux[A, Reps, F, Out] = tr

//   type Aux[A <: TypeSet, R <: TypeSet, F <: Singleton with Poly, Out] =
//     FromProperties[A, Out] { 
//       type Reps = R
//       type Fun = F
//     }

//   type Anyhow[A <: TypeSet, R <: TypeSet, Out] =
//     FromProperties[A, Out] { 
//       type Reps = R
//     }

//   implicit def empty[Out, F <: Singleton with Poly]
//     (implicit m: ListLike[Out]): FromProperties.Aux[∅, ∅, F, Out] = new FromProperties[∅, Out] {
//       type Reps = ∅
//       type Fun = F
//       def apply(r: ∅): Out = m.nil
//     }

//   implicit def cons[
//     F <: Singleton with Poly,
//     AH <: Singleton with AnyProperty, AT <: TypeSet,
//     RT <: TypeSet,
//     E, Out
//   ](implicit
//     tagOf: RepOf[AH] => AH,
//     listLike: ListLike.Of[Out, E], 
//     transform: Case1.Aux[F, (AH, RepOf[AH]), E], 
//     recOnTail: FromProperties.Aux[AT, RT, F, Out]
//   ): FromProperties.Aux[AH :~: AT, RepOf[AH] :~: RT, F, Out] =
//     new FromProperties[AH :~: AT, Out] {
//       type Reps = RepOf[AH] :~: RT
//       type Fun = F
//       def apply(r: RepOf[AH] :~: RT): Out = {
//         listLike.cons(
//           transform((tagOf(r.head), r.head)),
//           recOnTail(r.tail)
//         )
//       }
//     }
// }

// ///////////////////////////////////////////////////////////////

// /* Transforms properties set representation from something else */
// trait ToProperties[
//     In,          // some other representation
//     A <: TypeSet // set of corresponding properties
//   ] {

//   type Out <: TypeSet             // representation of properties
//   type Fun <: Singleton with Poly // transformation function

//   def apply(in: In, a: A): Out
// }

// object ToProperties {
//   type Aux[In, A <: TypeSet, O <: TypeSet, F <: Singleton with Poly] = ToProperties[In, A] { type Out = O; type Fun = F } 

//   def apply[In, A <: TypeSet, O <: TypeSet, F <: Singleton with Poly]
//     (implicit form: ToProperties.Aux[In, A, O, F]): ToProperties.Aux[In, A, O, F] = form

//   implicit def empty[In, F <: Singleton with Poly]: ToProperties.Aux[In, ∅, ∅, F] = new ToProperties[In, ∅] {
//       type Out = ∅
//       type Fun = F
//       def apply(in: In, a: ∅): Out = ∅
//     }

//   implicit def cons[
//     In,
//     AH <: Singleton with AnyProperty, AT <: TypeSet,
//     RH <: RepOf[AH], RT <: TypeSet,
//     F <: Singleton with Poly
//   ](implicit
//     f: Case1.Aux[F, (In, AH), RH], 
//     t: ToProperties.Aux[In, AT, RT, F]
//   ): ToProperties.Aux[In, AH :~: AT, RH :~: RT, F] =
//     new  ToProperties[In, AH :~: AT] {
      
//       type Out = RH :~: RT
//       type Fun = F
//       def apply(in: In, a: AH :~: AT): Out = f((in, a.head)) :~: t(in, a.tail)
//     }
// }
