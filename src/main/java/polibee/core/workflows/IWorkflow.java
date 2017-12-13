package polibee.core.workflows;

/**
 * Interface mô tả một WorkFlow
 */
public interface IWorkflow {
    /**
     * Thực thi một hành động đối với workflow
     * @param context ngữ cảnh của hành động (tên hành động là gì, các tham số đi kèm)
     */
    void run(IActionContext context);

    /**
     * Cài đặt cho Workflow
     * @param configuration thông cài đặt workflow
     */
    void setWorkflowConfiguration(IWorkflowConfiguration configuration);
}
