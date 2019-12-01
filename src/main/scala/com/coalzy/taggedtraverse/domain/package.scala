package com.coalzy.taggedtraverse

package object domain {

  trait TagType

  type TagGroup[TAG] = Map[TagType, Set[TAG]]

  sealed trait Node[ID, TAG] {
    def id: ID
    def tagGroups: List[TagGroup[TAG]]
  }

  trait ChildlessNode[ID, TAG] extends Node[ID, TAG] {
    def updateTagGroups(f: List[TagGroup[TAG]] => List[TagGroup[TAG]]): ChildlessNode[ID, TAG]
  }

  trait RecursiveNode[ID, TAG] extends Node[ID, TAG] {
    def friends: List[RecursiveNode[ID, TAG]]
  }

}
