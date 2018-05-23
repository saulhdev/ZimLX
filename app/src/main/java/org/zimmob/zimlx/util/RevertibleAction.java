package org.zimmob.zimlx.util;

public interface RevertibleAction {
    void revertLastItem();

    void consumeRevert();

    void setLastItem(Object... args);
}
