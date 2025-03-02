package com.soywiz.korge.component.docking

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.component.UpdateComponent
import com.soywiz.korge.component.attach
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View

@Deprecated("Use View.zIndex instead")
class SortedChildrenByComponent(override val view: Container, var comparator: Comparator<View>) : UpdateComponent {
    override fun update(dt: TimeSpan) {
        view.sortChildrenBy(comparator)
    }
}

@Deprecated("Use View.zIndex instead")
private fun <T, T2 : Comparable<T2>> ((T) -> T2).toComparator() = Comparator { a: T, b: T -> this(a).compareTo(this(b)) }
@Deprecated("Use View.zIndex instead")
fun <T2 : Comparable<T2>> Container.sortChildrenBy(selector: (View) -> T2) = sortChildrenBy(selector.toComparator())
@Deprecated("Use View.zIndex instead")
fun Container.sortChildrenByY() = sortChildrenBy(View::y)

// @TODO: kotlin-native: kotlin.Comparator { }
//             korge/korge/common/src/com/soywiz/korge/component/docking/SortedChildrenByComponent.kt:25:127: error: unresolved reference: Comparator
// @TODO: kotlin-native: recursive problem!
//korge/korge/common/src/com/soywiz/korge/component/docking/SortedChildrenByComponent.kt:18:140: error: cannot infer a type for this parameter. Please specify it explicitly.
//      fun <T : Container, T2 : Comparable<T2>> T.keepChildrenSortedBy(selector: (View) -> T2): T = this.keepChildrenSortedBy(kotlin.Comparator { a, b -> selector(a).compareTo(selector(b)) })

//fun <T : Container> T.keepChildrenSortedBy(comparator: Comparator<View>) = this.apply { SortedChildrenByComponent(this, comparator).attach() }
//fun <T : Container, T2 : Comparable<T2>> T.keepChildrenSortedBy(selector: (View) -> T2) = this.keepChildrenSortedBy(kotlin.Comparator { a, b -> selector(a).compareTo(selector(b)) })

//fun <T : Container> T.keepChildrenSortedBy(comparator: Comparator<View>): T = this.apply { SortedChildrenByComponent(this, comparator).attach() }
//fun <T : Container, T2 : Comparable<T2>> T.keepChildrenSortedBy(selector: (View) -> T2): T = this.keepChildrenSortedBy(kotlin.Comparator { a, b -> selector(a).compareTo(selector(b)) })
//fun <T : Container> T.keepChildrenSortedByY(): T = this.keepChildrenSortedBy(View::y)
//fun <T : Container, T2 : Comparable<T2>> T.keepChildrenSortedBy(selector: (View) -> T2): T = this.keepChildrenSortedBy(kotlin.Comparator { a: View, b: View -> selector(a).compareTo(selector(b)) })

@Deprecated("Use View.zIndex instead")
fun <T : Container> T.keepChildrenSortedBy(comparator: Comparator<View>): T {
    SortedChildrenByComponent(this, comparator).attach()
    return this
}

@Deprecated("Use View.zIndex instead")
fun <T : Container, T2 : Comparable<T2>> T.keepChildrenSortedBy(selector: (View) -> T2): T =
	this.keepChildrenSortedBy(selector.toComparator())

@Deprecated("Use View.zIndex instead")
fun <T : Container> T.keepChildrenSortedByY(): T = this.keepChildrenSortedBy(View::y)
