
```scala
package ohnosequences.typesets

import shapeless._, poly._

import AnyTag._
```


- `Properties`: `(id :~: name :~: ?)
- `Values`: `(id.Rep :~: name.Rep :~: ?)`
- `Entry`: `this.Rep = (id.Rep :~: name.Rep :~: ?) AsRepOf this.type`


```scala
trait AnyRecord extends Representable { record =>
```

Any item has a fixed set of properties

```scala
  type Properties <: TypeSet
  val  properties: Properties
  // should be provided implicitly:
  val  propertiesBound: Properties << AnyProperty
```

Then the raw presentation of the item is kind of a record 
in which the keys set is exactly the `Properties` type,
i.e. it's a set of properties representations

```scala
  type Raw <: TypeSet

  // WARNING!! it does not work
  final type Values = Raw
  // a record entry
  final type Entry = Rep

  // should be provided implicitly:
  val  representedProperties: Raw isValuesOf Properties
```


### Ops

This extends representation type by a getter method 


```scala
  // TODO is it possible to move this out of the Record type?
  implicit def propertyOps(entry: record.Rep): PropertyOps = PropertyOps(entry)

  case class   PropertyOps(recordEntry: record.Rep) {

    def get[P <: Singleton with AnyProperty](p: P)
      (implicit 
        isThere: P ? record.Properties,
        lookup: Lookup[record.Values, p.Rep]
      ): p.Rep = lookup(recordEntry)


    def update[P <: Singleton with AnyProperty, S <: TypeSet](pEntry: P#Rep)
      (implicit 
        isThere: P ? record.Properties,
        replace: Replace[record.Values, (P#Rep :~: ?)]
      ): record.Rep = record ->> replace(recordEntry, pEntry :~: ?)

    def update[Ps <: TypeSet, S <: TypeSet](pEntries: Ps)
      (implicit 
        check: Ps ? record.Values,
        replace: Replace[record.Values, Ps]
      ): record.Rep = record ->> replace(recordEntry, pEntries)


    def as[Other <: AnyRecord](other: Other)(implicit
      project: Choose[record.Values, other.Values]
    ): other.Rep = other ->> project(recordEntry)

    def as[Other <: AnyRecord, Rest <: TypeSet, Uni <: TypeSet, Missing <: TypeSet](other: Other, rest: Rest)
      (implicit
        missing: (other.Values \ record.Values) { type Out = Missing },
        allMissing: Rest ~ Missing,
        uni: (record.Values ? Rest) { type Out = Uni },
        project: Choose[Uni, other.Values]
      ): other.Rep = other ->> project(uni(recordEntry, rest))

  }
```

Same as just tagging with `->>`, but you can pass fields in any order

```scala
    def fields[Vs <: TypeSet](values: Vs)(implicit 
      p: Vs ~> record.Raw
    ): record.Rep = record ->> p(values)
}

object AnyRecord {

    type withProperties[Ps <: SingletonOf[TypeSet]] = AnyRecord { type Properties = Ps }

    implicit def propertyOps[R <: SingletonOf[AnyRecord]](entry: TaggedWith[R]): OtherPropertyOps[R] = 
      OtherPropertyOps(entry)

    case class OtherPropertyOps[R <: Singleton with AnyRecord](recordEntry: TaggedWith[R]) {

    def getIt[P <: Singleton with AnyProperty](p: P)
      (implicit 
        isThere: P ? R#Properties,
        lookup: Lookup[R#Raw, P#Rep]
      ): P#Rep = lookup(recordEntry)

    def get[P <: Singleton with AnyProperty](p: P)
      (implicit 
        isThere: P ? R#Properties,
        lookup: Lookup[R#Raw, P#Rep]
      ): P#Rep = lookup(recordEntry)


    def update[P <: SingletonOf[AnyProperty], S <: TypeSet](pEntry: P#Rep)
      (implicit 
        isThere: P ? R#Properties,
        replace: Replace[R#Raw, (P#Rep :~: ?)],
        getRecord: TaggedWith[R] => R
      ): R#Rep = {

        val uh = getRecord(recordEntry)
        (uh:R) ->> replace(recordEntry, pEntry :~: ?)
      }

    def update[Ps <: TypeSet, S <: TypeSet](pEntries: Ps)
      (implicit 
        check: Ps ? R#Raw,
        replace: Replace[R#Raw, Ps],
        getRecord: TaggedWith[R] => R
      ): R#Rep = {

        val uh = getRecord(recordEntry)
        (uh:R) ->> replace(recordEntry, pEntries)
      }


    def as[Other <: Singleton with AnyRecord](other: Other)(implicit
      project: Choose[R#Raw, Other#Raw]
    ): Other#Rep = (other:Other) ->> project(recordEntry)

    def as[Other <: Singleton with AnyRecord, Rest <: TypeSet, Uni <: TypeSet, Missing <: TypeSet](other: Other, rest: Rest)
      (implicit
        missing: (Other#Raw \ R#Raw) { type Out = Missing },
        allMissing: Rest ~ Missing,
        uni: (R#Raw ? Rest) { type Out = Uni },
        project: Choose[Uni, Other#Raw]
      ): Other#Rep = (other:Other) ->> project(uni(recordEntry, rest))

  }
}


class Record[Ps <: TypeSet, Vs <: TypeSet](val properties: Ps)(implicit 
  val representedProperties: Vs isValuesOf Ps,
  val propertiesBound: Ps << AnyProperty
) 
  extends AnyRecord
{

  val label = this.toString

  type Properties = Ps
  type Raw = Vs
}
```


This is a generic thing for deriving the set of representations 
from a set of representable singletons. For example:
```scala
case object id extends Property[Int]
case object name extends Property[String]

implicitly[Represented.By[
  id.type :~: name.type :~: ?,
  id.Rep  :~: name.Rep  :~: ?
]]
```

See examples of usage it for record properties in tests


```scala
@annotation.implicitNotFound(msg = "Can't construct a set of representations for ${S}")
sealed class Represented[S <: TypeSet] { type Out <: TypeSet }

object Represented {
  type By[S <: TypeSet, O <: TypeSet] = Represented[S] { type Out = O }

  implicit val empty: ? By ? = new Represented[?] { type Out = ? }

  implicit def cons[H <: Singleton with Representable, T <: TypeSet]
    (implicit t: Represented[T]): (H :~: T) By (TaggedWith[H] :~: t.Out) =
          new Represented[H :~: T] { type Out = TaggedWith[H] :~: t.Out }
}
```

Takes a set of Reps and returns the set of what they represent

```scala
import shapeless._, poly._

trait TagsOf[S <: TypeSet] extends DepFn1[S] { type Out <: TypeSet }

object TagsOf {

  def apply[S <: TypeSet](implicit keys: TagsOf[S]): Aux[S, keys.Out] = keys

  type Aux[S <: TypeSet, O <: TypeSet] = TagsOf[S] { type Out = O }

  implicit val empty: Aux[?, ?] =
    new TagsOf[?] {
      type Out = ?
      def apply(s: ?): Out = ?
    }

  implicit def cons[H <: Singleton with Representable, T <: TypeSet]
    (implicit fromRep: TaggedWith[H] => H, t: TagsOf[T]): Aux[TaggedWith[H] :~: T, H :~: t.Out] =
      new TagsOf[TaggedWith[H] :~: T] {
        type Out = H :~: t.Out
        def apply(s: TaggedWith[H] :~: T): Out = {

          val uh: H = fromRep(s.head)
          uh :~: t(s.tail)
        }
      }
}

//////////////////////////////////////////////

trait ListLike[L] {
  type E // elements type

  val nil: L
  def cons(h: E, t: L): L

  def head(l: L): E
  def tail(l: L): L
}

object ListLike {
  type Of[L, T] = ListLike[L] { type E = T }
}
```

Transforms a representation of item to something else

```scala
trait FromProperties[
    A <: TypeSet, // set of properties
    Out           // what we want to get
  ] {

  type Reps <: TypeSet            // representation of properties
  type Fun <: Singleton with Poly // transformation function

  def apply(r: Reps): Out
}

object FromProperties {
  
  def apply[A <: TypeSet, Reps <: TypeSet, F <: Singleton with Poly, Out](implicit tr: FromProperties.Aux[A, Reps, F, Out]):
    FromProperties.Aux[A, Reps, F, Out] = tr

  type Aux[A <: TypeSet, R <: TypeSet, F <: Singleton with Poly, Out] =
    FromProperties[A, Out] { 
      type Reps = R
      type Fun = F
    }

  type Anyhow[A <: TypeSet, R <: TypeSet, Out] =
    FromProperties[A, Out] { 
      type Reps = R
    }

  implicit def empty[Out, F <: Singleton with Poly]
    (implicit m: ListLike[Out]): FromProperties.Aux[?, ?, F, Out] = new FromProperties[?, Out] {
      type Reps = ?
      type Fun = F
      def apply(r: ?): Out = m.nil
    }

  implicit def cons[
    F <: Singleton with Poly,
    AH <: Singleton with AnyProperty, AT <: TypeSet,
    RT <: TypeSet,
    E, Out
  ](implicit
    tagOf: TaggedWith[AH] => AH,
    listLike: ListLike.Of[Out, E], 
    transform: Case1.Aux[F, (AH, TaggedWith[AH]), E], 
    recOnTail: FromProperties.Aux[AT, RT, F, Out]
  ): FromProperties.Aux[AH :~: AT, TaggedWith[AH] :~: RT, F, Out] =
    new FromProperties[AH :~: AT, Out] {
      type Reps = TaggedWith[AH] :~: RT
      type Fun = F
      def apply(r: TaggedWith[AH] :~: RT): Out = {
        listLike.cons(
          transform((tagOf(r.head), r.head)),
          recOnTail(r.tail)
        )
      }
    }
}

///////////////////////////////////////////////////////////////

```

Transforms properties set representation from something else

```scala
trait ToProperties[
    In,          // some other representation
    A <: TypeSet // set of corresponding properties
  ] {

  type Out <: TypeSet             // representation of properties
  type Fun <: Singleton with Poly // transformation function

  def apply(in: In, a: A): Out
}

object ToProperties {
  type Aux[In, A <: TypeSet, O <: TypeSet, F <: Singleton with Poly] = ToProperties[In, A] { type Out = O; type Fun = F } 

  def apply[In, A <: TypeSet, O <: TypeSet, F <: Singleton with Poly]
    (implicit form: ToProperties.Aux[In, A, O, F]): ToProperties.Aux[In, A, O, F] = form

  implicit def empty[In, F <: Singleton with Poly]: ToProperties.Aux[In, ?, ?, F] = new ToProperties[In, ?] {
      type Out = ?
      type Fun = F
      def apply(in: In, a: ?): Out = ?
    }

  implicit def cons[
    In,
    AH <: Singleton with AnyProperty, AT <: TypeSet,
    RH <: TaggedWith[AH], RT <: TypeSet,
    F <: Singleton with Poly
  ](implicit
    f: Case1.Aux[F, (In, AH), RH], 
    t: ToProperties.Aux[In, AT, RT, F]
  ): ToProperties.Aux[In, AH :~: AT, RH :~: RT, F] =
    new  ToProperties[In, AH :~: AT] {
      type Out = RH :~: RT
      type Fun = F
      def apply(in: In, a: AH :~: AT): Out = f((in, a.head)) :~: t(in, a.tail)
    }
}

```


------

### Index

+ src
  + main
    + scala
      + items
        + [items.scala][main/scala/items/items.scala]
      + ops
        + [Choose.scala][main/scala/ops/Choose.scala]
        + [Lookup.scala][main/scala/ops/Lookup.scala]
        + [Map.scala][main/scala/ops/Map.scala]
        + [MapFold.scala][main/scala/ops/MapFold.scala]
        + [Pop.scala][main/scala/ops/Pop.scala]
        + [Reorder.scala][main/scala/ops/Reorder.scala]
        + [Replace.scala][main/scala/ops/Replace.scala]
        + [Subtract.scala][main/scala/ops/Subtract.scala]
        + [ToList.scala][main/scala/ops/ToList.scala]
        + [Union.scala][main/scala/ops/Union.scala]
      + [package.scala][main/scala/package.scala]
      + pointless
        + impl
      + [Property.scala][main/scala/Property.scala]
      + [Record.scala][main/scala/Record.scala]
      + [Representable.scala][main/scala/Representable.scala]
      + [TypeSet.scala][main/scala/TypeSet.scala]
      + [TypeUnion.scala][main/scala/TypeUnion.scala]
  + test
    + scala
      + items
        + [itemsTests.scala][test/scala/items/itemsTests.scala]
      + [RecordTests.scala][test/scala/RecordTests.scala]
      + [TypeSetTests.scala][test/scala/TypeSetTests.scala]

[main/scala/items/items.scala]: items/items.scala.md
[main/scala/ops/Choose.scala]: ops/Choose.scala.md
[main/scala/ops/Lookup.scala]: ops/Lookup.scala.md
[main/scala/ops/Map.scala]: ops/Map.scala.md
[main/scala/ops/MapFold.scala]: ops/MapFold.scala.md
[main/scala/ops/Pop.scala]: ops/Pop.scala.md
[main/scala/ops/Reorder.scala]: ops/Reorder.scala.md
[main/scala/ops/Replace.scala]: ops/Replace.scala.md
[main/scala/ops/Subtract.scala]: ops/Subtract.scala.md
[main/scala/ops/ToList.scala]: ops/ToList.scala.md
[main/scala/ops/Union.scala]: ops/Union.scala.md
[main/scala/package.scala]: package.scala.md
[main/scala/Property.scala]: Property.scala.md
[main/scala/Record.scala]: Record.scala.md
[main/scala/Representable.scala]: Representable.scala.md
[main/scala/TypeSet.scala]: TypeSet.scala.md
[main/scala/TypeUnion.scala]: TypeUnion.scala.md
[test/scala/items/itemsTests.scala]: ../../test/scala/items/itemsTests.scala.md
[test/scala/RecordTests.scala]: ../../test/scala/RecordTests.scala.md
[test/scala/TypeSetTests.scala]: ../../test/scala/TypeSetTests.scala.md