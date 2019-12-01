package com.coalzy.taggedtraverse.traverse

import com.coalzy.taggedtraverse.domain._

class Traverse[ID, TAG] {

  type CN = ChildlessNode[ID, TAG]
  type RN = RecursiveNode[ID, TAG]
  type TG = TagGroup[TAG]

  /**
    * Create nested tree from root
    *
    * Children are only included if the parent and child have a tag group which overlaps for every tag type
    */
  def traverseNodes[C <: CN, R <: RN](root: C)(
    childFinder: CN => Iterable[CN],
    recursiveNode: (CN, Iterable[R]) => R
  ): R = {
    def recurse(node: CN, history: Set[ID]): R = {
      val newHistory = history + node.id

      val children = childFinder(node).filterNot(c => newHistory.contains(c.id))
        .filter(tagsIntersect(node))
        .map(_.updateTagGroups(_.map(filterTagGroup(node.tagGroups))))
        .map(cn => recurse(cn, newHistory))

      recursiveNode(node, children)
    }

    recurse(root, Set())
  }

  private def tagsIntersect(current: CN)(potential: CN): Boolean =
    current.tagGroups.exists(currentGroup =>
      potential.tagGroups.exists(potentialGroup =>
        intersectsForEveryKey(currentGroup)(potentialGroup)
      )
    )

  private def intersectsForEveryKey(currentGroup: TG)(potentialGroup: TG): Boolean = {
    currentGroup.forall { case (tt, set) => potentialGroup.get(tt).exists(s => set.exists(s.contains)) }
  }

  private def filterTagGroup(filterGroups: List[TG])(targetGroup: TG): TG = {
    def containsTag(tagGroup: TG)(tagType: TagType)(tag: TAG): Boolean = tagGroup.get(tagType).exists(_.contains(tag))
    def hasKeyWithEmptyValue[K, V](map: Map[K, Iterable[V]]): Boolean = map.exists(_._2.isEmpty)

    val overlaps = filterGroups.map(_.map {
      case (tagType, tags) => tagType -> tags.filter(containsTag(targetGroup)(tagType))
    })

    overlaps.filterNot(hasKeyWithEmptyValue).flatten.toMap
  }
}
