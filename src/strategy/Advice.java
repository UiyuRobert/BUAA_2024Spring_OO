package strategy;

public enum Advice {
    OVER, // 电梯结束运行
    MOVE, // 电梯按照当前方向移动
    WAIT, // 电梯停在当前楼层，等待
    OPENFORIN, // 电梯开门，有人要进入
    OPENFOROUT, // 电梯开门，有人要出
    UTURN; // 电梯更换运行方向
}
