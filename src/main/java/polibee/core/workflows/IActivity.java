package polibee.core.workflows;

/**
 * Interface diễn tả một hoạt động; implement IActivity để thực thi một hoạt động nào đó dựa trên workflow
 * Một hành động sẽ được thực thi nếu như có một transition được thực thi thành công
 * Thông tin context truyền vào sẽ giúp thực đi đúng hoạt động đó, ví dụ gửi mail khi chuyển trạng thái
 */
public interface IActivity {
    void execute(IActionContext context);
}
