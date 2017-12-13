package polibee.core.workflows;

import java.util.List;

/**
 * Cấu hình thông tin của một workflow
 */
public interface IWorkflowConfiguration {
    void load(Object[] configs);

    /**
     * Lấy về các transition có thể hiện từ một state
     * @param stateFrom State hiện tại
     * @param trigger trigger
     * @return danh sách các transition có thể có từ trigger này (việc thực thi transition nào sẽ cần kết hợp với điều kiện)
     */
    List<ITransition> getTransitions(String stateFrom, String trigger);

    /**
     * Lấy về đối tượng state từ một state name (tạo ra đối tượng state này)
     * @param stateName tên state
     * @return State có name như truyền vào
     */
    IState getState(String stateName);
}
