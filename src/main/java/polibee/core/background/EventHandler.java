package polibee.core.background;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Handler cơ bản để xử lý được một event
 */
public class EventHandler {
    private String _name;
    private Consumer<HashMap> _handler;

    public EventHandler(String handlerName, Consumer<HashMap> handler) {
        _name = handlerName;
        this._handler = handler;
    }

    /**
     * Thực thi event handler
     * @param params các tham số từ event
     */
    public void execute(HashMap params) {
        try {
            this._handler.accept(params);
        }
        catch (Exception ex) {
            //TODO: ghi log vào đâu đó
        }
    }
}
