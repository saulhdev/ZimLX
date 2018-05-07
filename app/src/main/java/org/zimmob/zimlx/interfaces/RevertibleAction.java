package org.zimmob.zimlx.interfaces;

public interface RevertibleAction {
    void revertLastItem();

    void consumeRevert();

    void setLastItem(Object... args);
}
