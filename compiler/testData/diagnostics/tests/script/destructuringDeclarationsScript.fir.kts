// !WITH_NEW_INFERENCE
val (a1, a2) = A()
val (b1: Int, b2: Int) = A()
val (c1) = <!COMPONENT_FUNCTION_MISSING, UNRESOLVED_REFERENCE!>unresolved<!>

<!WRONG_MODIFIER_TARGET!>private<!> val (d1) = A()

val (e1, _) = A()

<!UNRESOLVED_REFERENCE!>a1<!>
<!UNRESOLVED_REFERENCE!>a2<!>
<!UNRESOLVED_REFERENCE!>e1<!>

class A {
    operator fun component1() = 1
    operator fun component2() = ""
}