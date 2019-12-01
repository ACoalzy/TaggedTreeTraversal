package com.coalzy.taggedtraverse

import java.util.UUID

import com.coalzy.taggedtraverse.domain.{ChildlessNode, RecursiveNode, TagGroup, TagType}

package object example {

  case object Sport extends TagType {
    val golf = "golf"
    val cricket = "cricket"
    val swimming = "swimming"
    val football = "football"
    val dance = "dance"
    val rugby = "rugby"
    val judo = "judo"
  }
  case object Season extends TagType {
    val spring = "spring"
    val summer = "summer"
    val autumn = "autumn"
    val winter = "winter"
  }

  case class Person(id: UUID, name: String, preferences: List[Preferences]) {
    def toNode(friends: List[PersonNode] = Nil): PersonNode = PersonNode(id, preferences.map(_.tagMap), friends)
  }

  case class Preferences(sports: Set[String], seasons: Set[String]) {
    val tagMap: Map[TagType, Set[String]] = Map(Sport -> sports, Season -> seasons)
  }

  case class PersonNode(id: UUID, tagGroups: List[TagGroup[String]], friends: List[PersonNode])
    extends ChildlessNode[UUID, String] with RecursiveNode[UUID, String] {
    override def updateTagGroups(f: List[TagGroup[String]] => List[TagGroup[String]]): PersonNode = {
      this.copy(id, f(tagGroups))
    }
  }

}
