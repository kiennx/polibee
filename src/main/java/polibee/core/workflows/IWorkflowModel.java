package polibee.core.workflows;

/**
 * Đối tượng chủ thể của WorkFlow
 */
public interface IWorkflowModel {
    String getCurrentState();
    void setCurrentState(IState state);
}
