package org.zimmob.zimlx.util;

public interface IRevertibleAction {
    void revertLastItem();

    void consumeRevert();

    void setLastItem(Object... args);
}
