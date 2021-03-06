
```scala
package ohnosequences.cosas

import shapeless.{ <:!< }
```


The two type-level constructors of a type union. 
A generic term looks like `either[A]#or[B]#or[C]`.


```scala
trait AnyTypeUnion {

  type or[Y] <: AnyTypeUnion
  type union // kind of return
}

object AnyTypeUnion {

  private[cosas] type not[T] = T => Nothing
  private[cosas] type just[T] = not[not[T]]
```


Type-level operations


```scala
  @annotation.implicitNotFound(msg = "Can't prove that ${X} is one of ${U}")
  type    isOneOf[X, U <: AnyTypeUnion] = just[X] <:<  U#union

  @annotation.implicitNotFound(msg = "Can't prove that ${X} is not one of ${U}")
  type isNotOneOf[X, U <: AnyTypeUnion] = just[X] <:!< U#union

  final type oneOf[U <: AnyTypeUnion] = {
    type    is[X] = X    isOneOf U
    type isNot[X] = X isNotOneOf U
  }

  @annotation.implicitNotFound(msg = "Can't prove that ${V} is subunion of ${U}")
  type    isSubunionOf[V <: AnyTypeUnion, U <: AnyTypeUnion] = V#union <:<  U#union

  @annotation.implicitNotFound(msg = "Can't prove that ${V <: AnyTypeUnion} is not subunion of ${U}")
  type isNotSubunionOf[V <: AnyTypeUnion, U <: AnyTypeUnion] = V#union <:!< U#union

}

import AnyTypeUnion._

sealed trait either[X] extends TypeUnion[not[X]]
```

Builder

```scala
trait TypeUnion[T] extends AnyTypeUnion {

  type or[S] = TypeUnion[T with not[S]]  
  type union = not[T]
}

object TypeUnion {
  type empty = either[Nothing]
}

```


------

### Index

+ src
  + test
    + scala
      + cosas
        + [PropertyTests.scala][test/scala/cosas/PropertyTests.scala]
        + [WrapTests.scala][test/scala/cosas/WrapTests.scala]
        + [RecordTests.scala][test/scala/cosas/RecordTests.scala]
        + [TypeSetTests.scala][test/scala/cosas/TypeSetTests.scala]
  + main
    + scala
      + cosas
        + [Wrap.scala][main/scala/cosas/Wrap.scala]
        + [PropertiesHolder.scala][main/scala/cosas/PropertiesHolder.scala]
        + [Record.scala][main/scala/cosas/Record.scala]
        + ops
          + typeSet
            + [Check.scala][main/scala/cosas/ops/typeSet/Check.scala]
            + [Reorder.scala][main/scala/cosas/ops/typeSet/Reorder.scala]
            + [Conversions.scala][main/scala/cosas/ops/typeSet/Conversions.scala]
            + [AggregateProperties.scala][main/scala/cosas/ops/typeSet/AggregateProperties.scala]
            + [Subtract.scala][main/scala/cosas/ops/typeSet/Subtract.scala]
            + [Pop.scala][main/scala/cosas/ops/typeSet/Pop.scala]
            + [Representations.scala][main/scala/cosas/ops/typeSet/Representations.scala]
            + [Replace.scala][main/scala/cosas/ops/typeSet/Replace.scala]
            + [Take.scala][main/scala/cosas/ops/typeSet/Take.scala]
            + [Union.scala][main/scala/cosas/ops/typeSet/Union.scala]
            + [Mappers.scala][main/scala/cosas/ops/typeSet/Mappers.scala]
          + record
            + [Update.scala][main/scala/cosas/ops/record/Update.scala]
            + [Conversions.scala][main/scala/cosas/ops/record/Conversions.scala]
            + [Get.scala][main/scala/cosas/ops/record/Get.scala]
        + [Denotation.scala][main/scala/cosas/Denotation.scala]
        + [TypeUnion.scala][main/scala/cosas/TypeUnion.scala]
        + [Fn.scala][main/scala/cosas/Fn.scala]
        + [Property.scala][main/scala/cosas/Property.scala]
        + [TypeSet.scala][main/scala/cosas/TypeSet.scala]

[test/scala/cosas/PropertyTests.scala]: ../../../test/scala/cosas/PropertyTests.scala.md
[test/scala/cosas/WrapTests.scala]: ../../../test/scala/cosas/WrapTests.scala.md
[test/scala/cosas/RecordTests.scala]: ../../../test/scala/cosas/RecordTests.scala.md
[test/scala/cosas/TypeSetTests.scala]: ../../../test/scala/cosas/TypeSetTests.scala.md
[main/scala/cosas/Wrap.scala]: Wrap.scala.md
[main/scala/cosas/PropertiesHolder.scala]: PropertiesHolder.scala.md
[main/scala/cosas/Record.scala]: Record.scala.md
[main/scala/cosas/ops/typeSet/Check.scala]: ops/typeSet/Check.scala.md
[main/scala/cosas/ops/typeSet/Reorder.scala]: ops/typeSet/Reorder.scala.md
[main/scala/cosas/ops/typeSet/Conversions.scala]: ops/typeSet/Conversions.scala.md
[main/scala/cosas/ops/typeSet/AggregateProperties.scala]: ops/typeSet/AggregateProperties.scala.md
[main/scala/cosas/ops/typeSet/Subtract.scala]: ops/typeSet/Subtract.scala.md
[main/scala/cosas/ops/typeSet/Pop.scala]: ops/typeSet/Pop.scala.md
[main/scala/cosas/ops/typeSet/Representations.scala]: ops/typeSet/Representations.scala.md
[main/scala/cosas/ops/typeSet/Replace.scala]: ops/typeSet/Replace.scala.md
[main/scala/cosas/ops/typeSet/Take.scala]: ops/typeSet/Take.scala.md
[main/scala/cosas/ops/typeSet/Union.scala]: ops/typeSet/Union.scala.md
[main/scala/cosas/ops/typeSet/Mappers.scala]: ops/typeSet/Mappers.scala.md
[main/scala/cosas/ops/record/Update.scala]: ops/record/Update.scala.md
[main/scala/cosas/ops/record/Conversions.scala]: ops/record/Conversions.scala.md
[main/scala/cosas/ops/record/Get.scala]: ops/record/Get.scala.md
[main/scala/cosas/Denotation.scala]: Denotation.scala.md
[main/scala/cosas/TypeUnion.scala]: TypeUnion.scala.md
[main/scala/cosas/Fn.scala]: Fn.scala.md
[main/scala/cosas/Property.scala]: Property.scala.md
[main/scala/cosas/TypeSet.scala]: TypeSet.scala.md