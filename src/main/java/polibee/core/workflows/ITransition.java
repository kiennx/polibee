package polibee.core.workflows;

/**
 * Đánh dấu một sự chuyển đổi trạng thái. Một trạng thái có nhất thiết là được đánh dấu trên object không?
 * Câu trả lời là không, nhưng tên của nó thì cần được xác định.
 * Ở trong hàm getCurrentState của object model đó có thể manipulate dữ liệu
 * TODO: Cùng một đối tượng muốn là object của 2 Workflow khác nhau thì làm như thế nào? --> hàm getCurrentState không được phép
 * nằm ở trong object model mà cần phải nằm trong workflow hoặc workflow config
 */
public interface ITransition {
    /**
     * Trigger bởi
     * @return tên trigger
     */
    String getTriggeredBy();

    /**
     * Chuyển đổi từ trạng thái
     * @return trạng thái từ
     */
    String fromState();

    /**
     * Trạng thái chuyển đổi đến
     * @return Tên trạng thái
     */
    String nextState();

    /**
     * Điều kiện chuyển đổi có được thỏa mãn bởi ngữ cảnh hiện tại hay không
     * @param context ngữ cảnh chuyển đổi
     * @return True nếu ngữ cảnh thỏa mãn, False nếu không
     */
    boolean isSatisfied(IActionContext context);
}
