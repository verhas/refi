package javax0.refi.tools.basepackage;

import javax0.refi.tools.basepackage.childpackage.GrandParentClass;

public class ParentClass extends GrandParentClass {
    private int notInheritedField;
    protected int inheritedField;

    private void notInheritedMethod(){}
    protected void inheritedMethod(){}
}
