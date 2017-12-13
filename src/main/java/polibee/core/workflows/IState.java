package polibee.core.workflows;

public interface IState {
    /**
     * Tên của State
     * @return tên của state
     */
    String getName();

    /**
     * Tên có thể sử dụng để hiển thị ra của trạng thái
     * @return Tên trạng thái
     */
    String getDisplayName();

    /**
     * Thực thi một hành động trên đối tượng Model, nếu trigger một hoặc nhiền transition thì sẽ kiểm tra xem có transition nào
     * được thỏa mãn condition không nếu có thì thực hiện việc transition
     * @param context ngữ cảnh hành động (bao gồm đối tượng của hành động là gì)
     */
    void execute(IActionContext context);
}
