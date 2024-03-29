package controller;

public class Request {
    private final int personId;
    private final int fromFloor;
    private final int toFloor;
    private final int elevatorId;
    private final boolean moveDirection;

    public Request(int personId, int fromFloor, int toFloor, int elevatorId) {
        this.personId = personId;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.elevatorId = elevatorId;
        moveDirection = (fromFloor < toFloor);
    }

    public int getPersonId() {
        return personId;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public boolean getMoveDirection() {
        return moveDirection;
    }
}
