package io.trivium.test;

import io.trivium.extension.type.Typed;

public interface TestCase extends Typed{
    public String getClassName();
    public String getMethodName();
    public String getName();
    public void run() throws Exception;
}
