package org.junnikym.springfbp.beans;

import org.jetbrains.annotations.NotNull;

public class UnmanagedClassC implements DummyInterface {

    private String dummyField;

    public static String DUMMY_STATIC_FIELD = "this is test";

    public static UnmanagedClassC STATIC_OBJECT = new UnmanagedClassC();

    private UnmanagedClassC(boolean isOption2) {
        if(isOption2)
            dummyField = "Option 2";
        else
            dummyField = "Option 1";
    }

    private UnmanagedClassC() {
        dummyField = "Option 0";
    }

    @NotNull
    @Override
    public String run() {
        return "dummy class C run :: " + dummyField;
    }

    public static UnmanagedClassC of() {
        return new UnmanagedClassC();
    }

    public static DummyInterface of(boolean isOption2) {
        return new UnmanagedClassC(isOption2);
    }

    public static DummyInterface of(int option) {
        switch(option) {
            case 1: return new UnmanagedClassC(true);
            case 2: return new UnmanagedClassC(false);
            default: return new UnmanagedClassC();
        }
    }

}