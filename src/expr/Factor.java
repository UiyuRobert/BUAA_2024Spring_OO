package expr;

import java.io.IOException;

public interface Factor {
    void setExp(String exp);

    Poly toPoly() throws IOException, ClassNotFoundException;
}
