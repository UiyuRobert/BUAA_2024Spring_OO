package strategy;

public interface Strategy {
    Advice getAdvice(int curFloor, boolean moveDirection);
}
