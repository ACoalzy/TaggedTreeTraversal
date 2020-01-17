package com.coalzy.taggedtraverse.traverse

import com.coalzy.taggedtraverse.domain._

class Traverse[ID, TAG] {

  type N = Node[ID, TAG]
  type RN = RecursiveNode[ID, TAG]
  type TG = TagGroup[TAG]

  /**
    * Create nested tree from root
    *
    * Children are only included if the parent and child have a tag group which overlaps for every tag type
    */
  def traverseNodes[C <: N, R <: RN](root: C)(
    childrenF: N => Iterable[N],
    buildR: (N, Iterable[R], List[TagGroup[TAG]]) => R
  ): R = {
    case class FilteredNode(node: N, filteredTagGroups: List[TagGroup[TAG]])
    def recurse(node: N, history: Set[ID], remainingTags: List[TagGroup[TAG]]): R = {
      val children = childrenF(node).filterNot(c => history.contains(c.id))
        .filter(tagsIntersect(node))
        .map(cn => FilteredNode(cn, cn.tagGroups.map(filterTagGroup(node.tagGroups))))
        .map(fn => recurse(fn.node, history + fn.node.id, fn.filteredTagGroups))

      buildR(node, children, remainingTags)
    }

    recurse(root, Set(root.id), root.tagGroups)
  }

  private def tagsIntersect(current: N)(potential: N): Boolean = {
    def intersectsForEveryKey(currentGroup: TG)(potentialGroup: TG): Boolean =
      currentGroup.forall { case (tt, set) => potentialGroup.get(tt).exists(s => set.exists(s.contains)) }

    current.tagGroups.exists(currentGroup =>
      potential.tagGroups.exists(potentialGroup =>
        intersectsForEveryKey(currentGroup)(potentialGroup)
      )
    )
  }

  private def filterTagGroup(filterGroups: List[TG])(targetGroup: TG): TG = {
    def containsTag(tagGroup: TG)(tagType: TagType)(tag: TAG): Boolean = tagGroup.get(tagType).exists(_.contains(tag))
    def hasKeyWithEmptyValue[K, V](map: Map[K, Iterable[V]]): Boolean = map.values.exists(_.isEmpty)

    val overlaps = filterGroups.map(_.map {
      case (tagType, tags) => tagType -> tags.filter(containsTag(targetGroup)(tagType))
    })

    overlaps.filterNot(hasKeyWithEmptyValue).flatten.toMap
  }
}
