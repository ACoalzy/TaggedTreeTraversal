package com.coalzy.taggedtraverse

package object domain {

  trait TagType

  type TagGroup[TAG] = Map[TagType, Set[TAG]]

  sealed trait Node[ID, TAG] {
    def id: ID
    def tagGroups: List[TagGroup[TAG]]
  }

  trait BasicNode[ID, TAG] extends Node[ID, TAG]

  trait RecursiveNode[ID, TAG] extends Node[ID, TAG] {
    def children: List[RecursiveNode[ID, TAG]]
  }

}
