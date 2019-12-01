package com.coalzy.taggedtraverse.traverse

import java.util.UUID

import com.coalzy.taggedtraverse.example._
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite

class TraverseTest extends AnyFunSuite with Matchers {

  import Sport._
  import Season._

  val sut = new Traverse[UUID, String]

  val looseygoosey = Person(UUID.randomUUID, "Lucy", List(Preferences(Set(golf, cricket, swimming, football, dance, rugby, judo), Set(spring, summer, autumn, winter))))
  val steve = Person(UUID.randomUUID, "Steve", List(Preferences(Set(golf, judo), Set(spring, summer))))
  val andrew = Person(UUID.randomUUID, "Andrew", List(Preferences(Set(swimming), Set(spring)), Preferences(Set(judo), Set(winter))))
  val gregory = Person(UUID.randomUUID, "Gregory", List(Preferences(Set(golf), Set(summer, autumn))))
  val oddbod = Person(UUID.randomUUID, "Bod", List(Preferences(Set(cricket), Set(winter))))
  val lazymazie = Person(UUID.randomUUID, "Masie", List(Preferences(Set(), Set())))

  private def testFriendship(person: Person, friendships: Map[UUID, Set[PersonNode]]) =
    sut.traverseNodes[PersonNode, PersonNode](person.toNode())(
      p => friendships.getOrElse(p.id, Set()),
      (p, friends) => PersonNode(p.id, p.tagGroups, friends.toList)
    )

  test("keeps overlapping friends") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      steve.id -> Set(gregory).map(_.toNode())
    )

    val result = testFriendship(steve, friendships)

    result.friends.head.id shouldBe gregory.id
  }

  test("ignores friends with no overlaps") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      steve.id -> Set(oddbod, lazymazie).map(_.toNode())
    )

    val result = testFriendship(steve, friendships)

    result.friends shouldBe Nil
  }

  test("ignores friends with overlap but split over different groups") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      steve.id -> Set(andrew).map(_.toNode())
    )

    val result = testFriendship(steve, friendships)

    result.friends shouldBe Nil
  }

  test("handles multiple friends") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      looseygoosey.id -> Set(steve, andrew).map(_.toNode()),
      steve.id -> Set(),
      andrew.id -> Set()
    )

    val result = testFriendship(looseygoosey, friendships)

    result.friends.map(_.id) should contain theSameElementsAs List(steve.id, andrew.id)
  }

  test("handles multiple depths of friends") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      looseygoosey.id -> Set(steve).map(_.toNode()),
      steve.id -> Set(gregory).map(_.toNode()),
      gregory.id -> Set()
    )

    val result = testFriendship(looseygoosey, friendships)

    result.friends.map(_.id) should contain theSameElementsAs List(steve.id)
    result.friends.flatMap(_.friends.map(_.id)) should contain theSameElementsAs List(gregory.id)
    result.friends.flatMap(_.friends.flatMap(_.friends)) shouldBe Nil
  }


  test("handles long and wide chain for friends") {
    val friendships: Map[UUID, Set[PersonNode]] = Map(
      looseygoosey.id -> Set(steve, andrew).map(_.toNode()),
      steve.id -> Set(looseygoosey, gregory).map(_.toNode()),
      andrew.id -> Set(looseygoosey).map(_.toNode()),
      gregory.id -> Set(steve).map(_.toNode())
    )

    val result = testFriendship(looseygoosey, friendships)

    result.friends.map(_.id) should contain theSameElementsAs List(steve.id, andrew.id)
    result.friends.map(_.friends.map(_.id)) should contain theSameElementsAs List(List(gregory.id), Nil)
    result.friends.flatMap(_.friends.flatMap(_.friends)) shouldBe Nil
  }


  }
