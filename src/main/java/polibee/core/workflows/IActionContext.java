package polibee.core.workflows;

import java.util.HashMap;

/**
 * Ngữ cảnh của một hành động, bao gồm tên (trigger) và các tham số đi kèm (nếu có)
 * Tham số tạm thời chấp nhận dạng HashMap, có thể có kiểu dữ liệu phù hợp hơn
 */
public interface IActionContext {
    /**
     * Đối tượng chính của WorkFlow
     * @return Đối tượng chính
     */
    IWorkflowModel getObject();
    void setObject(IWorkflowModel object);

    /**
     * Tác nhân gây ra hành động, có thể là một user
     * @return Đối tượng tác nhân gây ra hành động
     */
    IActor getActor();
    void setActor(IActor actor);

    HashMap getParams();
    void setParams(HashMap params);

    String getTrigger();
    void setTrigger(String trigger);
}
